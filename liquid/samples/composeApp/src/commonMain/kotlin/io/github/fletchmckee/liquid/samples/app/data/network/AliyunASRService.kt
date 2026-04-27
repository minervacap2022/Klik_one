// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.network

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Service for speech recognition via the backend ASR proxy.
 * The backend holds the Aliyun DashScope API key and proxies transcription requests.
 * Client never holds third-party API secrets.
 */
object AliyunASRService {
  private const val TAG = "AliyunASRService"

  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }

  /**
   * Transcribe audio via the backend ASR proxy endpoint.
   *
   * @param audioBase64 Base64 encoded audio data (WAV format)
   * @return Transcribed text
   * @throws Exception if transcription fails
   */
  suspend fun transcribe(audioBase64: String): String {
    KlikLogger.i(TAG, "Starting ASR transcription via backend proxy, audio length: ${audioBase64.length}")

    val requestBody = json.encodeToString(ASRProxyRequest(audio_base64 = audioBase64))

    val url = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.AUDIO_TRANSCRIBE}"
    val response = HttpClient.post(url, requestBody)
      ?: throw Exception("Backend ASR proxy returned null response")

    KlikLogger.d(TAG, "ASR response: $response")

    val asrResponse = json.decodeFromString<ASRProxyResponse>(response)
    val text = asrResponse.text
      ?: throw Exception("No transcription text in ASR response")

    KlikLogger.i(TAG, "Transcription successful: $text")
    return text
  }
}

@Serializable
private data class ASRProxyRequest(
  val audio_base64: String,
)

@Serializable
private data class ASRProxyResponse(
  val text: String? = null,
  val request_id: String? = null,
)
