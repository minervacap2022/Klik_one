// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryCalendarDataSource
import io.github.fletchmckee.liquid.samples.app.domain.entity.DailyBriefing
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.MeetingMinute
import io.github.fletchmckee.liquid.samples.app.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/**
 * Implementation of CalendarRepository.
 * PRODUCTION: Requires InMemoryCalendarDataSource - no optional dependencies.
 */
class CalendarRepositoryImpl(
  private val dataSource: InMemoryCalendarDataSource,
) : CalendarRepository {

  private val _meetingsFlow = MutableStateFlow<Result<List<Meeting>>>(Result.Loading)

  init {
    refreshMeetingsInternal()
  }

  private fun refreshMeetingsInternal() {
    try {
      // Surface every meeting the data source returned, including empty/silent
      // blank-template sessions. The UI is responsible for rendering them; we
      // never silently swallow data here. See feedback memory:
      // ~/.claude/projects/.../memory/feedback_no_silent_swallow.md
      val meetings = dataSource.getMeetings()
      _meetingsFlow.value = Result.Success(meetings)
    } catch (e: Exception) {
      _meetingsFlow.value = Result.Error(e, "Failed to load meetings")
    }
  }

  override fun getMeetingsFlow(): Flow<Result<List<Meeting>>> = _meetingsFlow

  override suspend fun getMeetingsForDate(date: LocalDate): Result<List<Meeting>> = try {
    val meetings = dataSource.getMeetingsForDate(date)
    Result.Success(meetings)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get meetings for date")
  }

  override suspend fun getMeetingsInRange(startDate: LocalDate, endDate: LocalDate): Result<List<Meeting>> = try {
    val meetings = dataSource.getMeetingsInRange(startDate, endDate)
    Result.Success(meetings)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get meetings in range")
  }

  override suspend fun getMeetingById(id: String): Result<Meeting> = try {
    val meeting = dataSource.getMeetingById(id)
      ?: throw NoSuchElementException("Meeting not found: $id")
    Result.Success(meeting)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get meeting")
  }

  override suspend fun getDailyBriefing(): Result<DailyBriefing> = try {
    val briefing = dataSource.getDailyBriefing()
    Result.Success(briefing)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get daily briefing")
  }

  override suspend fun getPinnedMeetings(): Result<List<Meeting>> = try {
    val meetings = dataSource.getPinnedMeetings()
    Result.Success(meetings)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get pinned meetings")
  }

  override suspend fun toggleMeetingPin(meetingId: String): Result<Meeting> = try {
    val meeting = dataSource.toggleMeetingPin(meetingId)
      ?: throw NoSuchElementException("Meeting not found: $meetingId")
    refreshMeetingsInternal()
    Result.Success(meeting)
  } catch (e: Exception) {
    Result.Error(e, "Failed to toggle meeting pin")
  }

  override suspend fun archiveMeeting(meetingId: String): Result<Unit> = try {
    val success = dataSource.archiveMeeting(meetingId)
    if (!success) {
      throw NoSuchElementException("Meeting not found: $meetingId")
    }
    refreshMeetingsInternal()
    Result.Success(Unit)
  } catch (e: Exception) {
    Result.Error(e, "Failed to archive meeting")
  }

  override suspend fun getMeetingMinutes(meetingId: String): Result<List<MeetingMinute>> = try {
    val minutes = dataSource.getMeetingMinutes(meetingId)
    Result.Success(minutes)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get meeting minutes")
  }

  override suspend fun updateMeetingMinute(meetingId: String, minute: MeetingMinute): Result<MeetingMinute> = try {
    val updated = dataSource.updateMeetingMinute(meetingId, minute)
      ?: throw NoSuchElementException("Minute not found for meeting: $meetingId")
    Result.Success(updated)
  } catch (e: Exception) {
    Result.Error(e, "Failed to update meeting minute")
  }

  override suspend fun getUpcomingMeetingsCount(): Result<Int> = try {
    val count = dataSource.getUpcomingMeetingsCount()
    Result.Success(count)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get upcoming meetings count")
  }

  override suspend fun refreshMeetings(): Result<Unit> = try {
    refreshMeetingsInternal()
    Result.Success(Unit)
  } catch (e: Exception) {
    Result.Error(e, "Failed to refresh meetings")
  }
}
