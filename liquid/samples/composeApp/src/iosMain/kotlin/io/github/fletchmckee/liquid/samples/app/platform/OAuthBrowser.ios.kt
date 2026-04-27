// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.AuthenticationServices.ASWebAuthenticationSessionErrorDomain
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject

/**
 * iOS OAuthBrowser using ASWebAuthenticationSession for OAuth and
 * UIApplication.open for plain links.
 *
 * Why ASWebAuthenticationSession over external Safari + Universal Links:
 *   - User stays in the app (no app-switch animation, no "swipe back")
 *   - Shares Safari's cookies — if the user is signed into Microsoft in
 *     Safari, they're signed in here too, one-tap authorize
 *   - Captures the `klik://oauth-callback?...` redirect natively — no
 *     AppDelegate URL routing or Universal Link AASA file required
 *   - Cancellation handled deterministically (no need to detect "user
 *     swiped back without completing")
 *
 * Apple's recommended OAuth API; available iOS 12+.
 */
actual object OAuthBrowser {

    actual fun openUrl(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: run {
            KlikLogger.e("OAuthBrowser", "Invalid URL: $url")
            return false
        }
        return try {
            if (UIApplication.sharedApplication.canOpenURL(nsUrl)) {
                UIApplication.sharedApplication.openURL(nsUrl, emptyMap<Any?, Any?>(), null)
                KlikLogger.i("OAuthBrowser", "Opened URL: $url")
                true
            } else {
                KlikLogger.w("OAuthBrowser", "Cannot open URL: $url")
                false
            }
        } catch (e: Exception) {
            KlikLogger.e("OAuthBrowser", "Error opening URL: ${e.message}", e)
            false
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun openOAuthSession(
        url: String,
        callbackScheme: String,
    ): OAuthSessionResult = suspendCancellableCoroutine { cont ->
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            KlikLogger.e("OAuthBrowser", "openOAuthSession: invalid URL: $url")
            cont.resume(OAuthSessionResult.Error("Invalid OAuth URL"))
            return@suspendCancellableCoroutine
        }

        // The session needs a UIWindow to present the SFAuthenticationViewController on.
        // Hold a strong reference to the provider — ASWebAuthenticationSession only
        // weakly retains it and the system crashes if it's GC'd mid-flow.
        val presenter = OAuthPresenter()

        val session = ASWebAuthenticationSession(
            uRL = nsUrl,
            callbackURLScheme = callbackScheme,
            completionHandler = { callback: NSURL?, error: NSError? ->
                when {
                    callback != null -> {
                        val captured = callback.absoluteString ?: ""
                        KlikLogger.i("OAuthBrowser", "OAuth session completed: $captured")
                        cont.resume(OAuthSessionResult.Completed(captured))
                    }
                    isCanceledLogin(error) -> {
                        KlikLogger.i("OAuthBrowser", "OAuth session cancelled by user")
                        cont.resume(OAuthSessionResult.Cancelled)
                    }
                    else -> {
                        val msg = error?.localizedDescription ?: "Unknown OAuth error"
                        KlikLogger.e("OAuthBrowser", "OAuth session failed: $msg")
                        cont.resume(OAuthSessionResult.Error(msg))
                    }
                }
            },
        )
        session.presentationContextProvider = presenter
        // Honour the system Safari session — uses the user's existing cookies
        // for one-tap re-authorization with providers they're already signed
        // into. Set to true if you instead want every flow to start fresh.
        session.prefersEphemeralWebBrowserSession = false

        cont.invokeOnCancellation {
            session.cancel()
        }

        if (!session.start()) {
            KlikLogger.e("OAuthBrowser", "ASWebAuthenticationSession.start() returned false")
            cont.resume(OAuthSessionResult.Error("Failed to start OAuth session"))
        }
    }

    private fun isCanceledLogin(error: NSError?): Boolean {
        if (error == null) return false
        // ASWebAuthenticationSessionErrorCodeCanceledLogin = 1
        // (Apple's docs:
        // https://developer.apple.com/documentation/authenticationservices/aswebauthenticationsessionerror)
        // The constant isn't surfaced by Kotlin/Native cinterop, so we use the
        // documented integer value directly. Domain check guards against
        // collisions with other error codes that happen to be 1.
        return error.domain == ASWebAuthenticationSessionErrorDomain &&
            error.code == 1L
    }
}

/**
 * Presentation anchor for ASWebAuthenticationSession. iOS asks the session
 * which window it should present its sheet on top of; we pick the first
 * key window of the active scene.
 */
private class OAuthPresenter :
    NSObject(),
    ASWebAuthenticationPresentationContextProvidingProtocol {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun presentationAnchorForWebAuthenticationSession(
        session: ASWebAuthenticationSession,
    ): UIWindow {
        // Walk windows to find the key one. UIApplication.keyWindow is
        // deprecated since iOS 13 in favour of per-scene windows.
        val windows = UIApplication.sharedApplication.windows
        for (i in 0 until windows.size) {
            val window = windows[i] as? UIWindow ?: continue
            if (window.isKeyWindow()) return window
        }
        // No key window — pick the first available UIWindow. This is the
        // expected path on multi-scene iPad and immediately after launch
        // before the system has marked a key window. ASWebAuthenticationSession
        // requires a non-null anchor; throwing here would surface as a hard
        // crash mid-OAuth. Failing-loud at the call site is impossible —
        // ASWebAuthenticationSessionPresentationContextProviding has no error
        // channel, only a UIWindow return.
        return (windows.firstOrNull() as? UIWindow)
            ?: error("No UIWindow available to present OAuth session")
    }
}
