@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.data.network.HttpClient
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.setActive
import platform.Foundation.NSError

/**
 * iOS implementation of FixedSessionAudioStreamer using AVAudioEngine.
 * Captures real-time audio via input node tap, converts to Int16 PCM,
 * and uploads chunks to the backend streaming endpoint.
 */
actual object FixedSessionAudioStreamer {
    private const val TAG = "FixedSessionAudioStreamer"
    // Upload when buffer reaches ~1MB
    private const val UPLOAD_THRESHOLD_BYTES = 1 * 1024 * 1024

    private val _isStreaming = MutableStateFlow(false)
    actual val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private var audioEngine: AVAudioEngine? = null
    private val pcmBufferLock = platform.Foundation.NSLock()
    private var pcmBuffer = mutableListOf<Byte>()
    private var userId: String = ""
    private var uploadScope: CoroutineScope? = null
    private var pileCounter = 0

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
            session.setMode(AVAudioSessionModeMeasurement, errorPtr.ptr)
            if (errorPtr.value != null) {
                KlikLogger.e(TAG, "FAIL at setMode: code=${errorPtr.value?.code}, domain=${errorPtr.value?.domain}, desc=${errorPtr.value?.localizedDescription}")
                return@withContext false
            }
            KlikLogger.i(TAG, "setMode(Measurement) OK")
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
        val inputFormat = inputNode.outputFormatForBus(0u)

        KlikLogger.i(TAG, "Input format: sampleRate=${inputFormat.sampleRate}Hz, channels=${inputFormat.channelCount}, interleaved=${inputFormat.isInterleaved()}, commonFormat=${inputFormat.commonFormat}")

        if (inputFormat.sampleRate == 0.0 || inputFormat.channelCount == 0u) {
            KlikLogger.e(TAG, "FAIL: Invalid input format (sampleRate=${inputFormat.sampleRate}, channels=${inputFormat.channelCount})")
            return@withContext false
        }

        // Create upload scope for background uploads
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        uploadScope = scope

        KlikLogger.i(TAG, "Installing tap on input node bus 0...")

        // Install tap to receive audio buffers
        var tapCallCount = 0
        inputNode.installTapOnBus(
            bus = 0u,
            bufferSize = 4096u,
            format = inputFormat
        ) { buffer, _ ->
            if (buffer == null) {
                KlikLogger.w(TAG, "Tap received null buffer")
                return@installTapOnBus
            }
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
                scope.launch {
                    uploadAudioChunk(data, pileNum)
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
        KlikLogger.i(TAG, "Audio streaming started successfully for user $userId, engine.isRunning=${engine.isRunning()}")
        return@withContext true
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
            withContext(Dispatchers.Default) {
                uploadAudioChunk(data, pileNum)
            }
        }

        // Deactivate audio session
        memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            AVAudioSession.sharedInstance().setActive(false, errorPtr.ptr)
        }

        uploadScope?.cancel()
        uploadScope = null
        _isStreaming.value = false
        KlikLogger.i(TAG, "Audio streaming stopped, uploaded $pileCounter piles total")
    }

    private suspend fun uploadAudioChunk(data: ByteArray, pileNumber: Int) {
        val url = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.AUDIO_STREAM}"
        val headers = mapOf(
            ApiConfig.Headers.USER_ID to userId,
            ApiConfig.Headers.RECORDING_MODE to "fixed"
        )
        KlikLogger.d(TAG, "Uploading pile #$pileNumber: ${data.size} bytes to $url")

        val response = HttpClient.postMultipartUrl(
            url = url,
            fileData = data,
            fileName = "audio_pile_$pileNumber.pcm",
            fieldName = "file",
            headers = headers
        )

        if (response != null && !response.contains("\"detail\"")) {
            KlikLogger.i(TAG, "Pile #$pileNumber uploaded successfully (${data.size} bytes)")
        } else {
            KlikLogger.e(TAG, "Pile #$pileNumber upload failed: ${response ?: "no response"}")
        }
    }
}
