// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.calendar

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing meetings as a reactive flow.
 */
class ObserveMeetingsUseCase(
  private val calendarRepository: CalendarRepository,
) {
  operator fun invoke(): Flow<Result<List<Meeting>>> = calendarRepository.getMeetingsFlow()
}
