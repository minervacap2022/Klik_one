// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.network

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.reporting.CrashReporter
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Internal platform-specific HTTP client implementation.
 * Uses expect/actual pattern to provide native implementations:
 * - iOS: NSURLSession
 * - Android: HttpURLConnection
 * - JVM: HttpURLConnection
 *
 * NOTE: This is an internal implementation detail.
 * External code should use [HttpClient] object which adds authentication and auto-refresh.
 */
internal expect class NativeHttpClient() {
  /**
   * Make a GET request to the specified URL with headers.
   * @param url The full URL to fetch
   * @param headers Map of header key-value pairs
   * @return The response body as a string, or null on error
   */
  suspend fun get(url: String, headers: Map<String, String> = emptyMap()): String?

  /**
   * Make a POST request to the specified URL with body and headers.
   * @param url The full URL to post to
   * @param body The request body as JSON string
   * @param headers Map of header key-value pairs
   * @return The response body as a string, or null on error
   */
  suspend fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): String?

  /**
   * Make a PUT request to the specified URL with body and headers.
   * @param url The full URL to put to
   * @param body The request body as JSON string
   * @param headers Map of header key-value pairs
   * @return The response body as a string, or null on error
   */
  suspend fun put(url: String, body: String, headers: Map<String, String> = emptyMap()): String?

  /**
   * Make a DELETE request to the specified URL with headers and optional body.
   * @param url The full URL to delete
   * @param headers Map of header key-value pairs
   * @param body Optional request body as JSON string
   * @return The response body as a string, or null on error
   */
  suspend fun delete(url: String, headers: Map<String, String> = emptyMap(), body: String? = null): String?

  /**
   * Make a multipart POST request to upload binary data as a file.
   * @param url The full URL to post to
   * @param fileData The binary data to upload
   * @param fileName The filename for the multipart form field
   * @param fieldName The form field name (default "file")
   * @param headers Map of header key-value pairs
   * @return The response body as a string, or null on error
   */
  suspend fun postMultipart(
    url: String,
    fileData: ByteArray,
    fileName: String,
    fieldName: String = "file",
    headers: Map<String, String> = emptyMap(),
  ): String?
}

/**
 * Singleton HTTP client instance for the app.
 * Implements automatic token refresh:
 * - Proactive refresh: Refreshes token before it expires (when < 2 minutes remaining)
 * - Reactive refresh: Refreshes on 401 responses and retries the request
 */
object HttpClient {
  private val nativeClient = NativeHttpClient()

  // Callback for token refresh - set by AuthRepository during initialization
  private var tokenRefreshCallback: (suspend () -> Boolean)? = null

  // Mutex to prevent concurrent refresh attempts — coroutines suspend (not busy-wait) while waiting
  private val refreshMutex = Mutex()

  // Terminal flag: once refresh permanently fails (e.g., expired refresh token),
  // stop all further refresh attempts until a new login occurs.
  private var refreshPermanentlyFailed = false

  // Proactive refresh threshold: refresh when less than 5 minutes until expiration
  // Backend token TTL is 30 minutes; 5-min buffer prevents 401 storms during polling
  private const val PROACTIVE_REFRESH_THRESHOLD_SECONDS = 300L

  // Cached Regex for extracting JWT "exp" claim — avoids re-compiling on every call
  private val EXP_REGEX = Regex("\"exp\"\\s*:\\s*(\\d+)")

  // Timestamp (epoch seconds) of the last successful token refresh.
  // Used to avoid redundant refreshes when multiple coroutines queue on the mutex.
  private var lastRefreshTimeSeconds = 0L

  // Window (seconds) within which a recent refresh is considered still valid
  private const val RECENT_REFRESH_WINDOW_SECONDS = 5L

  /**
   * Initialize the HTTP client with a token refresh callback.
   * This should be called once during app initialization after AuthRepository is ready.
   */
  fun setTokenRefreshCallback(callback: suspend () -> Boolean) {
    tokenRefreshCallback = callback
    refreshPermanentlyFailed = false
    KlikLogger.i("HttpClient", "Token refresh callback registered")
  }

  /**
   * Clear the token refresh callback (on logout)
   */
  fun clearTokenRefreshCallback() {
    tokenRefreshCallback = null
    refreshPermanentlyFailed = true
    KlikLogger.i("HttpClient", "Token refresh callback cleared (refresh permanently disabled)")
  }

  /**
   * Check if the current access token is about to expire.
   * Returns true if token expires in less than PROACTIVE_REFRESH_THRESHOLD_SECONDS.
   */
  private fun isTokenExpiringSoon(): Boolean {
    val token = CurrentUser.accessToken ?: return false

    return try {
      // JWT structure: header.payload.signature
      val parts = token.split(".")
      if (parts.size != 3) return false

      // Decode base64 payload (second part)
      val payload = decodeBase64(parts[1])

      // Parse expiration time from payload
      // Payload format: {"sub":"...","exp":1234567890,...}
      val expMatch = EXP_REGEX.find(payload)
      val expSeconds = expMatch?.groupValues?.get(1)?.toLongOrNull() ?: return false

      // Get current time in seconds
      val nowSeconds = kotlinx.datetime.Clock.System.now().epochSeconds

      // Check if expires within threshold
      val secondsUntilExpiry = expSeconds - nowSeconds
      val expiringSoon = secondsUntilExpiry < PROACTIVE_REFRESH_THRESHOLD_SECONDS

      if (expiringSoon && secondsUntilExpiry > 0) {
        KlikLogger.i("HttpClient", "Token expires in ${secondsUntilExpiry}s (< ${PROACTIVE_REFRESH_THRESHOLD_SECONDS}s), will refresh proactively")
      }

      expiringSoon
    } catch (e: Exception) {
      KlikLogger.e("HttpClient", "Error checking token expiration: ${e.message}", e)
      false
    }
  }

  /**
   * Decode base64url string (used in JWT)
   */
  private fun decodeBase64(base64: String): String {
    // Convert base64url to standard base64
    val standardBase64 = base64
      .replace('-', '+')
      .replace('_', '/')

    // Add padding if needed
    val padded = when (standardBase64.length % 4) {
      2 -> "$standardBase64=="
      3 -> "$standardBase64="
      else -> standardBase64
    }

    // Decode base64
    return try {
      // Use platform-specific base64 decoding
      decodeBase64Platform(padded)
    } catch (e: Exception) {
      KlikLogger.w("HttpClient", "Base64 decode failed: ${e.message}", e)
      ""
    }
  }

  /**
   * Check if response indicates an authentication error.
   * Only checks the first 2000 characters to avoid false positives
   * on large data payloads, while still catching auth errors with verbose
   * response bodies (e.g., HTML 401 pages from reverse proxy).
   */
  private fun isAuthError(responseText: String?): Boolean {
    if (responseText == null) return false
    val normalized = responseText.take(2000).lowercase()
    return normalized.contains("invalid or expired access token") ||
      normalized.contains("expired access token") ||
      normalized.contains("not authenticated") ||
      normalized.contains("missing authorization") ||
      normalized.contains("invalid authorization") ||
      normalized.contains("invalid client") ||
      normalized.contains("token has been revoked") ||
      (normalized.contains("\"detail\"") && normalized.contains("401"))
  }

  /**
   * Attempt to refresh the access token.
   * Uses a Mutex so concurrent callers suspend (not busy-wait) until the
   * first caller finishes. After acquiring the lock, a caller checks whether
   * a refresh already completed recently (within RECENT_REFRESH_WINDOW_SECONDS)
   * and reuses that result instead of refreshing again.
   */
  // Tracks whether the last refresh attempt succeeded (used by concurrent waiters)
  private var lastRefreshSucceeded = false

  private suspend fun tryRefreshToken(): Boolean {
    if (refreshPermanentlyFailed) {
      KlikLogger.w("HttpClient", "Token refresh permanently failed, skipping")
      return false
    }

    return refreshMutex.withLock {
      // Another coroutine may have just refreshed while we were waiting on the mutex.
      // If the last refresh succeeded within the recent window, reuse its result.
      val nowSeconds = kotlinx.datetime.Clock.System.now().epochSeconds
      if (lastRefreshSucceeded && (nowSeconds - lastRefreshTimeSeconds) < RECENT_REFRESH_WINDOW_SECONDS) {
        KlikLogger.i("HttpClient", "Token was just refreshed ${nowSeconds - lastRefreshTimeSeconds}s ago, reusing result")
        return@withLock true
      }

      val callback = tokenRefreshCallback
      if (callback == null) {
        KlikLogger.w("HttpClient", "No token refresh callback registered")
        return@withLock false
      }

      lastRefreshSucceeded = false
      try {
        withContext(NonCancellable) {
          val result = callback()
          lastRefreshSucceeded = result
          if (result) {
            lastRefreshTimeSeconds = kotlinx.datetime.Clock.System.now().epochSeconds
            KlikLogger.i("HttpClient", "Token refresh successful")
          } else {
            KlikLogger.e("HttpClient", "Token refresh failed")
          }
          result
        }
      } catch (e: Exception) {
        KlikLogger.e("HttpClient", "Token refresh error: ${e.message}", e)
        lastRefreshSucceeded = false
        false
      }
    }
  }

  /**
   * Ensure token is valid before making request.
   * Proactively refreshes if token is about to expire.
   */
  private suspend fun ensureValidToken() {
    if (isTokenExpiringSoon()) {
      KlikLogger.d("HttpClient", "Proactive token refresh triggered")
      tryRefreshToken()
    }
  }

  // Network retry: max attempts and delay for transient TLS/connection failures
  private const val NETWORK_RETRY_MAX = 2
  private const val NETWORK_RETRY_DELAY_MS = 1000L

  /**
   * Make a GET request to a backend API endpoint.
   * Automatically adds auth headers and retries on 401 with token refresh.
   */
  suspend fun get(endpoint: String): String? {
    val url = "${ApiConfig.BASE_URL}$endpoint"
    return getUrlWithRetry(url, emptyMap())
  }

  /**
   * Make a POST request to a backend API endpoint.
   * Automatically adds auth headers and retries on 401 with token refresh.
   */
  suspend fun post(endpoint: String, body: String): String? {
    val url = "${ApiConfig.BASE_URL}$endpoint"
    return postUrlWithRetry(url, body, emptyMap())
  }

  /**
   * Make a GET request to a full URL (not just endpoint).
   * Automatically adds auth headers and retries on 401 with token refresh.
   */
  suspend fun getUrl(url: String, headers: Map<String, String> = emptyMap()): String? = getUrlWithRetry(url, headers)

  /**
   * Make a POST request to a full URL (not just endpoint).
   * Automatically adds auth headers and retries on 401 with token refresh.
   */
  suspend fun postUrl(url: String, body: String, headers: Map<String, String> = emptyMap()): String? = postUrlWithRetry(url, body, headers)

  /**
   * Execute an HTTP request with unified retry for both network errors and auth errors.
   * All HTTP methods (GET, POST, PUT, DELETE) go through this single path.
   *
   * Retry behavior:
   * 1. Network failure (null response / TLS error): retry up to NETWORK_RETRY_MAX times with delay
   * 2. Auth error (401): refresh token and retry once
   */
  private suspend fun executeWithRetry(
    method: String,
    url: String,
    extraHeaders: Map<String, String>,
    body: String? = null,
  ): String? {
    // Proactive refresh: refresh token before it expires
    ensureValidToken()

    val baseHeaders = if (body != null) {
      extraHeaders + mapOf(
        ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
        ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
      )
    } else {
      extraHeaders
    }

    for (attempt in 1..NETWORK_RETRY_MAX) {
      val allHeaders = baseHeaders + CurrentUser.getAuthHeaders()
      if (attempt == 1) {
        KlikLogger.d("HTTP", "$method $url with Authorization: Bearer ***")
      } else {
        KlikLogger.i("HTTP", "$method $url retry $attempt/$NETWORK_RETRY_MAX")
      }

      val response = when (method) {
        "GET" -> nativeClient.get(url, allHeaders)
        "POST" -> nativeClient.post(url, body ?: "", allHeaders)
        "PUT" -> nativeClient.put(url, body ?: "", allHeaders)
        "DELETE" -> nativeClient.delete(url, allHeaders, body)
        else -> null
      }

      // Network failure (null = TLS error, connection reset, etc.) - retry
      if (response == null) {
        if (attempt < NETWORK_RETRY_MAX) {
          KlikLogger.w("HTTP", "$method $url failed (network error), retrying in ${NETWORK_RETRY_DELAY_MS}ms...")
          kotlinx.coroutines.delay(NETWORK_RETRY_DELAY_MS)
          continue
        }
        KlikLogger.e("HTTP", "$method $url failed after $NETWORK_RETRY_MAX attempts")
        CrashReporter.reportError("HttpClient", "$method $url failed after $NETWORK_RETRY_MAX retries", RuntimeException("Network failure: $method $url"))
        return null
      }

      // Auth error (401) - refresh token and retry once
      if (isAuthError(response)) {
        // If refresh already permanently failed (expired refresh token / logged out),
        // don't attempt again — return null to break any caller retry loops.
        if (refreshPermanentlyFailed) {
          KlikLogger.e("HttpClient", "Auth error on $method $url but refresh permanently failed - aborting")
          return null
        }

        KlikLogger.e("HttpClient", "Auth error detected, attempting token refresh...")
        if (tryRefreshToken()) {
          val newHeaders = baseHeaders + CurrentUser.getAuthHeaders()
          KlikLogger.d("HTTP", "Retrying $method $url after token refresh")
          return when (method) {
            "GET" -> nativeClient.get(url, newHeaders)
            "POST" -> nativeClient.post(url, body ?: "", newHeaders)
            "PUT" -> nativeClient.put(url, body ?: "", newHeaders)
            "DELETE" -> nativeClient.delete(url, newHeaders)
            else -> null
          }
        }

        // Refresh failed — return null to prevent callers from seeing 401 and retrying
        KlikLogger.e("HttpClient", "Token refresh failed for $method $url - returning null")
        return null
      }

      return response
    }

    return null
  }

  /**
   * Internal GET with unified retry
   */
  private suspend fun getUrlWithRetry(url: String, extraHeaders: Map<String, String>): String? = executeWithRetry("GET", url, extraHeaders)

  /**
   * Internal POST with unified retry
   */
  private suspend fun postUrlWithRetry(url: String, body: String, extraHeaders: Map<String, String>): String? = executeWithRetry("POST", url, extraHeaders, body)

  /**
   * Make a PUT request with unified retry
   */
  suspend fun putUrl(url: String, body: String, headers: Map<String, String> = emptyMap()): String? = executeWithRetry("PUT", url, headers, body)

  /**
   * Make a DELETE request with unified retry
   */
  suspend fun deleteUrl(url: String, headers: Map<String, String> = emptyMap(), body: String? = null): String? = executeWithRetry("DELETE", url, headers, body)

  /**
   * Make an authenticated multipart POST request to a full URL.
   * Used for binary file uploads (audio streaming).
   */
  suspend fun postMultipartUrl(
    url: String,
    fileData: ByteArray,
    fileName: String,
    fieldName: String = "file",
    headers: Map<String, String> = emptyMap(),
  ): String? {
    ensureValidToken()
    // Filter out Content-Type from auth headers — the native client sets
    // Content-Type: multipart/form-data with the correct boundary.
    // getAuthHeaders() includes Content-Type: application/json which would
    // overwrite the multipart Content-Type and cause 422 errors.
    val allHeaders = (headers + CurrentUser.getAuthHeaders())
      .filterKeys { it != ApiConfig.Headers.CONTENT_TYPE }
    KlikLogger.d("HTTP", "POST (multipart) $url, ${fileData.size} bytes")
    return nativeClient.postMultipart(url, fileData, fileName, fieldName, allHeaders)
  }

  /**
   * Make a POST request to an external API (no auth headers added).
   * Used for third-party APIs like Alibaba Cloud ASR.
   */
  suspend fun postExternal(url: String, body: String, headers: Map<String, String>): String? {
    val allHeaders = headers + mapOf(
      ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
      ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
    )
    KlikLogger.d("HTTP", "POST (external) $url")
    return nativeClient.post(url, body, allHeaders)
  }
}

/**
 * Platform-specific base64 decoding function.
 * Used for decoding JWT payload to check expiration time.
 */
expect fun decodeBase64Platform(base64: String): String
