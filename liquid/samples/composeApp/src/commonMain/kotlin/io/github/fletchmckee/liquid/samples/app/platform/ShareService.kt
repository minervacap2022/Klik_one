// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

/**
 * Platform-specific share service.
 * On iOS, this uses UIActivityViewController.
 * On Android, this would use Intent.ACTION_SEND.
 */
expect object ShareService {
  /**
   * Share text content using the native share sheet.
   * @param text The main text content to share
   * @param subject Optional subject line (used for email sharing)
   */
  fun share(text: String, subject: String? = null)
}
