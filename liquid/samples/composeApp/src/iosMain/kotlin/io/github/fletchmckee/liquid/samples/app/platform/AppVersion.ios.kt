// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import platform.Foundation.NSBundle

actual object AppVersion {
  actual val marketing: String
    get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "0.0"

  actual val build: String
    get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "0"
}
