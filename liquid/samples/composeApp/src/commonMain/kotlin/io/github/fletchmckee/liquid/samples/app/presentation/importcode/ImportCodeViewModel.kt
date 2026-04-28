// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.presentation.importcode

import io.github.fletchmckee.liquid.samples.app.core.SimpleViewModel
import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.data.network.HttpClient
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class ImportCodeUiState(
  val isLoading: Boolean = false,
  val code: String? = null,
  val secondsRemaining: Int = 0,
  val error: String? = null,
)

@Serializable
private data class GenerateResponse(
  val code: String? = null,
  val expires_at: String? = null,
  val ttl_seconds: Int = 0,
)

private val json = Json {
  ignoreUnknownKeys = true
  isLenient = true
  coerceInputValues = true
}

private const val IMPORT_CODE_GENERATE_PATH = "/api/v1/auth/import-code/generate"

class ImportCodeViewModel : SimpleViewModel<ImportCodeUiState>() {
  override val initialState = ImportCodeUiState()

  fun generateCode() {
    if (currentState.isLoading) return
    updateState { copy(isLoading = true, error = null) }
    launch {
      try {
        val url = "${ApiConfig.WEB_BASE_URL}$IMPORT_CODE_GENERATE_PATH"
        val response = HttpClient.postUrl(url, body = "{}")
          ?: throw IllegalStateException("No response from server")
        val parsed = json.decodeFromString(GenerateResponse.serializer(), response)
        val code = parsed.code
        if (code.isNullOrBlank() || parsed.ttl_seconds <= 0) {
          throw IllegalStateException("Server returned no code")
        }
        KlikLogger.i("ImportCode", "Generated code (ttl ${parsed.ttl_seconds}s)")
        updateState {
          copy(isLoading = false, code = code, secondsRemaining = parsed.ttl_seconds, error = null)
        }
        runCountdown()
      } catch (e: Exception) {
        KlikLogger.e("ImportCode", "Generate failed: ${e.message}")
        updateState {
          copy(isLoading = false, error = e.message ?: "Could not generate code")
        }
      }
    }
  }

  private fun runCountdown() {
    launch {
      while (isActive && currentState.secondsRemaining > 0) {
        delay(1_000)
        updateState { copy(secondsRemaining = (secondsRemaining - 1).coerceAtLeast(0)) }
      }
      if (currentState.secondsRemaining == 0) {
        updateState { copy(code = null) }
      }
    }
  }

  fun clear() {
    updateState { ImportCodeUiState() }
  }
}

fun formatImportCode(raw: String): String =
  if (raw.length == 6) "${raw.substring(0, 3)} ${raw.substring(3)}" else raw

fun formatCountdown(seconds: Int): String {
  val s = seconds.coerceAtLeast(0)
  val mm = (s / 60).toString().padStart(2, '0')
  val ss = (s % 60).toString().padStart(2, '0')
  return "$mm:$ss"
}
