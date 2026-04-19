package io.github.fletchmckee.liquid.samples.app.reporting

import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.data.network.HttpClient
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class BugReportRequest(
    val error_type: String,
    val error_message: String,
    val stack_trace: String,
    val fingerprint: String,
    val platform: String,
    val os_version: String,
    val app_version: String,
    val device_id: String,
    val device_type: String,
    val user_id: String?,
    val recent_logs: String,
    val current_route: String?,
    val timestamp: String
)

@Serializable
data class BugReportResponse(
    val success: Boolean,
    val issue_url: String? = null,
    val deduplicated: Boolean = false,
    val message: String
)

object BugReportClient {
    private const val TAG = "BugReportClient"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun submitReport(report: BugReportRequest): Result<BugReportResponse> {
        val url = "${ApiConfig.BUG_REPORT_BASE_URL}/v1/report"
        val body = json.encodeToString(report)

        return try {
            val response = HttpClient.postUrl(url, body)
                ?: throw Exception("Empty response from bug report endpoint")

            val trimmed = response.trimStart()
            if (!trimmed.startsWith("{") && !trimmed.startsWith("[") && !trimmed.startsWith("\"")) {
                // Non-JSON response (e.g. Cloudflare HTML 502) — silently fail, don't throw from the bug reporter
                KlikLogger.w(TAG, "Bug report API returned non-JSON response (likely proxy error), ignoring")
                return Result.failure(Exception("Non-JSON response from bug report API"))
            }

            val parsed = json.decodeFromString<BugReportResponse>(response)
            Result.success(parsed)
        } catch (e: Exception) {
            // Silently fail — this is the bug reporter, not critical path
            KlikLogger.w(TAG, "Bug report submission failed: ${e.message}")
            Result.failure(e)
        }
    }
}
