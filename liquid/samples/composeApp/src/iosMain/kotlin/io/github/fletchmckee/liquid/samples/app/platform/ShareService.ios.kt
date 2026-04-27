// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("DEPRECATION") // UIApplication.keyWindow is the only reliable API
                              // here; the modern scene API has complex Kotlin/Native
                              // interop requirements.
package io.github.fletchmckee.liquid.samples.app.platform

import platform.Foundation.setValue
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

/**
 * iOS implementation of ShareService using UIActivityViewController.
 */
actual object ShareService {

  /**
   * Share text content using iOS's native share sheet (UIActivityViewController).
   * @param text The main text content to share
   * @param subject Optional subject line (used for email sharing)
   */
  actual fun share(text: String, subject: String?) {
    val items = listOf<Any>(text)

    val activityController = UIActivityViewController(
      activityItems = items,
      applicationActivities = null,
    )

    // Set subject for email if provided (uses NSObject.setValue:forKey:)
    if (subject != null) {
      activityController.setValue(value = subject, forKey = "subject")
    }

    // Get the key window and present the share sheet
    val rootViewController = getKeyWindow()?.rootViewController

    // Find the topmost presented controller
    var topController: UIViewController? = rootViewController
    while (topController?.presentedViewController != null) {
      topController = topController.presentedViewController
    }

    topController?.presentViewController(
      activityController,
      animated = true,
      completion = null,
    )
  }

  /**
   * Get the key window from the application.
   */
  private fun getKeyWindow(): UIWindow? {
    // Use the keyWindow property - while deprecated, it works reliably
    // The modern scene API has complex Kotlin/Native interop requirements
    return UIApplication.sharedApplication.keyWindow
  }
}
