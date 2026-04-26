package io.github.fletchmckee.liquid.samples.app.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android KMP `actual` stub. Klik ships iOS only — Android is a non-shipping
 * target kept compiling so the common module stays portable. `startStreaming`
 * returns `false` to signal "not supported on this platform"; the lifecycle
 * methods below still drive the state flows so any caller observing them sees
 * a coherent stopped/non-paused state instead of stale defaults.
 */
actual object FixedSessionAudioStreamer {
    private val _isStreaming = MutableStateFlow(false)
    actual val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    actual val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    actual suspend fun startStreaming(userId: String): Boolean {
        _isStreaming.value = false
        _isPaused.value = false
        return false
    }
    actual suspend fun stopStreaming() {
        _isStreaming.value = false
        _isPaused.value = false
    }
    actual fun pauseStreaming() {
        _isPaused.value = true
    }
    actual fun resumeStreaming() {
        _isPaused.value = false
    }
}
