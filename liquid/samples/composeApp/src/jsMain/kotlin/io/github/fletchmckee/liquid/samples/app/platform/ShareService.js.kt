package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.browser.window

/**
 * JS/Browser implementation of ShareService using Web Share API.
 */
actual object ShareService {

    actual fun share(text: String, subject: String?) {
        // Use Web Share API if available
        try {
            val shareData = js("{}")
            shareData.text = text
            subject?.let { shareData.title = it }

            val navigator = window.navigator.asDynamic()
            if (navigator.share != undefined) {
                navigator.share(shareData)
            } else {
                // Fallback: copy to clipboard
                window.navigator.clipboard.writeText(text)
                KlikLogger.i("ShareService", "Copied to clipboard (Web Share API not available)")
            }
        } catch (e: Exception) {
            KlikLogger.e("ShareService", "Share failed: ${e.message}", e)
        }
    }
}
