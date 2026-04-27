// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.core

import io.github.fletchmckee.liquid.samples.app.data.network.NetworkException
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

/**
 * Centralized error handler that maps exceptions to user-friendly messages.
 * This provides consistent error messaging across the app.
 */
object ErrorHandler {

  /**
   * Get a user-friendly error message from an exception
   */
  fun getErrorMessage(throwable: Throwable?): String {
    if (throwable == null) return "An unexpected error occurred"

    return when (throwable) {
      is NetworkException.ConnectionError -> "Unable to connect to the server. Please check your internet connection."
      is NetworkException.TimeoutError -> "The request took too long. Please try again."
      is NetworkException.AuthenticationError -> "Your session has expired. Please log in again."
      is NetworkException.ServerError -> "Server error. Please try again later."
      is NetworkException.ParseError -> "We couldn't process the server response. Please try again."
      is NetworkException.UnknownError -> "An unexpected error occurred. Please try again."
      is IllegalStateException -> throwable.message ?: "Invalid application state"
      is IllegalArgumentException -> throwable.message ?: "Invalid input provided"
      is NoSuchElementException -> throwable.message ?: "The requested item was not found"
      is AppError.PermissionError -> "You don't have permission to perform this action"
      else -> throwable.message ?: "An unexpected error occurred"
    }
  }

  /**
   * Determine if the error is recoverable (user can retry)
   */
  fun isRecoverable(throwable: Throwable?): Boolean = when (throwable) {
    is NetworkException.ConnectionError -> true
    is NetworkException.TimeoutError -> true
    is NetworkException.ServerError -> true
    else -> true // Default to recoverable
  }

  /**
   * Determine if the error requires user authentication
   */
  fun requiresAuthentication(throwable: Throwable?): Boolean = when (throwable) {
    is NetworkException.AuthenticationError -> true
    else -> false
  }

  /**
   * Log error for debugging/analytics
   * In production, this would send to crash reporting service
   */
  fun logError(throwable: Throwable?, context: String? = null) {
    val tag = context ?: "ErrorHandler"
    val message = buildString {
      append(throwable?.let { it::class.simpleName } ?: "Unknown")
      append(": ")
      append(throwable?.message ?: "No message")
    }
    KlikLogger.e(tag, message, throwable)
  }
}

/**
 * Domain-specific error types for better error categorization
 */
sealed class AppError(message: String, cause: Throwable? = null) : Exception(message, cause) {
  // Network errors
  class NetworkError(message: String = "Network error", cause: Throwable? = null) : AppError(message, cause)
  class TimeoutError(message: String = "Request timed out") : AppError(message)

  // Authentication errors
  class AuthError(message: String = "Authentication required") : AppError(message)
  class PermissionError(message: String = "Permission denied") : AppError(message)

  // Data errors
  class NotFoundError(message: String = "Item not found") : AppError(message)
  class ValidationError(message: String = "Invalid data") : AppError(message)
  class DataParseError(message: String = "Failed to parse data", cause: Throwable? = null) : AppError(message, cause)

  // Business logic errors
  class BusinessError(message: String) : AppError(message)
  class ConcurrencyError(message: String = "Data was modified by another process") : AppError(message)

  // Generic errors
  class UnknownError(message: String = "An unexpected error occurred", cause: Throwable? = null) : AppError(message, cause)
}

/**
 * Extension function to convert Result error to user-friendly message
 */
fun <T> Result<T>.getErrorMessage(): String? = when (this) {
  is Result.Error -> ErrorHandler.getErrorMessage(exception)
  else -> null
}

/**
 * Extension function to handle Result with error handling
 */
inline fun <T, R> Result<T>.fold(
  onSuccess: (T) -> R,
  onError: (String) -> R,
  onLoading: () -> R,
): R = when (this) {
  is Result.Success -> onSuccess(data)
  is Result.Error -> onError(ErrorHandler.getErrorMessage(exception))
  is Result.Loading -> onLoading()
}
