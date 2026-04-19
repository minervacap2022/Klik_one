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
}
