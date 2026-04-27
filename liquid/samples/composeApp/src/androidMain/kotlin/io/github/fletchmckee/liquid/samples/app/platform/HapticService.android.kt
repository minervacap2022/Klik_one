// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("DEPRECATION") // Vibrator getSystemService + vibrate(Long) are
                              // the only available APIs for Android < S / < O.
package io.github.fletchmckee.liquid.samples.app.platform

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import io.github.fletchmckee.liquid.samples.app.data.storage.ApplicationContextProvider
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

actual object HapticService {
  private fun vibrate(durationMs: Long, amplitude: Int) {
    try {
      val context = ApplicationContextProvider.context
      val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
      } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
      } else {
        vibrator.vibrate(durationMs)
      }
    } catch (e: Exception) {
      KlikLogger.e("HapticService", "Haptic feedback failed: ${e.message}")
    }
  }

  actual fun lightImpact() = vibrate(10, 50)
  actual fun mediumImpact() = vibrate(20, 128)
  actual fun heavyImpact() = vibrate(30, 255)
  actual fun success() = vibrate(15, 100)
  actual fun error() = vibrate(40, 200)
}
