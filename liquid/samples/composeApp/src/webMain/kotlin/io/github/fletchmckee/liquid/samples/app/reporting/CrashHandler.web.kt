// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.reporting

actual fun installPlatformCrashHandler(onCrash: (Throwable) -> Unit) {
  // No-op on web targets
}
