package io.github.fletchmckee.liquid.samples.app.platform

import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-specific audio capture and streaming service for Fixed Sessions.
 * Captures real-time audio and streams PCM chunks to the backend.
 *
 * Unlike VoiceRecorderService (file-based recording for AskKlik chat),
 * this uses real-time audio taps (AVAudioEngine on iOS) for continuous streaming.
 */
expect object FixedSessionAudioStreamer {
    /**
     * Whether streaming is currently active.
     */
    val isStreaming: StateFlow<Boolean>

    /**
     * Start capturing and streaming audio for a fixed session.
     * Audio is captured in real-time and uploaded in chunks to the backend.
     *
     * @param userId User identifier for the backend API header
     * @return true if streaming started successfully
     */
    suspend fun startStreaming(userId: String): Boolean

    /**
     * Stop capturing and streaming audio.
     * Flushes any remaining buffered audio to the backend before stopping.
     */
    suspend fun stopStreaming()
}
