// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

/**
 * Web stub implementation of ShareService.
 */
actual object ShareService {

  actual fun share(text: String, subject: String?) {
    // Web sharing not implemented
    KlikLogger.i("ShareService", "Share (Web): ${subject ?: "No subject"} - $text")
  }
}
