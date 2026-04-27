// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

actual object HapticService {
  actual fun lightImpact() { /* No haptics in browser */ }
  actual fun mediumImpact() { /* No haptics in browser */ }
  actual fun heavyImpact() { /* No haptics in browser */ }
  actual fun success() { /* No haptics in browser */ }
  actual fun error() { /* No haptics in browser */ }
}
