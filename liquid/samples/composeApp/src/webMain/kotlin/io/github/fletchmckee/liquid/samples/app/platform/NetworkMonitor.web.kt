package io.github.fletchmckee.liquid.samples.app.platform

actual object NetworkMonitor {
    actual fun isConnected(): Boolean = true
    actual fun startMonitoring() {}
    actual fun stopMonitoring() {}
}
