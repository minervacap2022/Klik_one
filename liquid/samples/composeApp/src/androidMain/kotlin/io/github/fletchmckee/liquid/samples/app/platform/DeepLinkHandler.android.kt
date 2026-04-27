// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import java.util.concurrent.atomic.AtomicReference

actual object DeepLinkHandler {
  private val pendingUrl = AtomicReference<String?>(null)

  actual fun setPendingDeepLink(url: String) {
    pendingUrl.set(url)
  }

  actual fun consumePendingDeepLink(): String? = pendingUrl.getAndSet(null)
}
