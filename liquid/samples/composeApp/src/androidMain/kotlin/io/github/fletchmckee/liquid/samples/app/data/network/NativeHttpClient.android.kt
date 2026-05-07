// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.network

import android.util.Base64
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal actual class NativeHttpClient actual constructor() {

  /**
   * Read the body for [responseCode] off [connection], using the input stream for 2xx
   * and the error stream for everything else. Returns null if no readable body is available.
   */
  private fun readBody(connection: HttpURLConnection, responseCode: Int): String? = try {
    val stream = if (responseCode in 200..299) {
      connection.inputStream
    } else {
      // errorStream is the canonical body source on non-2xx; inputStream is the
      // documented fallback for servers that return an error code with the body
      // on the success channel. The outer try/catch handles either throwing.
      connection.errorStream ?: connection.inputStream
    }
    if (stream == null) {
      null
    } else {
      BufferedReader(InputStreamReader(stream)).use { it.readText() }
    }
  } catch (e: Exception) {
    KlikLogger.w("HTTP", "Failed to read body for status=$responseCode: ${e.message}")
    null
  }

  actual suspend fun get(url: String, headers: Map<String, String>): NativeHttpResponse =
    withContext(Dispatchers.IO) {
      var connection: HttpURLConnection? = null
      try {
        connection = (URL(url).openConnection() as HttpURLConnection).apply {
          requestMethod = "GET"
          headers.forEach { (key, value) -> setRequestProperty(key, value) }
        }
        val responseCode = connection.responseCode
        KlikLogger.d("HTTP", "GET $url -> $responseCode")
        NativeHttpResponse(responseCode, readBody(connection, responseCode))
      } catch (e: Exception) {
        KlikLogger.e("HTTP", "Error: ${e.message}", e)
        NativeHttpResponse(0, null)
      } finally {
        connection?.disconnect()
      }
    }

  actual suspend fun post(url: String, body: String, headers: Map<String, String>): NativeHttpResponse =
    withContext(Dispatchers.IO) {
      var connection: HttpURLConnection? = null
      try {
        connection = (URL(url).openConnection() as HttpURLConnection).apply {
          requestMethod = "POST"
          doOutput = true
          headers.forEach { (key, value) -> setRequestProperty(key, value) }
        }
        OutputStreamWriter(connection.outputStream).use {
          it.write(body)
          it.flush()
        }
        val responseCode = connection.responseCode
        KlikLogger.d("HTTP", "POST $url -> $responseCode")
        NativeHttpResponse(responseCode, readBody(connection, responseCode))
      } catch (e: Exception) {
        KlikLogger.e("HTTP", "POST error: ${e.message}", e)
        NativeHttpResponse(0, null)
      } finally {
        connection?.disconnect()
      }
    }

  actual suspend fun put(url: String, body: String, headers: Map<String, String>): NativeHttpResponse =
    withContext(Dispatchers.IO) {
      var connection: HttpURLConnection? = null
      try {
        connection = (URL(url).openConnection() as HttpURLConnection).apply {
          requestMethod = "PUT"
          doOutput = true
          headers.forEach { (key, value) -> setRequestProperty(key, value) }
        }
        OutputStreamWriter(connection.outputStream).use {
          it.write(body)
          it.flush()
        }
        val responseCode = connection.responseCode
        KlikLogger.d("HTTP", "PUT $url -> $responseCode")
        NativeHttpResponse(responseCode, readBody(connection, responseCode))
      } catch (e: Exception) {
        KlikLogger.e("HTTP", "Error: ${e.message}", e)
        NativeHttpResponse(0, null)
      } finally {
        connection?.disconnect()
      }
    }

  actual suspend fun patch(url: String, body: String, headers: Map<String, String>): NativeHttpResponse =
    withContext(Dispatchers.IO) {
      var connection: HttpURLConnection? = null
      try {
        // HttpURLConnection rejects PATCH directly; tunnel via X-HTTP-Method-Override.
        connection = (URL(url).openConnection() as HttpURLConnection).apply {
          requestMethod = "POST"
          setRequestProperty("X-HTTP-Method-Override", "PATCH")
          doOutput = true
          headers.forEach { (key, value) -> setRequestProperty(key, value) }
        }
        OutputStreamWriter(connection.outputStream).use {
          it.write(body)
          it.flush()
        }
        val responseCode = connection.responseCode
        KlikLogger.d("HTTP", "PATCH $url -> $responseCode")
        NativeHttpResponse(responseCode, readBody(connection, responseCode))
      } catch (e: Exception) {
        KlikLogger.e("HTTP", "Error: ${e.message}", e)
        NativeHttpResponse(0, null)
      } finally {
        connection?.disconnect()
      }
    }

  actual suspend fun delete(url: String, headers: Map<String, String>, body: String?): NativeHttpResponse =
    withContext(Dispatchers.IO) {
      var connection: HttpURLConnection? = null
      try {
        connection = (URL(url).openConnection() as HttpURLConnection).apply {
          requestMethod = "DELETE"
          headers.forEach { (key, value) -> setRequestProperty(key, value) }
          if (body != null) {
            doOutput = true
          }
        }
        if (body != null) {
          connection.outputStream.bufferedWriter().use { it.write(body) }
        }
        val responseCode = connection.responseCode
        KlikLogger.d("HTTP", "DELETE $url -> $responseCode")
        NativeHttpResponse(responseCode, readBody(connection, responseCode))
      } catch (e: Exception) {
        KlikLogger.e("HTTP", "Error: ${e.message}", e)
        NativeHttpResponse(0, null)
      } finally {
        connection?.disconnect()
      }
    }

  actual suspend fun postMultipart(
    url: String,
    fileData: ByteArray,
    fileName: String,
    fieldName: String,
    headers: Map<String, String>,
  ): NativeHttpResponse = withContext(Dispatchers.IO) {
    var connection: HttpURLConnection? = null
    try {
      val boundary = "Boundary-${java.util.UUID.randomUUID()}"
      connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        doOutput = true
        setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        headers.forEach { (key, value) -> setRequestProperty(key, value) }
      }
      val lineBreak = "\r\n"
      val headerPart = "--$boundary$lineBreak" +
        "Content-Disposition: form-data; name=\"$fieldName\"; filename=\"$fileName\"$lineBreak" +
        "Content-Type: application/octet-stream$lineBreak$lineBreak"
      val footer = "$lineBreak--$boundary--$lineBreak"
      connection.outputStream.use { os ->
        os.write(headerPart.toByteArray(Charsets.UTF_8))
        os.write(fileData)
        os.write(footer.toByteArray(Charsets.UTF_8))
        os.flush()
      }
      val responseCode = connection.responseCode
      KlikLogger.d("HTTP", "POST (multipart) $url -> $responseCode")
      NativeHttpResponse(responseCode, readBody(connection, responseCode))
    } catch (e: Exception) {
      KlikLogger.e("HTTP", "Multipart error: ${e.message}", e)
      NativeHttpResponse(0, null)
    } finally {
      connection?.disconnect()
    }
  }
}

/**
 * Android implementation of base64 decoding using android.util.Base64.
 */
actual fun decodeBase64Platform(base64: String): String {
  val bytes = Base64.decode(base64, Base64.DEFAULT)
  return String(bytes, Charsets.UTF_8)
}
