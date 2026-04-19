package io.github.fletchmckee.liquid.samples.app.platform

/**
 * Platform-specific browser handler for OAuth flows.
 * Opens URLs in the system browser for OAuth authorization.
 *
 * On iOS, this uses UIApplication.shared.open() to open Safari.
 * On Android, this would use Intent.ACTION_VIEW.
 *
 * After OAuth completes, the callback URL (Universal Link) will return the user to the app.
 */
expect object OAuthBrowser {
    /**
     * Open a URL in the system browser for OAuth authorization.
     *
     * @param url The OAuth authorization URL to open
     * @return true if the URL was opened successfully, false otherwise
     */
    fun openUrl(url: String): Boolean
}
