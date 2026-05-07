// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.logging

import io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Centralized structured logging for the Klik app.
 *
 * Produces Elastic Common Schema (ECS) compatible log entries with:
 * - ISO 8601 timestamps
 * - Log levels (DEBUG, INFO, WARN, ERROR)
 * - Component tags
 * - User context (user_id from CurrentUser)
 * - In-memory ring buffer for debugging/export
 * - Platform-native output (NSLog on iOS, android.util.Log on Android, println on JVM/Web)
 *
 * Usage:
 *   KlikLogger.i("HttpClient", "Token refresh successful")
 *   KlikLogger.e("RemoteDataFetcher", "Failed to fetch meetings", exception)
 *   KlikLogger.d("CalendarScreen", "Filtering 42 meetings for date")
 *   KlikLogger.w("CurrentUser", "Token might be malformed")
 */
object KlikLogger {

  private val sink: LogSink = platformLogSink()
  private val buffer: LogBuffer = LogBuffer()

  /** Minimum level to output. Logs below this level are silently discarded. */
  var minimumLevel: LogLevel = LogLevel.INFO

  /**
   * Optional tap invoked for every accepted log entry.
   * Used by [RemoteLogShipper] to stream logs to the backend.
   * Must not throw and must not call KlikLogger itself.
   */
  var onEntry: ((LogEntry) -> Unit)? = null

  private val json = Json {
    encodeDefaults = false
    prettyPrint = false
  }

  // ==================== Public API ====================

  fun d(tag: String, message: String, throwable: Throwable? = null, labels: Map<String, String>? = null) = log(LogLevel.DEBUG, tag, message, throwable, labels)

  fun i(tag: String, message: String, throwable: Throwable? = null, labels: Map<String, String>? = null) = log(LogLevel.INFO, tag, message, throwable, labels)

  fun w(tag: String, message: String, throwable: Throwable? = null, labels: Map<String, String>? = null) = log(LogLevel.WARN, tag, message, throwable, labels)

  fun e(tag: String, message: String, throwable: Throwable? = null, labels: Map<String, String>? = null) = log(LogLevel.ERROR, tag, message, throwable, labels)

  // ==================== Buffer Access ====================

  /** Get all buffered log entries. */
  fun getRecentLogs(): List<LogEntry> = buffer.getAll()

  /** Get recent logs at or above the given severity. */
  fun getRecentLogs(minLevel: LogLevel): List<LogEntry> = buffer.getByLevel(minLevel)

  /** Get recent logs for a specific tag. */
  fun getRecentLogs(tag: String): List<LogEntry> = buffer.getByTag(tag)

  /** Serialize recent logs to Elastic-compatible NDJSON (newline-delimited JSON). */
  fun exportLogsAsNdjson(): String = buffer.getAll().joinToString("\n") { entry ->
    json.encodeToString(entry)
  }

  /** Clear the log buffer. */
  fun clearBuffer() = buffer.clear()

  // ==================== Internal ====================

  private fun log(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?,
    labels: Map<String, String>?,
  ) {
    if (level.value < minimumLevel.value) return

    val userId = CurrentUser.userId

    val entry = LogEntry.create(
      level = level,
      tag = tag,
      message = message,
      throwable = throwable,
      userId = userId,
      labels = labels,
    )

    buffer.add(entry)
    sink.emit(level, tag, message, throwable)

    val tap = onEntry
    if (tap != null) {
      try {
        tap(entry)
      } catch (e: Throwable) {
        sink.emit(LogLevel.ERROR, "KlikLogger", "Log tap failed: ${e.message}", e)
      }
    }
  }
}
