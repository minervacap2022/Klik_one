// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.network

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

// PRODUCTION NOTE: This file previously contained AuthenticatedNetworkClient class.
// That class is UNUSED and has been removed.
// The app uses HttpClient singleton (HttpClient.kt) for all authenticated requests.
//
// Kept in this file: CurrentUser singleton (still in use by HttpClient)

/**
 * Singleton holder for the current user ID.
 * This allows repositories and data sources to access the current user
 * without needing direct access to the AuthRepository.
 */
object CurrentUser {
  private var _userId: String? = null
  private var _accessToken: String? = null
  private var _deviceId: String? = null

  /**
   * Current user ID (null if not logged in)
   */
  val userId: String?
    get() = _userId

  /**
   * Current access token (null if not logged in)
   */
  val accessToken: String?
    get() = _accessToken

  /**
   * Current device ID (for tracking native integrations per device)
   */
  val deviceId: String?
    get() = _deviceId

  /**
   * Check if a user is currently logged in
   */
  val isLoggedIn: Boolean
    get() = _userId != null && _accessToken != null

  /**
   * Set the current user (called after successful login).
   * PRODUCTION: Validates that JWT token contains matching user_id.
   * Prevents header injection attacks where userId and token don't match.
   */
  fun setUser(userId: String?, accessToken: String?) {
    // Security validation: ensure token contains matching user_id
    if (userId != null && accessToken != null) {
      val tokenUserId = extractUserIdFromJWT(accessToken)
      if (tokenUserId != null && tokenUserId != userId) {
        KlikLogger.e("CurrentUser", "SECURITY ERROR: Token user_id mismatch! Expected: $userId, Token contains: $tokenUserId")
        throw IllegalStateException(
          "JWT token user_id mismatch: expected '$userId' but token contains '$tokenUserId'. " +
            "This indicates a security violation or token tampering.",
        )
      }
      if (tokenUserId == null) {
        KlikLogger.w("CurrentUser", "Could not extract user_id from JWT token - token might be malformed or using unexpected structure")
      }
    }

    _userId = userId
    _accessToken = accessToken
  }

  /**
   * Extract user_id from JWT token (from "sub" claim).
   * Returns null if token is malformed or doesn't contain "sub" claim.
   */
  private fun extractUserIdFromJWT(token: String): String? {
    return try {
      // JWT structure: header.payload.signature
      val parts = token.split(".")
      if (parts.size != 3) {
        KlikLogger.e("CurrentUser", "Invalid JWT structure: expected 3 parts, got ${parts.size}")
        return null
      }

      // PRODUCTION: Use platform-specific base64 decoder (same as HttpClient uses)
      val payload = decodeBase64Url(parts[1])

      // Extract "sub" claim (user_id) from JSON payload
      // Payload format: {"sub":"user_12345","exp":1234567890,...}
      val subMatch = Regex("\"sub\"\\s*:\\s*\"([^\"]+)\"").find(payload)
      val userId = subMatch?.groupValues?.get(1)

      if (userId == null) {
        KlikLogger.w("CurrentUser", "JWT token does not contain 'sub' claim")
      }

      userId
    } catch (e: Exception) {
      KlikLogger.e("CurrentUser", "Error extracting user_id from JWT: ${e.message}", e)
      null
    }
  }

  /**
   * Decode base64url string (used in JWT).
   * Base64url is like base64 but uses - instead of + and _ instead of /
   * Uses platform-specific decoder from HttpClient.
   */
  private fun decodeBase64Url(base64url: String): String {
    // Convert base64url to standard base64
    val standardBase64 = base64url
      .replace('-', '+')
      .replace('_', '/')

    // Add padding if needed (base64 requires length to be multiple of 4)
    val padded = when (standardBase64.length % 4) {
      2 -> "$standardBase64=="
      3 -> "$standardBase64="
      else -> standardBase64
    }

    // PRODUCTION: Use the same platform-specific decoder as HttpClient
    return io.github.fletchmckee.liquid.samples.app.data.network.decodeBase64Platform(padded)
  }

  /**
   * Set the device ID (typically generated on first app launch and stored)
   */
  fun setDeviceId(deviceId: String?) {
    _deviceId = deviceId
  }

  /**
   * Clear the current user (called on logout)
   */
  fun clear() {
    _userId = null
    _accessToken = null
    // Note: deviceId is not cleared on logout - it's device-specific, not user-specific
  }

  /**
   * Get headers map for API requests.
   *
   * Returns ONLY the Authorization Bearer token — backend extracts user_id from the JWT
   * "sub" claim. Callers (e.g. [HttpClient.executeWithRetry]) are responsible for adding
   * Content-Type and Accept headers appropriate to the request body, which keeps multipart
   * uploads and external-API calls free of unwanted application/json overrides.
   */
  fun getAuthHeaders(): Map<String, String> {
    val token = _accessToken ?: return emptyMap()
    return mapOf(ApiConfig.Headers.AUTHORIZATION to "Bearer $token")
  }
}
