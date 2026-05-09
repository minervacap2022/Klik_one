// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

/**
 * Google Sign-In credential returned after successful iOS authentication.
 *
 * @property idToken JWT from Google, audience=WEB_CLIENT_ID (usable by the backend unchanged)
 * @property email User's Google email address
 * @property displayName User's display name from Google profile
 */
data class GoogleSignInCredential(
  val idToken: String,
  val email: String?,
  val displayName: String?,
)

/**
 * Result of a Google Sign-In operation.
 */
sealed class GoogleSignInResult {
  data class Success(val credential: GoogleSignInCredential) : GoogleSignInResult()
  data class Error(val message: String) : GoogleSignInResult()
  data object Cancelled : GoogleSignInResult()
  data object NotSupported : GoogleSignInResult()
}

/**
 * Provider interface implemented in Swift (iosApp) and injected at app startup.
 *
 * Swift implements this as an ObjC protocol. The Kotlin iosMain actual calls through
 * it so no Google SDK dependency leaks into the KMP module — the SDK lives purely
 * in the Swift layer.
 */
interface GoogleSignInHandler {
  fun onSuccess(idToken: String, email: String?, displayName: String?)
  fun onCancelled()
  fun onFailure(message: String)
}

interface GoogleSignInProvider {
  fun startSignIn(handler: GoogleSignInHandler)
}

/**
 * Platform-specific Google Sign-In service.
 *
 * - iOS: delegates to a [GoogleSignInProvider] registered at app startup by Swift code
 * - All other platforms: returns [GoogleSignInResult.NotSupported]
 *
 * Usage:
 * ```kotlin
 * if (GoogleSignInService.isSupported()) {
 *     GoogleSignInService.signIn { result ->
 *         when (result) {
 *             is GoogleSignInResult.Success   -> { /* use result.credential.idToken */ }
 *             is GoogleSignInResult.Cancelled -> { /* no-op */ }
 *             is GoogleSignInResult.Error     -> { /* show error */ }
 *             is GoogleSignInResult.NotSupported -> { /* should not happen */ }
 *         }
 *     }
 * }
 * ```
 */
expect object GoogleSignInService {
  fun setProvider(provider: GoogleSignInProvider?)
  fun isSupported(): Boolean
  fun signIn(onResult: (GoogleSignInResult) -> Unit)
}
