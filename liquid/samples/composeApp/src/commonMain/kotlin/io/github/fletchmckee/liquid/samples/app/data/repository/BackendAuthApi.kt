// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.data.network.DeviceInfo
import io.github.fletchmckee.liquid.samples.app.data.network.NativeHttpClient
import io.github.fletchmckee.liquid.samples.app.data.network.retryNetworkRaw
import io.github.fletchmckee.liquid.samples.app.domain.entity.AppleSignInCredentials
import io.github.fletchmckee.liquid.samples.app.domain.entity.AuthResponse
import io.github.fletchmckee.liquid.samples.app.domain.entity.LoginCredentials
import io.github.fletchmckee.liquid.samples.app.domain.entity.SignupCredentials
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Thin wrapper over the unauthenticated KK_auth endpoints (login, register, Apple Sign-In,
 * refresh). Lives in its own file because it owns the wire-format DTOs and is intentionally
 * separate from [AuthRepositoryImpl] — the repository handles state, callbacks and
 * orchestration; this class handles only the request/response shapes.
 *
 * Why [NativeHttpClient] directly instead of [io.github.fletchmckee.liquid.samples.app.data.network.HttpClient]:
 * the singleton client adds automatic 401-refresh, which would loop forever for /refresh
 * (the very call that exists to break that loop) and is meaningless for /login (no token yet).
 */
internal class BackendAuthApi(
  private val nativeHttpClient: NativeHttpClient,
  private val json: Json,
  private val errorParser: FastApiErrorParser,
) {

  @Serializable
  private data class BackendLoginRequest(
    val username_or_email: String,
    val password: String,
    val device_id: String,
    val device_name: String? = null,
    val device_type: String? = null,
    val timezone: String,
  )

  @Serializable
  private data class BackendRegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val device_id: String,
    val device_name: String? = null,
    val device_type: String? = null,
    val age_confirmed_over_13: Boolean,
    val timezone: String,
  )

  @Serializable
  private data class BackendAppleLoginRequest(
    val identity_token: String,
    val authorization_code: String,
    val user_id: String,
    val email: String?,
    val full_name: String?,
    val is_real_email: Boolean,
    val device_id: String,
    val age_confirmed_over_13: Boolean,
    val timezone: String,
  )

  @Serializable
  private data class BackendTokenResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String = "bearer",
    val user_id: String,
    val username: String? = null,
    val role: String? = null,
  )

  @Serializable
  private data class BackendRefreshRequest(
    val refresh_token: String,
  )

  private val jsonHeaders: Map<String, String> = mapOf(
    ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
    ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
  )

  suspend fun login(credentials: LoginCredentials): AuthResponse {
    val request = BackendLoginRequest(
      username_or_email = credentials.identifier.trim(),
      password = credentials.password,
      device_id = DeviceInfo.getDeviceId(),
      device_name = DeviceInfo.getDeviceName(),
      device_type = DeviceInfo.getDeviceType(),
      timezone = kotlinx.datetime.TimeZone.currentSystemDefault().id,
    )
    val token = postForToken("${ApiConfig.AUTH_BASE_URL}/login", json.encodeToString(request), "Login")
    return AuthResponse(
      success = true,
      userId = token.user_id,
      accessToken = token.access_token,
      refreshToken = token.refresh_token,
      userName = token.username,
      userEmail = if (credentials.isEmail) credentials.identifier.trim() else null,
      message = "Login successful",
    )
  }

  suspend fun register(credentials: SignupCredentials): AuthResponse {
    val username = credentials.email.substringBefore("@").ifBlank { credentials.name.trim() }
    val request = BackendRegisterRequest(
      username = username,
      email = credentials.email.trim(),
      password = credentials.password,
      device_id = DeviceInfo.getDeviceId(),
      device_name = DeviceInfo.getDeviceName(),
      device_type = DeviceInfo.getDeviceType(),
      age_confirmed_over_13 = true,
      timezone = kotlinx.datetime.TimeZone.currentSystemDefault().id,
    )
    val token = postForToken("${ApiConfig.AUTH_BASE_URL}/register", json.encodeToString(request), "Register")
    return AuthResponse(
      success = true,
      userId = token.user_id,
      accessToken = token.access_token,
      refreshToken = token.refresh_token,
      userName = token.username ?: username,
      userEmail = credentials.email.trim(),
      message = "Account created successfully",
    )
  }

  suspend fun appleLogin(credentials: AppleSignInCredentials): AuthResponse {
    val request = BackendAppleLoginRequest(
      identity_token = credentials.identityToken,
      authorization_code = credentials.authorizationCode,
      user_id = credentials.userId,
      email = credentials.email,
      full_name = credentials.fullName,
      is_real_email = credentials.isRealEmail,
      device_id = DeviceInfo.getDeviceId(),
      age_confirmed_over_13 = true,
      timezone = kotlinx.datetime.TimeZone.currentSystemDefault().id,
    )
    val url = "${ApiConfig.AUTH_BASE_URL}/apple/callback"
    KlikLogger.d("AuthRepository", "Apple Sign In: calling $url")
    val token = postForToken(url, json.encodeToString(request), "Apple Sign In")
    return AuthResponse(
      success = true,
      userId = token.user_id,
      accessToken = token.access_token,
      refreshToken = token.refresh_token,
      userName = token.username ?: credentials.fullName,
      userEmail = credentials.email,
      message = "Apple Sign In successful",
    )
  }

  /**
   * Refresh access token via the /refresh endpoint.
   *
   * Status semantics:
   *   - 0       → exhausted network retries (callers should not clear auth state)
   *   - 401/403 → refresh token is invalid/expired; throws an Exception whose message
   *               contains "refresh token", which [AuthRepositoryImpl.refreshToken] uses
   *               to trigger terminal auth-state cleanup
   *   - 2xx     → parse [BackendTokenResponse] and return [AuthResponse]
   *   - other   → throw with FastAPI detail when present
   */
  suspend fun refreshToken(refreshToken: String): AuthResponse {
    val url = "${ApiConfig.AUTH_BASE_URL}/refresh"
    val body = json.encodeToString(BackendRefreshRequest(refresh_token = refreshToken))
    KlikLogger.d("AuthRepository", "Refreshing token via $url")

    val response = retryNetworkRaw("POST", url) {
      nativeHttpClient.post(url, body, jsonHeaders)
    }
    if (response.status == 0) {
      throw Exception("Empty response from auth server - network connection failed")
    }
    if (response.status == 401 || response.status == 403) {
      throw Exception("Invalid or expired refresh token (status=${response.status})")
    }
    val responseText = response.body
      ?: throw Exception("Empty body from auth server (status=${response.status})")

    val token = try {
      json.decodeFromString(BackendTokenResponse.serializer(), responseText)
    } catch (parseError: Exception) {
      KlikLogger.e("AuthRepository", "BACKEND ERROR: /refresh returned invalid response. Expected: {access_token, refresh_token, user_id}. Got: $responseText. Parse error: ${parseError.message}", parseError)
      val detail = errorParser.extract(responseText)
      throw Exception(detail ?: "BACKEND BUG: /refresh must return access_token, refresh_token, user_id. Got: $responseText")
    }

    KlikLogger.i("AuthRepository", "Token refresh successful, userId=${token.user_id}")

    return AuthResponse(
      success = true,
      userId = token.user_id,
      accessToken = token.access_token,
      refreshToken = token.refresh_token,
      userName = token.username,
      userEmail = null,
      message = "Token refreshed successfully",
    )
  }

  private suspend fun postForToken(url: String, body: String, label: String): BackendTokenResponse {
    val responseText = nativeHttpClient.post(url, body, jsonHeaders).body
      ?: throw Exception("Empty response from auth server")
    return try {
      json.decodeFromString(BackendTokenResponse.serializer(), responseText)
    } catch (parseError: Exception) {
      val detail = errorParser.extract(responseText)
      throw Exception(detail ?: "$label failed: ${parseError.message}")
    }
  }
}
