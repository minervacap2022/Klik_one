package io.github.fletchmckee.liquid.samples.app.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of FixedSessionAudioStreamer.
 * TODO: Implement using AudioRecord when needed.
 */
actual object FixedSessionAudioStreamer {
    private val _isStreaming = MutableStateFlow(false)
    actual val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    actual suspend fun startStreaming(userId: String): Boolean {
        // TODO: Implement using AudioRecord
        return false
    }

    actual suspend fun stopStreaming() {}
}
