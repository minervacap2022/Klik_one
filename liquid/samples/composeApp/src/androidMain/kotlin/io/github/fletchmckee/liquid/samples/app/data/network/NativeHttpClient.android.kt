// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.network

import android.util.Base64
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * TLS certificate pinning trust manager for hiklik.ai.
 *
 * Wraps the platform default trust manager and adds SPKI (Subject Public Key Info)
 * pin validation after standard chain verification succeeds. If the server's
 * certificate chain does not contain any certificate whose SHA-256 SPKI hash
 * matches [CertificatePins.HIKLIK_PINS], the connection is rejected.
 */
private object PinningTrustManager : X509TrustManager {

  private val defaultTrustManager: X509TrustManager by lazy {
    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(null as java.security.KeyStore?)
    tmf.trustManagers.first { it is X509TrustManager } as X509TrustManager
  }

  override fun getAcceptedIssuers(): Array<X509Certificate> = defaultTrustManager.acceptedIssuers

  override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {
    defaultTrustManager.checkClientTrusted(chain, authType)
  }

  override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
    // First, run standard platform trust evaluation (CA chain, expiry, etc.)
    defaultTrustManager.checkServerTrusted(chain, authType)

    // Then enforce SPKI pinning
    val sha256 = MessageDigest.getInstance("SHA-256")
    for (cert in chain) {
      val spkiHash = sha256.digest(cert.publicKey.encoded)
      val base64Hash = Base64.encodeToString(spkiHash, Base64.NO_WRAP)
      val pin = "sha256/$base64Hash"
      if (pin in CertificatePins.HIKLIK_PINS) {
        KlikLogger.d("CertPin", "Pin matched for certificate: ${cert.subjectDN}")
        return
      }
    }

    throw CertificateException(
      "Certificate pinning failed for hiklik.ai: none of the ${chain.size} " +
        "certificates in the chain matched any known pin",
    )
  }
}

/**
 * Lazily-initialized SSLSocketFactory that enforces certificate pinning via [PinningTrustManager].
 */
private val pinningSslSocketFactory: SSLSocketFactory by lazy {
  val sslContext = SSLContext.getInstance("TLS")
  sslContext.init(null, arrayOf<TrustManager>(PinningTrustManager), null)
  sslContext.socketFactory
}

/**
 * Apply certificate pinning to an [HttpURLConnection] if the URL targets hiklik.ai.
 * For non-hiklik.ai hosts, the connection is left unchanged (uses system defaults).
 */
private fun HttpURLConnection.applyCertificatePinning() {
  if (this is HttpsURLConnection) {
    val host = url.host
    if (host == CertificatePins.PINNED_HOSTNAME) {
      sslSocketFactory = pinningSslSocketFactory
    }
  }
}

internal actual class NativeHttpClient actual constructor() {

  actual suspend fun get(url: String, headers: Map<String, String>): String? {
    return withContext(Dispatchers.IO) {
      try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.applyCertificatePinning()
        connection.requestMethod = "GET"

        headers.forEach { (key, value) ->
          connection.setRequestProperty(key, value)
        }

        val responseCode = connection.responseCode
        KlikLogger.d("HTTP", "GET $url -> $responseCode")

        if (responseCode in 500..599) {
          KlikLogger.e("HTTP", "Server error $responseCode for GET $url")
          connection.disconnect()
          return@withContext null
        }

        val inputStream = if (responseCode in 200..299) {
          connection.inputStream
        } else {
          connection.errorStream
        }

        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = reader.readText()
        reader.close()
        connection.disconnect()

        response
      } catch (e: Exception) {
        KlikLogger.e("HTTP", "Error: ${e.message}", e)
        null
      }
    }
  }

  actual suspend fun post(url: String, body: String, headers: Map<String, String>): String? {
    return withContext(Dispatchers.IO) {
      try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.applyCertificatePinning()
        connection.requestMethod = "POST"
        connection.doOutput = true

        headers.forEach { (key, value) ->
          connection.setRequestProperty(key, value)
        }

        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(body)
        writer.flush()
        writer.close()

        val responseCode = connection.responseCode
        KlikLogger.d("HTTP", "POST $url -> $responseCode")

        if (responseCode in 500..599) {
          KlikLogger.e("HTTP", "Server error $responseCode for POST $url")
          connection.disconnect()
          return@withContext null
        }

        val inputStream = if (responseCode in 200..299) {
          connection.inputStream
        } else {
          connection.errorStream ?: connection.inputStream
        }

        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = reader.readText()
        reader.close()
        connection.disconnect()

        response
      } catch (e: Exception) {
        KlikLogger.e("HTTP", "POST error: ${e.message}", e)
        null
      }
    }
  }

  actual suspend fun put(url: String, body: String, headers: Map<String, String>): String? {
    return withContext(Dispatchers.IO) {
      try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.applyCertificatePinning()
        connection.requestMethod = "PUT"
        connection.doOutput = true

        headers.forEach { (key, value) ->
          connection.setRequestProperty(key, value)
        }

        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(body)
        writer.flush()
        writer.close()

        val responseCode = connection.responseCode
        KlikLogger.d("HTTP", "PUT $url -> $responseCode")

        if (responseCode in 500..599) {
          KlikLogger.e("HTTP", "Server error $responseCode for PUT $url")
          connection.disconnect()
          return@withContext null
        }

        val inputStream = if (responseCode in 200..299) {
          connection.inputStream
        } else {
          connection.errorStream
        }

        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = reader.readText()
        reader.close()
        connection.disconnect()

        response
      } catch (e: Exception) {
        KlikLogger.e("HTTP", "Error: ${e.message}", e)
        null
      }
    }
  }

  actual suspend fun patch(url: String, body: String, headers: Map<String, String>): String? {
    return withContext(Dispatchers.IO) {
      try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.applyCertificatePinning()
        // HttpURLConnection rejects PATCH directly; tunnel via X-HTTP-Method-Override.
        connection.requestMethod = "POST"
        connection.setRequestProperty("X-HTTP-Method-Override", "PATCH")
        connection.doOutput = true

        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(body)
        writer.flush()
        writer.close()

        val responseCode = connection.responseCode
        KlikLogger.d("HTTP", "PATCH $url -> $responseCode")

        if (responseCode in 500..599) {
          KlikLogger.e("HTTP", "Server error $responseCode for PATCH $url")
          connection.disconnect()
          return@withContext null
        }

        val inputStream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = reader.readText()
        reader.close()
        connection.disconnect()
        response
      } catch (e: Exception) {
        KlikLogger.e("HTTP", "Error: ${e.message}", e)
        null
      }
    }
  }

  actual suspend fun delete(url: String, headers: Map<String, String>, body: String?): String? {
    return withContext(Dispatchers.IO) {
      try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.applyCertificatePinning()
        connection.requestMethod = "DELETE"

        headers.forEach { (key, value) ->
          connection.setRequestProperty(key, value)
        }

        if (body != null) {
          connection.doOutput = true
          connection.outputStream.bufferedWriter().use { it.write(body) }
        }

        val responseCode = connection.responseCode
        KlikLogger.d("HTTP", "DELETE $url -> $responseCode")

        if (responseCode in 500..599) {
          KlikLogger.e("HTTP", "Server error $responseCode for DELETE $url")
          connection.disconnect()
          return@withContext null
        }

        val inputStream = if (responseCode in 200..299) {
          connection.inputStream
        } else {
          connection.errorStream
        }

        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = reader.readText()
        reader.close()
        connection.disconnect()

        response
      } catch (e: Exception) {
        KlikLogger.e("HTTP", "Error: ${e.message}", e)
        null
      }
    }
  }

  actual suspend fun postMultipart(
    url: String,
    fileData: ByteArray,
    fileName: String,
    fieldName: String,
    headers: Map<String, String>,
  ): String? = withContext(Dispatchers.IO) {
    try {
      val boundary = "Boundary-${java.util.UUID.randomUUID()}"
      val connection = URL(url).openConnection() as HttpURLConnection
      connection.applyCertificatePinning()
      connection.requestMethod = "POST"
      connection.doOutput = true
      connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
      headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

      val outputStream = connection.outputStream
      val lineBreak = "\r\n"

      val headerPart = "--$boundary$lineBreak" +
        "Content-Disposition: form-data; name=\"$fieldName\"; filename=\"$fileName\"$lineBreak" +
        "Content-Type: application/octet-stream$lineBreak$lineBreak"
      outputStream.write(headerPart.toByteArray(Charsets.UTF_8))
      outputStream.write(fileData)
      outputStream.write("$lineBreak--$boundary--$lineBreak".toByteArray(Charsets.UTF_8))
      outputStream.flush()
      outputStream.close()

      val responseCode = connection.responseCode
      KlikLogger.d("HTTP", "POST (multipart) $url -> $responseCode")

      val inputStream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
      val reader = BufferedReader(InputStreamReader(inputStream))
      val response = reader.readText()
      reader.close()
      connection.disconnect()

      response
    } catch (e: Exception) {
      KlikLogger.e("HTTP", "Multipart error: ${e.message}", e)
      null
    }
  }
}

/**
 * Android implementation of base64 decoding using android.util.Base64.
 */
actual fun decodeBase64Platform(base64: String): String = try {
  val bytes = Base64.decode(base64, Base64.DEFAULT)
  String(bytes, Charsets.UTF_8)
} catch (e: Exception) {
  ""
}
