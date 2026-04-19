package io.github.fletchmckee.liquid.samples.app.platform

import android.content.Intent
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import android.net.Uri
import io.github.fletchmckee.liquid.samples.app.data.storage.ApplicationContextProvider

/**
 * Android implementation of OAuthBrowser using Intent.ACTION_VIEW.
 * Opens the OAuth authorization URL in the default browser.
 */
actual object OAuthBrowser {

    /**
     * Open a URL in the default browser for OAuth authorization.
     *
     * @param url The OAuth authorization URL to open
     * @return true if the URL was opened successfully, false otherwise
     */
    actual fun openUrl(url: String): Boolean {
        val context = try {
            ApplicationContextProvider.context
        } catch (e: UninitializedPropertyAccessException) {
            KlikLogger.e("OAuthBrowser", "Android context not initialized")
            return false
        }

        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            KlikLogger.i("OAuthBrowser", "Opened URL: $url")
            true
        } catch (e: Exception) {
            KlikLogger.e("OAuthBrowser", "Error opening URL: ${e.message}", e)
            false
        }
    }
}
