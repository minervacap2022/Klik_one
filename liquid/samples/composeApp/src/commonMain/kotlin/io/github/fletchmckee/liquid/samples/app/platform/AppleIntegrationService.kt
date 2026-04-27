// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

/**
 * Permission status for Apple native integrations.
 */
enum class ApplePermissionStatus {
  /** Permission has been granted by the user */
  GRANTED,

  /** Permission has been explicitly denied by the user */
  DENIED,

  /** Permission has not been requested yet */
  NOT_DETERMINED,

  /** Permission is restricted (e.g., parental controls) */
  RESTRICTED,

  /** Platform does not support this feature */
  NOT_SUPPORTED,
}

/**
 * Result of saving data to Apple native apps.
 */
sealed class AppleSaveResult {
  data class Success(val identifier: String) : AppleSaveResult()
  data class Error(val message: String) : AppleSaveResult()
  data object NotSupported : AppleSaveResult()
  data object PermissionDenied : AppleSaveResult()
}

/**
 * Data class for a calendar event read from Apple Calendar.
 */
data class AppleCalendarEvent(
  val id: String,
  val title: String,
  val startTimeMillis: Long,
  val endTimeMillis: Long,
  val notes: String? = null,
  val location: String? = null,
  val isAllDay: Boolean = false,
  val calendarName: String? = null,
  val calendarColorArgb: Long? = null,
)

/**
 * Data class for creating calendar events.
 */
data class CalendarEventData(
  val title: String,
  val startTimeMillis: Long,
  val endTimeMillis: Long,
  val notes: String? = null,
  val location: String? = null,
  val isAllDay: Boolean = false,
)

/**
 * Data class for creating reminders.
 */
data class ReminderData(
  val title: String,
  val notes: String? = null,
  val dueDateMillis: Long? = null,
  val priority: Int = 0, // 0 = none, 1-9 with 1 being highest
)

/**
 * Platform-specific service for Apple native integrations.
 * - iOS: Uses EventKit (EKEventStore) for Calendar and Reminders
 * - Other platforms: Returns NotSupported
 *
 * This service handles:
 * 1. Permission checking and requesting for Calendar and Reminders
 * 2. Creating calendar events in Apple Calendar
 * 3. Creating reminders in Apple Reminders
 *
 * Integration with Klik:
 * - Permission status is checked on app startup
 * - If not granted, shown in IntegrationPromptDialog alongside OAuth integrations
 * - Permission grants are synced to backend oauth_credentials table
 * - Data can be auto-saved to Calendar/Reminders when user has granted permission
 */
expect object AppleIntegrationService {

  // ==================== Permission Checking ====================

  /**
   * Check if calendar permission has been granted.
   * @return Current permission status for calendar access
   */
  fun checkCalendarPermission(): ApplePermissionStatus

  /**
   * Check if reminders permission has been granted.
   * @return Current permission status for reminders access
   */
  fun checkRemindersPermission(): ApplePermissionStatus

  // ==================== Permission Requesting ====================

  /**
   * Request calendar permission from the user.
   * Shows iOS native permission dialog if not yet determined.
   * @param onResult Callback with true if permission was granted, false otherwise
   */
  fun requestCalendarPermission(onResult: (Boolean) -> Unit)

  /**
   * Request reminders permission from the user.
   * Shows iOS native permission dialog if not yet determined.
   * @param onResult Callback with true if permission was granted, false otherwise
   */
  fun requestRemindersPermission(onResult: (Boolean) -> Unit)

  // ==================== Data Saving ====================

  /**
   * Save an event to Apple Calendar.
   * Requires calendar permission to be granted.
   * @param event The calendar event data to save
   * @param onResult Callback with the save result
   */
  fun saveCalendarEvent(event: CalendarEventData, onResult: (AppleSaveResult) -> Unit)

  /**
   * Save a reminder to Apple Reminders.
   * Requires reminders permission to be granted.
   * @param reminder The reminder data to save
   * @param onResult Callback with the save result
   */
  fun saveReminder(reminder: ReminderData, onResult: (AppleSaveResult) -> Unit)

  // ==================== Data Reading ====================

  /**
   * Fetch calendar events from Apple Calendar within a date range.
   * Requires calendar permission to be granted.
   * @param startDateMillis Start of date range (epoch millis)
   * @param endDateMillis End of date range (epoch millis)
   * @param onResult Callback with the list of calendar events
   */
  fun fetchCalendarEvents(startDateMillis: Long, endDateMillis: Long, onResult: (List<AppleCalendarEvent>) -> Unit)

  // ==================== Platform Support Check ====================

  /**
   * Check if this platform supports Apple integrations.
   * @return true on iOS, false on all other platforms
   */
  fun isSupported(): Boolean
}
