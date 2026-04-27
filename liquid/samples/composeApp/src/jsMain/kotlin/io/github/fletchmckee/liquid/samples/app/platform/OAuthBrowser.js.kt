// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.browser.window

/**
 * JS/Browser implementation of OAuthBrowser using window.open.
 */
actual object OAuthBrowser {

  actual fun openUrl(url: String): Boolean = try {
    window.open(url, "_blank")
    KlikLogger.i("OAuthBrowser", "Opened URL: $url")
    true
  } catch (e: Exception) {
    KlikLogger.e("OAuthBrowser", "Error opening URL: ${e.message}", e)
    false
  }

  actual suspend fun openOAuthSession(url: String, callbackScheme: String): OAuthSessionResult {
    // Browser/JS uses the redirect-based web flow handled by the backend.
    openUrl(url)
    return OAuthSessionResult.Error("Browser target uses the redirect flow; no in-app session.")
  }
}
