// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.calendar

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.repository.CalendarRepository

/**
 * Use case for toggling meeting pin status.
 */
class ToggleMeetingPinUseCase(
  private val calendarRepository: CalendarRepository,
) {
  suspend operator fun invoke(meetingId: String): Result<Meeting> = calendarRepository.toggleMeetingPin(meetingId)
}
