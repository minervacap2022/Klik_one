// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.logging

import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Structured log entry compatible with Elastic Common Schema (ECS).
 *
 * Fields map to ECS:
 * - @timestamp -> ISO 8601 timestamp
 * - log.level -> severity level
 * - log.logger -> tag (component name)
 * - message -> log message
 * - error.message -> exception message (if present)
 * - error.type -> exception class name (if present)
 * - error.stack_trace -> stack trace string (if present)
 * - user.id -> current user ID
 * - labels -> additional key-value metadata
 */
@Serializable
data class LogEntry(
  @SerialName("@timestamp")
  val timestamp: String,
  @SerialName("log.level")
  val level: String,
  @SerialName("log.logger")
  val tag: String,
  val message: String,
  @SerialName("error.message")
  val errorMessage: String? = null,
  @SerialName("error.type")
  val errorType: String? = null,
  @SerialName("error.stack_trace")
  val errorStackTrace: String? = null,
  @SerialName("user.id")
  val userId: String? = null,
  val labels: Map<String, String>? = null,
) {
  companion object {
    fun create(
      level: LogLevel,
      tag: String,
      message: String,
      throwable: Throwable? = null,
      userId: String? = null,
      labels: Map<String, String>? = null,
    ): LogEntry {
      val now = Clock.System.now()
      return LogEntry(
        timestamp = now.toString(),
        level = level.label,
        tag = tag,
        message = message,
        errorMessage = throwable?.message,
        errorType = throwable?.let { it::class.simpleName },
        errorStackTrace = throwable?.stackTraceToString(),
        userId = userId,
        labels = labels,
      )
    }
  }
}
