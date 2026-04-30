// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package io.github.fletchmckee.liquid.samples.app.data.network

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSURLSessionWebSocketMessage
import platform.Foundation.NSURLSessionWebSocketTask
import platform.Foundation.dataWithBytes
import kotlin.concurrent.Volatile

/**
 * Proxy/VPN-tolerant audio streaming over a single WebSocket connection.
 *
 * Why this replaces per-pile multipart POSTs: every MITM proxy (Clash, Surge,
 * Charles, mitmproxy, corp ZTNA, …) handles ONE long-lived TLS session cleanly,
 * but chokes when forced to re-buffer hundreds of multipart bodies per minute.
 * A WebSocket gives the proxy the simplest possible shape — a CONNECT tunnel,
 * a single TLS handshake, then opaque binary frames flowing through.
 *
 * Lifecycle:
 *   • [start] opens a WS to `wss://hiklik.ai/api/v1/audio/ws/{userId}/{mode}`.
 *   • [sendBinary] enqueues a binary frame; sends are serialized through
 *     [sendMutex] so simultaneous tap callbacks can't interleave on the wire.
 *   • A background reader drains server acks (we don't act on the JSON ack
 *     bodies but they keep the read side pumped so the task stays open).
 *   • If the connection drops mid-recording, [start] is reinvoked from the
 *     caller's reconnect loop — there's no hidden state.
 *   • [stop] sends a normal close frame and tears down.
 */
internal class AudioStreamClient(
  private val baseUrl: String,
  private val userId: String,
  private val recordingMode: String,
  private val authBearer: () -> String?,
) {
  private val tag = "AudioStreamClient"

  private val session: NSURLSession by lazy {
    val cfg = NSURLSessionConfiguration.ephemeralSessionConfiguration()
    cfg.setHTTPShouldSetCookies(false)
    cfg.setTimeoutIntervalForRequest(30.0)
    cfg.setTimeoutIntervalForResource(0.0) // 0 = no resource timeout (long-lived stream)
    cfg.setWaitsForConnectivity(false)
    NSURLSession.sessionWithConfiguration(cfg)
  }

  private var task: NSURLSessionWebSocketTask? = null
  private var readerJob: Job? = null
  private val sendMutex = Mutex()
  private var scope: CoroutineScope? = null

  /** True once [start] succeeded and the WS is ready to receive [sendBinary]. */
  @Volatile var isOpen: Boolean = false
    private set

  fun start() {
    // Use the WS-only subdomain (DNS-only / grey cloud in Cloudflare) instead of
    // the main hiklik.ai. Reason: Cloudflare's edge negotiates HTTP/2 with the
    // client over the proxied hostname, then doesn't honor RFC 8441 WebSocket-
    // over-HTTP/2 bootstrap → returns HTTP/2 404 to every WS upgrade.
    // ws.hiklik.ai bypasses Cloudflare entirely; origin nginx serves HTTP/1.1
    // only on this host, so NSURLSession's classic Sec-WebSocket-Key/Upgrade
    // handshake works through any proxy/VPN that forwards a single TLS session.
    val wsBase = baseUrl
      .replace("https://hiklik.ai", "wss://ws.hiklik.ai")
      .replace("http://hiklik.ai", "ws://ws.hiklik.ai")
      .replace("https://", "wss://")
      .replace("http://", "ws://")
      .removeSuffix("/")
    val url = "$wsBase/audio/ws/$userId/$recordingMode"
    // Auth: Kotlin/Native's NSURLSession exposes `webSocketTaskWithURL(NSURL)`
    // but not the `(NSURLRequest)` variant, so we can't attach an Authorization
    // header. Fall back to a query-string token, which the backend's
    // verify_client_id() accepts via either the Authorization header OR
    // the `token` query param (see KK_common/auth/jwt verify wrapper).
    val token = authBearer().orEmpty()
    val urlWithToken = if (token.isEmpty()) url else "$url?token=${urlEncode(token)}"
    val nsUrl = NSURL.URLWithString(urlWithToken) ?: run {
      KlikLogger.e(tag, "Invalid WS URL: $urlWithToken")
      return
    }
    val newTask: NSURLSessionWebSocketTask = session.webSocketTaskWithURL(nsUrl)
    task = newTask
    newTask.resume()
    isOpen = true
    KlikLogger.i(tag, "WS opening: $url")

    val s = CoroutineScope(Dispatchers.Default + SupervisorJob())
    scope = s
    readerJob = s.launch { runReader() }
  }

  suspend fun sendBinary(bytes: ByteArray): Boolean {
    val t = task ?: return false
    if (!isOpen) return false
    val nsData: NSData = bytes.usePinned { pinned ->
      NSData.dataWithBytes(pinned.addressOf(0), length = bytes.size.toULong())
    }
    val msg = NSURLSessionWebSocketMessage(nsData)
    val ok = sendMutex.withLock {
      kotlinx.coroutines.suspendCancellableCoroutine<Boolean> { cont ->
        t.sendMessage(msg) { error: NSError? ->
          if (error != null) {
            KlikLogger.e(tag, "WS send failed: ${error.localizedDescription}")
            isOpen = false
            cont.resumeWith(Result.success(false))
          } else {
            cont.resumeWith(Result.success(true))
          }
        }
      }
    }
    return ok
  }

  /**
   * Drain server messages so the URLSession read pipe doesn't back-pressure
   * the connection closed. We log JSON acks but don't act on them — the
   * client is fire-and-forget at the WebSocket layer (server-side dedup is
   * by pile_id which the server itself generates).
   */
  private suspend fun runReader() {
    val t = task ?: return
    while (isOpen) {
      val gotMessage = kotlinx.coroutines.suspendCancellableCoroutine<Boolean> { cont ->
        t.receiveMessageWithCompletionHandler { _: NSURLSessionWebSocketMessage?, error: NSError? ->
          if (error != null) {
            KlikLogger.w(tag, "WS read ended: ${error.localizedDescription}")
            isOpen = false
            cont.resumeWith(Result.success(false))
          } else {
            cont.resumeWith(Result.success(true))
          }
        }
      }
      if (!gotMessage) break
    }
    KlikLogger.i(tag, "WS reader exited (isOpen=$isOpen)")
  }

  private fun urlEncode(s: String): String {
    val sb = StringBuilder(s.length)
    for (c in s) {
      when (c) {
        in 'A'..'Z', in 'a'..'z', in '0'..'9', '-', '_', '.', '~' -> sb.append(c)
        else -> {
          for (b in c.toString().encodeToByteArray()) {
            sb.append('%').append(((b.toInt() and 0xFF).toString(16)).padStart(2, '0').uppercase())
          }
        }
      }
    }
    return sb.toString()
  }

  fun stop() {
    isOpen = false
    try {
      task?.cancelWithCloseCode(1000L, reason = null)
    } catch (_: Throwable) {}
    task = null
    readerJob?.cancel()
    readerJob = null
    scope?.cancel()
    scope = null
    KlikLogger.i(tag, "WS stopped")
  }
}
