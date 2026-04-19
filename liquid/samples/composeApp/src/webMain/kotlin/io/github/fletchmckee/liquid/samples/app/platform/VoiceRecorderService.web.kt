package io.github.fletchmckee.liquid.samples.app.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Web implementation of VoiceRecorderService.
 * Voice recording not supported on web.
 */
actual object VoiceRecorderService {
    private val _isRecording = MutableStateFlow(false)
    actual val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    actual suspend fun startRecording(): Boolean = false

    actual suspend fun stopRecording(): String? = null

    actual fun hasMicrophonePermission(): Boolean = false

    actual suspend fun requestMicrophonePermission(): Boolean = false
}
