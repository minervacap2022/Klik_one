// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

actual object HapticService {
  actual fun lightImpact() {
    val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
    generator.prepare()
    generator.impactOccurred()
  }

  actual fun mediumImpact() {
    val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
    generator.prepare()
    generator.impactOccurred()
  }

  actual fun heavyImpact() {
    val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
    generator.prepare()
    generator.impactOccurred()
  }

  actual fun success() {
    val generator = UINotificationFeedbackGenerator()
    generator.prepare()
    generator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
  }

  actual fun error() {
    val generator = UINotificationFeedbackGenerator()
    generator.prepare()
    generator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
  }
}
