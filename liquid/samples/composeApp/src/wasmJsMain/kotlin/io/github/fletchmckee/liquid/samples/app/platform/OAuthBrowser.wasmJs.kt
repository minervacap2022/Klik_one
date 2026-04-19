package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

/**
 * WASM JS stub implementation of OAuthBrowser.
 */
actual object OAuthBrowser {

    actual fun openUrl(url: String): Boolean {
        // WASM browser opening not implemented
        KlikLogger.i("OAuthBrowser", "WASM: Would open URL: $url")
        return false
    }
}
