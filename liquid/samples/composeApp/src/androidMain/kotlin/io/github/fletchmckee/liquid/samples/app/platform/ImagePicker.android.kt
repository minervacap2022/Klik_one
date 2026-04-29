// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

/**
 * Android stub. The shipping product is iOS; this stub keeps the KMP module
 * compilable for Android and surfaces a loud log so we notice if it ever runs.
 */
actual object ImagePicker {
  actual suspend fun pickAvatar(): PickedImage? {
    KlikLogger.e("ImagePicker", "pickAvatar called on Android — not implemented (iOS-only product)")
    return null
  }
}
