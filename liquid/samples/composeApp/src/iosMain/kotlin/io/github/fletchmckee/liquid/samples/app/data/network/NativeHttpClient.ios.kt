// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.network

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import platform.posix.memcpy
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal actual class NativeHttpClient actual constructor() {

    private val session: NSURLSession by lazy {
        // PRODUCTION: Use custom session configuration to bypass URL cache
        // This prevents stale HTML responses from being returned when nginx routes change
        val config = NSURLSessionConfiguration.defaultSessionConfiguration()
        config.setRequestCachePolicy(NSURLRequestReloadIgnoringLocalAndRemoteCacheData)
        config.setURLCache(null)
        config.setTimeoutIntervalForRequest(180.0) // 180 seconds per request (LLM endpoints like insights/encourage/worklife/chat call cloud Qwen and need >60s)
        config.setTimeoutIntervalForResource(300.0) // 300 seconds total per resource
        // TODO: Add certificate pinning via CommonCrypto cinterop when available
        NSURLSession.sessionWithConfiguration(config)
    }

    actual suspend fun get(url: String, headers: Map<String, String>): String? {
        return makeRequest(url, "GET", null, headers)
    }

    actual suspend fun post(url: String, body: String, headers: Map<String, String>): String? {
        return makeRequest(url, "POST", body, headers)
    }

    actual suspend fun put(url: String, body: String, headers: Map<String, String>): String? {
        return makeRequest(url, "PUT", body, headers)
    }

    actual suspend fun delete(url: String, headers: Map<String, String>, body: String?): String? {
        return makeRequest(url, "DELETE", body, headers)
    }

    actual suspend fun postMultipart(
        url: String,
        fileData: ByteArray,
        fileName: String,
        fieldName: String,
        headers: Map<String, String>
    ): String? = suspendCancellableCoroutine { continuation ->
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            KlikLogger.e("HTTP", "Invalid URL: $url")
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val boundary = "Boundary-${platform.Foundation.NSUUID().UUIDString}"
        val request = NSMutableURLRequest.requestWithURL(nsUrl)
        request.setHTTPMethod("POST")
        request.setCachePolicy(NSURLRequestReloadIgnoringLocalAndRemoteCacheData)
        request.setValue("multipart/form-data; boundary=$boundary", forHTTPHeaderField = "Content-Type")

        headers.forEach { (key, value) ->
            request.setValue(value, forHTTPHeaderField = key)
        }

        // Build multipart body
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

        bodyBytes.usePinned { pinned ->
            val nsData = NSData.dataWithBytes(pinned.addressOf(0), length = bodyBytes.size.toULong())
            request.setHTTPBody(nsData)
        }

        val task: NSURLSessionDataTask = session.dataTaskWithRequest(
            request as NSURLRequest
        ) { data: NSData?, response: platform.Foundation.NSURLResponse?, error: NSError? ->
            if (error != null) {
                KlikLogger.e("HTTP", "Multipart error: ${error.localizedDescription}")
                continuation.resume(null)
                return@dataTaskWithRequest
            }

            val httpResponse = response as? NSHTTPURLResponse
            val statusCode = httpResponse?.statusCode ?: 0

            if (statusCode !in 200..299) {
                KlikLogger.e("HTTP", "Multipart error status: $statusCode")
            }

            if (data != null && data.length.toInt() > 0) {
                val bytes = ByteArray(data.length.toInt())
                bytes.usePinned { pinned ->
                    memcpy(pinned.addressOf(0), data.bytes, data.length)
                }
                val responseString = bytes.decodeToString()
                KlikLogger.d("HTTP", "Multipart response ($statusCode): ${responseString.take(200)}...")
                continuation.resume(responseString)
            } else {
                KlikLogger.w("HTTP", "Empty multipart response")
                continuation.resume(null)
            }
        }

        task.resume()
        continuation.invokeOnCancellation { task.cancel() }
    }

    private suspend fun makeRequest(
        urlString: String,
        method: String,
        body: String?,
        headers: Map<String, String>
    ): String? = suspendCancellableCoroutine { continuation ->
        val nsUrl = NSURL.URLWithString(urlString)
        if (nsUrl == null) {
            KlikLogger.e("HTTP", "Invalid URL: $urlString")
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        val request = NSMutableURLRequest.requestWithURL(nsUrl)
        request.setHTTPMethod(method)

        // PRODUCTION: Disable caching at request level to ensure fresh responses
        request.setCachePolicy(NSURLRequestReloadIgnoringLocalAndRemoteCacheData)

        // Add cache-control headers to bypass any intermediate caches (CDN, proxy)
        request.setValue("no-cache, no-store, must-revalidate", forHTTPHeaderField = "Cache-Control")
        request.setValue("no-cache", forHTTPHeaderField = "Pragma")
        request.setValue("0", forHTTPHeaderField = "Expires")

        // Add custom headers
        headers.forEach { (key, value) ->
            request.setValue(value, forHTTPHeaderField = key)
        }

        // Add body for POST and PUT
        if (body != null && (method == "POST" || method == "PUT")) {
            val bodyData = body.encodeToByteArray()
            bodyData.usePinned { pinned ->
                val nsData = NSData.dataWithBytes(
                    pinned.addressOf(0),
                    length = bodyData.size.toULong()
                )
                request.setHTTPBody(nsData)
            }
        }

        val task: NSURLSessionDataTask = session.dataTaskWithRequest(
            request as NSURLRequest
        ) { data: NSData?, response: platform.Foundation.NSURLResponse?, error: NSError? ->
            if (error != null) {
                if (error.code.toLong() == NSURLErrorCancelled) {
                    continuation.resume(null)
                    return@dataTaskWithRequest
                }
                KlikLogger.e("HTTP", "Error: ${error.localizedDescription}")
                continuation.resume(null)
                return@dataTaskWithRequest
            }

            val httpResponse = response as? NSHTTPURLResponse
            val statusCode = httpResponse?.statusCode ?: 0

            if (statusCode in 500..599) {
                KlikLogger.e("HTTP", "Server error $statusCode")
                continuation.resume(null)
                return@dataTaskWithRequest
            }
            if (statusCode !in 200..299) {
                KlikLogger.e("HTTP", "Error status: $statusCode")
                // Return body for 4xx so auth error detection works
            }

            if (data != null && data.length.toInt() > 0) {
                val bytes = ByteArray(data.length.toInt())
                bytes.usePinned { pinned ->
                    memcpy(pinned.addressOf(0), data.bytes, data.length)
                }
                val responseString = bytes.decodeToString()
                KlikLogger.d("HTTP", "Response ($statusCode): ${responseString.take(200)}...")
                continuation.resume(responseString)
            } else {
                KlikLogger.w("HTTP", "Empty response")
                continuation.resume(null)
            }
        }

        task.resume()

        continuation.invokeOnCancellation {
            task.cancel()
        }
    }
}

/**
 * iOS implementation of base64 decoding.
 * Uses manual decoding since NSData base64 init is not directly accessible from Kotlin/Native.
 */
actual fun decodeBase64Platform(base64: String): String {
    return try {
        // Manual base64 decoding for Kotlin/Native iOS
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        val cleanInput = base64.replace("=", "")
        
        val byteList = mutableListOf<Byte>()
        var buffer = 0
        var bitsCollected = 0
        
        for (c in cleanInput) {
            val value = chars.indexOf(c)
            if (value < 0) continue
            
            buffer = (buffer shl 6) or value
            bitsCollected += 6
            
            if (bitsCollected >= 8) {
                bitsCollected -= 8
                byteList.add(((buffer shr bitsCollected) and 0xFF).toByte())
            }
        }
        
        byteList.toByteArray().decodeToString()
    } catch (e: Exception) {
        ""
    }
}
