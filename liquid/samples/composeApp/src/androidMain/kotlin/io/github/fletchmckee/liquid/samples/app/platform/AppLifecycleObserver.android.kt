// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Android implementation of AppLifecycleObserver using ProcessLifecycleOwner.
 * Observes onStart (foreground) and onStop (background) lifecycle events.
 */
actual object AppLifecycleObserver {
  private const val TAG = "AppLifecycleObserver"

  private val _foregroundEvents = MutableSharedFlow<Boolean>(replay = 0, extraBufferCapacity = 1)
  actual val foregroundEvents: SharedFlow<Boolean> = _foregroundEvents.asSharedFlow()

  private var isObserving = false

  private val lifecycleObserver = object : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
      val emitted = _foregroundEvents.tryEmit(true)
      KlikLogger.i(TAG, "App entered foreground, emitted=$emitted")
    }

    override fun onStop(owner: LifecycleOwner) {
      val emitted = _foregroundEvents.tryEmit(false)
      KlikLogger.i(TAG, "App entered background, emitted=$emitted")
    }
  }

  actual fun startObserving() {
    if (isObserving) {
      KlikLogger.d(TAG, "Already observing lifecycle events")
      return
    }

    ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    isObserving = true
    KlikLogger.i(TAG, "Started observing app lifecycle events")
  }

  actual fun stopObserving() {
    if (!isObserving) return

    ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
    isObserving = false
    KlikLogger.i(TAG, "Stopped observing app lifecycle events")
  }
}
