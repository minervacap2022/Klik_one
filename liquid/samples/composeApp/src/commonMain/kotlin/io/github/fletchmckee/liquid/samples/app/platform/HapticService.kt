package io.github.fletchmckee.liquid.samples.app.platform

/**
 * Platform-specific haptic feedback service.
 * iOS: UIImpactFeedbackGenerator / UINotificationFeedbackGenerator
 * Android: HapticFeedbackConstants via View
 */
expect object HapticService {
    /**
     * Light impact - for subtle interactions like toggles
     */
    fun lightImpact()

    /**
     * Medium impact - for swipe gestures and confirmations
     */
    fun mediumImpact()

    /**
     * Heavy impact - for destructive actions like delete
     */
    fun heavyImpact()

    /**
     * Success notification - for completed actions
     */
    fun success()

    /**
     * Error notification - for failed actions
     */
    fun error()
}
