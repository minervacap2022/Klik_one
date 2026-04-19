package io.github.fletchmckee.liquid.samples.app.domain.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.DailyBriefing
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.MeetingMinute
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Repository interface for calendar and meeting operations.
 * Implementations can be mock, local database, or remote API.
 */
interface CalendarRepository {

    /**
     * Get all meetings as a reactive flow.
     */
    fun getMeetingsFlow(): Flow<Result<List<Meeting>>>

    /**
     * Get meetings for a specific date.
     */
    suspend fun getMeetingsForDate(date: LocalDate): Result<List<Meeting>>

    /**
     * Get meetings within a date range.
     */
    suspend fun getMeetingsInRange(startDate: LocalDate, endDate: LocalDate): Result<List<Meeting>>

    /**
     * Get a single meeting by ID.
     */
    suspend fun getMeetingById(id: String): Result<Meeting>

    /**
     * Get today's daily briefing.
     */
    suspend fun getDailyBriefing(): Result<DailyBriefing>

    /**
     * Get pinned meetings.
     */
    suspend fun getPinnedMeetings(): Result<List<Meeting>>

    /**
     * Pin/unpin a meeting.
     */
    suspend fun toggleMeetingPin(meetingId: String): Result<Meeting>

    /**
     * Archive a meeting.
     */
    suspend fun archiveMeeting(meetingId: String): Result<Unit>

    /**
     * Get meeting minutes for a specific meeting.
     */
    suspend fun getMeetingMinutes(meetingId: String): Result<List<MeetingMinute>>

    /**
     * Update a meeting minute (for corrections).
     */
    suspend fun updateMeetingMinute(meetingId: String, minute: MeetingMinute): Result<MeetingMinute>

    /**
     * Get upcoming meetings count.
     */
    suspend fun getUpcomingMeetingsCount(): Result<Int>

    /**
     * Refresh meetings from remote source.
     */
    suspend fun refreshMeetings(): Result<Unit>
}
