// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import platform.Foundation.NSUserDefaults

private const val DEEP_LINK_KEY = "pending_deep_link"

actual object DeepLinkHandler {
  actual fun setPendingDeepLink(url: String) {
    val defaults = NSUserDefaults.standardUserDefaults
    defaults.setObject(url, forKey = DEEP_LINK_KEY)
    defaults.synchronize()
  }

  actual fun consumePendingDeepLink(): String? {
    val defaults = NSUserDefaults.standardUserDefaults
    val url = defaults.stringForKey(DEEP_LINK_KEY)
    if (url != null) {
      defaults.removeObjectForKey(DEEP_LINK_KEY)
      defaults.synchronize()
    }
    return url
  }
}
