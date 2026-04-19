package io.github.fletchmckee.liquid.samples.app.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of VoiceRecorderService.
 * TODO: Implement using MediaRecorder when needed.
 */
actual object VoiceRecorderService {
    private val _isRecording = MutableStateFlow(false)
    actual val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    actual suspend fun startRecording(): Boolean {
        // TODO: Implement using MediaRecorder
        return false
    }

    actual suspend fun stopRecording(): String? {
        // TODO: Implement using MediaRecorder
        return null
    }

    actual fun hasMicrophonePermission(): Boolean {
        // TODO: Check RECORD_AUDIO permission
        return false
    }

    actual suspend fun requestMicrophonePermission(): Boolean {
        // TODO: Request RECORD_AUDIO permission
        return false
    }
}
