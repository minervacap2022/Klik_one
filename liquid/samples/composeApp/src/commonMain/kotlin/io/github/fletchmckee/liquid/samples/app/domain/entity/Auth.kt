// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.entity

/**
 * Authentication state for the app
 */
data class AuthState(
  val isLoggedIn: Boolean = false,
  val userId: String? = null,
  val accessToken: String? = null,
  val refreshToken: String? = null,
  val userName: String? = null,
  val userEmail: String? = null,
)

/**
 * Login credentials - supports both email and username
 */
data class LoginCredentials(
  val identifier: String, // Can be email OR username
  val password: String,
) {
  /**
   * Check if the identifier looks like an email
   */
  val isEmail: Boolean
    get() = identifier.contains("@")

  /**
   * Get the identifier as email (if it is one)
   */
  val email: String?
    get() = if (isEmail) identifier else null

  /**
   * Get the identifier as username (if not an email)
   */
  val username: String?
    get() = if (!isEmail) identifier else null
}

/**
 * Signup credentials
 */
data class SignupCredentials(
  val email: String,
  val password: String,
  val name: String,
)

/**
 * Apple Sign In credentials for backend authentication.
 * Sent to POST /api/auth/apple/callback
 */
data class AppleSignInCredentials(
  /** JWT from Apple used for backend verification */
  val identityToken: String,
  /** One-time code for backend to exchange for refresh token */
  val authorizationCode: String,
  /** Apple's unique user identifier (stable across app reinstalls) */
  val userId: String,
  /** User's email (may be relay email like user@privaterelay.appleid.com) */
  val email: String?,
  /** User's full name (only provided on first sign-in) */
  val fullName: String?,
  /** true if user shared real email, false if using relay */
  val isRealEmail: Boolean,
)

/**
 * Auth response from the backend
 */
data class AuthResponse(
  val success: Boolean,
  val userId: String? = null,
  val accessToken: String? = null,
  val refreshToken: String? = null,
  val userName: String? = null,
  val userEmail: String? = null,
  val message: String? = null,
)
