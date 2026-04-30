// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import android.content.pm.PackageInfo
import io.github.fletchmckee.liquid.samples.app.data.storage.ApplicationContextProvider

actual object AppVersion {
  actual val marketing: String
    get() = readPackageInfo().versionName.orEmpty()

  actual val build: String
    get() = readPackageInfo().longVersionCode.toString()

  private fun readPackageInfo(): PackageInfo {
    val ctx = ApplicationContextProvider.context
    return ctx.packageManager.getPackageInfo(ctx.packageName, 0)
  }
}
