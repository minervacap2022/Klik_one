// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

/**
 * Web stub implementation of AppleSignInService.
 * Sign In with Apple on web requires JavaScript SDK integration.
 * For now, returns NotSupported.
 */
actual object AppleSignInService {

  /**
   * Web does not currently support Sign In with Apple.
   * TODO: Integrate Apple JS SDK for web support if needed.
   */
  actual fun isSupported(): Boolean = false

  /**
   * Returns NotSupported on Web.
   */
  actual fun signIn(onResult: (AppleSignInResult) -> Unit) {
    onResult(AppleSignInResult.NotSupported)
  }
}
