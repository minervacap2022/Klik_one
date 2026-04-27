// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * WASM JS implementation of FixedSessionAudioStreamer.
 * Audio streaming not supported on WASM JS platform.
 */
actual object FixedSessionAudioStreamer {
  private val _isStreaming = MutableStateFlow(false)
  actual val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

  actual suspend fun startStreaming(userId: String): Boolean = false

  actual suspend fun stopStreaming() { /* no-op: audio streaming unsupported on WASM JS */ }
}
