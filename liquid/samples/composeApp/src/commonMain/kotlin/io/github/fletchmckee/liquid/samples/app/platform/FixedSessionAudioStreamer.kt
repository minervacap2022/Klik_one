// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
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
  /** Whether streaming is currently active. */
  val isStreaming: StateFlow<Boolean>

  /** Whether streaming is paused (audio capture halted, session still open). */
  val isPaused: StateFlow<Boolean>

  /**
   * Start capturing and streaming audio for a fixed session.
   * @return true if streaming started successfully
   */
  suspend fun startStreaming(userId: String): Boolean

  /** Stop capturing and streaming audio, flushing remaining buffer. */
  suspend fun stopStreaming()

  /** Pause audio capture without ending the session. */
  fun pauseStreaming()

  /** Resume audio capture after a pause. */
  fun resumeStreaming()
}
