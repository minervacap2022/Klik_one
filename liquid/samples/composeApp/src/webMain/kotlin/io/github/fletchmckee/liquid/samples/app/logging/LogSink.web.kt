package io.github.fletchmckee.liquid.samples.app.logging

actual fun platformLogSink(): LogSink = object : LogSink {
    override fun emit(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val prefix = "[${level.label}][$tag]"
        println("$prefix $message")
        if (throwable != null) {
            println("$prefix ${throwable.stackTraceToString()}")
        }
    }
}
