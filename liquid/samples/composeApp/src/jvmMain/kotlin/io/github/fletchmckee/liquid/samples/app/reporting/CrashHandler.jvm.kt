// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.reporting

actual fun installPlatformCrashHandler(onCrash: (Throwable) -> Unit) {
  val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
  Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
    onCrash(throwable)
    defaultHandler?.uncaughtException(thread, throwable)
  }
}
