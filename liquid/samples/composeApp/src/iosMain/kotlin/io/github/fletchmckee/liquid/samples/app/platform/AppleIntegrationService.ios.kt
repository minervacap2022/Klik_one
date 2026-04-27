// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package io.github.fletchmckee.liquid.samples.app.platform

import eventkit.bridge.getEKEntityTypeEvent
import eventkit.bridge.getEKEntityTypeReminder
import eventkit.bridge.getEKSpanThisEvent
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.EventKit.EKAuthorizationStatus
import platform.EventKit.EKAuthorizationStatusAuthorized
import platform.EventKit.EKAuthorizationStatusDenied
import platform.EventKit.EKAuthorizationStatusNotDetermined
import platform.EventKit.EKAuthorizationStatusRestricted
import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKit.EKReminder
import platform.EventKit.EKSpan
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * iOS implementation of AppleIntegrationService using EventKit framework.
 * Provides access to Apple Calendar and Reminders.
 *
 * EventKit requires:
 * - NSCalendarsUsageDescription in Info.plist for Calendar access
 * - NSRemindersUsageDescription in Info.plist for Reminders access
 */
actual object AppleIntegrationService {

  // Shared EKEventStore instance - should be reused across the app lifecycle
  private val eventStore: EKEventStore by lazy { EKEventStore() }

  // EventKit enum values from C bridge
  private val entityTypeEvent: EKEntityType by lazy { getEKEntityTypeEvent() }
  private val entityTypeReminder: EKEntityType by lazy { getEKEntityTypeReminder() }
  private val spanThisEvent: EKSpan by lazy { getEKSpanThisEvent() }

  // ==================== Permission Checking ====================

  /**
   * Check if calendar permission has been granted.
   * Uses EKEventStore.authorizationStatusForEntityType for EKEntityTypeEvent.
   */
  actual fun checkCalendarPermission(): ApplePermissionStatus = mapAuthorizationStatus(
    EKEventStore.authorizationStatusForEntityType(entityTypeEvent),
  )

  /**
   * Check if reminders permission has been granted.
   * Uses EKEventStore.authorizationStatusForEntityType for EKEntityTypeReminder.
   */
  actual fun checkRemindersPermission(): ApplePermissionStatus = mapAuthorizationStatus(
    EKEventStore.authorizationStatusForEntityType(entityTypeReminder),
  )

  /**
   * Map EKAuthorizationStatus to our ApplePermissionStatus enum.
   * STRICT: Logs unexpected status values for debugging.
   */
  private fun mapAuthorizationStatus(status: EKAuthorizationStatus): ApplePermissionStatus = when (status) {
    EKAuthorizationStatusAuthorized -> ApplePermissionStatus.GRANTED

    EKAuthorizationStatusDenied -> ApplePermissionStatus.DENIED

    EKAuthorizationStatusNotDetermined -> ApplePermissionStatus.NOT_DETERMINED

    EKAuthorizationStatusRestricted -> ApplePermissionStatus.RESTRICTED

    else -> {
      // Log unexpected status value - could be a new iOS version with additional statuses
      KlikLogger.w("AppleIntegrationService", "Unexpected EKAuthorizationStatus value: $status, treating as NOT_DETERMINED")
      ApplePermissionStatus.NOT_DETERMINED
    }
  }

  // ==================== Permission Requesting ====================

  /**
   * Request calendar permission from the user.
   * Shows iOS native permission dialog if not yet determined.
   * Callback is invoked on main thread.
   */
  actual fun requestCalendarPermission(onResult: (Boolean) -> Unit) {
    requestPermission(entityTypeEvent, onResult)
  }

  /**
   * Request reminders permission from the user.
   * Shows iOS native permission dialog if not yet determined.
   * Callback is invoked on main thread.
   */
  actual fun requestRemindersPermission(onResult: (Boolean) -> Unit) {
    requestPermission(entityTypeReminder, onResult)
  }

  /**
   * Generic permission request handler.
   */
  private fun requestPermission(entityType: EKEntityType, onResult: (Boolean) -> Unit) {
    eventStore.requestAccessToEntityType(entityType) { granted, error ->
      // Ensure callback is on main thread for UI updates
      dispatch_async(dispatch_get_main_queue()) {
        if (error != null) {
          KlikLogger.e("AppleIntegrationService", "Permission request error: ${error.localizedDescription}")
          onResult(false)
        } else {
          KlikLogger.i("AppleIntegrationService", "Permission ${if (granted) "granted" else "denied"} for entityType: $entityType")
          onResult(granted)
        }
      }
    }
  }

  // ==================== Data Saving ====================

  /**
   * Save an event to Apple Calendar.
   * Requires calendar permission to be granted.
   * STRICT: No fallbacks - fails explicitly if anything goes wrong.
   */
  actual fun saveCalendarEvent(event: CalendarEventData, onResult: (AppleSaveResult) -> Unit) {
    // Validate required fields
    if (event.title.isBlank()) {
      val error = "Event title is required"
      KlikLogger.e("AppleIntegrationService", error)
      onResult(AppleSaveResult.Error(error))
      return
    }

    // Check permission first - no fallback, explicit fail
    val permission = checkCalendarPermission()
    if (permission != ApplePermissionStatus.GRANTED) {
      KlikLogger.e("AppleIntegrationService", "Calendar permission not granted, status=$permission")
      onResult(AppleSaveResult.PermissionDenied)
      return
    }

    try {
      // Create EKEvent
      val ekEvent = EKEvent.eventWithEventStore(eventStore)
      ekEvent.title = event.title
      ekEvent.startDate = NSDate.dateWithTimeIntervalSince1970(event.startTimeMillis / 1000.0)
      ekEvent.endDate = NSDate.dateWithTimeIntervalSince1970(event.endTimeMillis / 1000.0)
      ekEvent.notes = event.notes
      ekEvent.location = event.location
      ekEvent.allDay = event.isAllDay

      // Get default calendar - fail if not available
      val calendar = eventStore.defaultCalendarForNewEvents
      if (calendar == null) {
        val error = "No default calendar configured on this device"
        KlikLogger.e("AppleIntegrationService", error)
        onResult(AppleSaveResult.Error(error))
        return
      }
      ekEvent.calendar = calendar

      // Save the event using memScoped for proper NSError pointer handling
      memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        val success = eventStore.saveEvent(ekEvent, spanThisEvent, errorPtr.ptr)
        val saveError = errorPtr.value

        if (success && saveError == null) {
          val eventId = ekEvent.eventIdentifier
          if (eventId == null) {
            val error = "Event saved but identifier not returned by EventKit"
            KlikLogger.e("AppleIntegrationService", error)
            onResult(AppleSaveResult.Error(error))
            return@memScoped
          }
          KlikLogger.i("AppleIntegrationService", "Calendar event saved successfully: eventId=$eventId, title=${event.title}")
          onResult(AppleSaveResult.Success(eventId))
        } else {
          val errorMsg = saveError?.localizedDescription
            ?: "EventKit saveEvent returned false without error details"
          KlikLogger.e("AppleIntegrationService", "Failed to save calendar event: $errorMsg")
          onResult(AppleSaveResult.Error(errorMsg))
        }
      }
    } catch (e: Exception) {
      val errorMsg = e.message ?: e.toString()
      KlikLogger.e("AppleIntegrationService", "Exception saving calendar event: $errorMsg", e)
      onResult(AppleSaveResult.Error(errorMsg))
    }
  }

  /**
   * Save a reminder to Apple Reminders.
   * Requires reminders permission to be granted.
   * STRICT: No fallbacks - fails explicitly if anything goes wrong.
   */
  actual fun saveReminder(reminder: ReminderData, onResult: (AppleSaveResult) -> Unit) {
    // Validate required fields
    if (reminder.title.isBlank()) {
      val error = "Reminder title is required"
      KlikLogger.e("AppleIntegrationService", error)
      onResult(AppleSaveResult.Error(error))
      return
    }

    // Check permission first - no fallback, explicit fail
    val permission = checkRemindersPermission()
    if (permission != ApplePermissionStatus.GRANTED) {
      KlikLogger.e("AppleIntegrationService", "Reminders permission not granted, status=$permission")
      onResult(AppleSaveResult.PermissionDenied)
      return
    }

    try {
      // Create EKReminder
      val ekReminder = EKReminder.reminderWithEventStore(eventStore)
      ekReminder.title = reminder.title
      ekReminder.notes = reminder.notes
      ekReminder.priority = reminder.priority.toULong()

      // Set due date if provided (using dueDateComponents for proper reminder scheduling)
      if (reminder.dueDateMillis != null) {
        val dueDate = NSDate.dateWithTimeIntervalSince1970(reminder.dueDateMillis / 1000.0)
        val calendar = platform.Foundation.NSCalendar.currentCalendar
        val components = calendar.components(
          platform.Foundation.NSCalendarUnitYear or
            platform.Foundation.NSCalendarUnitMonth or
            platform.Foundation.NSCalendarUnitDay or
            platform.Foundation.NSCalendarUnitHour or
            platform.Foundation.NSCalendarUnitMinute,
          fromDate = dueDate,
        )
        ekReminder.dueDateComponents = components
        KlikLogger.d("AppleIntegrationService", "Reminder due date set: ${reminder.dueDateMillis}")
      }

      // Get default reminders list - fail if not available
      val calendar = eventStore.defaultCalendarForNewReminders()
      if (calendar == null) {
        val error = "No default reminders list configured on this device"
        KlikLogger.e("AppleIntegrationService", error)
        onResult(AppleSaveResult.Error(error))
        return
      }
      ekReminder.calendar = calendar

      // Save the reminder using memScoped for proper NSError pointer handling
      memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        val success = eventStore.saveReminder(ekReminder, true, errorPtr.ptr)
        val saveError = errorPtr.value

        if (success && saveError == null) {
          val reminderId = ekReminder.calendarItemIdentifier
          if (reminderId == null) {
            val error = "Reminder saved but identifier not returned by EventKit"
            KlikLogger.e("AppleIntegrationService", error)
            onResult(AppleSaveResult.Error(error))
            return@memScoped
          }
          KlikLogger.i("AppleIntegrationService", "Reminder saved successfully: reminderId=$reminderId, title=${reminder.title}")
          onResult(AppleSaveResult.Success(reminderId))
        } else {
          val errorMsg = saveError?.localizedDescription
            ?: "EventKit saveReminder returned false without error details"
          KlikLogger.e("AppleIntegrationService", "Failed to save reminder: $errorMsg")
          onResult(AppleSaveResult.Error(errorMsg))
        }
      }
    } catch (e: Exception) {
      val errorMsg = e.message ?: e.toString()
      KlikLogger.e("AppleIntegrationService", "Exception saving reminder: $errorMsg", e)
      onResult(AppleSaveResult.Error(errorMsg))
    }
  }

  // ==================== Data Reading ====================

  /**
   * Fetch calendar events from Apple Calendar within a date range.
   * Uses EKEventStore predicate query to efficiently retrieve events.
   */
  actual fun fetchCalendarEvents(startDateMillis: Long, endDateMillis: Long, onResult: (List<AppleCalendarEvent>) -> Unit) {
    val permission = checkCalendarPermission()
    if (permission != ApplePermissionStatus.GRANTED) {
      KlikLogger.d("AppleIntegrationService", "Calendar permission not granted ($permission), returning empty events")
      onResult(emptyList())
      return
    }

    try {
      val startDate = NSDate.dateWithTimeIntervalSince1970(startDateMillis / 1000.0)
      val endDate = NSDate.dateWithTimeIntervalSince1970(endDateMillis / 1000.0)

      // Create predicate for events in date range (all calendars)
      val predicate = eventStore.predicateForEventsWithStartDate(
        startDate,
        endDate = endDate,
        calendars = null,
      )

      // Query events (synchronous call) - returns List<*> in Kotlin/Native
      @Suppress("UNCHECKED_CAST")
      val ekEvents = eventStore.eventsMatchingPredicate(predicate) as? List<EKEvent>

      val events = mutableListOf<AppleCalendarEvent>()
      if (ekEvents != null) {
        for (ekEvent in ekEvents) {
          val eventId = ekEvent.eventIdentifier ?: continue
          val title = ekEvent.title ?: continue
          val eventStart: NSDate = ekEvent.startDate ?: continue
          val eventEnd: NSDate = ekEvent.endDate ?: continue

          // NSDate reference date is 2001-01-01; epoch is 1970-01-01
          // Offset = 978307200 seconds
          val startMs = ((eventStart.timeIntervalSinceReferenceDate + 978307200.0) * 1000).toLong()
          val endMs = ((eventEnd.timeIntervalSinceReferenceDate + 978307200.0) * 1000).toLong()

          // Extract calendar color as ARGB Long
          val calendarColorArgb = extractCalendarColor(ekEvent)

          events.add(
            AppleCalendarEvent(
              id = eventId,
              title = title,
              startTimeMillis = startMs,
              endTimeMillis = endMs,
              notes = ekEvent.notes,
              location = ekEvent.location,
              isAllDay = ekEvent.allDay,
              calendarName = ekEvent.calendar?.title,
              calendarColorArgb = calendarColorArgb,
            ),
          )
        }
      }

      KlikLogger.i("AppleIntegrationService", "Fetched ${events.size} Apple Calendar events")
      onResult(events)
    } catch (e: Exception) {
      val errorMsg = e.message ?: e.toString()
      KlikLogger.e("AppleIntegrationService", "Exception fetching calendar events: $errorMsg", e)
      onResult(emptyList())
    }
  }

  /**
   * Extract the ARGB color from an EKEvent's calendar using UIColor bridge.
   * Falls back to null if color extraction fails.
   */
  private fun extractCalendarColor(ekEvent: EKEvent): Long? {
    return try {
      val cgColor = ekEvent.calendar?.CGColor ?: return null
      // Use CIColor to extract RGBA components from CGColor
      val ciColor = platform.CoreImage.CIColor(cGColor = cgColor)
      val r = (ciColor.red * 255).toInt().coerceIn(0, 255)
      val g = (ciColor.green * 255).toInt().coerceIn(0, 255)
      val b = (ciColor.blue * 255).toInt().coerceIn(0, 255)
      val a = (ciColor.alpha * 255).toInt().coerceIn(0, 255)
      ((a.toLong() and 0xFF) shl 24) or
        ((r.toLong() and 0xFF) shl 16) or
        ((g.toLong() and 0xFF) shl 8) or
        (b.toLong() and 0xFF)
    } catch (e: Exception) {
      KlikLogger.w("AppleIntegrationService", "Failed to extract calendar color: ${e.message}")
      null
    }
  }

  // ==================== Platform Support Check ====================

  /**
   * iOS supports Apple integrations.
   */
  actual fun isSupported(): Boolean = true
}
