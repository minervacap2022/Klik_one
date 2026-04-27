// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

/**
 * Apple Sign In credential returned after successful authentication.
 *
 * @property identityToken JWT from Apple used for backend verification
 * @property authorizationCode One-time code for backend to exchange for refresh token
 * @property userId Apple's unique user identifier (stable across app reinstalls)
 * @property email User's email (may be relay email like user@privaterelay.appleid.com)
 * @property fullName User's full name (only provided on first sign-in)
 * @property isRealEmail true if user shared real email, false if using relay
 */
data class AppleSignInCredential(
  val identityToken: String,
  val authorizationCode: String,
  val userId: String,
  val email: String?,
  val fullName: String?,
  val isRealEmail: Boolean,
)

/**
 * Result of Apple Sign In operation.
 */
sealed class AppleSignInResult {
  /** Sign-in succeeded with credential */
  data class Success(val credential: AppleSignInCredential) : AppleSignInResult()

  /** Sign-in failed with error message */
  data class Error(val message: String) : AppleSignInResult()

  /** User cancelled the sign-in flow */
  data object Cancelled : AppleSignInResult()

  /** Platform does not support Sign In with Apple */
  data object NotSupported : AppleSignInResult()
}

/**
 * Platform-specific service for Sign In with Apple.
 *
 * - iOS: Uses AuthenticationServices framework (ASAuthorizationController)
 * - Other platforms: Returns NotSupported
 *
 * Usage:
 * ```kotlin
 * if (AppleSignInService.isSupported()) {
 *     AppleSignInService.signIn { result ->
 *         when (result) {
 *             is AppleSignInResult.Success -> { /* Handle credential */ }
 *             is AppleSignInResult.Cancelled -> { /* User cancelled */ }
 *             is AppleSignInResult.Error -> { /* Show error */ }
 *             is AppleSignInResult.NotSupported -> { /* Should not happen */ }
 *         }
 *     }
 * }
 * ```
 */
expect object AppleSignInService {
  /**
   * Check if Sign In with Apple is supported on this platform.
   * @return true on iOS 13+, false on all other platforms
   */
  fun isSupported(): Boolean

  /**
   * Initiate Sign In with Apple flow.
   * Shows native Apple sign-in UI on iOS.
   * @param onResult Callback with sign-in result
   */
  fun signIn(onResult: (AppleSignInResult) -> Unit)
}
