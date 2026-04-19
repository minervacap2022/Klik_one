package io.github.fletchmckee.liquid.samples.app.reporting

import io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser
import io.github.fletchmckee.liquid.samples.app.data.network.DeviceInfo
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.logging.LogLevel
import io.github.fletchmckee.liquid.samples.app.platform.OAuthBrowser
import kotlinx.datetime.Clock

/**
 * Opens a pre-filled GitHub issue in the system browser with full error context.
 *
 * Issue includes:
 * - Error title and full message
 * - Device info (name, type) — device ID omitted for privacy
 * - Anonymized user ID (first/last 4 chars only)
 * - Recent ERROR-level log entries from KlikLogger
 */
object GitHubIssueReporter {

    private const val REPO = "minervacap2022/Klik_liquidglass"
    private const val MAX_URL_LENGTH = 8000

    /**
     * Open a GitHub issue with the given error context.
     *
     * @param errorTitle Short title describing the error category (e.g. "Initialization Error")
     * @param errorMessage Full error message to include in the issue body
     */
    fun reportError(errorTitle: String, errorMessage: String) {
        // Auto-report to backend (creates GitHub issue automatically)
        CrashReporter.reportError("UserReport", "$errorTitle: $errorMessage", RuntimeException("$errorTitle: $errorMessage"))

        // Also open browser for user visibility (existing behavior)
        val title = "Bug: ${errorTitle.take(100)}"
        val body = buildIssueBody(errorTitle, errorMessage)

        val url = buildString {
            append("https://github.com/$REPO/issues/new")
            append("?labels=bug")
            append("&title=")
            append(percentEncode(title))
            append("&body=")
            append(percentEncode(body))
        }

        val safeUrl = if (url.length > MAX_URL_LENGTH) url.take(MAX_URL_LENGTH) else url
        OAuthBrowser.openUrl(safeUrl)
    }

    private fun buildIssueBody(errorTitle: String, errorMessage: String): String {
        val now = Clock.System.now().toString()
        // Anonymize user ID to avoid leaking identity on public GitHub issues
        val anonymizedId = CurrentUser.userId?.let {
            it.take(4) + "****" + it.takeLast(4)
        } ?: "unknown"
        val deviceName = DeviceInfo.getDeviceName()
        val deviceType = DeviceInfo.getDeviceType()

        val recentErrors = KlikLogger.getRecentLogs(LogLevel.ERROR).takeLast(20)
        val logsSection = if (recentErrors.isNotEmpty()) {
            val logLines = recentErrors.joinToString("\n") { entry ->
                "${entry.timestamp} | ${entry.tag} | ${entry.message}${entry.errorMessage?.let { " ($it)" } ?: ""}"
            }
            "## Recent Error Logs\n```\n$logLines\n```"
        } else {
            "## Recent Error Logs\nNo recent error logs."
        }

        return buildString {
            appendLine("## Error Report")
            appendLine()
            appendLine("**Error**: $errorTitle")
            appendLine()
            appendLine("```")
            appendLine(errorMessage)
            appendLine("```")
            appendLine()
            appendLine("## Device Info")
            appendLine("_Note: User ID is anonymized for privacy. Device ID is omitted._")
            appendLine("| Field | Value |")
            appendLine("|-------|-------|")
            appendLine("| Device | $deviceName ($deviceType) |")
            appendLine("| User (anonymized) | $anonymizedId |")
            appendLine("| Timestamp | $now |")
            appendLine()
            appendLine(logsSection)
        }
    }

    /**
     * Percent-encode a string for use in a URL query parameter.
     * Handles all characters outside the unreserved set (RFC 3986).
     */
    private fun percentEncode(input: String): String {
        val sb = StringBuilder()
        for (byte in input.encodeToByteArray()) {
            val c = byte.toInt() and 0xFF
            when {
                c in 'A'.code..'Z'.code ||
                c in 'a'.code..'z'.code ||
                c in '0'.code..'9'.code ||
                c == '-'.code || c == '_'.code || c == '.'.code || c == '~'.code -> {
                    sb.append(c.toChar())
                }
                else -> {
                    sb.append('%')
                    sb.append(HEX_CHARS[c shr 4])
                    sb.append(HEX_CHARS[c and 0x0F])
                }
            }
        }
        return sb.toString()
    }

    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()
}
