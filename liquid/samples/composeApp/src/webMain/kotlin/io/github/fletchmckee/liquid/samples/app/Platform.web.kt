// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app

class WebPlatform : Platform {
  override val name: String = "Web"
}

actual fun getPlatform(): Platform = WebPlatform()

// `bindToBrowserNavigation` still has issues.
actual fun displayNavIcons(): Boolean = true
