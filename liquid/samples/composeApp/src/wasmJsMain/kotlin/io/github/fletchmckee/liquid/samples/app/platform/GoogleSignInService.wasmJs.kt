// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

actual object GoogleSignInService {
  actual fun setProvider(provider: GoogleSignInProvider?) = Unit
  actual fun isSupported(): Boolean = false
  actual fun signIn(onResult: (GoogleSignInResult) -> Unit) {
    onResult(GoogleSignInResult.NotSupported)
  }
}
