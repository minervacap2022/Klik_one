package io.github.fletchmckee.liquid.samples.app.platform

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * WASM JS implementation of AppLifecycleObserver.
 */
actual object AppLifecycleObserver {
    private val _foregroundEvents = MutableSharedFlow<Boolean>(replay = 0, extraBufferCapacity = 1)
    actual val foregroundEvents: SharedFlow<Boolean> = _foregroundEvents.asSharedFlow()

    actual fun startObserving() {
        // No-op for WASM
    }

    actual fun stopObserving() {
        // No-op
    }
}
