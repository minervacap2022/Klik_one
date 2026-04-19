// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.presentation.auth

import io.github.fletchmckee.liquid.samples.app.core.BaseViewModel
import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.repository.AuthRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.domain.entity.AppleSignInCredentials
import io.github.fletchmckee.liquid.samples.app.domain.entity.LoginCredentials
import io.github.fletchmckee.liquid.samples.app.domain.entity.SignupCredentials
import io.github.fletchmckee.liquid.samples.app.domain.repository.AuthRepository
import io.github.fletchmckee.liquid.samples.app.di.AppModule
import io.github.fletchmckee.liquid.samples.app.platform.AppleSignInResult
import io.github.fletchmckee.liquid.samples.app.platform.AppleSignInService

/**
 * UI State for the Auth screen
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val identifier: String = "",  // Can be email OR username for login
    val email: String = "",       // Email for signup
    val password: String = "",
    val name: String = "",
    val isLoginMode: Boolean = true, // true = login, false = signup
    val isForgotPasswordMode: Boolean = false,
    val forgotPasswordEmail: String = "",
    val passwordResetSent: Boolean = false,
    val error: String? = null,
    val userName: String? = null,
    val userId: String? = null
)

/**
 * One-time events for the Auth screen
 */
sealed class AuthEvent {
    data object LoginSuccess : AuthEvent()
    data object SignupSuccess : AuthEvent()
    data object LogoutSuccess : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
}

/**
 * ViewModel for authentication
 */
class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : BaseViewModel<AuthUiState, AuthEvent>() {

    override val initialState: AuthUiState = AuthUiState()

    init {
        // Check initial auth state
        launch {
            val authState = authRepository.getAuthState()
            updateState {
                copy(
                    isLoggedIn = authState.isLoggedIn,
                    userName = authState.userName,
                    userId = authState.userId
                )
            }
        }

        // Observe auth state changes
        launch {
            authRepository.observeAuthState().collect { authState ->
                updateState {
                    copy(
                        isLoggedIn = authState.isLoggedIn,
                        userName = authState.userName,
                        userId = authState.userId
                    )
                }
            }
        }
    }

    fun updateIdentifier(identifier: String) {
        updateState { copy(identifier = identifier, error = null) }
    }

    fun updateEmail(email: String) {
        updateState { copy(email = email, error = null) }
    }

    fun updatePassword(password: String) {
        updateState { copy(password = password, error = null) }
    }

    fun updateName(name: String) {
        updateState { copy(name = name, error = null) }
    }

    fun toggleMode() {
        updateState {
            copy(
                isLoginMode = !isLoginMode,
                isForgotPasswordMode = false,
                passwordResetSent = false,
                error = null
            )
        }
    }

    fun enterForgotPasswordMode() {
        updateState {
            copy(
                isForgotPasswordMode = true,
                passwordResetSent = false,
                forgotPasswordEmail = identifier.ifBlank { email },
                error = null
            )
        }
    }

    fun exitForgotPasswordMode() {
        updateState {
            copy(
                isForgotPasswordMode = false,
                passwordResetSent = false,
                error = null
            )
        }
    }

    fun updateForgotPasswordEmail(email: String) {
        updateState { copy(forgotPasswordEmail = email, error = null) }
    }

    fun requestPasswordReset() {
        val email = currentState.forgotPasswordEmail.trim()
        if (email.isBlank()) {
            updateState { copy(error = "Email is required") }
            return
        }

        launch {
            updateState { copy(isLoading = true, error = null) }

            when (val result = authRepository.requestPasswordReset(email)) {
                is Result.Success -> {
                    updateState {
                        copy(isLoading = false, passwordResetSent = true, error = null)
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoading = false,
                            error = result.exception.message ?: "Failed to send reset email"
                        )
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun login() {
        val state = currentState

        if (state.identifier.isBlank()) {
            updateState { copy(error = "Email or username is required") }
            return
        }

        if (state.password.isBlank()) {
            updateState { copy(error = "Password is required") }
            return
        }

        launch {
            updateState { copy(isLoading = true, error = null) }

            val result = authRepository.login(
                LoginCredentials(
                    identifier = state.identifier.trim(),
                    password = state.password
                )
            )

            when (result) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isLoading = false,
                            isLoggedIn = true,
                            password = "", // Clear password
                            error = null
                        )
                    }
                    try {
                        // Sync CurrentUser singleton with auth state for API calls
                        AppModule.syncCurrentUser()
                        // Reload data from backend with authenticated user
                        AppModule.reload()
                        sendEvent(AuthEvent.LoginSuccess)
                    } catch (e: Exception) {
                        val msg = e.message ?: "Login succeeded but data initialization failed"
                        updateState { copy(error = msg) }
                        sendEvent(AuthEvent.ShowError(msg))
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoading = false,
                            error = result.exception.message ?: "Login failed"
                        )
                    }
                    sendEvent(AuthEvent.ShowError(result.exception.message ?: "Login failed"))
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun signup() {
        val state = currentState

        if (state.name.isBlank()) {
            updateState { copy(error = "Name is required") }
            return
        }

        if (state.email.isBlank()) {
            updateState { copy(error = "Email is required") }
            return
        }

        if (state.password.length < 6) {
            updateState { copy(error = "Password must be at least 6 characters") }
            return
        }

        launch {
            updateState { copy(isLoading = true, error = null) }

            val result = authRepository.signup(
                SignupCredentials(
                    email = state.email.trim(),
                    password = state.password,
                    name = state.name.trim()
                )
            )

            when (result) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isLoading = false,
                            isLoggedIn = true,
                            password = "",
                            error = null
                        )
                    }
                    try {
                        // Sync CurrentUser singleton with auth state for API calls
                        AppModule.syncCurrentUser()
                        // Reload data from backend with authenticated user
                        AppModule.reload()
                        sendEvent(AuthEvent.SignupSuccess)
                    } catch (e: Exception) {
                        val msg = e.message ?: "Signup succeeded but data initialization failed"
                        updateState { copy(error = msg) }
                        sendEvent(AuthEvent.ShowError(msg))
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoading = false,
                            error = result.exception.message ?: "Signup failed"
                        )
                    }
                    sendEvent(AuthEvent.ShowError(result.exception.message ?: "Signup failed"))
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun logout() {
        launch {
            val result = authRepository.logout()
            when (result) {
                is Result.Success -> {
                    // Clear CurrentUser singleton
                    AppModule.syncCurrentUser()
                    setState(AuthUiState()) // Reset to initial state
                    sendEvent(AuthEvent.LogoutSuccess)
                }
                is Result.Error -> {
                    sendEvent(AuthEvent.ShowError("Logout failed"))
                }
                is Result.Loading -> {
                    // No-op
                }
            }
        }
    }

    /**
     * Initiate Sign In with Apple flow.
     * Only available on iOS.
     */
    fun loginWithApple() {
        if (!AppleSignInService.isSupported()) {
            updateState { copy(error = "Sign In with Apple is not supported on this platform") }
            return
        }

        updateState { copy(isLoading = true, error = null) }

        AppleSignInService.signIn { result ->
            when (result) {
                is AppleSignInResult.Success -> {
                    val credential = result.credential
                    launch {
                        val authResult = authRepository.loginWithApple(
                            AppleSignInCredentials(
                                identityToken = credential.identityToken,
                                authorizationCode = credential.authorizationCode,
                                userId = credential.userId,
                                email = credential.email,
                                fullName = credential.fullName,
                                isRealEmail = credential.isRealEmail
                            )
                        )

                        when (authResult) {
                            is Result.Success -> {
                                updateState {
                                    copy(
                                        isLoading = false,
                                        isLoggedIn = true,
                                        error = null
                                    )
                                }
                                try {
                                    AppModule.syncCurrentUser()
                                    AppModule.reload()
                                    sendEvent(AuthEvent.LoginSuccess)
                                } catch (e: Exception) {
                                    val msg = e.message ?: "Apple Sign In succeeded but data initialization failed"
                                    updateState { copy(error = msg) }
                                    sendEvent(AuthEvent.ShowError(msg))
                                }
                            }
                            is Result.Error -> {
                                updateState {
                                    copy(
                                        isLoading = false,
                                        error = authResult.exception.message ?: "Apple Sign In failed"
                                    )
                                }
                                sendEvent(AuthEvent.ShowError(authResult.exception.message ?: "Apple Sign In failed"))
                            }
                            is Result.Loading -> {
                                // Already handled
                            }
                        }
                    }
                }
                is AppleSignInResult.Cancelled -> {
                    updateState { copy(isLoading = false, error = null) }
                    // User cancelled, no error message needed
                }
                is AppleSignInResult.Error -> {
                    updateState {
                        copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    sendEvent(AuthEvent.ShowError(result.message))
                }
                is AppleSignInResult.NotSupported -> {
                    updateState {
                        copy(
                            isLoading = false,
                            error = "Sign In with Apple is not supported"
                        )
                    }
                }
            }
        }
    }

    /**
     * Check if Sign In with Apple is available on this platform.
     */
    fun isAppleSignInSupported(): Boolean = AppleSignInService.isSupported()

    /**
     * Check if user is currently logged in (for initial app state)
     */
    suspend fun checkAuthState(): Boolean {
        val authState = authRepository.getAuthState()
        return authState.isLoggedIn
    }

    /**
     * Get current user ID for API calls
     */
    fun getCurrentUserId(): String? {
        return currentState.userId
    }
}
