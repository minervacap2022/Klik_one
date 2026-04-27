// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.logging

const val LOG_BUFFER_CAPACITY = 500

/**
 * In-memory ring buffer for recent log entries.
 * Stores the most recent [capacity] entries for debugging and export.
 * When full, oldest entries are overwritten.
 */
class LogBuffer(private val capacity: Int = LOG_BUFFER_CAPACITY) {
  private val entries = arrayOfNulls<LogEntry>(capacity)
  private var writeIndex = 0
  private var count = 0

  fun add(entry: LogEntry) {
    entries[writeIndex] = entry
    writeIndex = (writeIndex + 1) % capacity
    if (count < capacity) count++
  }

  /** Get all buffered entries in chronological order. */
  fun getAll(): List<LogEntry> {
    if (count == 0) return emptyList()
    val result = mutableListOf<LogEntry>()
    val start = if (count < capacity) 0 else writeIndex
    for (i in 0 until count) {
      val index = (start + i) % capacity
      entries[index]?.let { result.add(it) }
    }
    return result
  }

  /** Get entries at or above the given severity. */
  fun getByLevel(minLevel: LogLevel): List<LogEntry> = getAll().filter { entry ->
    LogLevel.entries.first { it.label == entry.level }.value >= minLevel.value
  }

  /** Get entries for a specific tag. */
  fun getByTag(tag: String): List<LogEntry> = getAll().filter { it.tag == tag }

  fun clear() {
    entries.fill(null)
    writeIndex = 0
    count = 0
  }

  val size: Int get() = count
}
