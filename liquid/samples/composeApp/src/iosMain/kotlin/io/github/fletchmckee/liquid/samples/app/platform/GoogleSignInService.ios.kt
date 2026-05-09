// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

/**
 * iOS implementation of [GoogleSignInService].
 *
 * Delegates to a [GoogleSignInProvider] that is registered by Swift code in iOSApp.swift
 * at app startup. The provider wraps the native Google Sign-In SDK (GIDSignIn), keeping
 * the SDK dependency entirely in the Swift/iosApp layer — no Kotlin-side Google dependency.
 *
 * Mobile-vs-web difference:
 *   Web: GIS JS library runs in browser, returns credential directly via callback.
 *   iOS: Native SDK shows an account-picker sheet, returns GIDGoogleUser with idToken.
 *        The SDK is configured with serverClientID=WEB_CLIENT_ID so the idToken's `aud`
 *        claim equals the web client_id — the backend verifies it identically to web tokens.
 */
actual object GoogleSignInService {

  private var provider: GoogleSignInProvider? = null

  actual fun setProvider(provider: GoogleSignInProvider?) {
    this.provider = provider
    KlikLogger.i("GoogleSignInService", "Provider ${if (provider != null) "registered" else "cleared"}")
  }

  actual fun isSupported(): Boolean = provider != null

  actual fun signIn(onResult: (GoogleSignInResult) -> Unit) {
    val p = provider
    if (p == null) {
      KlikLogger.e("GoogleSignInService", "signIn called but no provider registered")
      onResult(GoogleSignInResult.NotSupported)
      return
    }

    p.startSignIn(object : GoogleSignInHandler {
      override fun onSuccess(idToken: String, email: String?, displayName: String?) {
        KlikLogger.i("GoogleSignInService", "Sign-in succeeded: email=$email")
        onResult(GoogleSignInResult.Success(GoogleSignInCredential(idToken, email, displayName)))
      }

      override fun onCancelled() {
        KlikLogger.i("GoogleSignInService", "User cancelled Google Sign-In")
        onResult(GoogleSignInResult.Cancelled)
      }

      override fun onFailure(message: String) {
        KlikLogger.e("GoogleSignInService", "Google Sign-In failed: $message")
        onResult(GoogleSignInResult.Error(message))
      }
    })
  }
}
