package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import platform.Foundation.NSDictionary
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * iOS implementation of OAuthBrowser using UIApplication.shared.open.
 * Opens the OAuth authorization URL in Safari.
 *
 * After OAuth completes, the user will be redirected back to the app
 * via Universal Links (https://hiklik.ai/integrations?...).
 */
actual object OAuthBrowser {

    /**
     * Open a URL in Safari for OAuth authorization.
     * Uses the modern open(_:options:completionHandler:) API.
     *
     * @param url The OAuth authorization URL to open
     * @return true if the URL was opened successfully, false otherwise
     */
    actual fun openUrl(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            KlikLogger.e("OAuthBrowser", "Invalid URL: $url")
            return false
        }

        return try {
            val canOpen = UIApplication.sharedApplication.canOpenURL(nsUrl)
            if (canOpen) {
                // Use non-deprecated open(_:options:completionHandler:) API
                UIApplication.sharedApplication.openURL(
                    nsUrl,
                    emptyMap<Any?, Any?>(),
                    null
                )
                KlikLogger.i("OAuthBrowser", "Opened URL: $url")
                true
            } else {
                KlikLogger.w("OAuthBrowser", "Cannot open URL: $url")
                false
            }
        } catch (e: Exception) {
            KlikLogger.e("OAuthBrowser", "Error opening URL: ${e.message}", e)
            false
        }
    }
}
