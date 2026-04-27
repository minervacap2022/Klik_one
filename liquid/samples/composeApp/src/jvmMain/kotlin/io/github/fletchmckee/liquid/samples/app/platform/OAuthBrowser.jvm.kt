// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import java.awt.Desktop
import java.net.URI

/**
 * JVM/Desktop implementation of OAuthBrowser using java.awt.Desktop.
 * Opens the OAuth authorization URL in the default system browser.
 */
actual object OAuthBrowser {

  /**
   * Open a URL in the default browser for OAuth authorization.
   *
   * @param url The OAuth authorization URL to open
   * @return true if the URL was opened successfully, false otherwise
   */
  actual fun openUrl(url: String): Boolean = try {
    if (Desktop.isDesktopSupported()) {
      Desktop.getDesktop().browse(URI(url))
      KlikLogger.i("OAuthBrowser", "Opened URL: $url")
      true
    } else {
      KlikLogger.w("OAuthBrowser", "Desktop not supported")
      false
    }
  } catch (e: Exception) {
    KlikLogger.e("OAuthBrowser", "Error opening URL: ${e.message}", e)
    false
  }

  /**
   * Desktop has no in-app OAuth session API. Open in the system browser
   * and return Error — desktop callers should use the web flow (no
   * `callback_scheme` on /authorize, redirect lands on hiklik.ai).
   */
  actual suspend fun openOAuthSession(url: String, callbackScheme: String): OAuthSessionResult {
    openUrl(url)
    return OAuthSessionResult.Error(
      "Desktop OAuth runs through the web flow; no in-app session API.",
    )
  }
}
