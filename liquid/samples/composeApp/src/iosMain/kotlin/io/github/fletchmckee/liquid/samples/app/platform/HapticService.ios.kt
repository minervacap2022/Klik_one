package io.github.fletchmckee.liquid.samples.app.platform

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

actual object HapticService {
    actual fun lightImpact() {
        try {
            val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
            generator.prepare()
            generator.impactOccurred()
        } catch (_: Exception) {
            // Haptics unavailable (simulator) — ignore
        }
    }

    actual fun mediumImpact() {
        try {
            val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
            generator.prepare()
            generator.impactOccurred()
        } catch (_: Exception) {
            // Haptics unavailable (simulator) — ignore
        }
    }

    actual fun heavyImpact() {
        try {
            val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
            generator.prepare()
            generator.impactOccurred()
        } catch (_: Exception) {
            // Haptics unavailable (simulator) — ignore
        }
    }

    actual fun success() {
        try {
            val generator = UINotificationFeedbackGenerator()
            generator.prepare()
            generator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
        } catch (_: Exception) {
            // Haptics unavailable (simulator) — ignore
        }
    }

    actual fun error() {
        try {
            val generator = UINotificationFeedbackGenerator()
            generator.prepare()
            generator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
        } catch (_: Exception) {
            // Haptics unavailable (simulator) — ignore
        }
    }
}
