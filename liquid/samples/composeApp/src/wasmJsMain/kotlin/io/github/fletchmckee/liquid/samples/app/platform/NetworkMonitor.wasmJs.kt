// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

actual object NetworkMonitor {
  actual fun isConnected(): Boolean = true
  actual fun startMonitoring() { /* no-op: WASM JS target has no system network monitor */ }
  actual fun stopMonitoring() { /* no-op: WASM JS target has no system network monitor */ }
}
