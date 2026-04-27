// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * JS KMP `actual` stub. Audio streaming is not implemented for the JS target.
 * Lifecycle methods drive the state flows so observers see coherent stopped /
 * non-paused state rather than stale defaults.
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
