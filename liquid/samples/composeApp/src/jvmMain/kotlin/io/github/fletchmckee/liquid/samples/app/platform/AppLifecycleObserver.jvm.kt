// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * JVM/Desktop implementation of AppLifecycleObserver.
 * Desktop apps don't have the same foreground/background concept.
 */
actual object AppLifecycleObserver {
  private val _foregroundEvents = MutableSharedFlow<Boolean>(replay = 0, extraBufferCapacity = 1)
  actual val foregroundEvents: SharedFlow<Boolean> = _foregroundEvents.asSharedFlow()

  actual fun startObserving() {
    // Desktop apps are always "foreground" when running
  }

  actual fun stopObserving() {
    // No-op
  }
}
