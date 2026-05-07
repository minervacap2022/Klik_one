// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Best-effort extraction of a human-readable error message from FastAPI / nginx /
 * Cloudflare / load-balancer responses.
 *
 * Returns null if no message is recoverable. Used wherever the auth path needs to surface
 * a backend error to the UI without dumping the raw HTML/JSON payload.
 */
internal class FastApiErrorParser(private val json: Json) {

  fun extract(responseText: String): String? {
    val trimmed = responseText.trim()
    // Plain-text infra error pages (no JSON envelope).
    if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
      return when {
        trimmed.contains("Internal Server Error", ignoreCase = true) ->
          "Server error. Please try again later."

        trimmed.contains("Service Unavailable", ignoreCase = true) ->
          "Service temporarily unavailable. Please try again later."

        trimmed.contains("Bad Gateway", ignoreCase = true) ->
          "Server connection error. Please try again later."

        trimmed.contains("Gateway Timeout", ignoreCase = true) ->
          "Server timeout. Please try again later."

        trimmed.isNotEmpty() && trimmed.length < 200 -> trimmed

        else -> null
      }
    }

    val element = try {
      json.parseToJsonElement(responseText)
    } catch (e: Exception) {
      KlikLogger.w("FastApiErrorParser", "Failed to parse FastAPI error body: ${e.message}", e)
      return null
    }
    val obj: JsonObject = element as? JsonObject ?: return null

    val detail: JsonElement = obj["detail"] ?: return null
    return when (detail) {
      is JsonPrimitive -> if (detail.isString) detail.content else detail.toString()
      is JsonObject -> detail.toString()
      is JsonArray -> detail.toString()
      else -> detail.toString()
    }
  }
}
