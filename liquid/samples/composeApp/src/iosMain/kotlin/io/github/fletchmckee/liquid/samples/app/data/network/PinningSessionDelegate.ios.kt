// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package io.github.fletchmckee.liquid.samples.app.data.network

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFRelease
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.NSURLAuthenticationChallenge
import platform.Foundation.NSURLAuthenticationMethodServerTrust
import platform.Foundation.NSURLCredential
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionAuthChallengeCancelAuthenticationChallenge
import platform.Foundation.NSURLSessionAuthChallengeDisposition
import platform.Foundation.NSURLSessionAuthChallengePerformDefaultHandling
import platform.Foundation.NSURLSessionAuthChallengeUseCredential
import platform.Foundation.NSURLSessionDelegateProtocol
import platform.Foundation.credentialForTrust
import platform.Foundation.serverTrust
import platform.Security.SecCertificateCopyKey
import platform.Security.SecCertificateRef
import platform.Security.SecKeyCopyAttributes
import platform.Security.SecKeyCopyExternalRepresentation
import platform.Security.SecTrustEvaluateWithError
import platform.Security.SecTrustGetCertificateAtIndex
import platform.Security.SecTrustGetCertificateCount
import platform.Security.kSecAttrKeySizeInBits
import platform.Security.kSecAttrKeyType
import platform.Security.kSecAttrKeyTypeECSECPrimeRandom
import platform.Security.kSecAttrKeyTypeRSA
import platform.darwin.NSObject

/**
 * NSURLSession delegate that enforces SHA-256 SPKI pinning for [CertificatePins.PINNED_HOSTNAME].
 *
 * Order of operations on a server-trust challenge:
 *   1. If the host is not the pinned hostname, fall back to default handling.
 *   2. Run standard chain validation via [SecTrustEvaluateWithError]; reject on failure.
 *   3. Walk the trust chain, computing SHA-256 over each certificate's SubjectPublicKeyInfo;
 *      reconstruct the SPKI by prepending the well-known ASN.1 algorithm header to
 *      [SecKeyCopyExternalRepresentation]'s raw key bytes (matches Android's
 *      `cert.publicKey.encoded` digest).
 *   4. Accept if any pin in [CertificatePins.HIKLIK_PINS] matches; otherwise cancel the challenge.
 */
internal class PinningSessionDelegate : NSObject(), NSURLSessionDelegateProtocol {

  override fun URLSession(
    session: NSURLSession,
    didReceiveChallenge: NSURLAuthenticationChallenge,
    completionHandler: (NSURLSessionAuthChallengeDisposition, NSURLCredential?) -> Unit,
  ) {
    val protectionSpace = didReceiveChallenge.protectionSpace
    if (protectionSpace.authenticationMethod != NSURLAuthenticationMethodServerTrust) {
      completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
      return
    }
    val host = protectionSpace.host
    if (host != CertificatePins.PINNED_HOSTNAME) {
      // We only pin the production hostname; everything else (e.g. third-party APIs
      // called via postExternal) falls through to the default platform trust evaluation.
      completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
      return
    }
    val serverTrust = protectionSpace.serverTrust
    if (serverTrust == null) {
      KlikLogger.e("CertPin", "No serverTrust on auth challenge for $host — rejecting")
      completionHandler(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null)
      return
    }

    if (!SecTrustEvaluateWithError(serverTrust, null)) {
      KlikLogger.e("CertPin", "SecTrustEvaluateWithError failed for $host — rejecting")
      completionHandler(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null)
      return
    }

    val certCount = SecTrustGetCertificateCount(serverTrust).toInt()
    if (certCount == 0) {
      KlikLogger.e("CertPin", "Empty certificate chain for $host — rejecting")
      completionHandler(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null)
      return
    }

    for (i in 0 until certCount) {
      val cert = SecTrustGetCertificateAtIndex(serverTrust, i.toLong()) ?: continue
      val spkiBytes = computeSpkiBytes(cert) ?: continue
      val pin = "sha256/" + base64Encode(sha256(spkiBytes))
      if (pin in CertificatePins.HIKLIK_PINS) {
        KlikLogger.d("CertPin", "Pin matched for $host (chain index $i)")
        val credential = NSURLCredential.credentialForTrust(serverTrust)
        completionHandler(NSURLSessionAuthChallengeUseCredential, credential)
        return
      }
    }

    KlikLogger.e("CertPin", "No pin matched any of the $certCount certificates for $host — rejecting")
    completionHandler(NSURLSessionAuthChallengeCancelAuthenticationChallenge, null)
  }
}

/**
 * Reconstruct the SubjectPublicKeyInfo DER for [cert] and return its bytes.
 *
 * iOS gives us the raw key (via `SecKeyCopyExternalRepresentation`) without the ASN.1
 * algorithm wrapper. To produce the same SPKI bytes that Android's `cert.publicKey.encoded`
 * (and OpenSSL's `X509_get_pubkey().PublicKey`) hash over, we prepend the well-known
 * ASN.1 header for the (algorithm, size) pair.
 *
 * Returns null for unsupported key types/sizes — those certificates are skipped, and
 * pinning falls back to other certificates in the chain.
 */
@OptIn(BetaInteropApi::class)
private fun computeSpkiBytes(cert: SecCertificateRef): ByteArray? {
  val keyRef = SecCertificateCopyKey(cert) ?: run {
    KlikLogger.w("CertPin", "SecCertificateCopyKey returned null")
    return null
  }
  try {
    // SecKeyCopyAttributes returns a CFDictionaryRef which bridges to NSDictionary;
    // the Kotlin runtime represents it as Map<Any?, Any?> with NSString keys → String.
    val attrsCf = SecKeyCopyAttributes(keyRef) ?: run {
      KlikLogger.w("CertPin", "SecKeyCopyAttributes returned null")
      return null
    }
    @Suppress("UNCHECKED_CAST")
    val attrs = CFBridgingRelease(attrsCf) as? Map<Any?, Any?> ?: return null

    val keyTypeKey = (kSecAttrKeyType as NSString) as String
    val keySizeKey = (kSecAttrKeySizeInBits as NSString) as String
    val keyType = attrs[keyTypeKey] as? String
    val keySizeBits = (attrs[keySizeKey] as? NSNumber)?.intValue

    val rawKeyDataCf: CFDataRef = SecKeyCopyExternalRepresentation(keyRef, null) ?: run {
      KlikLogger.w("CertPin", "SecKeyCopyExternalRepresentation returned null")
      return null
    }
    val rawKeyBytes = try {
      cfDataToByteArray(rawKeyDataCf)
    } finally {
      CFRelease(rawKeyDataCf)
    }

    val asn1Prefix = asn1HeaderFor(keyType, keySizeBits) ?: run {
      KlikLogger.w("CertPin", "Unsupported key type/size: type=$keyType size=$keySizeBits")
      return null
    }
    return asn1Prefix + rawKeyBytes
  } finally {
    CFRelease(keyRef)
  }
}

private fun cfDataToByteArray(data: CFDataRef): ByteArray {
  val length = CFDataGetLength(data).toInt()
  if (length <= 0) return ByteArray(0)
  val ptr = CFDataGetBytePtr(data) ?: return ByteArray(0)
  return ptr.readBytes(length)
}

/**
 * ASN.1 SubjectPublicKeyInfo headers for the (algorithm, key size) pairs we expect from
 * Google Trust Services and other common CAs. Constants from RFC 5480 / 8017,
 * cross-checked against the well-known values used by TrustKit and OWASP MASVS.
 */
@OptIn(BetaInteropApi::class)
private fun asn1HeaderFor(keyType: String?, keySizeBits: Int?): ByteArray? {
  if (keyType == null || keySizeBits == null) return null
  val rsaTag = (kSecAttrKeyTypeRSA as NSString) as String
  val ecTag = (kSecAttrKeyTypeECSECPrimeRandom as NSString) as String
  return when {
    keyType == rsaTag && keySizeBits == 2048 -> RSA_2048_SPKI_HEADER
    keyType == rsaTag && keySizeBits == 3072 -> RSA_3072_SPKI_HEADER
    keyType == rsaTag && keySizeBits == 4096 -> RSA_4096_SPKI_HEADER
    keyType == ecTag && keySizeBits == 256 -> EC_P256_SPKI_HEADER
    keyType == ecTag && keySizeBits == 384 -> EC_P384_SPKI_HEADER
    else -> null
  }
}

private val RSA_2048_SPKI_HEADER: ByteArray = byteArrayOf(
  0x30, 0x82.toByte(), 0x01, 0x22, 0x30, 0x0D, 0x06, 0x09,
  0x2A, 0x86.toByte(), 0x48, 0x86.toByte(), 0xF7.toByte(), 0x0D, 0x01, 0x01,
  0x01, 0x05, 0x00, 0x03, 0x82.toByte(), 0x01, 0x0F, 0x00,
)

private val RSA_3072_SPKI_HEADER: ByteArray = byteArrayOf(
  0x30, 0x82.toByte(), 0x01, 0xA2.toByte(), 0x30, 0x0D, 0x06, 0x09,
  0x2A, 0x86.toByte(), 0x48, 0x86.toByte(), 0xF7.toByte(), 0x0D, 0x01, 0x01,
  0x01, 0x05, 0x00, 0x03, 0x82.toByte(), 0x01, 0x8F.toByte(), 0x00,
)

private val RSA_4096_SPKI_HEADER: ByteArray = byteArrayOf(
  0x30, 0x82.toByte(), 0x02, 0x22, 0x30, 0x0D, 0x06, 0x09,
  0x2A, 0x86.toByte(), 0x48, 0x86.toByte(), 0xF7.toByte(), 0x0D, 0x01, 0x01,
  0x01, 0x05, 0x00, 0x03, 0x82.toByte(), 0x02, 0x0F, 0x00,
)

private val EC_P256_SPKI_HEADER: ByteArray = byteArrayOf(
  0x30, 0x59, 0x30, 0x13, 0x06, 0x07,
  0x2A, 0x86.toByte(), 0x48, 0xCE.toByte(), 0x3D, 0x02,
  0x01, 0x06, 0x08,
  0x2A, 0x86.toByte(), 0x48, 0xCE.toByte(), 0x3D, 0x03, 0x01, 0x07,
  0x03, 0x42, 0x00,
)

private val EC_P384_SPKI_HEADER: ByteArray = byteArrayOf(
  0x30, 0x76, 0x30, 0x10, 0x06, 0x07,
  0x2A, 0x86.toByte(), 0x48, 0xCE.toByte(), 0x3D, 0x02,
  0x01, 0x06, 0x05,
  0x2B, 0x81.toByte(), 0x04, 0x00, 0x22,
  0x03, 0x62, 0x00,
)

// ============================
// SHA-256 (FIPS 180-4) — pure-Kotlin implementation.
// Used only for SPKI pin verification; not perf-sensitive.
// ============================

private val SHA256_K: IntArray = intArrayOf(
  0x428a2f98.toInt(), 0x71374491.toInt(), 0xb5c0fbcf.toInt(), 0xe9b5dba5.toInt(),
  0x3956c25b.toInt(), 0x59f111f1.toInt(), 0x923f82a4.toInt(), 0xab1c5ed5.toInt(),
  0xd807aa98.toInt(), 0x12835b01.toInt(), 0x243185be.toInt(), 0x550c7dc3.toInt(),
  0x72be5d74.toInt(), 0x80deb1fe.toInt(), 0x9bdc06a7.toInt(), 0xc19bf174.toInt(),
  0xe49b69c1.toInt(), 0xefbe4786.toInt(), 0x0fc19dc6.toInt(), 0x240ca1cc.toInt(),
  0x2de92c6f.toInt(), 0x4a7484aa.toInt(), 0x5cb0a9dc.toInt(), 0x76f988da.toInt(),
  0x983e5152.toInt(), 0xa831c66d.toInt(), 0xb00327c8.toInt(), 0xbf597fc7.toInt(),
  0xc6e00bf3.toInt(), 0xd5a79147.toInt(), 0x06ca6351.toInt(), 0x14292967.toInt(),
  0x27b70a85.toInt(), 0x2e1b2138.toInt(), 0x4d2c6dfc.toInt(), 0x53380d13.toInt(),
  0x650a7354.toInt(), 0x766a0abb.toInt(), 0x81c2c92e.toInt(), 0x92722c85.toInt(),
  0xa2bfe8a1.toInt(), 0xa81a664b.toInt(), 0xc24b8b70.toInt(), 0xc76c51a3.toInt(),
  0xd192e819.toInt(), 0xd6990624.toInt(), 0xf40e3585.toInt(), 0x106aa070.toInt(),
  0x19a4c116.toInt(), 0x1e376c08.toInt(), 0x2748774c.toInt(), 0x34b0bcb5.toInt(),
  0x391c0cb3.toInt(), 0x4ed8aa4a.toInt(), 0x5b9cca4f.toInt(), 0x682e6ff3.toInt(),
  0x748f82ee.toInt(), 0x78a5636f.toInt(), 0x84c87814.toInt(), 0x8cc70208.toInt(),
  0x90befffa.toInt(), 0xa4506ceb.toInt(), 0xbef9a3f7.toInt(), 0xc67178f2.toInt(),
)

private fun rotr(x: Int, n: Int): Int = (x ushr n) or (x shl (32 - n))

internal fun sha256(input: ByteArray): ByteArray {
  val bitLen = input.size.toLong() * 8L
  val withOne = input + byteArrayOf(0x80.toByte())
  val padLen = ((56 - withOne.size % 64) + 64) % 64
  val padded = withOne + ByteArray(padLen) + ByteArray(8) { i ->
    ((bitLen ushr ((7 - i) * 8)) and 0xFF).toByte()
  }

  val h = intArrayOf(
    0x6a09e667.toInt(), 0xbb67ae85.toInt(), 0x3c6ef372.toInt(), 0xa54ff53a.toInt(),
    0x510e527f.toInt(), 0x9b05688c.toInt(), 0x1f83d9ab.toInt(), 0x5be0cd19.toInt(),
  )

  val w = IntArray(64)
  var offset = 0
  while (offset < padded.size) {
    for (i in 0 until 16) {
      val o = offset + i * 4
      w[i] = ((padded[o].toInt() and 0xFF) shl 24) or
        ((padded[o + 1].toInt() and 0xFF) shl 16) or
        ((padded[o + 2].toInt() and 0xFF) shl 8) or
        (padded[o + 3].toInt() and 0xFF)
    }
    for (i in 16 until 64) {
      val s0 = rotr(w[i - 15], 7) xor rotr(w[i - 15], 18) xor (w[i - 15] ushr 3)
      val s1 = rotr(w[i - 2], 17) xor rotr(w[i - 2], 19) xor (w[i - 2] ushr 10)
      w[i] = w[i - 16] + s0 + w[i - 7] + s1
    }

    var a = h[0]; var b = h[1]; var c = h[2]; var d = h[3]
    var e = h[4]; var f = h[5]; var g = h[6]; var hh = h[7]

    for (i in 0 until 64) {
      val s1 = rotr(e, 6) xor rotr(e, 11) xor rotr(e, 25)
      val ch = (e and f) xor (e.inv() and g)
      val temp1 = hh + s1 + ch + SHA256_K[i] + w[i]
      val s0 = rotr(a, 2) xor rotr(a, 13) xor rotr(a, 22)
      val maj = (a and b) xor (a and c) xor (b and c)
      val temp2 = s0 + maj
      hh = g; g = f; f = e
      e = d + temp1
      d = c; c = b; b = a
      a = temp1 + temp2
    }

    h[0] += a; h[1] += b; h[2] += c; h[3] += d
    h[4] += e; h[5] += f; h[6] += g; h[7] += hh
    offset += 64
  }

  val out = ByteArray(32)
  for (i in 0 until 8) {
    out[i * 4] = (h[i] ushr 24 and 0xFF).toByte()
    out[i * 4 + 1] = (h[i] ushr 16 and 0xFF).toByte()
    out[i * 4 + 2] = (h[i] ushr 8 and 0xFF).toByte()
    out[i * 4 + 3] = (h[i] and 0xFF).toByte()
  }
  return out
}

// ============================
// Standard base64 encoding (RFC 4648, with `=` padding).
// ============================

private const val BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

internal fun base64Encode(input: ByteArray): String {
  val sb = StringBuilder()
  var i = 0
  while (i + 3 <= input.size) {
    val n = ((input[i].toInt() and 0xFF) shl 16) or
      ((input[i + 1].toInt() and 0xFF) shl 8) or
      (input[i + 2].toInt() and 0xFF)
    sb.append(BASE64_ALPHABET[(n ushr 18) and 0x3F])
    sb.append(BASE64_ALPHABET[(n ushr 12) and 0x3F])
    sb.append(BASE64_ALPHABET[(n ushr 6) and 0x3F])
    sb.append(BASE64_ALPHABET[n and 0x3F])
    i += 3
  }
  val remaining = input.size - i
  if (remaining == 1) {
    val n = (input[i].toInt() and 0xFF) shl 16
    sb.append(BASE64_ALPHABET[(n ushr 18) and 0x3F])
    sb.append(BASE64_ALPHABET[(n ushr 12) and 0x3F])
    sb.append("==")
  } else if (remaining == 2) {
    val n = ((input[i].toInt() and 0xFF) shl 16) or
      ((input[i + 1].toInt() and 0xFF) shl 8)
    sb.append(BASE64_ALPHABET[(n ushr 18) and 0x3F])
    sb.append(BASE64_ALPHABET[(n ushr 12) and 0x3F])
    sb.append(BASE64_ALPHABET[(n ushr 6) and 0x3F])
    sb.append('=')
  }
  return sb.toString()
}
