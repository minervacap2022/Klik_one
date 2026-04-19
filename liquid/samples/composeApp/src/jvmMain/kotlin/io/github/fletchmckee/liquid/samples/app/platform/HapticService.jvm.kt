package io.github.fletchmckee.liquid.samples.app.platform

actual object HapticService {
    actual fun lightImpact() { /* No haptics on desktop */ }
    actual fun mediumImpact() { /* No haptics on desktop */ }
    actual fun heavyImpact() { /* No haptics on desktop */ }
    actual fun success() { /* No haptics on desktop */ }
    actual fun error() { /* No haptics on desktop */ }
}
