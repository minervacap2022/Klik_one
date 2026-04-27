// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.calendar

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.MeetingMinute
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.domain.repository.CalendarRepository
import io.github.fletchmckee.liquid.samples.app.domain.repository.PersonRepository
import io.github.fletchmckee.liquid.samples.app.domain.repository.TaskRepository

/**
 * Use case for getting complete meeting details with related data.
 */
class GetMeetingDetailsUseCase(
  private val calendarRepository: CalendarRepository,
  private val personRepository: PersonRepository,
  private val taskRepository: TaskRepository,
) {
  suspend operator fun invoke(meetingId: String): Result<MeetingDetails> = when (val meetingResult = calendarRepository.getMeetingById(meetingId)) {
    is Result.Success -> {
      val meeting = meetingResult.data
      val minutes = when (val minutesResult = calendarRepository.getMeetingMinutes(meetingId)) {
        is Result.Success -> minutesResult.data
        else -> emptyList()
      }
      val participants = when (val participantsResult = personRepository.getPeopleForMeeting(meetingId)) {
        is Result.Success -> participantsResult.data
        else -> meeting.participants
      }
      val relatedTasks = when (val tasksResult = taskRepository.getTasksForMeeting(meetingId)) {
        is Result.Success -> tasksResult.data
        else -> emptyList()
      }
      Result.Success(
        MeetingDetails(
          meeting = meeting.copy(participants = participants, minutes = minutes),
          relatedTasks = relatedTasks,
        ),
      )
    }

    is Result.Error -> meetingResult

    is Result.Loading -> Result.Loading
  }
}

/**
 * Complete meeting details with related data.
 */
data class MeetingDetails(
  val meeting: Meeting,
  val relatedTasks: List<TaskMetadata>,
)
