// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-specific voice recording service.
 * Records audio and provides it as base64 encoded data for ASR processing.
 */
expect object VoiceRecorderService {
  /**
   * Whether recording is currently active.
   */
  val isRecording: StateFlow<Boolean>

  /**
   * Start recording audio.
   * @return true if recording started successfully, false otherwise
   */
  suspend fun startRecording(): Boolean

  /**
   * Stop recording and get the recorded audio as base64.
   * @return Base64 encoded audio data, or null if recording failed
   */
  suspend fun stopRecording(): String?

  /**
   * Check if the device has microphone permission.
   * @return true if permission is granted
   */
  fun hasMicrophonePermission(): Boolean

  /**
   * Request microphone permission.
   * @return true if permission was granted
   */
  suspend fun requestMicrophonePermission(): Boolean
}
