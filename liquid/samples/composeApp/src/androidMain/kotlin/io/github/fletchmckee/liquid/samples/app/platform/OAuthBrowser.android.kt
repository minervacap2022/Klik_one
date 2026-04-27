// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import android.content.Intent
import android.net.Uri
import io.github.fletchmckee.liquid.samples.app.data.storage.ApplicationContextProvider
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android OAuthBrowser.
 *
 * Plain `openUrl` uses Intent.ACTION_VIEW (system browser).
 *
 * `openOAuthSession` opens the auth URL in the system browser, then suspends
 * until MainActivity receives the post-OAuth `klik://oauth-callback?...`
 * deep link and calls [completePendingOAuth] to hand the captured URL back.
 * Chrome auto-closes after navigating to a `klik://` URL the system claims.
 *
 * (We could use `androidx.browser.customtabs.CustomTabsIntent` for a slightly
 * better in-app feel, but it pulls a transitive `androidx.activity` upgrade
 * that requires AGP 8.9+. Plain Intent works on every supported Android with
 * no dependency churn — and the deep-link return path is identical.)
 */
actual object OAuthBrowser {

    @Volatile private var pendingDeferred: CompletableDeferred<OAuthSessionResult>? = null
    @Volatile private var pendingScheme: String? = null

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

    actual suspend fun openOAuthSession(
        url: String,
        callbackScheme: String,
    ): OAuthSessionResult = suspendCancellableCoroutine { cont ->
        // Only one OAuth flow in flight at a time. Stale prior session →
        // mark as cancelled so any awaiter gets a definitive answer.
        pendingDeferred?.let { prior ->
            KlikLogger.w("OAuthBrowser", "Replacing in-flight OAuth session")
            prior.complete(OAuthSessionResult.Cancelled)
        }

        val deferred = CompletableDeferred<OAuthSessionResult>()
        pendingDeferred = deferred
        pendingScheme = callbackScheme

        cont.invokeOnCancellation {
            if (pendingDeferred === deferred) {
                pendingDeferred = null
                pendingScheme = null
            }
        }

        if (!openUrl(url)) {
            pendingDeferred = null
            pendingScheme = null
            cont.resume(OAuthSessionResult.Error("Failed to launch browser for OAuth"))
            return@suspendCancellableCoroutine
        }

        deferred.invokeOnCompletion { cause ->
            if (cont.isActive) {
                val result = if (cause == null) {
                    deferred.getCompleted()
                } else {
                    OAuthSessionResult.Error(cause.message ?: "OAuth session failed")
                }
                cont.resume(result)
            }
        }
    }

    /**
     * Hand a captured deep-link URL back to the suspended OAuth flow.
     *
     * Returns true if the URL matched an in-flight OAuth session and was
     * consumed; false otherwise (caller routes the URL through the regular
     * DeepLinkHandler).
     *
     * Wire from MainActivity.handleDeepLinkIntent BEFORE calling
     * DeepLinkHandler.setPendingDeepLink so OAuth callbacks short-circuit
     * the in-app deep-link router.
     */
    fun completePendingOAuth(uri: String): Boolean {
        val deferred = pendingDeferred ?: return false
        val scheme = pendingScheme ?: return false
        if (!uri.startsWith("$scheme://")) return false

        KlikLogger.i("OAuthBrowser", "Completing OAuth session with: $uri")
        pendingDeferred = null
        pendingScheme = null
        deferred.complete(OAuthSessionResult.Completed(uri))
        return true
    }
}
