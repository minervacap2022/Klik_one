// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser
import io.github.fletchmckee.liquid.samples.app.data.network.HttpClient
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
import kotlin.concurrent.Volatile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Implementation of AuthRepository.
 *
 * Owns auth state (StateFlow + persisted Keychain mirror), token-refresh callback wiring,
 * and the user-facing endpoints (password reset, email verify, account delete, profile,
 * avatar). The unauthenticated wire-format DTOs and login/register/Apple/refresh requests
 * are delegated to [BackendAuthApi]; backend error parsing lives in [FastApiErrorParser].
 *
 * SecureStorage = iOS Keychain on iOS, EncryptedSharedPreferences on Android.
 */
class AuthRepositoryImpl : AuthRepository {

  // PRODUCTION: Auth endpoints (login/refresh) MUST use NativeHttpClient directly.
  // The HttpClient singleton adds automatic 401-refresh, which would loop forever for
  // /refresh and is meaningless for /login (no token yet).
  private val nativeHttpClient: NativeHttpClient = NativeHttpClient()
  private val secureStorage: SecureStorage = SecureStorage()

  private val json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
  }

  private val errorParser = FastApiErrorParser(json)
  private val backendAuth = BackendAuthApi(nativeHttpClient, json, errorParser)

  companion object {
    // Singleton state shared across ALL AuthRepositoryImpl instances. This avoids state
    // divergence when AuthViewModel/AppModule each construct their own AuthRepositoryImpl.
    //
    // sharedAuthStateFlow is the single source of truth — its `.value` replaces the earlier
    // `cachedAuthState` mirror, which was redundant and risked drift on concurrent updates.
    private val sharedAuthStateFlow = MutableStateFlow(AuthState())

    // @Volatile gives both JVM and Kotlin/Native a happens-before fence on the boolean
    // reads/writes, so concurrent AuthRepositoryImpl() construction can't observe a torn
    // value of either flag.
    @Volatile
    private var hasLoadedFromStorage = false

    @Volatile
    private var hasRegisteredCallback = false
  }

  init {
    // Load from secure storage on first initialization. Two parallel constructors may both
    // observe `hasLoadedFromStorage == false` — that's fine; both load the same data and
    // both set the flag, idempotent.
    if (!hasLoadedFromStorage) {
      loadFromSecureStorage()
      hasLoadedFromStorage = true
    }
    val currentState = sharedAuthStateFlow.value
    if (currentState.isLoggedIn) {
      CurrentUser.setUser(currentState.userId, currentState.accessToken)
    }
    if (!hasRegisteredCallback) {
      registerTokenRefreshCallback()
      hasRegisteredCallback = true
    }
  }

  /**
   * Register token refresh callback with the global HttpClient so 401 responses
   * trigger automatic token refresh + retry.
   */
  private fun registerTokenRefreshCallback() {
    HttpClient.setTokenRefreshCallback {
      try {
        KlikLogger.i("AuthRepository", "Token refresh callback invoked")
        when (val result = refreshToken()) {
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
      val state = AuthState(
        isLoggedIn = true,
        userId = secureStorage.getString(AuthStorageKeys.USER_ID),
        accessToken = secureStorage.getString(AuthStorageKeys.ACCESS_TOKEN),
        refreshToken = secureStorage.getString(AuthStorageKeys.REFRESH_TOKEN),
        userName = secureStorage.getString(AuthStorageKeys.USER_NAME),
        userEmail = secureStorage.getString(AuthStorageKeys.USER_EMAIL),
      )
      sharedAuthStateFlow.value = state
      KlikLogger.i("AuthRepository", "Loaded auth state from secure storage: userId=${state.userId}")
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
    val response = backendAuth.login(credentials)
    saveAuthFromResponse(response)
    Result.Success(response)
  } catch (e: Exception) {
    KlikLogger.e("AuthRepository", "Login failed: ${e.message}", e)
    Result.Error(e)
  }

  override suspend fun signup(credentials: SignupCredentials): Result<AuthResponse> = try {
    val response = backendAuth.register(credentials)
    saveAuthFromResponse(response)
    Result.Success(response)
  } catch (e: Exception) {
    KlikLogger.e("AuthRepository", "Signup failed: ${e.message}", e)
    Result.Error(e)
  }

  override suspend fun loginWithApple(credentials: AppleSignInCredentials): Result<AuthResponse> = try {
    val response = backendAuth.appleLogin(credentials)
    saveAuthFromResponse(response)
    Result.Success(response)
  } catch (e: Exception) {
    KlikLogger.e("AuthRepository", "Apple Sign In failed: ${e.message}", e)
    Result.Error(e)
  }

  /**
   * Materialize an [AuthResponse] from any of the backend auth endpoints into local state.
   */
  private suspend fun saveAuthFromResponse(response: AuthResponse) {
    val newState = AuthState(
      isLoggedIn = true,
      userId = response.userId,
      accessToken = response.accessToken,
      refreshToken = response.refreshToken,
      userName = response.userName,
      userEmail = response.userEmail,
    )
    saveAuthState(newState)
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
    sharedAuthStateFlow.value = state
    saveToSecureStorage(state)
    if (state.isLoggedIn) {
      CurrentUser.setUser(state.userId, state.accessToken)
    } else {
      CurrentUser.clear()
    }
  }

  override suspend fun clearAuthState() {
    sharedAuthStateFlow.value = AuthState()
    clearSecureStorage()
    CurrentUser.clear()
    // Clear token refresh callback and reset the registration guard so the next
    // AuthRepositoryImpl constructed after re-login re-registers it. Without this reset,
    // login → logout → login leaves HttpClient with tokenRefreshCallback=null and
    // refreshPermanentlyFailed=true, so the next access-token expiry silently logs the
    // user out again.
    HttpClient.clearTokenRefreshCallback()
    hasRegisteredCallback = false
    // Clear archive/pin state to prevent leaking across users.
    clearArchivePinState()
  }

  override suspend fun refreshToken(): Result<AuthResponse> {
    KlikLogger.i("AuthRepository", "refreshToken() called")
    val currentState = sharedAuthStateFlow.value
    val refreshToken = currentState.refreshToken
    if (refreshToken == null) {
      KlikLogger.e("AuthRepository", "No refresh token available in state")
      return Result.Error(Exception("No refresh token available"))
    }

    return try {
      val response = backendAuth.refreshToken(refreshToken)
      KlikLogger.i("AuthRepository", "Backend returned new access token")

      val newState = currentState.copy(
        accessToken = response.accessToken,
        refreshToken = response.refreshToken ?: currentState.refreshToken,
      )
      saveAuthState(newState)
      CurrentUser.setUser(newState.userId, newState.accessToken)
      KlikLogger.i("AuthRepository", "CurrentUser updated with new token")
      Result.Success(response)
    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
      throw e
    } catch (e: Exception) {
      KlikLogger.e("AuthRepository", "Token refresh EXCEPTION: ${e.message}", e)

      // Terminal auth failure: refresh token is invalid/expired. Clear all auth state to
      // stop retry loops and force the user back through login.
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
      val body = buildJsonObject { put("email", email.trim()) }.toString()
      val url = "${ApiConfig.AUTH_BASE_URL}/password/reset"
      val headers = mapOf(
        ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
        ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
      )

      KlikLogger.d("AuthRepository", "Requesting password reset for $email via $url")
      val responseText = nativeHttpClient.post(url, body, headers).body
        ?: return Result.Error(Exception("No response from server"))

      val detail = errorParser.extract(responseText)
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
        ApiConfig.Headers.AUTHORIZATION to "Bearer ${sharedAuthStateFlow.value.accessToken}",
      )

      KlikLogger.d("AuthRepository", "Requesting email verification via $url")
      val responseText = nativeHttpClient.post(url, "{}", headers).body
        ?: return Result.Error(Exception("No response from server"))

      val detail = errorParser.extract(responseText)
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
        ApiConfig.Headers.AUTHORIZATION to "Bearer ${sharedAuthStateFlow.value.accessToken}",
      )
      val body = buildJsonObject {
        put("password", password)
        put("confirmation", "DELETE MY ACCOUNT")
      }.toString()

      KlikLogger.d("AuthRepository", "Deleting account via $url")
      val responseText = nativeHttpClient.delete(url, headers, body).body
        ?: return Result.Error(Exception("No response from server"))

      val detail = errorParser.extract(responseText)
      if (detail != null && responseText.contains("\"error\"")) {
        return Result.Error(Exception(detail))
      }

      // Local cleanup happens after the backend confirms deletion.
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
      val responseText = HttpClient.patchUrl(url, body)
        ?: return Result.Error(Exception("No response from server"))

      val detail = errorParser.extract(responseText)
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
      val responseText = HttpClient.postMultipartUrl(
        url = url,
        fileData = image.bytes,
        fileName = image.fileName,
        fieldName = "file",
        headers = mapOf("Content-Type" to image.mimeType),
      ) ?: return Result.Error(Exception("No response from server"))

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
}
