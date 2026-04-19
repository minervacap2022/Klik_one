@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.cinterop.ObjCAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.darwin.NSObject

/**
 * iOS implementation of AppLifecycleObserver using NSNotificationCenter.
 * Observes UIApplicationDidBecomeActiveNotification and UIApplicationDidEnterBackgroundNotification.
 */
actual object AppLifecycleObserver {
    private const val TAG = "AppLifecycleObserver"

    private val _foregroundEvents = MutableSharedFlow<Boolean>(replay = 0, extraBufferCapacity = 1)
    actual val foregroundEvents: SharedFlow<Boolean> = _foregroundEvents.asSharedFlow()

    private var observer: LifecycleNotificationObserver? = null
    private var isObserving = false

    actual fun startObserving() {
        if (isObserving) {
            KlikLogger.d(TAG, "Already observing lifecycle events")
            return
        }

        observer = LifecycleNotificationObserver { isForeground ->
            val emitted = _foregroundEvents.tryEmit(isForeground)
            KlikLogger.i(TAG, "Lifecycle event: isForeground=$isForeground, emitted=$emitted")
        }

        val notificationCenter = NSNotificationCenter.defaultCenter

        // Observe app becoming active (foreground)
        notificationCenter.addObserver(
            observer = observer!!,
            selector = platform.objc.sel_registerName("onDidBecomeActive"),
            name = UIApplicationDidBecomeActiveNotification,
            `object` = null
        )

        // Observe app entering background
        notificationCenter.addObserver(
            observer = observer!!,
            selector = platform.objc.sel_registerName("onDidEnterBackground"),
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null
        )

        isObserving = true
        KlikLogger.i(TAG, "Started observing app lifecycle events")

        // Also start observing push notification events (tap handling + late token registration)
        PushNotificationHandler.startObserving()
    }

    actual fun stopObserving() {
        if (!isObserving || observer == null) {
            return
        }

        NSNotificationCenter.defaultCenter.removeObserver(observer!!)
        observer = null
        isObserving = false
        KlikLogger.i(TAG, "Stopped observing app lifecycle events")

        // Also stop push notification observer
        PushNotificationHandler.stopObserving()
    }
}

/**
 * NSObject subclass to receive NSNotificationCenter callbacks.
 */
private class LifecycleNotificationObserver(
    private val onLifecycleChange: (Boolean) -> Unit
) : NSObject() {

    @ObjCAction
    fun onDidBecomeActive() {
        onLifecycleChange(true)
    }

    @ObjCAction
    fun onDidEnterBackground() {
        onLifecycleChange(false)
    }
}
