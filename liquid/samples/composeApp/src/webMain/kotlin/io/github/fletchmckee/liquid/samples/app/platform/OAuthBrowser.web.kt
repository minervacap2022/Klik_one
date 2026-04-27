// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

/**
 * Web stub implementation of OAuthBrowser.
 */
actual object OAuthBrowser {

  actual fun openUrl(url: String): Boolean {
    // Web browser opening not implemented
    KlikLogger.i("OAuthBrowser", "Web: Would open URL: $url")
    return false
  }

  actual suspend fun openOAuthSession(url: String, callbackScheme: String): OAuthSessionResult {
    return OAuthSessionResult.Error("Web target uses the browser flow; no in-app session.")
  }
}
