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
 * Result of a native HTTP exchange.
 *
 * - [status] is the HTTP response code (e.g. 200, 401, 503), or 0 if no HTTP exchange
 *   completed (DNS failure, TLS error, socket reset, request cancelled, etc.).
 * - [body] is the raw response body string. May be null when the response had no body.
 */
internal data class NativeHttpResponse(val status: Int, val body: String?)

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
  suspend fun get(url: String, headers: Map<String, String> = emptyMap()): NativeHttpResponse
  suspend fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): NativeHttpResponse
  suspend fun put(url: String, body: String, headers: Map<String, String> = emptyMap()): NativeHttpResponse
  suspend fun delete(url: String, headers: Map<String, String> = emptyMap(), body: String? = null): NativeHttpResponse
  suspend fun patch(url: String, body: String, headers: Map<String, String> = emptyMap()): NativeHttpResponse
  suspend fun postMultipart(
    url: String,
    fileData: ByteArray,
    fileName: String,
    fieldName: String = "file",
    headers: Map<String, String> = emptyMap(),
  ): NativeHttpResponse
}

/**
 * Singleton HTTP client instance for the app.
 * Implements automatic token refresh:
 * - Proactive refresh: refreshes when token has < 5 min until expiration
 * - Reactive refresh: refreshes on 401 responses and retries the request
 */
object HttpClient {
  private val nativeClient = NativeHttpClient()

  // Callback for token refresh - set by AuthRepository during initialization.
  private var tokenRefreshCallback: (suspend () -> Boolean)? = null

  // Mutex preventing concurrent refresh attempts — coroutines suspend (not busy-wait) waiting.
  private val refreshMutex = Mutex()

  // Terminal flag: once refresh permanently fails (e.g., expired refresh token),
  // stop all further refresh attempts until a new login occurs.
  private var refreshPermanentlyFailed = false

  // Proactive refresh threshold: refresh when less than 5 minutes until expiration.
  // Backend token TTL is 30 minutes; the 5-min buffer prevents 401 storms during polling.
  private const val PROACTIVE_REFRESH_THRESHOLD_SECONDS = 300L

  private val EXP_REGEX = Regex("\"exp\"\\s*:\\s*(\\d+)")

  // Timestamp (epoch seconds) of the last successful token refresh.
  private var lastRefreshTimeSeconds = 0L
  private const val RECENT_REFRESH_WINDOW_SECONDS = 5L

  // Network retry policy.
  internal const val NETWORK_RETRY_MAX = 3
  internal const val NETWORK_RETRY_BASE_DELAY_MS = 1000L

  fun setTokenRefreshCallback(callback: suspend () -> Boolean) {
    tokenRefreshCallback = callback
    refreshPermanentlyFailed = false
    KlikLogger.i("HttpClient", "Token refresh callback registered")
  }

  fun clearTokenRefreshCallback() {
    tokenRefreshCallback = null
    refreshPermanentlyFailed = true
    KlikLogger.i("HttpClient", "Token refresh callback cleared (refresh permanently disabled)")
  }

  /**
   * Check if the current access token expires within [PROACTIVE_REFRESH_THRESHOLD_SECONDS].
   */
  private fun isTokenExpiringSoon(): Boolean {
    val token = CurrentUser.accessToken ?: return false
    return try {
      val parts = token.split(".")
      require(parts.size == 3) { "Access token is not a JWT" }
      val payload = decodeBase64(parts[1])
      val expSeconds = EXP_REGEX.find(payload)?.groupValues?.get(1)?.toLongOrNull()
        ?: error("Access token missing numeric exp claim")
      val nowSeconds = kotlinx.datetime.Clock.System.now().epochSeconds
      val secondsUntilExpiry = expSeconds - nowSeconds
      val expiringSoon = secondsUntilExpiry < PROACTIVE_REFRESH_THRESHOLD_SECONDS
      if (expiringSoon && secondsUntilExpiry > 0) {
        KlikLogger.i("HttpClient", "Token expires in ${secondsUntilExpiry}s (< ${PROACTIVE_REFRESH_THRESHOLD_SECONDS}s), refreshing proactively")
      }
      expiringSoon
    } catch (e: Exception) {
      KlikLogger.e("HttpClient", "Error checking token expiration: ${e.message}", e)
      false
    }
  }

  private fun decodeBase64(base64: String): String {
    val standardBase64 = base64.replace('-', '+').replace('_', '/')
    val padded = when (standardBase64.length % 4) {
      2 -> "$standardBase64=="
      3 -> "$standardBase64="
      else -> standardBase64
    }
    return decodeBase64Platform(padded)
  }

  // Tracks whether the last refresh attempt succeeded (used by concurrent waiters).
  private var lastRefreshSucceeded = false

  private suspend fun tryRefreshToken(): Boolean {
    if (refreshPermanentlyFailed) {
      KlikLogger.w("HttpClient", "Token refresh permanently failed, skipping")
      return false
    }
    return refreshMutex.withLock {
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

  private suspend fun ensureValidToken() {
    if (isTokenExpiringSoon()) {
      KlikLogger.d("HttpClient", "Proactive token refresh triggered")
      tryRefreshToken()
    }
  }

  // ============================
  // Public API
  // ============================

  suspend fun get(endpoint: String): String? = executeWithRetry("GET", "${ApiConfig.BASE_URL}$endpoint", emptyMap())
  suspend fun post(endpoint: String, body: String): String? = executeWithRetry("POST", "${ApiConfig.BASE_URL}$endpoint", emptyMap(), body)
  suspend fun getUrl(url: String, headers: Map<String, String> = emptyMap()): String? = executeWithRetry("GET", url, headers)
  suspend fun postUrl(url: String, body: String, headers: Map<String, String> = emptyMap()): String? = executeWithRetry("POST", url, headers, body)
  internal suspend fun postUrlResponse(url: String, body: String, headers: Map<String, String> = emptyMap()): NativeHttpResponse = executeWithRetryResponse("POST", url, headers, body)
  suspend fun putUrl(url: String, body: String, headers: Map<String, String> = emptyMap()): String? = executeWithRetry("PUT", url, headers, body)
  suspend fun deleteUrl(url: String, headers: Map<String, String> = emptyMap(), body: String? = null): String? = executeWithRetry("DELETE", url, headers, body)
  suspend fun patchUrl(url: String, body: String, headers: Map<String, String> = emptyMap()): String? = executeWithRetry("PATCH", url, headers, body)

  /**
   * Authenticated multipart POST to a full URL. Used for binary file uploads (audio streaming).
   *
   * Multipart sets its own Content-Type with a boundary, so callers should not pre-add a
   * Content-Type header. [getAuthHeaders] no longer adds Content-Type, which keeps the
   * boundary intact.
   */
  suspend fun postMultipartUrl(
    url: String,
    fileData: ByteArray,
    fileName: String,
    fieldName: String = "file",
    headers: Map<String, String> = emptyMap(),
  ): String? {
    ensureValidToken()
    val allHeaders = headers + CurrentUser.getAuthHeaders()
    KlikLogger.d("HTTP", "POST (multipart) $url, ${fileData.size} bytes")
    return nativeClient.postMultipart(url, fileData, fileName, fieldName, allHeaders).body
  }

  /**
   * POST to an external API (no auth headers). Used for third-party APIs like Alibaba Cloud ASR.
   */
  suspend fun postExternal(url: String, body: String, headers: Map<String, String>): String? {
    val allHeaders = headers + mapOf(
      ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
      ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
    )
    KlikLogger.d("HTTP", "POST (external) $url")
    return nativeClient.post(url, body, allHeaders).body
  }

  /**
   * Dispatch a request to the native client by method name.
   */
  private suspend fun dispatch(
    method: String,
    url: String,
    headers: Map<String, String>,
    body: String?,
  ): NativeHttpResponse = when (method) {
    "GET" -> nativeClient.get(url, headers)
    "POST" -> nativeClient.post(url, body ?: "", headers)
    "PUT" -> nativeClient.put(url, body ?: "", headers)
    "PATCH" -> nativeClient.patch(url, body ?: "", headers)
    "DELETE" -> nativeClient.delete(url, headers, body)
    else -> error("Unsupported HTTP method: $method")
  }

  private suspend fun executeWithRetryResponse(
    method: String,
    url: String,
    extraHeaders: Map<String, String>,
    body: String? = null,
  ): NativeHttpResponse {
    ensureValidToken()

    val contentHeaders = if (body != null) {
      mapOf(
        ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
        ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
      )
    } else {
      mapOf(ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON)
    }
    val baseHeaders = extraHeaders + contentHeaders

    var didRefresh = false
    while (true) {
      val response = retryNetworkRaw(method, url) {
        val allHeaders = baseHeaders + CurrentUser.getAuthHeaders()
        dispatch(method, url, allHeaders, body)
      }
      if (response.status == 401 || response.status == 403) {
        if (didRefresh || refreshPermanentlyFailed) {
          KlikLogger.e("HttpClient", "Auth error on $method $url after refresh path status=${response.status}")
          return response
        }
        KlikLogger.e("HttpClient", "Auth error (status=${response.status}) on $method $url, refreshing token")
        if (tryRefreshToken()) {
          didRefresh = true
          KlikLogger.d("HTTP", "Retrying $method $url after token refresh")
          continue
        }
      }
      if (response.status !in 200..299) {
        KlikLogger.e("HttpClient", "$method $url returned non-2xx status ${response.status}")
      }
      return response
    }
  }

  /**
   * Execute an HTTP request with unified retry for transient failures and token-refresh.
   * All HTTP methods (GET, POST, PUT, PATCH, DELETE) go through this single path.
   *
   * Behavior:
   * - status == 0 or status in 500..599: transient — retried up to NETWORK_RETRY_MAX times
   * - status == 401 or 403: refresh token, retry once; the retried request itself goes
   *   through the full network-retry budget so a TLS hiccup on the post-refresh attempt
   *   doesn't masquerade as a hard auth failure
   * - everything else: return body verbatim
   *
   * Returns the response body, or null if all retries failed or refresh permanently failed.
   */
  private suspend fun executeWithRetry(
    method: String,
    url: String,
    extraHeaders: Map<String, String>,
    body: String? = null,
  ): String? {
    ensureValidToken()

    val contentHeaders = if (body != null) {
      mapOf(
        ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
        ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
      )
    } else {
      mapOf(ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON)
    }
    val baseHeaders = extraHeaders + contentHeaders

    var didRefresh = false
    while (true) {
      val response = retryNetworkRaw(method, url) {
        val allHeaders = baseHeaders + CurrentUser.getAuthHeaders()
        dispatch(method, url, allHeaders, body)
      }
      // retryNetworkRaw returns status=0 only when retries are exhausted on transient failure.
      if (response.status == 0) {
        CrashReporter.reportError(
          "HttpClient",
          "$method $url failed after $NETWORK_RETRY_MAX retries",
          RuntimeException("Network failure: $method $url"),
        )
        return null
      }

      val isAuthError = response.status == 401 || response.status == 403

      if (isAuthError && !didRefresh) {
        if (refreshPermanentlyFailed) {
          KlikLogger.e("HttpClient", "Auth error on $method $url but refresh permanently failed - aborting")
          return null
        }
        KlikLogger.e("HttpClient", "Auth error (status=${response.status}) on $method $url, refreshing token")
        if (tryRefreshToken()) {
          didRefresh = true
          KlikLogger.d("HTTP", "Retrying $method $url after token refresh")
          continue
        }
        KlikLogger.e("HttpClient", "Token refresh failed for $method $url - returning null")
        return null
      }

      // Non-2xx responses (other than the auth-retry path above) carry an error envelope,
      // not a valid resource body. Returning the body here would feed the error JSON into
      // callers' `decodeFromString<DomainDto>(...)` and crash with MissingFieldException
      // (e.g. UserDto missing `id`/`planType`, BugReportResponse missing `success`/`message`).
      if (response.status !in 200..299) {
        KlikLogger.e("HttpClient", "$method $url returned non-2xx status ${response.status}")
        return null
      }

      return response.body
    }
  }
}

/**
 * Run [call] with linear-backoff retries on transient network failures
 * (status == 0 — no HTTP exchange completed — or status in 500..599).
 *
 * Returns the final [NativeHttpResponse]. If all attempts hit a transient failure,
 * returns `NativeHttpResponse(0, null)` — callers can disambiguate via `status == 0`.
 *
 * Used by both [HttpClient.executeWithRetry] (authenticated requests) and
 * [io.github.fletchmckee.liquid.samples.app.data.repository.AuthRepositoryImpl.backendRefreshToken]
 * (the unauthenticated /refresh path that cannot use the HttpClient singleton).
 */
internal suspend fun retryNetworkRaw(
  method: String,
  url: String,
  call: suspend () -> NativeHttpResponse,
): NativeHttpResponse {
  val maxAttempts = HttpClient.NETWORK_RETRY_MAX
  for (attempt in 1..maxAttempts) {
    if (attempt > 1) {
      KlikLogger.i("HTTP", "$method $url retry $attempt/$maxAttempts")
    }
    val response = call()
    val transient = response.status == 0 || response.status in 500..599
    if (!transient) {
      return response
    }
    if (attempt < maxAttempts) {
      val delayMs = HttpClient.NETWORK_RETRY_BASE_DELAY_MS * attempt
      KlikLogger.w("HTTP", "$method $url transient failure (status=${response.status}), retrying in ${delayMs}ms")
      kotlinx.coroutines.delay(delayMs)
    } else {
      KlikLogger.e("HTTP", "$method $url failed after $maxAttempts attempts (last status=${response.status})")
    }
  }
  return NativeHttpResponse(0, null)
}

/**
 * Platform-specific base64 decoding function.
 * Used for decoding JWT payload to check expiration time.
 */
expect fun decodeBase64Platform(base64: String): String
