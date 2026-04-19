package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.browser.window

/**
 * JS/Browser implementation of OAuthBrowser using window.open.
 */
actual object OAuthBrowser {

    actual fun openUrl(url: String): Boolean {
        return try {
            window.open(url, "_blank")
            KlikLogger.i("OAuthBrowser", "Opened URL: $url")
            true
        } catch (e: Exception) {
            KlikLogger.e("OAuthBrowser", "Error opening URL: ${e.message}", e)
            false
        }
    }
}
