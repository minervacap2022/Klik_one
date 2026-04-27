// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.logging

import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser
import io.github.fletchmckee.liquid.samples.app.data.network.NativeHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Ships structured log entries from [KlikLogger] to the KK_logs backend
 * (`/api/logs/v1/ingest`) in near-realtime batches.
 *
 * Design:
 * - Runs a single background coroutine that wakes every [FLUSH_INTERVAL_MS].
 * - Drains a bounded [Channel] populated by KlikLogger's onEntry tap, batches up
 *   to [MAX_BATCH_SIZE] entries, and POSTs to the ingest endpoint.
 * - Channel overflow policy is DROP_OLDEST: during sustained backpressure we
 *   keep the most recent entries rather than block the hot logging path.
 * - If the user is not authenticated yet (no access token), entries sit in the
 *   channel until auth is ready.
 * - On flush failure we drop the batch (best-effort — no retry storms).
 * - NEVER ships logs whose message mentions the `/api/logs/` URL to prevent a
 *   self-referential loop when the ingest endpoint itself fails.
 *
 * Channel is used instead of locks because synchronized / Mutex blocking are
 * not both available in Kotlin Multiplatform common code — Channel is the
 * idiomatic KMP primitive for producer/consumer across platforms.
 */
object RemoteLogShipper {

  // ==================== Tunables ====================

  /** How often the background coroutine wakes to flush. */
  private const val FLUSH_INTERVAL_MS = 10_000L

  /** Hard cap on the in-memory queue (oldest dropped on overflow). */
  private const val MAX_QUEUE_SIZE = 2000

  /** Max entries per POST (must match backend MAX_ENTRIES_PER_BATCH). */
  private const val MAX_BATCH_SIZE = 500

  /** Endpoint path appended to ApiConfig.LOGS_BASE_URL. */
  private const val INGEST_PATH = "/v1/ingest"

  /**
   * Any log message containing this substring is dropped from the shipper queue
   * to prevent self-referential amplification during outages of the logs backend.
   */
  private const val LOGS_URL_MARKER = "/api/logs/"

  // ==================== State ====================

  private val channel = Channel<LogEntry>(
    capacity = MAX_QUEUE_SIZE,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
  )
  private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
  private val nativeClient = NativeHttpClient()
  private var job: Job? = null
  private var started: Boolean = false

  private val json = Json {
    encodeDefaults = false
    prettyPrint = false
    ignoreUnknownKeys = true
  }

  private var clientContext: ClientContext = ClientContext()

  // ==================== Public API ====================

  /**
   * Start the background shipper. Idempotent — calling multiple times has no effect.
   * Must be called AFTER [io.github.fletchmckee.liquid.samples.app.data.network.Environment]
   * is configured and [HttpClient] is ready.
   */
  fun start(platform: String, appVersion: String? = null, deviceId: String? = null) {
    if (started) return
    started = true
    clientContext = ClientContext(
      platform = platform,
      app_version = appVersion,
      device_id = deviceId,
    )
    // Install the tap so KlikLogger pushes each new entry into our channel.
    KlikLogger.onEntry = { entry -> enqueue(entry) }
    job = scope.launch {
      runLoop()
    }
  }

  /** Stop the shipper and flush any pending entries. Safe to call multiple times. */
  suspend fun stop() {
    if (!started) return
    started = false
    KlikLogger.onEntry = null
    job?.cancelAndJoin()
    job = null
    flush()
  }

  // ==================== Internal ====================

  private fun enqueue(entry: LogEntry) {
    // Drop logs that would create a self-referential loop.
    if (entry.message.contains(LOGS_URL_MARKER)) return
    // trySend never blocks. If the channel is full, DROP_OLDEST kicks in.
    channel.trySend(entry)
  }

  private suspend fun runLoop() {
    while (scope.isActive) {
      try {
        delay(FLUSH_INTERVAL_MS)
        flush()
      } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
      } catch (_: Throwable) {
        // Never let the loop die. Swallow everything except cancellation.
      }
    }
  }

  /** Drain up to MAX_BATCH_SIZE entries and ship. Best-effort; never throws. */
  private suspend fun flush() {
    // No user yet — keep entries in channel for the next cycle.
    val token = CurrentUser.accessToken
    if (token.isNullOrEmpty()) return

    val batch = ArrayList<LogEntry>(MAX_BATCH_SIZE)
    while (batch.size < MAX_BATCH_SIZE) {
      val entry = channel.tryReceive().getOrNull() ?: break
      batch.add(entry)
    }
    if (batch.isEmpty()) return

    val entriesJson: List<JsonElement> = batch.map { e ->
      json.encodeToJsonElement(LogEntry.serializer(), e)
    }
    val payload = IngestRequest(entries = entriesJson, client = clientContext)
    val body = try {
      json.encodeToString(IngestRequest.serializer(), payload)
    } catch (_: Throwable) {
      return
    }

    val url = ApiConfig.LOGS_BASE_URL + INGEST_PATH
    try {
      val headers = CurrentUser.getAuthHeaders() + mapOf(
        ApiConfig.Headers.CONTENT_TYPE to ApiConfig.ContentTypes.JSON,
        ApiConfig.Headers.ACCEPT to ApiConfig.ContentTypes.JSON,
      )
      nativeClient.post(url, body, headers)
    } catch (_: Throwable) {
      // Drop on failure. Next flush cycle will try again with fresh entries.
    }
  }

  // ==================== DTOs ====================

  @Serializable
  private data class ClientContext(
    val platform: String = "unknown",
    val app_version: String? = null,
    val device_id: String? = null,
  )

  @Serializable
  private data class IngestRequest(
    val entries: List<JsonElement>,
    val client: ClientContext,
  )
}
