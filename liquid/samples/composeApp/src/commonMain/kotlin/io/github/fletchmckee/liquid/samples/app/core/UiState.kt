// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.core

/**
 * Represents the UI state for any screen.
 * Provides a consistent way to handle loading, success, error, and empty states.
 */
sealed class UiState<out T> {
  data object Initial : UiState<Nothing>()
  data object Loading : UiState<Nothing>()
  data class Success<T>(val data: T) : UiState<T>()
  data class Error(val message: String, val exception: Throwable? = null) : UiState<Nothing>()
  data object Empty : UiState<Nothing>()

  val isLoading: Boolean get() = this is Loading
  val isSuccess: Boolean get() = this is Success
  val isError: Boolean get() = this is Error
  val isEmpty: Boolean get() = this is Empty

  fun getOrNull(): T? = (this as? Success)?.data

  fun <R> map(transform: (T) -> R): UiState<R> = when (this) {
    is Initial -> Initial
    is Loading -> Loading
    is Success -> Success(transform(data))
    is Error -> Error(message, exception)
    is Empty -> Empty
  }

  companion object {
    fun <T> fromResult(result: Result<T>): UiState<T> = when (result) {
      is Result.Success -> Success(result.data)
      is Result.Error -> Error(result.message ?: "Unknown error", result.exception)
      is Result.Loading -> Loading
    }

    fun <T> fromResult(result: Result<T>, emptyCheck: (T) -> Boolean): UiState<T> = when (result) {
      is Result.Success -> if (emptyCheck(result.data)) Empty else Success(result.data)
      is Result.Error -> Error(result.message ?: "Unknown error", result.exception)
      is Result.Loading -> Loading
    }
  }
}

/**
 * Extension to convert Result to UiState
 */
fun <T> Result<T>.toUiState(): UiState<T> = UiState.fromResult(this)

/**
 * Extension to convert Result to UiState with empty check
 */
fun <T> Result<T>.toUiState(emptyCheck: (T) -> Boolean): UiState<T> = UiState.fromResult(this, emptyCheck)
