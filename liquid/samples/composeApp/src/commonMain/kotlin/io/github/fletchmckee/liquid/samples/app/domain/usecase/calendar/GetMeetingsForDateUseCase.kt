// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.calendar

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.repository.CalendarRepository
import kotlinx.datetime.LocalDate

/**
 * Use case for getting meetings for a specific date.
 */
class GetMeetingsForDateUseCase(
  private val calendarRepository: CalendarRepository,
) {
  suspend operator fun invoke(date: LocalDate): Result<List<Meeting>> = calendarRepository.getMeetingsForDate(date)
}
