// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.data.network.AudioStreamClient
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioPCMFormatFloat32
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.setActive
import platform.Foundation.NSError

/**
 * iOS implementation of FixedSessionAudioStreamer using AVAudioEngine.
 * Captures real-time audio via input node tap, converts to Int16 PCM,
 * and uploads chunks to the backend streaming endpoint.
 */
actual object FixedSessionAudioStreamer {
  private const val TAG = "FixedSessionAudioStreamer"

  // Upload every ~1s of audio (100KB at 48kHz Int16 mono ≈ 1.04s)
  private const val UPLOAD_THRESHOLD_BYTES = 100 * 1024

  private val _isStreaming = MutableStateFlow(false)
  actual val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

  private val _isPaused = MutableStateFlow(false)
  actual val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

  private var audioEngine: AVAudioEngine? = null
  private val pcmBufferLock = platform.Foundation.NSLock()
  private var pcmBuffer = mutableListOf<Byte>()
  private var userId: String = ""
  private var uploadScope: CoroutineScope? = null
  private var pileCounter = 0

  // Resilience design:
  //  • Tap callback NEVER does I/O — it only enqueues onto [uploadChannel].
  //  • Exactly ONE [uploadWorkerJob] coroutine drains the channel sequentially. This
  //    serializes all pile uploads and prevents N parallel coroutines from racing
  //    on a single-connection NSURLSession (which causes timeout cascades under
  //    flaky proxies/VPNs).
  //  • If a pile exhausts its inline retries it goes to [deferred] (cap 120 ≈ 2 min).
  //    The worker drains [deferred] opportunistically between fresh piles.
  private const val DEFERRED_QUEUE_MAX = 120
  private val deferredLock = platform.Foundation.NSLock()
  private val deferred = ArrayDeque<DeferredPile>()
  private var uploadChannel: Channel<DeferredPile>? = null
  private var uploadWorkerJob: Job? = null
  private var streamClient: AudioStreamClient? = null

  private data class DeferredPile(val pileNumber: Int, val data: ByteArray)

  actual suspend fun startStreaming(userId: String): Boolean = withContext(Dispatchers.Main) {
    KlikLogger.i(TAG, "startStreaming called for user=$userId, currentlyStreaming=${_isStreaming.value}")

    if (_isStreaming.value) {
      KlikLogger.w(TAG, "Already streaming, returning false")
      return@withContext false
    }

    val hasPerm = VoiceRecorderService.hasMicrophonePermission()
    KlikLogger.i(TAG, "Microphone permission check: $hasPerm")
    if (!hasPerm) {
      KlikLogger.i(TAG, "Requesting microphone permission...")
      val granted = VoiceRecorderService.requestMicrophonePermission()
      KlikLogger.i(TAG, "Microphone permission request result: $granted")
      if (!granted) {
        KlikLogger.e(TAG, "Microphone permission denied by user")
        return@withContext false
      }
    }

    this@FixedSessionAudioStreamer.userId = userId
    pileCounter = 0

    // Configure audio session
    val session = AVAudioSession.sharedInstance()
    KlikLogger.i(TAG, "AVAudioSession category=${session.category}, mode=${session.mode}")

    memScoped {
      val errorPtr = alloc<ObjCObjectVar<NSError?>>()
      session.setCategory(AVAudioSessionCategoryRecord, errorPtr.ptr)
      if (errorPtr.value != null) {
        KlikLogger.e(TAG, "FAIL at setCategory: code=${errorPtr.value?.code}, domain=${errorPtr.value?.domain}, desc=${errorPtr.value?.localizedDescription}")
        return@withContext false
      }
      KlikLogger.i(TAG, "setCategory(Record) OK")
    }
    memScoped {
      val errorPtr = alloc<ObjCObjectVar<NSError?>>()
      session.setMode(AVAudioSessionModeDefault, errorPtr.ptr)
      if (errorPtr.value != null) {
        KlikLogger.e(TAG, "FAIL at setMode: code=${errorPtr.value?.code}, domain=${errorPtr.value?.domain}, desc=${errorPtr.value?.localizedDescription}")
        return@withContext false
      }
      KlikLogger.i(TAG, "setMode(Default) OK")
    }
    memScoped {
      val errorPtr = alloc<ObjCObjectVar<NSError?>>()
      session.setActive(true, errorPtr.ptr)
      if (errorPtr.value != null) {
        KlikLogger.e(TAG, "FAIL at setActive(true): code=${errorPtr.value?.code}, domain=${errorPtr.value?.domain}, desc=${errorPtr.value?.localizedDescription}")
        return@withContext false
      }
      KlikLogger.i(TAG, "setActive(true) OK")
    }

    KlikLogger.i(TAG, "Creating AVAudioEngine...")
    val engine = AVAudioEngine()
    val inputNode = engine.inputNode
    val nativeFormat = inputNode.outputFormatForBus(0u)

    KlikLogger.i(TAG, "Native input format: sampleRate=${nativeFormat.sampleRate}Hz, channels=${nativeFormat.channelCount}, interleaved=${nativeFormat.isInterleaved()}, commonFormat=${nativeFormat.commonFormat}")

    if (nativeFormat.sampleRate == 0.0 || nativeFormat.channelCount == 0u) {
      KlikLogger.e(TAG, "FAIL: Invalid input format (sampleRate=${nativeFormat.sampleRate}, channels=${nativeFormat.channelCount})")
      return@withContext false
    }

    // Always request Float32 mono 48kHz from AVAudioEngine — it resamples/converts from native hardware format.
    // Using the native format risks getting non-Float32 data where floatChannelData would be null.
    val tapFormat = AVAudioFormat(
      commonFormat = AVAudioPCMFormatFloat32,
      sampleRate = 48000.0,
      channels = 1u,
      interleaved = false,
    )

    // Create upload scope for background uploads
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    uploadScope = scope

    KlikLogger.i(TAG, "Installing tap on input node bus 0 (Float32 48kHz mono)...")

    // Install tap to receive audio buffers
    var tapCallCount = 0
    inputNode.installTapOnBus(
      bus = 0u,
      bufferSize = 4096u,
      format = tapFormat,
    ) { buffer, _ ->
      if (buffer == null) {
        KlikLogger.w(TAG, "Tap received null buffer")
        return@installTapOnBus
      }
      if (_isPaused.value) return@installTapOnBus
      tapCallCount++
      val frameCount = buffer.frameLength.toInt()
      val floatData = buffer.floatChannelData?.get(0)
      if (floatData == null) {
        KlikLogger.w(TAG, "Tap buffer has null floatChannelData[0], frameCount=$frameCount")
        return@installTapOnBus
      }

      if (tapCallCount <= 3) {
        KlikLogger.d(TAG, "Tap callback #$tapCallCount: frameCount=$frameCount")
      }

      // Convert float32 PCM to Int16 PCM bytes (little-endian)
      val newBytes = ByteArray(frameCount * 2)
      for (i in 0 until frameCount) {
        val clamped = floatData[i].coerceIn(-1.0f, 1.0f)
        val int16 = (clamped * 32767).toInt().toShort()
        newBytes[i * 2] = (int16.toInt() and 0xFF).toByte()
        newBytes[i * 2 + 1] = ((int16.toInt() shr 8) and 0xFF).toByte()
      }

      var dataToUpload: ByteArray? = null
      var pileNum = 0
      pcmBufferLock.lock()
      try {
        for (b in newBytes) pcmBuffer.add(b)
        if (pcmBuffer.size >= UPLOAD_THRESHOLD_BYTES) {
          dataToUpload = ByteArray(pcmBuffer.size).also { arr ->
            for (idx in pcmBuffer.indices) arr[idx] = pcmBuffer[idx]
          }
          pcmBuffer.clear()
          pileCounter++
          pileNum = pileCounter
        }
      } finally {
        pcmBufferLock.unlock()
      }

      dataToUpload?.let { data ->
        val ch = uploadChannel
        if (ch != null) {
          val sent = ch.trySend(DeferredPile(pileNum, data)).isSuccess
          if (!sent) {
            // Channel is full — should not happen with UNLIMITED capacity, but if it
            // does we fall back to the deferred queue rather than dropping silently.
            enqueueDeferred(DeferredPile(pileNum, data))
            KlikLogger.w(TAG, "uploadChannel full, deferred pile #$pileNum")
          }
        }
      }
    }

    // Start the engine
    KlikLogger.i(TAG, "Starting AVAudioEngine...")
    memScoped {
      val errorPtr = alloc<ObjCObjectVar<NSError?>>()
      engine.startAndReturnError(errorPtr.ptr)
      if (errorPtr.value != null) {
        KlikLogger.e(TAG, "FAIL at engine.start: code=${errorPtr.value?.code}, domain=${errorPtr.value?.domain}, desc=${errorPtr.value?.localizedDescription}")
        inputNode.removeTapOnBus(0u)
        scope.cancel()
        uploadScope = null
        return@withContext false
      }
    }

    audioEngine = engine
    _isStreaming.value = true

    // Open WebSocket transport: ONE persistent TLS session per recording, binary
    // frames flow through it. Universally proxy/VPN tolerant — Clash, Surge,
    // Charles, corp ZTNA, hotel WiFi all forward WS cleanly.
    val client = AudioStreamClient(
      baseUrl = ApiConfig.BASE_URL,
      userId = userId,
      recordingMode = "fixed",
      authBearer = { io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser.accessToken },
    )
    client.start()
    streamClient = client

    // Single-worker pipeline. Piles enter via [uploadChannel] from the tap
    // callback; the worker drains them one at a time and pumps them into the
    // open WS. Failed sends go to [deferred] for retry on the next reconnect.
    val ch = Channel<DeferredPile>(Channel.UNLIMITED)
    uploadChannel = ch
    uploadWorkerJob = scope.launch { runUploadWorker(ch, client) }

    KlikLogger.i(TAG, "Audio streaming started successfully for user $userId, engine.isRunning=${engine.isRunning()}")
    return@withContext true
  }

  actual fun pauseStreaming() {
    if (_isStreaming.value && !_isPaused.value) {
      _isPaused.value = true
      KlikLogger.i(TAG, "Audio capture paused")
    }
  }

  actual fun resumeStreaming() {
    if (_isStreaming.value && _isPaused.value) {
      _isPaused.value = false
      KlikLogger.i(TAG, "Audio capture resumed")
    }
  }

  actual suspend fun stopStreaming() = withContext(Dispatchers.Main) {
    if (!_isStreaming.value) return@withContext

    // Stop audio engine
    audioEngine?.inputNode?.removeTapOnBus(0u)
    audioEngine?.stop()
    audioEngine = null

    // Flush remaining buffer
    var remainingData: ByteArray? = null
    var pileNum = 0
    pcmBufferLock.lock()
    try {
      if (pcmBuffer.isNotEmpty()) {
        remainingData = ByteArray(pcmBuffer.size).also { arr ->
          for (idx in pcmBuffer.indices) arr[idx] = pcmBuffer[idx]
        }
        pcmBuffer.clear()
        pileCounter++
        pileNum = pileCounter
      }
    } finally {
      pcmBufferLock.unlock()
    }

    remainingData?.let { data ->
      val ch = uploadChannel
      if (ch != null) {
        ch.trySend(DeferredPile(pileNum, data))
      } else {
        enqueueDeferred(DeferredPile(pileNum, data))
      }
    }

    // Close the channel so the worker exits after draining everything in flight.
    uploadChannel?.close()

    // Wait (bounded) for the worker to drain channel + deferred queue.
    val worker = uploadWorkerJob
    withTimeoutOrNull(90_000L) {
      worker?.join()
    }
    KlikLogger.i(TAG, "Drain phase complete, deferred queue size=${deferredSize()}")

    // Deactivate audio session
    memScoped {
      val errorPtr = alloc<ObjCObjectVar<NSError?>>()
      AVAudioSession.sharedInstance().setActive(false, errorPtr.ptr)
    }

    uploadChannel = null
    uploadWorkerJob = null
    streamClient?.stop()
    streamClient = null
    uploadScope?.cancel()
    uploadScope = null
    _isStreaming.value = false
    _isPaused.value = false
    KlikLogger.i(TAG, "Audio streaming stopped, sent $pileCounter piles total")
  }

  /**
   * Sequential WebSocket pipeline. Single coroutine, one frame at a time.
   *
   * Order of operations per cycle:
   *   1. Drain any deferred (previously failed) piles first — they're older.
   *   2. Then take the next fresh pile off the channel.
   *   3. Send via [AudioStreamClient.sendBinary]. If send fails, the WS dropped:
   *      defer the pile, reopen the connection, continue.
   *   4. When the channel is closed (stopStreaming), drain remaining items and
   *      exit so the suspend join() in stopStreaming can return.
   */
  private suspend fun runUploadWorker(channel: Channel<DeferredPile>, initial: AudioStreamClient) {
    var client = initial

    suspend fun ensureConnection(): AudioStreamClient {
      if (client.isOpen) return client
      KlikLogger.w(TAG, "WS reconnecting…")
      try {
        client.stop()
      } catch (e: Throwable) {
        KlikLogger.e(TAG, "WS stop during reconnect failed: ${e.message}", e)
      }
      val fresh = AudioStreamClient(
        baseUrl = ApiConfig.BASE_URL,
        userId = userId,
        recordingMode = "fixed",
        authBearer = { io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser.accessToken },
      )
      fresh.start()
      streamClient = fresh
      client = fresh
      // Brief settle so the upgrade completes before we slam frames into it.
      delay(250L)
      return fresh
    }

    suspend fun send(pile: DeferredPile, isRetry: Boolean): Boolean {
      val c = ensureConnection()
      val ok = c.sendBinary(pile.data)
      if (ok) {
        KlikLogger.i(
          TAG,
          if (isRetry) "Pile #${pile.pileNumber} recovered (${pile.data.size} bytes via WS)"
          else "Pile #${pile.pileNumber} sent (${pile.data.size} bytes via WS)",
        )
      } else {
        enqueueDeferred(pile)
        KlikLogger.w(TAG, "Pile #${pile.pileNumber} deferred — WS send failed (queue size=${deferredSize()})")
      }
      return ok
    }

    while (true) {
      // Drain deferred first so older audio gets sent before newer audio.
      var drained = 0
      while (true) {
        val deferredPile = pollDeferred() ?: break
        val ok = send(deferredPile, isRetry = true)
        if (!ok) {
          delay(1_000L) // brief backoff before next reconnect attempt
          break
        }
        drained++
        if (drained >= 4) break // yield so fresh piles don't starve forever
      }

      val next = try {
        channel.receiveCatching().getOrNull()
      } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
      } catch (e: Throwable) {
        KlikLogger.e(TAG, "Audio upload channel receive failed: ${e.message}", e)
        null
      }
      if (next == null) {
        while (true) {
          val deferredPile = pollDeferred() ?: break
          send(deferredPile, isRetry = true)
        }
        return
      }
      send(next, isRetry = false)
    }
  }

  private fun enqueueDeferred(pile: DeferredPile) {
    deferredLock.lock()
    try {
      while (deferred.size >= DEFERRED_QUEUE_MAX) {
        val dropped = deferred.removeFirst()
        KlikLogger.w(TAG, "Deferred queue full, dropping pile #${dropped.pileNumber}")
      }
      deferred.addLast(pile)
    } finally {
      deferredLock.unlock()
    }
  }

  private fun pollDeferred(): DeferredPile? {
    deferredLock.lock()
    return try {
      if (deferred.isEmpty()) null else deferred.removeFirst()
    } finally {
      deferredLock.unlock()
    }
  }

  private fun deferredSize(): Int {
    deferredLock.lock()
    return try {
      deferred.size
    } finally {
      deferredLock.unlock()
    }
  }
}
