// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser
import io.github.fletchmckee.liquid.samples.app.data.network.NativeHttpClient
import io.github.fletchmckee.liquid.samples.app.data.storage.AuthStorageKeys
import io.github.fletchmckee.liquid.samples.app.data.storage.SecureStorage
import io.github.fletchmckee.liquid.samples.app.domain.entity.AppleSignInCredentials
import io.github.fletchmckee.liquid.samples.app.domain.entity.AuthResponse
import io.github.fletchmckee.liquid.samples.app.domain.entity.AuthState
import io.github.fletchmckee.liquid.samples.app.domain.entity.LoginCredentials
import io.github.fletchmckee.liquid.samples.app.domain.entity.SignupCredentials
import io.github.fletchmckee.liquid.samples.app.domain.repository.AuthRepository
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.model.clearArchivePinState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

/**
 * Implementation of AuthRepository.
 * Uses SecureStorage (Keychain on iOS) for persistent auth state.
 */
class AuthRepositoryImpl : AuthRepository {

  // PRODUCTION: Auth endpoints (login/refresh) MUST use NativeHttpClient directly
  // Reason: HttpClient singleton adds token refresh logic which would create infinite loop
  // - Login: No token exists yet
  // - Refresh: Called BY HttpClient when token expires - can't use HttpClient
  private val authHttpClient: NativeHttpClient = NativeHttpClient()
  private val secureStorage: SecureStorage = SecureStorage()

  private val json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
  }

  companion object {
    // Singleton state shared across ALL AuthRepositoryImpl instances.
    // This avoids state divergence when AuthViewModel/AppModule each create their own AuthRepositoryImpl.
    private var cachedAuthState: AuthState = AuthState()
    private val sharedAuthStateFlow = MutableStateFlow(AuthState())
    private var hasLoadedFromStorage = false
    private var hasRegisteredCallback = false // PRODUCTION: Prevent duplicate callback registration
  }

  init {
    // Load from secure storage on first initialization
    if (!hasLoadedFromStorage) {
      loadFromSecureStorage()
      hasLoadedFromStorage = true
    }
    // Restore cached state on initialization (shared)
    sharedAuthStateFlow.value = cachedAuthState
    // Sync CurrentUser with restored state
    if (cachedAuthState.isLoggedIn) {
      CurrentUser.setUser(cachedAuthState.userId, cachedAuthState.accessToken)
    }
    // PRODUCTION: Register token refresh callback ONCE globally
    if (!hasRegisteredCallback) {
      registerTokenRefreshCallback()
      hasRegisteredCallback = true
    }
  }

  /**
   * Register token refresh callback with global HttpClient.
   * This enables automatic token refresh when 401 responses are received.
   */
  private fun registerTokenRefreshCallback() {
    io.github.fletchmckee.liquid.samples.app.data.network.HttpClient.setTokenRefreshCallback {
      try {
        KlikLogger.i("AuthRepository", "Token refresh callback invoked")
        val result = refreshToken()
        when (result) {
          is Result.Success -> {
            KlikLogger.i("AuthRepository", "Token refresh callback SUCCESS")
            true
          }

          is Result.Error -> {
            KlikLogger.e("AuthRepository", "Token refresh callback FAILED: ${result.exception.message}", result.exception)
            false
          }

          is Result.Loading -> {
            KlikLogger.w("AuthRepository", "Token refresh callback returned Loading (unexpected)")
            false
          }
        }
      } catch (e: kotlin.coroutines.cancellation.CancellationException) {
        throw e
      } catch (e: Exception) {
        KlikLogger.e("AuthRepository", "Token refresh callback EXCEPTION: ${e.message}", e)
        false
      }
    }
  }

  private fun loadFromSecureStorage() {
    val isLoggedIn = secureStorage.getString(AuthStorageKeys.IS_LOGGED_IN) == "true"
    if (isLoggedIn) {
      cachedAuthState = AuthState(
        isLoggedIn = true,
        userId = secureStorage.getString(AuthStorageKeys.USER_ID),
        accessToken = secureStorage.getString(AuthStorageKeys.ACCESS_TOKEN),
        refreshToken = secureStorage.getString(AuthStorageKeys.REFRESH_TOKEN),
        userName = secureStorage.getString(AuthStorageKeys.USER_NAME),
        userEmail = secureStorage.getString(AuthStorageKeys.USER_EMAIL),
      )
      KlikLogger.i("AuthRepository", "Loaded auth state from secure storage: userId=${cachedAuthState.userId}")
    }
  }

  private fun saveToSecureStorage(state: AuthState) {
    if (state.isLoggedIn) {
      secureStorage.saveString(AuthStorageKeys.IS_LOGGED_IN, "true")
      state.userId?.let { secureStorage.saveString(AuthStorageKeys.USER_ID, it) }
      state.accessToken?.let { secureStorage.saveString(AuthStorageKeys.ACCESS_TOKEN, it) }
      state.refreshToken?.let { secureStorage.saveString(AuthStorageKeys.REFRESH_TOKEN, it) }
      state.userName?.let { secureStorage.saveString(AuthStorageKeys.USER_NAME, it) }
      state.userEmail?.let { secureStorage.saveString(AuthStorageKeys.USER_EMAIL, it) }
      KlikLogger.i("AuthRepository", "Saved auth state to secure storage")
    } else {
      clearSecureStorage()
    }
  }

  private fun clearSecureStorage() {
    secureStorage.clear()
    KlikLogger.i("AuthRepository", "Cleared secure storage")
  }

  override suspend fun login(credentials: LoginCredentials): Result<AuthResponse> = try {
    val response = backendLogin(credentials)

    if (response.success) {
      val newState = AuthState(
        isLoggedIn = true,
        userId = response.userId,
        accessToken = response.accessToken,
        refreshToken = response.refreshToken,
        userName = response.userName,
        userEmail = response.userEmail,
      )
      saveAuthState(newState)
      Result.Success(response)
    } else {
      Result.Error(Exception(response.message ?: "Login failed"))
    }
  } catch (e: Exception) {
    KlikLogger.e("AuthRepository", "Login failed: ${e.message}", e)
    Result.Error(e)
  }

  override suspend fun signup(credentials: SignupCredentials): Result<AuthResponse> = try {
    val response = backendRegister(credentials)

    if (response.success) {
      val newState = AuthState(
        isLoggedIn = true,
        userId = response.userId,
        accessToken = response.accessToken,
        refreshToken = response.refreshToken,
        userName = response.userName,
        userEmail = response.userEmail,
      )
      saveAuthState(newState)
      Result.Success(response)
    } else {
      Result.Error(Exception(response.message ?: "Signup failed"))
    }
  } catch (e: Exception) {
    KlikLogger.e("AuthRepository", "Signup failed: ${e.message}", e)
    Result.Error(e)
  }

  override suspend fun loginWithApple(credentials: AppleSignInCredentials): Result<AuthResponse> = try {
    val response = backendAppleLogin(credentials)

    if (response.success) {
      val newState = AuthState(
        isLoggedIn = true,
        userId = response.userId,
        accessToken = response.accessToken,
        refreshToken = response.refreshToken,
        userName = response.userName,
        userEmail = response.userEmail,
      )
      saveAuthState(newState)
      Result.Success(response)
    } else {
      Result.Error(Exception(response.message ?: "Apple Sign In failed"))
    }
  } catch (e: Exception) {
    KlikLogger.e("AuthRepository", "Apple Sign In failed: ${e.message}", e)
    Result.Error(e)
  }

  override suspend fun logout(): Result<Unit> = try {
    clearAuthState()
    Result.Success(Unit)
  } catch (e: Exception) {
    KlikLogger.e("AuthRepository", "Logout failed: ${e.message}", e)
    Result.Error(e)
  }

  override suspend fun getAuthState(): AuthState = sharedAuthStateFlow.value

  override fun observeAuthState(): Flow<AuthState> = sharedAuthStateFlow.asStateFlow()

  override suspend fun saveAuthState(state: AuthState) {
    cachedAuthState = state
    sharedAuthStateFlow.value = state
    // Persist to secure storage
    saveToSecureStorage(state)
    // Sync with CurrentUser for API calls
    if (state.isLoggedIn) {
      CurrentUser.setUser(state.userId, state.accessToken)
    } else {
      CurrentUser.clear()
    }
  }

  override suspend fun clearAuthState() {
    cachedAuthState = AuthState()
    sharedAuthStateFlow.value = AuthState()
    // Clear secure storage
    clearSecureStorage()
    // Clear CurrentUser on logout
    CurrentUser.clear()
    // Clear token refresh callback
    io.github.fletchmckee.liquid.samples.app.data.network.HttpClient.clearTokenRefreshCallback()
    // Clear archive/pin state to prevent leaking across users
    clearArchivePinState()
  }

  override suspend fun refreshToken(): Result<AuthResponse> {
    KlikLogger.i("AuthRepository", "refreshToken() called")
    val currentState = sharedAuthStateFlow.value
    if (currentState.refreshToken == null) {
      KlikLogger.e("AuthRepository", "No refresh token available in state")
      return Result.Error(Exception("No refresh token available"))
    }

    KlikLogger.d("AuthRepository", "Using refresh token: [present]")

    return try {
      val response = backendRefreshToken(currentState.refreshToken!!)
      KlikLogger.i("AuthRepository", "Backend returned new access token")

      val newState = currentState.copy(
        accessToken = response.accessToken,
        refreshToken = response.refreshToken ?: currentState.refreshToken,
      )
      saveAuthState(newState)

      // Update CurrentUser with new token
      CurrentUser.setUser(newState.userId, newState.accessToken)
      KlikLogger.i("AuthRepository", "CurrentUser updated with new token")

      Result.Success(response)
    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
      throw e
    } catch (e: Exception) {
      KlikLogger.e("AuthRepository", "Token refresh EXCEPTION: ${e.message}", e)

      // Terminal auth failure: refresh token is invalid/expired.
      // Clear all auth state to stop retry loops and redirect to login.
      val msg = e.message ?: ""
      if (msg.contains("Invalid or expired refresh token") ||
        msg.contains("refresh token", ignoreCase = true)
      ) {
        KlikLogger.e("AuthRepository", "Terminal auth failure — clearing auth state, user must re-login")
        clearAuthState()
      }

      Result.Error(Exception("Token refresh failed: ${e.message}"))
    }
  }

  override suspend fun isTokenValid(): Boolean {
    val state = sharedAuthStateFlow.value
    return state.isLoggedIn && state.accessToken != null
  }

  override suspend fun requestPasswordReset(email: String): Result<Unit> {
    return try {
      val body = buildJsonObject {
        put("email", email.trim())
      }.toString()
      val url = "${ApiConfig.AUTH_BASE_URL}/password/reset"
      val headers = mapOf(
        ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
        ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
      )

      KlikLogger.d("AuthRepository", "Requesting password reset for $email via $url")
      val responseText = authHttpClient.post(url, body, headers)

      if (responseText == null) {
        return Result.Error(Exception("No response from server"))
      }

      // Check for error responses
      val detail = extractFastApiDetail(responseText)
      if (detail != null && (responseText.contains("\"error\"") || responseText.contains("\"detail\""))) {
        val isError = responseText.contains("error", ignoreCase = true) &&
          !responseText.contains("\"success\"", ignoreCase = true)
        if (isError) {
          return Result.Error(Exception(detail))
        }
      }

      KlikLogger.i("AuthRepository", "Password reset email sent to $email")
      Result.Success(Unit)
    } catch (e: Exception) {
      KlikLogger.e("AuthRepository", "Password reset request failed: ${e.message}", e)
      Result.Error(e)
    }
  }

  override suspend fun requestEmailVerification(): Result<Unit> {
    return try {
      val url = "${ApiConfig.AUTH_BASE_URL}/email/verify"
      val headers = mapOf(
        ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
        ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
        "Authorization" to "Bearer ${sharedAuthStateFlow.value.accessToken}",
      )

      KlikLogger.d("AuthRepository", "Requesting email verification via $url")
      val responseText = authHttpClient.post(url, "{}", headers)

      if (responseText == null) {
        return Result.Error(Exception("No response from server"))
      }

      val detail = extractFastApiDetail(responseText)
      if (detail != null && responseText.contains("\"error\"")) {
        return Result.Error(Exception(detail))
      }

      KlikLogger.i("AuthRepository", "Email verification sent")
      Result.Success(Unit)
    } catch (e: Exception) {
      KlikLogger.e("AuthRepository", "Email verification request failed: ${e.message}", e)
      Result.Error(e)
    }
  }

  override suspend fun deleteAccount(password: String): Result<Unit> {
    return try {
      val url = "${ApiConfig.AUTH_BASE_URL}/account"
      val headers = mapOf(
        ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
        ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
        "Authorization" to "Bearer ${sharedAuthStateFlow.value.accessToken}",
      )
      val body = buildJsonObject {
        put("password", password)
        put("confirmation", "DELETE MY ACCOUNT")
      }.toString()

      KlikLogger.d("AuthRepository", "Deleting account via $url")
      val responseText = authHttpClient.delete(url, headers, body)

      if (responseText == null) {
        return Result.Error(Exception("No response from server"))
      }

      val detail = extractFastApiDetail(responseText)
      if (detail != null && responseText.contains("\"error\"")) {
        return Result.Error(Exception(detail))
      }

      // Clear local state after account deletion
      clearAuthState()
      KlikLogger.i("AuthRepository", "Account deleted successfully")
      Result.Success(Unit)
    } catch (e: Exception) {
      KlikLogger.e("AuthRepository", "Account deletion failed: ${e.message}", e)
      Result.Error(e)
    }
  }

  override suspend fun updateProfile(fullName: String?, occupation: String?): Result<Unit> {
    if (fullName == null && occupation == null) {
      return Result.Error(IllegalArgumentException("At least one of fullName or occupation is required"))
    }
    return try {
      val url = "${ApiConfig.AUTH_BASE_URL}/profile"
      val body = buildJsonObject {
        if (fullName != null) put("full_name", fullName)
        if (occupation != null) put("occupation", occupation)
      }.toString()

      KlikLogger.d("AuthRepository", "PATCH $url")
      val responseText = io.github.fletchmckee.liquid.samples.app.data.network.HttpClient.patchUrl(url, body)
      if (responseText == null) return Result.Error(Exception("No response from server"))

      val detail = extractFastApiDetail(responseText)
      if (detail != null && responseText.contains("\"detail\"")) {
        return Result.Error(Exception(detail))
      }
      KlikLogger.i("AuthRepository", "Profile updated (name=${fullName != null}, occupation=$occupation)")
      Result.Success(Unit)
    } catch (e: Exception) {
      KlikLogger.e("AuthRepository", "updateProfile failed: ${e.message}", e)
      Result.Error(e)
    }
  }

  override suspend fun uploadAvatar(
    image: io.github.fletchmckee.liquid.samples.app.platform.PickedImage,
  ): Result<String> {
    return try {
      val url = "${ApiConfig.AUTH_BASE_URL}/profile/avatar"
      KlikLogger.d("AuthRepository", "POST (multipart) $url, ${image.bytes.size} bytes (${image.mimeType})")
      val responseText = io.github.fletchmckee.liquid.samples.app.data.network.HttpClient.postMultipartUrl(
        url = url,
        fileData = image.bytes,
        fileName = image.fileName,
        fieldName = "file",
        headers = mapOf("Content-Type" to image.mimeType),
      )
      if (responseText == null) return Result.Error(Exception("No response from server"))

      val parsed = json.parseToJsonElement(responseText).jsonObject
      val detail = parsed["detail"]?.jsonPrimitive?.contentOrNull
      if (detail != null) return Result.Error(Exception(detail))
      val avatarUrl = parsed["avatar_url"]?.jsonPrimitive?.contentOrNull
        ?: return Result.Error(Exception("Server did not return avatar_url"))

      KlikLogger.i("AuthRepository", "Avatar uploaded: $avatarUrl")
      Result.Success(avatarUrl)
    } catch (e: Exception) {
      KlikLogger.e("AuthRepository", "uploadAvatar failed: ${e.message}", e)
      Result.Error(e)
    }
  }

  // ============================
  // Real backend auth (8833)
  // ============================

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

  private suspend fun backendLogin(credentials: LoginCredentials): AuthResponse {
    val request = BackendLoginRequest(
      username_or_email = credentials.identifier.trim(),
      password = credentials.password,
      device_id = io.github.fletchmckee.liquid.samples.app.data.network.DeviceInfo.getDeviceId(),
      device_name = io.github.fletchmckee.liquid.samples.app.data.network.DeviceInfo.getDeviceName(),
      device_type = io.github.fletchmckee.liquid.samples.app.data.network.DeviceInfo.getDeviceType(),
      timezone = kotlinx.datetime.TimeZone.currentSystemDefault().id,
    )
    val body = json.encodeToString(request)
    val url = "${ApiConfig.AUTH_BASE_URL}/login"
    val headers = mapOf(
      ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
      ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
    )

    val responseText = authHttpClient.post(url, body, headers)
      ?: throw Exception("Empty response from auth server")

    val token = try {
      json.decodeFromString(BackendTokenResponse.serializer(), responseText)
    } catch (parseError: Exception) {
      val detail = extractFastApiDetail(responseText)
      throw Exception(detail ?: "Login failed: ${parseError.message}")
    }

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

  private suspend fun backendRegister(credentials: SignupCredentials): AuthResponse {
    val username = credentials.email.substringBefore("@").ifBlank { credentials.name.trim() }
    val request = BackendRegisterRequest(
      username = username,
      email = credentials.email.trim(),
      password = credentials.password,
      device_id = io.github.fletchmckee.liquid.samples.app.data.network.DeviceInfo.getDeviceId(),
      device_name = io.github.fletchmckee.liquid.samples.app.data.network.DeviceInfo.getDeviceName(),
      device_type = io.github.fletchmckee.liquid.samples.app.data.network.DeviceInfo.getDeviceType(),
      age_confirmed_over_13 = true,
      timezone = kotlinx.datetime.TimeZone.currentSystemDefault().id,
    )
    val body = json.encodeToString(request)
    val url = "${ApiConfig.AUTH_BASE_URL}/register"
    val headers = mapOf(
      ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
      ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
    )

    val responseText = authHttpClient.post(url, body, headers)
      ?: throw Exception("Empty response from auth server")

    val token = try {
      json.decodeFromString(BackendTokenResponse.serializer(), responseText)
    } catch (parseError: Exception) {
      val detail = extractFastApiDetail(responseText)
      throw Exception(detail ?: "Register failed: ${parseError.message}")
    }

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

  private suspend fun backendAppleLogin(credentials: AppleSignInCredentials): AuthResponse {
    val request = BackendAppleLoginRequest(
      identity_token = credentials.identityToken,
      authorization_code = credentials.authorizationCode,
      user_id = credentials.userId,
      email = credentials.email,
      full_name = credentials.fullName,
      is_real_email = credentials.isRealEmail,
      device_id = io.github.fletchmckee.liquid.samples.app.data.network.DeviceInfo.getDeviceId(),
      age_confirmed_over_13 = true,
      timezone = kotlinx.datetime.TimeZone.currentSystemDefault().id,
    )
    val body = json.encodeToString(request)
    val url = "${ApiConfig.AUTH_BASE_URL}/apple/callback"
    val headers = mapOf(
      ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
      ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
    )

    KlikLogger.d("AuthRepository", "Apple Sign In: calling $url")

    val responseText = authHttpClient.post(url, body, headers)
      ?: throw Exception("Empty response from auth server")

    val token = try {
      json.decodeFromString(BackendTokenResponse.serializer(), responseText)
    } catch (parseError: Exception) {
      val detail = extractFastApiDetail(responseText)
      throw Exception(detail ?: "Apple Sign In failed: ${parseError.message}")
    }

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

  @Serializable
  private data class BackendRefreshRequest(
    val refresh_token: String,
  )

  /**
   * Refresh token via backend /refresh endpoint.
   * Backend MUST return the same format as login: access_token, refresh_token, user_id.
   * NO FALLBACKS - if backend returns incomplete response, it FAILS.
   */
  private suspend fun backendRefreshToken(refreshToken: String): AuthResponse {
    val request = BackendRefreshRequest(refresh_token = refreshToken)
    val body = json.encodeToString(request)
    val url = "${ApiConfig.AUTH_BASE_URL}/refresh"
    val headers = mapOf(
      ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
      ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
    )

    KlikLogger.d("AuthRepository", "Refreshing token via $url")
    // Retry on network failure (TLS errors) - same resilience as all other HTTP calls
    var responseText: String? = null
    for (attempt in 1..3) {
      responseText = authHttpClient.post(url, body, headers)
      if (responseText != null) break
      if (attempt < 3) {
        KlikLogger.w("AuthRepository", "Token refresh network error (attempt $attempt/3), retrying in ${attempt * 2}s...")
        kotlinx.coroutines.delay(attempt * 2000L)
      }
    }
    if (responseText == null) {
      throw Exception("Empty response from auth server after 3 attempts - network connection failed")
    }

    val token = try {
      json.decodeFromString(BackendTokenResponse.serializer(), responseText)
    } catch (parseError: Exception) {
      KlikLogger.e("AuthRepository", "BACKEND ERROR: /refresh returned invalid response. Expected: {access_token, refresh_token, user_id}. Got: $responseText. Parse error: ${parseError.message}", parseError)
      val detail = extractFastApiDetail(responseText)
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

  private fun extractFastApiDetail(responseText: String): String? {
    // Handle plain text error responses (e.g., "Internal Server Error")
    val trimmed = responseText.trim()
    if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
      // Not JSON - return as plain text if it's a recognizable error
      return when {
        trimmed.contains("Internal Server Error", ignoreCase = true) ->
          "Server error. Please try again later."

        trimmed.contains("Service Unavailable", ignoreCase = true) ->
          "Service temporarily unavailable. Please try again later."

        trimmed.contains("Bad Gateway", ignoreCase = true) ->
          "Server connection error. Please try again later."

        trimmed.contains("Gateway Timeout", ignoreCase = true) ->
          "Server timeout. Please try again later."

        trimmed.isNotEmpty() && trimmed.length < 200 -> trimmed

        else -> null
      }
    }

    // Try to parse as JSON
    val element = try {
      json.parseToJsonElement(responseText)
    } catch (_: Exception) {
      return null
    }
    val obj: JsonObject = element as? JsonObject ?: return null

    val detail: JsonElement = obj["detail"] ?: return null
    return when (detail) {
      is JsonPrimitive -> if (detail.isString) detail.content else detail.toString()
      is JsonObject -> detail.toString()
      is JsonArray -> detail.toString()
      else -> detail.toString()
    }
  }
}
