package io.github.fletchmckee.liquid.samples.app.reporting

actual fun installPlatformCrashHandler(onCrash: (Throwable) -> Unit) {
    // Kotlin/Native unhandled exception hook
    // This catches any uncaught Kotlin exception on iOS
    @OptIn(kotlin.experimental.ExperimentalNativeApi::class)
    kotlin.native.setUnhandledExceptionHook { throwable: Throwable ->
        onCrash(throwable)
    }
}
