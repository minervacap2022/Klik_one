package io.github.fletchmckee.liquid.samples.app.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * JS implementation of FixedSessionAudioStreamer.
 * Audio streaming not supported on JS platform.
 */
actual object FixedSessionAudioStreamer {
    private val _isStreaming = MutableStateFlow(false)
    actual val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    actual suspend fun startStreaming(userId: String): Boolean = false

    actual suspend fun stopStreaming() {}
}
