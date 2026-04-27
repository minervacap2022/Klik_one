// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import kotlinx.coroutines.flow.SharedFlow

/**
 * Platform-agnostic app lifecycle observer.
 * Emits events when the app enters foreground or background.
 */
expect object AppLifecycleObserver {
  /**
   * Flow that emits true when app enters foreground, false when enters background.
   * Collectors should use this to trigger data refresh on foreground.
   */
  val foregroundEvents: SharedFlow<Boolean>

  /**
   * Start observing lifecycle events.
   * Call this once during app initialization.
   */
  fun startObserving()

  /**
   * Stop observing lifecycle events.
   * Call this during app teardown if needed.
   */
  fun stopObserving()
}
