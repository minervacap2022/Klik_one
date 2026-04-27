// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.network

/**
 * Network client interface for making HTTP requests.
 *
 * This provides an abstraction over the actual HTTP client implementation.
 * When Ktor is enabled, implement this using KtorNetworkClient.
 *
 * Usage:
 * ```
 * // When Ktor dependencies are enabled, create the actual implementation:
 * class KtorNetworkClient(
 *     private val httpClient: HttpClient
 * ) : NetworkClient {
 *     override suspend fun <T> get(
 *         endpoint: String,
 *         params: Map<String, String>,
 *         deserializer: (String) -> T
 *     ): ApiResponse<T> {
 *         return try {
 *             val response = httpClient.get(ApiConfig.BASE_URL + endpoint) {
 *                 params.forEach { (key, value) ->
 *                     parameter(key, value)
 *                 }
 *             }
 *             ApiResponse.Success(deserializer(response.bodyAsText()))
 *         } catch (e: Exception) {
 *             ApiResponse.Error(500, e.message ?: "Unknown error")
 *         }
 *     }
 *     // ... other methods
 * }
 * ```
 */
interface NetworkClient {
  /**
   * Perform a GET request
   */
  suspend fun <T> get(
    endpoint: String,
    params: Map<String, String> = emptyMap(),
    deserializer: (String) -> T,
  ): ApiResponse<T>

  /**
   * Perform a POST request
   */
  suspend fun <T> post(
    endpoint: String,
    body: String,
    deserializer: (String) -> T,
  ): ApiResponse<T>

  /**
   * Perform a PUT request
   */
  suspend fun <T> put(
    endpoint: String,
    body: String,
    deserializer: (String) -> T,
  ): ApiResponse<T>

  /**
   * Perform a PATCH request
   */
  suspend fun <T> patch(
    endpoint: String,
    body: String,
    deserializer: (String) -> T,
  ): ApiResponse<T>

  /**
   * Perform a DELETE request
   */
  suspend fun <T> delete(
    endpoint: String,
    deserializer: (String) -> T,
  ): ApiResponse<T>
}

/**
 * Stub implementation that returns errors - used when network is not available.
 * Replace with KtorNetworkClient when Ktor dependencies are enabled.
 */
class StubNetworkClient : NetworkClient {
  override suspend fun <T> get(
    endpoint: String,
    params: Map<String, String>,
    deserializer: (String) -> T,
  ): ApiResponse<T> = ApiResponse.Error(
    code = 503,
    message = "Network client not configured. StubNetworkClient cannot make requests.",
  )

  override suspend fun <T> post(
    endpoint: String,
    body: String,
    deserializer: (String) -> T,
  ): ApiResponse<T> = ApiResponse.Error(
    code = 503,
    message = "Network client not configured. StubNetworkClient cannot make requests.",
  )

  override suspend fun <T> put(
    endpoint: String,
    body: String,
    deserializer: (String) -> T,
  ): ApiResponse<T> = ApiResponse.Error(
    code = 503,
    message = "Network client not configured. StubNetworkClient cannot make requests.",
  )

  override suspend fun <T> patch(
    endpoint: String,
    body: String,
    deserializer: (String) -> T,
  ): ApiResponse<T> = ApiResponse.Error(
    code = 503,
    message = "Network client not configured. StubNetworkClient cannot make requests.",
  )

  override suspend fun <T> delete(
    endpoint: String,
    deserializer: (String) -> T,
  ): ApiResponse<T> = ApiResponse.Error(
    code = 503,
    message = "Network client not configured. StubNetworkClient cannot make requests.",
  )
}

/**
 * Network exception types
 */
sealed class NetworkException(message: String) : Exception(message) {
  class ConnectionError(message: String = "Unable to connect to server") : NetworkException(message)
  class TimeoutError(message: String = "Request timed out") : NetworkException(message)
  class AuthenticationError(message: String = "Authentication failed") : NetworkException(message)
  class ServerError(code: Int, message: String) : NetworkException("Server error ($code): $message")
  class ParseError(message: String = "Failed to parse response") : NetworkException(message)
  class UnknownError(message: String = "Unknown error occurred") : NetworkException(message)
}
