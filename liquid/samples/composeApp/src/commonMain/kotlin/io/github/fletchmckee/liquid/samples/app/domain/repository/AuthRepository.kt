// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.AppleSignInCredentials
import io.github.fletchmckee.liquid.samples.app.domain.entity.AuthResponse
import io.github.fletchmckee.liquid.samples.app.domain.entity.AuthState
import io.github.fletchmckee.liquid.samples.app.domain.entity.LoginCredentials
import io.github.fletchmckee.liquid.samples.app.domain.entity.SignupCredentials
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
  /**
   * Login with email and password
   */
  suspend fun login(credentials: LoginCredentials): Result<AuthResponse>

  /**
   * Signup with email, password, and name
   */
  suspend fun signup(credentials: SignupCredentials): Result<AuthResponse>

  /**
   * Login with Apple Sign In credentials.
   * Sends identity token to backend for verification.
   */
  suspend fun loginWithApple(credentials: AppleSignInCredentials): Result<AuthResponse>

  /**
   * Logout the current user
   */
  suspend fun logout(): Result<Unit>

  /**
   * Get the current auth state
   */
  suspend fun getAuthState(): AuthState

  /**
   * Observe auth state changes
   */
  fun observeAuthState(): Flow<AuthState>

  /**
   * Save auth state to persistent storage
   */
  suspend fun saveAuthState(state: AuthState)

  /**
   * Clear auth state from persistent storage
   */
  suspend fun clearAuthState()

  /**
   * Refresh the access token using refresh token
   */
  suspend fun refreshToken(): Result<AuthResponse>

  /**
   * Check if the current token is valid
   */
  suspend fun isTokenValid(): Boolean

  /**
   * Request a password reset email
   */
  suspend fun requestPasswordReset(email: String): Result<Unit>

  /**
   * Request email verification for the current user
   */
  suspend fun requestEmailVerification(): Result<Unit>

  /**
   * Delete the current user's account
   */
  suspend fun deleteAccount(password: String): Result<Unit>

  /**
   * Update the current user's onboarding profile fields. At least one of [fullName]
   * or [occupation] must be non-null. Returns the persisted full_name on success.
   *
   * PATCH /api/auth/profile
   */
  suspend fun updateProfile(fullName: String?, occupation: String?): Result<Unit>

  /**
   * Upload an avatar image for the current user. The server stores the file on
   * the cloud-server filesystem and returns a public URL string, which is
   * persisted on the user record.
   *
   * POST /api/auth/profile/avatar (multipart, field name "file")
   * @return the avatar_url string from the server.
   */
  suspend fun uploadAvatar(image: io.github.fletchmckee.liquid.samples.app.platform.PickedImage): Result<String>
}
