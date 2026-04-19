@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.cinterop.ObjCAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.darwin.NSObject

/**
 * Observes iOS NSNotificationCenter events from the Swift PushNotificationService:
 *
 * 1. "PushNotificationTapped" - When user taps a push notification.
 *    Extracts `type` and `id` from userInfo, constructs a `klik://{type}/{id}` deep link,
 *    and writes it via DeepLinkHandler so MainApp's deep-link polling picks it up.
 *
 * 2. "APNsTokenReceived" - When device token arrives from APNs.
 *    Registers the token with the backend immediately (handles late-arriving tokens
 *    that weren't available when AppModule.initialize() ran).
 *
 * Call [startObserving] once during app startup (e.g., from AppLifecycleObserver or MainApp).
 * Call [stopObserving] on teardown.
 */
object PushNotificationHandler {

    private const val TAG = "PushNotificationHandler"

    private var observer: PushNotificationObserver? = null
    private var isObserving = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun startObserving() {
        if (isObserving) {
            KlikLogger.d(TAG, "Already observing push notification events")
            return
        }

        observer = PushNotificationObserver(
            onNotificationTapped = { type, id ->
                handleNotificationTap(type, id)
            },
            onTokenReceived = { token ->
                handleTokenReceived(token)
            }
        )

        val notificationCenter = NSNotificationCenter.defaultCenter

        // Observe push notification taps
        notificationCenter.addObserver(
            observer = observer!!,
            selector = platform.objc.sel_registerName("onNotificationTapped:"),
            name = "PushNotificationTapped",
            `object` = null
        )

        // Observe APNs token arrival (for late-arriving tokens)
        notificationCenter.addObserver(
            observer = observer!!,
            selector = platform.objc.sel_registerName("onTokenReceived:"),
            name = "APNsTokenReceived",
            `object` = null
        )

        isObserving = true
        KlikLogger.i(TAG, "Started observing push notification events")
    }

    fun stopObserving() {
        if (!isObserving || observer == null) {
            return
        }

        NSNotificationCenter.defaultCenter.removeObserver(observer!!)
        observer = null
        isObserving = false
        KlikLogger.i(TAG, "Stopped observing push notification events")
    }

    private fun handleNotificationTap(type: String?, id: String?) {
        if (type == null) {
            KlikLogger.w(TAG, "Notification tapped but no type in payload, ignoring")
            return
        }

        val deepLinkUrl = if (id != null) {
            "klik://$type/$id"
        } else {
            "klik://$type"
        }

        KlikLogger.i(TAG, "Notification tapped: type=$type, id=$id -> deep link: $deepLinkUrl")
        DeepLinkHandler.setPendingDeepLink(deepLinkUrl)
    }

    private fun handleTokenReceived(token: String?) {
        if (token == null) {
            KlikLogger.w(TAG, "APNs token received event but token is null")
            return
        }

        KlikLogger.i(TAG, "APNs token received, registering with backend")

        scope.launch {
            val pushService = PushNotificationService()
            val registered = pushService.registerDeviceToken(token)
            KlikLogger.i(TAG, "Late token registration result: $registered")
        }
    }
}

/**
 * NSObject subclass to receive NSNotificationCenter callbacks for push notifications.
 */
private class PushNotificationObserver(
    private val onNotificationTapped: (type: String?, id: String?) -> Unit,
    private val onTokenReceived: (token: String?) -> Unit
) : NSObject() {

    @ObjCAction
    fun onNotificationTapped(notification: NSNotification) {
        val userInfo = notification.userInfo
        val type = userInfo?.get("type") as? String
        val id = userInfo?.get("id") as? String

        onNotificationTapped(type, id)
    }

    @ObjCAction
    fun onTokenReceived(notification: NSNotification) {
        val userInfo = notification.userInfo
        val token = userInfo?.get("token") as? String

        onTokenReceived(token)
    }
}
