// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package io.github.fletchmckee.liquid.samples.app.data.network

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlin.coroutines.resume
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLRequestReloadIgnoringLocalAndRemoteCacheData
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSURLSessionDataTask
import platform.Foundation.NSUUID
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.dataWithBytes
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import platform.posix.memcpy

/**
 * Proxy/VPN-resilient multipart uploader for streaming audio piles.
 *
 * Why this exists separate from [NativeHttpClient]:
 *  - Audio piles ship every ~1s and tolerate zero connection-pooling quirks. MITM proxies
 *    (Clash, Surge, Charles, mitmproxy, corp ZTNA) frequently reset reused HTTPS tunnels
 *    mid-body, surfacing as `NSURLErrorDomain -1005` / CFStreamError 57 (ECONNRESET).
 *  - We isolate audio onto a dedicated `NSURLSession` configured for *no* connection reuse,
 *    *no* pipelining, *no* cache, and use `uploadTaskWithRequest:fromData:` (the platform's
 *    proxy-friendly upload path) instead of `dataTaskWithRequest:`.
 *  - Each request also carries `Connection: close` to defeat any upstream keep-alive caching.
 */
internal object AudioUploadClient {
  private const val TAG = "AudioUploadClient"

  // We DO NOT cache an NSURLSession — every pile gets a brand-new session that is
  // `finishTasksAndInvalidate`'d after the upload completes. This is the only way
  // to guarantee a fresh TCP/TLS handshake per pile under MITM proxies (Clash,
  // Surge, Charles…) which cache and corrupt reused tunnels. `Connection: close`
  // is not honored by NSURLSession's internal connection pool — invalidating the
  // session is.
  private fun freshSession(): NSURLSession {
    val config = NSURLSessionConfiguration.ephemeralSessionConfiguration()
    config.setHTTPMaximumConnectionsPerHost(1)
    config.setHTTPShouldUsePipelining(false)
    config.setHTTPShouldSetCookies(false)
    config.setRequestCachePolicy(NSURLRequestReloadIgnoringLocalAndRemoteCacheData)
    config.setURLCache(null)
    // Aggressive timeouts: Clash MITM either answers fast or it's dead. With a
    // serialized upload worker (1 in flight), every wasted second blocks every
    // subsequent pile. 6s gives one TLS handshake + 100KB body a real chance
    // without burning minutes on a half-dead conn.
    config.setTimeoutIntervalForRequest(6.0)
    config.setTimeoutIntervalForResource(10.0)
    config.setWaitsForConnectivity(false)
    return NSURLSession.sessionWithConfiguration(config)
  }

  /**
   * Upload a single audio pile with bounded exponential backoff.
   * Returns true on a 2xx response, false after [maxAttempts] failures.
   */
  suspend fun uploadPile(
    url: String,
    fileData: ByteArray,
    fileName: String,
    fieldName: String,
    headers: Map<String, String>,
    pileNumber: Int,
    maxAttempts: Int = 5,
  ): Boolean {
    var attempt = 0
    var backoffMs = 250L
    while (attempt < maxAttempts) {
      attempt++
      val (ok, status, errorDesc) = uploadOnce(url, fileData, fileName, fieldName, headers)
      if (ok) {
        if (attempt > 1) {
          KlikLogger.i(TAG, "Pile #$pileNumber recovered on attempt $attempt")
        }
        return true
      }
      // 4xx (client error) is not transient — don't waste retries.
      if (status in 400..499) {
        KlikLogger.e(TAG, "Pile #$pileNumber permanent failure status=$status (no retry)")
        return false
      }
      KlikLogger.w(
        TAG,
        "Pile #$pileNumber attempt $attempt/$maxAttempts failed (status=$status, err=$errorDesc); backoff ${backoffMs}ms",
      )
      if (attempt < maxAttempts) {
        delay(backoffMs)
        backoffMs = (backoffMs * 2).coerceAtMost(4_000L)
      }
    }
    return false
  }

  private data class UploadResult(val ok: Boolean, val status: Int, val errorDesc: String?)

  private suspend fun uploadOnce(
    url: String,
    fileData: ByteArray,
    fileName: String,
    fieldName: String,
    headers: Map<String, String>,
  ): UploadResult = suspendCancellableCoroutine { continuation ->
    val nsUrl = NSURL.URLWithString(url)
    if (nsUrl == null) {
      KlikLogger.e(TAG, "Invalid URL: $url")
      continuation.resume(UploadResult(false, 0, "invalid url"))
      return@suspendCancellableCoroutine
    }

    val boundary = "Boundary-${NSUUID().UUIDString}"
    val request = NSMutableURLRequest.requestWithURL(nsUrl)
    request.setHTTPMethod("POST")
    request.setCachePolicy(NSURLRequestReloadIgnoringLocalAndRemoteCacheData)
    request.setValue("multipart/form-data; boundary=$boundary", forHTTPHeaderField = "Content-Type")
    // Force a fresh TCP connection per pile — defeats proxy idle-close races.
    request.setValue("close", forHTTPHeaderField = "Connection")
    request.setValue("identity", forHTTPHeaderField = "Accept-Encoding")
    headers.forEach { (key, value) -> request.setValue(value, forHTTPHeaderField = key) }

    val lineBreak = "\r\n"
    val headerPart = "--$boundary$lineBreak" +
      "Content-Disposition: form-data; name=\"$fieldName\"; filename=\"$fileName\"$lineBreak" +
      "Content-Type: application/octet-stream$lineBreak$lineBreak"
    val footerPart = "$lineBreak--$boundary--$lineBreak"

    val headerBytes = headerPart.encodeToByteArray()
    val footerBytes = footerPart.encodeToByteArray()
    val totalSize = headerBytes.size + fileData.size + footerBytes.size
    val bodyBytes = ByteArray(totalSize)
    headerBytes.copyInto(bodyBytes, 0)
    fileData.copyInto(bodyBytes, headerBytes.size)
    footerBytes.copyInto(bodyBytes, headerBytes.size + fileData.size)

    val nsBody = bodyBytes.usePinned { pinned ->
      NSData.dataWithBytes(pinned.addressOf(0), length = bodyBytes.size.toULong())
    }
    request.setHTTPBody(nsBody)

    // Brand-new session for THIS pile only. Invalidated in the completion handler
    // so its connection pool is destroyed before the next pile runs.
    val session = freshSession()
    val task: NSURLSessionDataTask = session.dataTaskWithRequest(
      request as NSURLRequest,
    ) { data: NSData?, response: NSURLResponse?, error: NSError? ->
      try {
        if (error != null) {
          continuation.resume(UploadResult(false, 0, error.localizedDescription))
          return@dataTaskWithRequest
        }
        val httpResponse = response as? NSHTTPURLResponse
        val status = httpResponse?.statusCode?.toInt() ?: 0
        val ok = status in 200..299
        if (!ok && data != null && data.length.toInt() > 0) {
          val bytes = ByteArray(data.length.toInt())
          bytes.usePinned { pinned -> memcpy(pinned.addressOf(0), data.bytes, data.length) }
          KlikLogger.e(TAG, "status=$status body=${bytes.decodeToString().take(200)}")
        }
        continuation.resume(UploadResult(ok, status, null))
      } finally {
        session.finishTasksAndInvalidate()
      }
    }

    task.resume()
    continuation.invokeOnCancellation {
      task.cancel()
      session.finishTasksAndInvalidate()
    }
  }
}
