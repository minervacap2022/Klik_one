// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

/**
 * Result of an in-app OAuth session.
 *
 * The session is "in-app" because we use ASWebAuthenticationSession on iOS
 * and Chrome Custom Tabs on Android — the user authenticates inside a system
 * web view that shares Safari/Chrome cookies but never leaves Klik. When the
 * upstream provider redirects to our `klik://oauth-callback?...` URL, the
 * system intercepts it and hands the URL back to us via this result.
 */
sealed class OAuthSessionResult {
    /** Provider redirected to `klik://oauth-callback?success=true&provider=X` (or `?error=...`). */
    data class Completed(val callbackUrl: String) : OAuthSessionResult()

    /** User dismissed the in-app web view (e.g. tapped "Cancel"). Not an error. */
    object Cancelled : OAuthSessionResult()

    /** Anything else — invalid URL, presentation failure, no scheme registered, etc. */
    data class Error(val message: String) : OAuthSessionResult()
}

/**
 * Platform-specific browser handler.
 *
 * Two flavours:
 *   - `openUrl(url)` — fire-and-forget external browser open. Used for non-OAuth
 *     links: privacy/terms pages, GitHub issues, dropbox file URLs, etc.
 *   - `openOAuthSession(url, callbackScheme)` — purpose-built OAuth flow.
 *     Opens the URL inside the app via ASWebAuthenticationSession (iOS) or
 *     Chrome Custom Tabs (Android), waits for the upstream redirect to a URL
 *     with `callbackScheme` (e.g. `klik://oauth-callback?...`), and returns
 *     the captured URL synchronously. The user never leaves the app.
 *
 * Web fallback: when this method is called from web/JS targets, it falls back
 * to `openUrl()` — the user authenticates in the same browser tab and the
 * backend's web redirect (`https://hiklik.ai/integrations?...`) handles the
 * rest. The caller should NOT pass a custom callbackScheme on web.
 */
expect object OAuthBrowser {
    /** Open a URL in the system browser. Used for non-OAuth links. */
    fun openUrl(url: String): Boolean

    /**
     * Run an OAuth flow inside the app, returning the captured callback URL.
     *
     * Suspends until the provider redirects to a URL whose scheme matches
     * [callbackScheme], or until the user cancels.
     *
     * @param url The OAuth authorization URL to load.
     * @param callbackScheme The URL scheme the system should intercept
     *   (e.g. `"klik"` for `klik://oauth-callback?...`). Must match the
     *   scheme registered with the app bundle.
     */
    suspend fun openOAuthSession(url: String, callbackScheme: String): OAuthSessionResult
}
