// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.source.inmemory

import io.github.fletchmckee.liquid.samples.app.domain.entity.DailyBriefing
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.MeetingMinute
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * In-memory data source for calendar/meeting data.
 * Data is provided by the backend via RemoteDataFetcher.
 */
class InMemoryCalendarDataSource {

  private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

  private val meetings = mutableListOf<Meeting>()
  private var dailyBriefing: DailyBriefing? = null

  /**
   * Set meetings externally (e.g. from AppModule)
   */
  fun setMeetings(meetings: List<Meeting>) {
    this.meetings.clear()
    this.meetings.addAll(meetings)
  }

  /**
   * Set daily briefing from JSON data
   */
  fun setBriefing(briefing: DailyBriefing?) {
    dailyBriefing = briefing
  }

  fun getMeetings(): List<Meeting> = meetings.filter { !it.isArchived }

  fun getMeetingsForDate(date: LocalDate): List<Meeting> = meetings.filter { it.date == date && !it.isArchived }

  fun getMeetingsInRange(startDate: LocalDate, endDate: LocalDate): List<Meeting> = meetings.filter { it.date in startDate..endDate && !it.isArchived }

  fun getMeetingById(id: String): Meeting? = meetings.find { it.id == id }

  fun getPinnedMeetings(): List<Meeting> = meetings.filter { it.isPinned }.sortedByDescending { it.pinnedAt }

  fun toggleMeetingPin(meetingId: String): Meeting? {
    val index = meetings.indexOfFirst { it.id == meetingId }
    if (index == -1) return null

    val meeting = meetings[index]
    val updated = meeting.copy(
      isPinned = !meeting.isPinned,
      pinnedAt = if (!meeting.isPinned) Clock.System.now().toEpochMilliseconds() else null,
    )
    meetings[index] = updated
    return updated
  }

  fun archiveMeeting(meetingId: String): Boolean {
    val index = meetings.indexOfFirst { it.id == meetingId }
    if (index == -1) return false

    meetings[index] = meetings[index].copy(isArchived = true, isPinned = false)
    return true
  }

  fun getMeetingMinutes(meetingId: String): List<MeetingMinute> = getMeetingById(meetingId)?.minutes ?: emptyList()

  fun updateMeetingMinute(meetingId: String, minute: MeetingMinute): MeetingMinute? {
    val meetingIndex = meetings.indexOfFirst { it.id == meetingId }
    if (meetingIndex == -1) return null

    val meeting = meetings[meetingIndex]
    val minuteIndex = meeting.minutes.indexOfFirst { it.category == minute.category }
    if (minuteIndex == -1) return null

    val updatedMinutes = meeting.minutes.toMutableList()
    updatedMinutes[minuteIndex] = minute
    meetings[meetingIndex] = meeting.copy(minutes = updatedMinutes)
    return minute
  }

  fun getDailyBriefing(): DailyBriefing = dailyBriefing ?: throw IllegalStateException(
    "No daily briefing available. Backend must provide daily briefing data.",
  )

  fun getUpcomingMeetingsCount(): Int = meetings.count { it.date >= today && !it.isArchived }
}
