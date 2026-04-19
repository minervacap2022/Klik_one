package io.github.fletchmckee.liquid.samples.app.reporting

import io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser
import io.github.fletchmckee.liquid.samples.app.data.network.DeviceInfo
import io.github.fletchmckee.liquid.samples.app.di.BuildConfig
import io.github.fletchmckee.liquid.samples.app.getPlatform
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

object CrashReporter {
    private const val TAG = "CrashReporter"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var isInitialized = false
    private var currentRoute: String? = null

    // Track recently reported fingerprints to avoid double-reporting within one session
    private val reportedFingerprints = mutableSetOf<String>()
    private const val MAX_REPORTED_PER_SESSION = 10

    fun initialize() {
        if (isInitialized) return
        isInitialized = true
        installPlatformCrashHandler { throwable ->
            reportCrash(throwable)
        }
        KlikLogger.i(TAG, "Crash reporter initialized")
    }

    fun setCurrentRoute(route: String) {
        currentRoute = route
    }

    /**
     * Report a caught exception (non-fatal).
     * Called from catch blocks where errors should be tracked.
     */
    fun reportError(tag: String, message: String, throwable: Throwable) {
        val fingerprint = generateFingerprint(throwable)
        if (reportedFingerprints.contains(fingerprint)) return
        if (reportedFingerprints.size >= MAX_REPORTED_PER_SESSION) return

        reportedFingerprints.add(fingerprint)

        scope.launch {
            try {
                val report = buildReport(
                    errorType = throwable::class.simpleName ?: "Unknown",
                    errorMessage = "$tag: $message",
                    stackTrace = throwable.stackTraceToString(),
                    fingerprint = fingerprint
                )
                BugReportClient.submitReport(report)
            } catch (e: Exception) {
                KlikLogger.e(TAG, "Failed to report error: ${e.message}", e)
            }
        }
    }

    /**
     * Report an uncaught crash (fatal).
     */
    private fun reportCrash(throwable: Throwable) {
        val fingerprint = generateFingerprint(throwable)
        if (reportedFingerprints.contains(fingerprint)) return
        reportedFingerprints.add(fingerprint)

        scope.launch {
            try {
                val report = buildReport(
                    errorType = throwable::class.simpleName ?: "UnknownCrash",
                    errorMessage = throwable.message ?: "No message",
                    stackTrace = throwable.stackTraceToString(),
                    fingerprint = fingerprint
                )
                BugReportClient.submitReport(report)
            } catch (e: Exception) {
                KlikLogger.e(TAG, "Failed to report crash: ${e.message}", e)
            }
        }
    }

    private fun buildReport(
        errorType: String,
        errorMessage: String,
        stackTrace: String,
        fingerprint: String
    ): BugReportRequest {
        val recentLogs = KlikLogger.exportLogsAsNdjson()
        val platform = getPlatform()

        return BugReportRequest(
            error_type = errorType,
            error_message = errorMessage,
            stack_trace = stackTrace,
            fingerprint = fingerprint,
            platform = DeviceInfo.getDeviceName(),
            os_version = platform.name,
            app_version = BuildConfig.VERSION_NAME,
            device_id = DeviceInfo.getDeviceId(),
            device_type = DeviceInfo.getDeviceType(),
            user_id = CurrentUser.userId,
            recent_logs = recentLogs,
            current_route = currentRoute,
            timestamp = Clock.System.now().toString()
        )
    }

    /**
     * Generate a fingerprint for deduplication.
     * Hash of: error class name + first 3 meaningful stack frames.
     */
    private fun generateFingerprint(throwable: Throwable): String {
        val errorClass = throwable::class.simpleName ?: "Unknown"
        val stackLines = throwable.stackTraceToString()
            .lines()
            .filter { it.trimStart().startsWith("at ") }
            .take(3)
            .joinToString("|")
        val raw = "$errorClass|$stackLines"
        return raw.hashCode().toUInt().toString(16).padStart(8, '0')
    }
}

/**
 * Platform-specific uncaught exception handler installation.
 * - iOS: kotlin.native.setUnhandledExceptionHook
 * - Android/JVM: Thread.setDefaultUncaughtExceptionHandler
 * - Web: no-op
 */
expect fun installPlatformCrashHandler(onCrash: (Throwable) -> Unit)
