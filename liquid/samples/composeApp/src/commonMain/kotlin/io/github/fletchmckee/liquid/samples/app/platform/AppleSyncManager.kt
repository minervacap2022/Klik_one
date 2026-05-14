// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser
import io.github.fletchmckee.liquid.samples.app.data.storage.AppleIntegrationStorageKeys
import io.github.fletchmckee.liquid.samples.app.data.storage.SecureStorage
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.MeetingSource
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Manages automatic sync of meetings to Apple Calendar and tasks/todos to Apple Reminders.
 * Deduplicates using JSON-serialized sets of synced entity IDs stored in SecureStorage,
 * scoped per userId so sync state survives token refresh and is naturally isolated
 * across accounts without requiring an explicit clear on logout.
 *
 * Sync work is dispatched to a background IO scope so callers never block the main thread.
 */
object AppleSyncManager {

  private const val TAG = "AppleSyncManager"
  private val storage = SecureStorage()
  private val json = Json { ignoreUnknownKeys = true }

  // Background scope for all EventKit I/O — keeps main thread free so Today screen
  // renders immediately after data state is set.
  private val syncScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  // One mutex per sync type: serializes concurrent launches from initial load + foreground
  // refresh so the dedup set is always fully written before the next sync reads it.
  private val calendarMutex = Mutex()
  private val reminderMutex = Mutex()

  // Sync dedup keys are scoped by userId so they survive token refresh/re-login for
  // the same account and are naturally isolated when a different user logs in.
  private val calendarKey: String
    get() {
      val uid = CurrentUser.userId
      return if (uid != null) "${AppleIntegrationStorageKeys.SYNCED_CALENDAR_IDS}_$uid"
      else AppleIntegrationStorageKeys.SYNCED_CALENDAR_IDS
    }
  private val reminderKey: String
    get() {
      val uid = CurrentUser.userId
      return if (uid != null) "${AppleIntegrationStorageKeys.SYNCED_REMINDER_TODO_IDS}_$uid"
      else AppleIntegrationStorageKeys.SYNCED_REMINDER_TODO_IDS
    }

  // ==================== Meetings → Apple Calendar ====================

  /**
   * Sync meetings to Apple Calendar. Returns immediately; sync runs on a background thread.
   * Only syncs new meetings that haven't been synced before (deduplication by userId-scoped key).
   * No-ops on non-iOS platforms or when calendar permission is not granted.
   */
  fun syncMeetingsToCalendar(meetings: List<Meeting>) {
    if (!AppleIntegrationService.isSupported()) return
    if (meetings.isEmpty()) return
    syncScope.launch {
      syncMeetingsInternal(meetings)
    }
  }

  private suspend fun syncMeetingsInternal(meetings: List<Meeting>) = calendarMutex.withLock {
    val permission = AppleIntegrationService.checkCalendarPermission()
    if (permission != ApplePermissionStatus.GRANTED) {
      KlikLogger.d(TAG, "Calendar permission not granted ($permission), skipping meeting sync")
      return@withLock
    }

    val key = calendarKey
    val syncedIds = loadSyncedIds(key)
    val newMeetings = meetings.filter { it.id !in syncedIds }

    if (newMeetings.isEmpty()) {
      KlikLogger.d(TAG, "No new meetings to sync (${meetings.size} already synced)")
      return@withLock
    }

    KlikLogger.i(TAG, "Syncing ${newMeetings.size} new meetings to Apple Calendar")

    // Mark all IDs as synced upfront so concurrent calls (initial load + foreground refresh)
    // see the full set immediately and don't re-sync the same meetings.
    saveSyncedIds(key, syncedIds + newMeetings.map { it.id })

    for (meeting in newMeetings) {
      val eventData = meetingToCalendarEvent(meeting) ?: continue

      AppleIntegrationService.saveCalendarEvent(eventData) { result ->
        when (result) {
          is AppleSaveResult.Success -> {
            KlikLogger.i(TAG, "Meeting synced to Calendar: id=${meeting.id}, title=${meeting.title}, calendarId=${result.identifier}")
          }

          is AppleSaveResult.Error -> {
            KlikLogger.e(TAG, "Failed to sync meeting ${meeting.id}: ${result.message}")
          }

          is AppleSaveResult.PermissionDenied -> {
            KlikLogger.e(TAG, "Calendar permission denied during sync of meeting ${meeting.id}")
          }

          is AppleSaveResult.NotSupported -> {
            KlikLogger.e(TAG, "Calendar not supported during sync of meeting ${meeting.id}")
          }
        }
      }
    }
  }

  // ==================== KK_exec Todos → Apple Reminders ====================

  /**
   * Sync KK_exec todos (as TaskMetadata) to Apple Reminders. Returns immediately;
   * sync runs on a background thread. Only syncs new todos (deduplication by userId-scoped key).
   * Syncs ALL statuses (PENDING, IN_PROGRESS, COMPLETED, CANCELLED, FAILED).
   */
  fun syncTodosToReminders(todos: List<TaskMetadata>) {
    if (!AppleIntegrationService.isSupported()) return
    if (todos.isEmpty()) return
    syncScope.launch {
      syncTodosInternal(todos)
    }
  }

  private suspend fun syncTodosInternal(todos: List<TaskMetadata>) = reminderMutex.withLock {
    val permission = AppleIntegrationService.checkRemindersPermission()
    if (permission != ApplePermissionStatus.GRANTED) {
      KlikLogger.d(TAG, "Reminders permission not granted ($permission), skipping todo sync")
      return@withLock
    }

    val key = reminderKey
    val syncedIds = loadSyncedIds(key)
    val newTodos = todos.filter { it.id !in syncedIds }

    if (newTodos.isEmpty()) {
      KlikLogger.d(TAG, "No new todos to sync (${todos.size} already synced)")
      return@withLock
    }

    KlikLogger.i(TAG, "Syncing ${newTodos.size} new todos to Apple Reminders")

    // Mark all IDs as synced upfront so concurrent calls (initial load + foreground refresh)
    // see the full set immediately and don't re-sync the same todos.
    saveSyncedIds(key, syncedIds + newTodos.map { it.id })

    for (todo in newTodos) {
      val reminderData = todoToReminder(todo)

      AppleIntegrationService.saveReminder(reminderData) { result ->
        when (result) {
          is AppleSaveResult.Success -> {
            KlikLogger.i(TAG, "Todo synced to Reminders: id=${todo.id}, title=${todo.title}, reminderId=${result.identifier}")
          }

          is AppleSaveResult.Error -> {
            KlikLogger.e(TAG, "Failed to sync todo ${todo.id}: ${result.message}")
          }

          is AppleSaveResult.PermissionDenied -> {
            KlikLogger.e(TAG, "Reminders permission denied during sync of todo ${todo.id}")
          }

          is AppleSaveResult.NotSupported -> {
            KlikLogger.e(TAG, "Reminders not supported during sync of todo ${todo.id}")
          }
        }
      }
    }
  }

  // ==================== State Management ====================

  /**
   * Clear sync state for the currently logged-in user. Not called on token expiry —
   * userId-scoped keys naturally survive re-login for the same account.
   */
  fun clearSyncState() {
    storage.remove(calendarKey)
    storage.remove(reminderKey)
    KlikLogger.i(TAG, "Cleared sync state for user=${CurrentUser.userId}")
  }

  // ==================== Apple Calendar → Klik (Read) ====================

  /**
   * Fetch Apple Calendar events for a 3-month window and convert to Meeting objects.
   * Deduplicates: filters out events that were originally pushed from Klik (by checking
   * against the synced calendar IDs stored in SecureStorage).
   * Returns results via callback since AppleIntegrationService uses callback-based APIs.
   */
  fun fetchAppleCalendarEvents(onResult: (List<Meeting>) -> Unit) {
    if (!AppleIntegrationService.isSupported()) {
      onResult(emptyList())
      return
    }

    val permission = AppleIntegrationService.checkCalendarPermission()
    if (permission != ApplePermissionStatus.GRANTED) {
      KlikLogger.d(TAG, "Calendar permission not granted ($permission), skipping Apple Calendar fetch")
      onResult(emptyList())
      return
    }

    val tz = TimeZone.currentSystemDefault()
    val today = kotlinx.datetime.Clock.System.now().toLocalDateTime(tz).date

    // 3-month window: previous month to next month
    val startDate = today.minus(DatePeriod(months = 1)).let {
      LocalDate(it.year, it.monthNumber, 1)
    }
    val endDate = today.plus(DatePeriod(months = 2)).let {
      LocalDate(it.year, it.monthNumber, 1)
    }

    val startMillis = startDate.atStartOfDayIn(tz).toEpochMilliseconds()
    val endMillis = endDate.atStartOfDayIn(tz).toEpochMilliseconds()

    // Load IDs of events that Klik already synced TO Apple Calendar (to avoid duplicates)
    val klikSyncedCalendarIds = loadSyncedIds(calendarKey)

    AppleIntegrationService.fetchCalendarEvents(startMillis, endMillis) { appleEvents ->
      // Filter out events that Klik originally pushed to Apple Calendar
      val externalEvents = appleEvents.filter { it.id !in klikSyncedCalendarIds }

      val meetings = externalEvents.mapNotNull { event ->
        appleCalendarEventToMeeting(event, tz)
      }

      KlikLogger.i(TAG, "Fetched ${meetings.size} Apple Calendar events (filtered ${appleEvents.size - externalEvents.size} Klik-synced)")
      onResult(meetings)
    }
  }

  /**
   * Convert an AppleCalendarEvent to a Meeting domain entity.
   */
  private fun appleCalendarEventToMeeting(event: AppleCalendarEvent, tz: TimeZone): Meeting? = try {
    val startInstant = Instant.fromEpochMilliseconds(event.startTimeMillis)
    val endInstant = Instant.fromEpochMilliseconds(event.endTimeMillis)
    val startDt = startInstant.toLocalDateTime(tz)
    val endDt = endInstant.toLocalDateTime(tz)

    // Format time string like "6:07 PM - 6:17 PM"
    val timeStr = "${formatAmPm(startDt.hour, startDt.minute)} - ${formatAmPm(endDt.hour, endDt.minute)}"

    Meeting(
      id = "apple_${event.id}",
      title = event.title,
      date = startDt.date,
      time = if (event.isAllDay) "All Day" else timeStr,
      participants = emptyList(),
      summary = buildString {
        if (event.calendarName != null) append("[${event.calendarName}]")
        if (event.location != null) {
          if (isNotEmpty()) append(" · ")
          append(event.location)
        }
        if (event.notes != null) {
          if (isNotEmpty()) append("\n")
          append(event.notes)
        }
      },
      actionItems = emptyList(),
      source = MeetingSource.APPLE_CALENDAR,
      sourceColor = event.calendarColorArgb,
    )
  } catch (e: Exception) {
    KlikLogger.e(TAG, "Failed to convert Apple Calendar event '${event.title}': ${e.message}", e)
    null
  }

  /**
   * Format hour/minute to "H:mm AM/PM" string.
   */
  private fun formatAmPm(hour: Int, minute: Int): String {
    val isPM = hour >= 12
    val displayHour = when {
      hour == 0 -> 12
      hour > 12 -> hour - 12
      else -> hour
    }
    val suffix = if (isPM) "PM" else "AM"
    return "$displayHour:${minute.toString().padStart(2, '0')} $suffix"
  }

  // ==================== Data Mapping ====================

  /**
   * Convert a Meeting to CalendarEventData.
   * Parses time string like "6:07 PM - 6:17 PM" into start/end epoch millis.
   * Returns null if time parsing fails completely (no valid start time).
   */
  private fun meetingToCalendarEvent(meeting: Meeting): CalendarEventData? {
    val tz = TimeZone.currentSystemDefault()

    // Parse start and end times from the time string
    val (startTime, endTime) = parseTimeRange(meeting.time)
    if (startTime == null) {
      KlikLogger.e(TAG, "Cannot parse time for meeting ${meeting.id}: '${meeting.time}'")
      return null
    }

    val startDateTime = meeting.date.atTime(startTime)
    val startMillis = startDateTime.toInstant(tz).toEpochMilliseconds()

    // End time: use parsed end time, or default to start + 30 min
    var endMillis = if (endTime != null) {
      meeting.date.atTime(endTime).toInstant(tz).toEpochMilliseconds()
    } else {
      startMillis + 30 * 60 * 1000L
    }

    // Handle midnight-crossing times (e.g., "11:45 PM - 12:15 AM")
    // If end time is before or equal to start time, add 24 hours to end time
    if (endMillis <= startMillis) {
      endMillis += 24 * 60 * 60 * 1000L
      KlikLogger.d(TAG, "Adjusted midnight-crossing end time for meeting ${meeting.id}: '${meeting.time}'")
    }

    // Build notes from participants and summary
    val notesParts = mutableListOf<String>()
    if (meeting.participants.isNotEmpty()) {
      val participantNames = meeting.participants.map { it.canonicalName }
      notesParts.add("Participants: ${participantNames.joinToString(", ")}")
    }
    if (meeting.summary.isNotBlank()) {
      notesParts.add(meeting.summary)
    }

    return CalendarEventData(
      title = meeting.title,
      startTimeMillis = startMillis,
      endTimeMillis = endMillis,
      notes = notesParts.joinToString("\n\n").takeIf { it.isNotBlank() },
    )
  }

  /**
   * Parse a time range string like "6:07 PM - 6:17 PM" into (startTime, endTime).
   * Handles formats: "H:mm AM/PM - H:mm AM/PM", "HH:mm - HH:mm"
   */
  private fun parseTimeRange(timeStr: String): Pair<LocalTime?, LocalTime?> {
    val parts = timeStr.split(" - ", " – ") // Handle both regular and em-dash separators
    val startTime = parseTimeComponent(parts.getOrNull(0)?.trim())
    val endTime = parseTimeComponent(parts.getOrNull(1)?.trim())
    return startTime to endTime
  }

  /**
   * Parse a single time component like "6:07 PM" or "14:07" into LocalTime.
   */
  private fun parseTimeComponent(timeStr: String?): LocalTime? {
    if (timeStr.isNullOrBlank()) return null

    return try {
      val clean = timeStr.uppercase().trim()
      val isPM = clean.contains("PM")
      val isAM = clean.contains("AM")
      val timeOnly = clean.replace("AM", "").replace("PM", "").trim()
      val colonParts = timeOnly.split(":")
      var hour = colonParts[0].toInt()
      val minute = if (colonParts.size > 1) colonParts[1].toInt() else 0

      if (isPM && hour != 12) hour += 12
      if (isAM && hour == 12) hour = 0

      LocalTime(hour, minute)
    } catch (e: Exception) {
      KlikLogger.e(TAG, "Failed to parse time component '$timeStr': ${e.message}", e)
      null
    }
  }

  /**
   * Convert a TaskMetadata (from KK_exec todo) to ReminderData.
   */
  private fun todoToReminder(todo: TaskMetadata): ReminderData {
    // Build notes from description + status + context
    val notesParts = mutableListOf<String>()
    if (!todo.description.isNullOrBlank()) {
      notesParts.add(todo.description)
    }
    if (!todo.kkExecStatus.isNullOrBlank()) {
      notesParts.add("Status: ${todo.kkExecStatus}")
    }
    if (todo.context.isNotBlank() && todo.context != "Auto-executable") {
      notesParts.add("Context: ${todo.context}")
    }

    // Parse due date from dueInfo (format: "YYYY-MM-DD" or descriptive text)
    val dueDateMillis = parseDueDate(todo.dueInfo)

    // Map priority: "High" → 1, "Normal" → 5, "Low" → 9, else → 0
    val priority = when (todo.priority.lowercase()) {
      "high" -> 1
      "normal" -> 5
      "low" -> 9
      else -> 0
    }

    return ReminderData(
      title = todo.title,
      notes = notesParts.joinToString("\n").takeIf { it.isNotBlank() },
      dueDateMillis = dueDateMillis,
      priority = priority,
    )
  }

  /**
   * Parse a due date string. Handles "YYYY-MM-DD" format.
   * Returns epoch millis at start of day in local timezone, or null if not parseable.
   */
  private fun parseDueDate(dueInfo: String?): Long? {
    if (dueInfo.isNullOrBlank()) return null

    return try {
      // Try YYYY-MM-DD format
      val date = LocalDate.parse(dueInfo)
      val startOfDay = date.atTime(LocalTime(9, 0)) // 9 AM local time for reminder
      startOfDay.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    } catch (e: Exception) {
      KlikLogger.e(TAG, "Failed to parse due date '$dueInfo': ${e.message}", e)
      null
    }
  }

  // ==================== Sync ID Persistence ====================

  private fun loadSyncedIds(key: String): Set<String> {
    val raw = storage.getString(key) ?: return emptySet()
    return try {
      val array = json.parseToJsonElement(raw).jsonArray
      array.map { it.jsonPrimitive.content }.toSet()
    } catch (e: Exception) {
      KlikLogger.e(TAG, "Failed to parse synced IDs for key=$key, clearing corrupt data: ${e.message}", e)
      storage.remove(key)
      emptySet()
    }
  }

  private fun saveSyncedIds(key: String, ids: Set<String>) {
    val jsonArray = JsonArray(ids.map { JsonPrimitive(it) })
    storage.saveString(key, jsonArray.toString())
  }
}
