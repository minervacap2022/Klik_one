// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

/**
 * URL scheme registered in iOS Info.plist (`CFBundleURLSchemes`) and the
 * Android intent-filter — the system uses it to intercept the post-OAuth
 * redirect and route the URL back to the in-app session. Single source of
 * truth so the value can't drift between call sites.
 */
const val OAUTH_CALLBACK_SCHEME: String = "klik"

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
    /**
     * Provider redirected to `klik://oauth-callback?success=true&provider=X`
     * (or `?error=...`). [params] parses the query string once; the typed
     * accessors hide the wire format from callers so they don't have to
     * substring-match `success=true` or worry about URL-encoding.
     */
    data class Completed(val callbackUrl: String) : OAuthSessionResult() {
        val params: Map<String, String> by lazy { parseQueryParams(callbackUrl) }

        /** True when the backend reported the OAuth flow completed cleanly. */
        val isSuccess: Boolean get() = params["success"] == "true"

        /** Provider id (e.g. "microsoft") or null if the redirect was malformed. */
        val provider: String? get() = params["provider"]

        /** Backend / provider error code, if any (e.g. "access_denied"). */
        val errorCode: String? get() = params["error"]
    }

    /** User dismissed the in-app web view (e.g. tapped "Cancel"). Not an error. */
    object Cancelled : OAuthSessionResult()

    /** Anything else — invalid URL, presentation failure, no scheme registered, etc. */
    data class Error(val message: String) : OAuthSessionResult()
}

/**
 * Minimal URL query-param parser. Sufficient for OAuth callback URLs
 * (which the backend builds and we control); not a general-purpose
 * URL parser. Returns {} for malformed input rather than throwing — a
 * malformed callback is already caught by the absent `success=true`,
 * no need to add a second failure mode.
 */
internal fun parseQueryParams(url: String): Map<String, String> {
    val q = url.substringAfter('?', "").substringBefore('#')
    if (q.isEmpty()) return emptyMap()
    val out = mutableMapOf<String, String>()
    for (pair in q.split('&')) {
        if (pair.isEmpty()) continue
        val eq = pair.indexOf('=')
        if (eq <= 0) continue
        val key = pair.substring(0, eq)
        val raw = pair.substring(eq + 1)
        out[key] = percentDecode(raw)
    }
    return out
}

private fun percentDecode(s: String): String {
    if ('%' !in s && '+' !in s) return s
    val sb = StringBuilder(s.length)
    var i = 0
    while (i < s.length) {
        val c = s[i]
        when {
            c == '+' -> {
                sb.append(' ')
                i++
            }
            c == '%' && i + 2 < s.length -> {
                val hex = s.substring(i + 1, i + 3)
                val code = hex.toIntOrNull(16)
                if (code != null) {
                    sb.append(code.toChar())
                    i += 3
                } else {
                    sb.append(c)
                    i++
                }
            }
            else -> {
                sb.append(c)
                i++
            }
        }
    }
    return sb.toString()
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
