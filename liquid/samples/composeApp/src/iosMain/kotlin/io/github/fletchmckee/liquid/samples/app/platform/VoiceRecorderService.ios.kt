// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlin.coroutines.resume
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVLinearPCMBitDepthKey
import platform.AVFAudio.AVLinearPCMIsBigEndianKey
import platform.AVFAudio.AVLinearPCMIsFloatKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatLinearPCM
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.dataWithContentsOfURL

/**
 * iOS implementation of VoiceRecorderService using AVAudioRecorder.
 * Records audio in WAV format for ASR processing.
 */
actual object VoiceRecorderService {
  private const val TAG = "VoiceRecorderService"

  private val _isRecording = MutableStateFlow(false)
  actual val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

  private var audioRecorder: AVAudioRecorder? = null
  private var recordingUrl: NSURL? = null

  actual suspend fun startRecording(): Boolean = withContext(Dispatchers.Main) {
    if (_isRecording.value) {
      KlikLogger.w(TAG, "Already recording")
      return@withContext false
    }

    if (!hasMicrophonePermission()) {
      KlikLogger.e(TAG, "Microphone permission not granted")
      return@withContext false
    }

    try {
      // Configure audio session
      val session = AVAudioSession.sharedInstance()

      memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        session.setCategory(AVAudioSessionCategoryRecord, errorPtr.ptr)
        val error = errorPtr.value
        if (error != null) {
          KlikLogger.e(TAG, "Failed to set audio session category: ${error.localizedDescription}")
          return@withContext false
        }
      }

      memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        session.setMode(AVAudioSessionModeMeasurement, errorPtr.ptr)
        val error = errorPtr.value
        if (error != null) {
          KlikLogger.e(TAG, "Failed to set audio session mode: ${error.localizedDescription}")
          return@withContext false
        }
      }

      memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        session.setActive(true, errorPtr.ptr)
        val error = errorPtr.value
        if (error != null) {
          KlikLogger.e(TAG, "Failed to activate audio session: ${error.localizedDescription}")
          return@withContext false
        }
      }

      // Create recording URL
      val fileManager = NSFileManager.defaultManager
      val documentsDir = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).firstOrNull() as? NSURL
      if (documentsDir == null) {
        KlikLogger.e(TAG, "Failed to get documents directory")
        return@withContext false
      }
      recordingUrl = documentsDir.URLByAppendingPathComponent("voice_recording.wav")

      // Configure recording settings for WAV format
      val settings = mapOf<Any?, Any?>(
        AVFormatIDKey to kAudioFormatLinearPCM.toLong(),
        AVSampleRateKey to 16000.0,
        AVNumberOfChannelsKey to 1,
        AVLinearPCMBitDepthKey to 16,
        AVLinearPCMIsBigEndianKey to false,
        AVLinearPCMIsFloatKey to false,
        AVEncoderAudioQualityKey to 127L, // Max quality
      )

      // Create recorder
      memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        audioRecorder = AVAudioRecorder(recordingUrl!!, settings, errorPtr.ptr)
        val error = errorPtr.value
        if (error != null || audioRecorder == null) {
          KlikLogger.e(TAG, "Failed to create audio recorder: ${error?.localizedDescription}")
          return@withContext false
        }
      }

      // Start recording
      val prepared = audioRecorder?.prepareToRecord() ?: false
      if (!prepared) {
        KlikLogger.e(TAG, "Failed to prepare audio recorder")
        return@withContext false
      }

      val started = audioRecorder?.record() ?: false
      if (!started) {
        KlikLogger.e(TAG, "Failed to start recording")
        return@withContext false
      }

      _isRecording.value = true
      KlikLogger.i(TAG, "Recording started")
      return@withContext true
    } catch (e: Exception) {
      KlikLogger.e(TAG, "Error starting recording: ${e.message}", e)
      return@withContext false
    }
  }

  actual suspend fun stopRecording(): String? = withContext(Dispatchers.Main) {
    if (!_isRecording.value) {
      KlikLogger.w(TAG, "Not currently recording")
      return@withContext null
    }

    try {
      audioRecorder?.stop()
      _isRecording.value = false
      KlikLogger.i(TAG, "Recording stopped")

      // Deactivate audio session
      memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        AVAudioSession.sharedInstance().setActive(false, errorPtr.ptr)
        // Ignore error on deactivation
      }

      // Read the recorded file and convert to base64
      val url = recordingUrl ?: return@withContext null
      val data = NSData.dataWithContentsOfURL(url)
      if (data == null) {
        KlikLogger.e(TAG, "Failed to read recorded audio file")
        return@withContext null
      }

      val base64 = data.base64EncodedStringWithOptions(0u)
      KlikLogger.i(TAG, "Audio converted to base64, length: ${base64.length}")

      // Clean up
      NSFileManager.defaultManager.removeItemAtURL(url, null)
      audioRecorder = null
      recordingUrl = null

      return@withContext base64
    } catch (e: Exception) {
      KlikLogger.e(TAG, "Error stopping recording: ${e.message}", e)
      _isRecording.value = false
      return@withContext null
    }
  }

  actual fun hasMicrophonePermission(): Boolean = AVAudioSession.sharedInstance().recordPermission == AVAudioSessionRecordPermissionGranted

  actual suspend fun requestMicrophonePermission(): Boolean = suspendCancellableCoroutine { cont ->
    AVAudioSession.sharedInstance().requestRecordPermission { granted ->
      KlikLogger.i(TAG, "Microphone permission ${if (granted) "granted" else "denied"}")
      cont.resume(granted)
    }
  }
}
