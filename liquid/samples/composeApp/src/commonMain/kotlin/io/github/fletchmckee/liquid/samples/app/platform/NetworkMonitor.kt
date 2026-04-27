// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

/**
 * Platform-specific network connectivity monitor.
 * Provides real-time network state for displaying offline indicators.
 *
 * On iOS, uses NWPathMonitor.
 * On Android, uses ConnectivityManager.
 */
expect object NetworkMonitor {
  /**
   * Check if the device currently has network connectivity.
   */
  fun isConnected(): Boolean

  /**
   * Start monitoring network changes.
   * Call this once at app startup.
   */
  fun startMonitoring()

  /**
   * Stop monitoring network changes.
   */
  fun stopMonitoring()
}
