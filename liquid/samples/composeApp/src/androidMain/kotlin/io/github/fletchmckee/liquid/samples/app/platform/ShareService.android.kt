// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import android.content.Intent
import io.github.fletchmckee.liquid.samples.app.data.storage.ApplicationContextProvider
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

/**
 * Android implementation of ShareService using Intent.ACTION_SEND.
 */
actual object ShareService {

  /**
   * Share text content using Android's native share sheet.
   * @param text The main text content to share
   * @param subject Optional subject line (used for email sharing)
   */
  actual fun share(text: String, subject: String?) {
    val context = try {
      ApplicationContextProvider.context
    } catch (e: UninitializedPropertyAccessException) {
      KlikLogger.e("ShareService", "Android context not initialized")
      return
    }

    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, text)
      subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
      type = "text/plain"
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val shareIntent = Intent.createChooser(sendIntent, null).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(shareIntent)
  }
}
