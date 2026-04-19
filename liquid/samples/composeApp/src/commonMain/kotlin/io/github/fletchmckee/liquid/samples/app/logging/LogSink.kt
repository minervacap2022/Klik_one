package io.github.fletchmckee.liquid.samples.app.logging

/**
 * Platform-specific log output target.
 * Each platform routes log output to its native logging system.
 */
interface LogSink {
    fun emit(level: LogLevel, tag: String, message: String, throwable: Throwable?)
}

/**
 * Returns the platform-appropriate LogSink implementation.
 * - iOS: NSLog
 * - Android: android.util.Log
 * - JVM/Web: println with structured prefix
 */
expect fun platformLogSink(): LogSink
