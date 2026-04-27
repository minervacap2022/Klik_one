// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Web implementation of AppLifecycleObserver.
 * Could use Page Visibility API in the future.
 */
actual object AppLifecycleObserver {
  private val _foregroundEvents = MutableSharedFlow<Boolean>(replay = 0, extraBufferCapacity = 1)
  actual val foregroundEvents: SharedFlow<Boolean> = _foregroundEvents.asSharedFlow()

  actual fun startObserving() {
    // TODO: Could use document.visibilityState and visibilitychange event
  }

  actual fun stopObserving() {
    // No-op
  }
}
