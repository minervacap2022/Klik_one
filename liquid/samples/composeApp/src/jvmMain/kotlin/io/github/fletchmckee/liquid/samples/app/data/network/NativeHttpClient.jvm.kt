// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.network

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal actual class NativeHttpClient actual constructor() {

  actual suspend fun get(url: String, headers: Map<String, String>): String? {
    return withContext(Dispatchers.IO) {
      try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        headers.forEach { (key, value) ->
          connection.setRequestProperty(key, value)
        }

        val responseCode = connection.responseCode
        KlikLogger.d("HTTP", "GET $url -> $responseCode")

        if (responseCode in 500..599) {
          KlikLogger.e("HTTP", "Server error $responseCode for GET $url")
          connection.disconnect()
          return@withContext null
        }

        val inputStream = if (responseCode in 200..299) {
          connection.inputStream
        } else {
          connection.errorStream
        }

        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = reader.readText()
        reader.close()
        connection.disconnect()

        KlikLogger.d("HTTP", "Response: ${response.take(200)}...")
        response
      } catch (e: Exception) {
        KlikLogger.e("HTTP", "Error: ${e.message}", e)
        null
      }
    }
  }

  actual suspend fun post(url: String, body: String, headers: Map<String, String>): String? {
    return withContext(Dispatchers.IO) {
      try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true

        headers.forEach { (key, value) ->
          connection.setRequestProperty(key, value)
        }

        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(body)
        writer.flush()
        writer.close()

        val responseCode = connection.responseCode
        KlikLogger.d("HTTP", "POST $url -> $responseCode")

        if (responseCode in 500..599) {
          KlikLogger.e("HTTP", "Server error $responseCode for POST $url")
          connection.disconnect()
          return@withContext null
        }

        val inputStream = if (responseCode in 200..299) {
          connection.inputStream
        } else {
          connection.errorStream
        }

        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = reader.readText()
        reader.close()
        connection.disconnect()

        response
      } catch (e: Exception) {
        KlikLogger.e("HTTP", "Error: ${e.message}", e)
        null
      }
    }
  }

  actual suspend fun put(url: String, body: String, headers: Map<String, String>): String? {
    return withContext(Dispatchers.IO) {
      try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.doOutput = true

        headers.forEach { (key, value) ->
          connection.setRequestProperty(key, value)
        }

        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(body)
        writer.flush()
        writer.close()

        val responseCode = connection.responseCode
        KlikLogger.d("HTTP", "PUT $url -> $responseCode")

        if (responseCode in 500..599) {
          KlikLogger.e("HTTP", "Server error $responseCode for PUT $url")
          connection.disconnect()
          return@withContext null
        }

        val inputStream = if (responseCode in 200..299) {
          connection.inputStream
        } else {
          connection.errorStream
        }

        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = reader.readText()
        reader.close()
        connection.disconnect()

        response
      } catch (e: Exception) {
        KlikLogger.e("HTTP", "Error: ${e.message}", e)
        null
      }
    }
  }

  actual suspend fun delete(url: String, headers: Map<String, String>, body: String?): String? {
    return withContext(Dispatchers.IO) {
      try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"

        headers.forEach { (key, value) ->
          connection.setRequestProperty(key, value)
        }

        if (body != null) {
          connection.doOutput = true
          connection.outputStream.bufferedWriter().use { it.write(body) }
        }

        val responseCode = connection.responseCode
        KlikLogger.d("HTTP", "DELETE $url -> $responseCode")

        if (responseCode in 500..599) {
          KlikLogger.e("HTTP", "Server error $responseCode for DELETE $url")
          connection.disconnect()
          return@withContext null
        }

        val inputStream = if (responseCode in 200..299) {
          connection.inputStream
        } else {
          connection.errorStream
        }

        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = reader.readText()
        reader.close()
        connection.disconnect()

        KlikLogger.d("HTTP", "Response: ${response.take(200)}...")
        response
      } catch (e: Exception) {
        KlikLogger.e("HTTP", "Error: ${e.message}", e)
        null
      }
    }
  }

  actual suspend fun postMultipart(
    url: String,
    fileData: ByteArray,
    fileName: String,
    fieldName: String,
    headers: Map<String, String>,
  ): String? = withContext(Dispatchers.IO) {
    try {
      val boundary = "Boundary-${java.util.UUID.randomUUID()}"
      val connection = URL(url).openConnection() as HttpURLConnection
      connection.requestMethod = "POST"
      connection.doOutput = true
      connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
      headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

      val outputStream = connection.outputStream
      val lineBreak = "\r\n"

      val headerPart = "--$boundary$lineBreak" +
        "Content-Disposition: form-data; name=\"$fieldName\"; filename=\"$fileName\"$lineBreak" +
        "Content-Type: application/octet-stream$lineBreak$lineBreak"
      outputStream.write(headerPart.toByteArray(Charsets.UTF_8))
      outputStream.write(fileData)
      outputStream.write("$lineBreak--$boundary--$lineBreak".toByteArray(Charsets.UTF_8))
      outputStream.flush()
      outputStream.close()

      val responseCode = connection.responseCode
      KlikLogger.d("HTTP", "POST (multipart) $url -> $responseCode")

      val inputStream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
      val reader = BufferedReader(InputStreamReader(inputStream))
      val response = reader.readText()
      reader.close()
      connection.disconnect()

      response
    } catch (e: Exception) {
      KlikLogger.e("HTTP", "Multipart error: ${e.message}", e)
      null
    }
  }
}

/**
 * JVM implementation of base64 decoding using java.util.Base64.
 */
actual fun decodeBase64Platform(base64: String): String = try {
  val bytes = Base64.getDecoder().decode(base64)
  String(bytes, Charsets.UTF_8)
} catch (e: Exception) {
  ""
}
