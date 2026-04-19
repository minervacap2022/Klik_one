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
    actual fun openUrl(url: String): Boolean {
        return try {
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
    }
}
