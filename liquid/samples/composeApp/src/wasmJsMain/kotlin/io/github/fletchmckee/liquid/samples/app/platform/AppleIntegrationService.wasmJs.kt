// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

/**
 * WasmJS implementation of AppleIntegrationService.
 * Apple integrations are not supported on WasmJS - all methods return NOT_SUPPORTED.
 */
actual object AppleIntegrationService {

  actual fun checkCalendarPermission(): ApplePermissionStatus = ApplePermissionStatus.NOT_SUPPORTED

  actual fun checkRemindersPermission(): ApplePermissionStatus = ApplePermissionStatus.NOT_SUPPORTED

  actual fun requestCalendarPermission(onResult: (Boolean) -> Unit) {
    onResult(false)
  }

  actual fun requestRemindersPermission(onResult: (Boolean) -> Unit) {
    onResult(false)
  }

  actual fun saveCalendarEvent(event: CalendarEventData, onResult: (AppleSaveResult) -> Unit) {
    onResult(AppleSaveResult.NotSupported)
  }

  actual fun saveReminder(reminder: ReminderData, onResult: (AppleSaveResult) -> Unit) {
    onResult(AppleSaveResult.NotSupported)
  }

  actual fun fetchCalendarEvents(startDateMillis: Long, endDateMillis: Long, onResult: (List<AppleCalendarEvent>) -> Unit) {
    onResult(emptyList())
  }

  actual fun isSupported(): Boolean = false
}
