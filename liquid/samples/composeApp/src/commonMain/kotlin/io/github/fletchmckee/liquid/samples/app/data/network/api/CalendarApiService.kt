package io.github.fletchmckee.liquid.samples.app.data.network.api

import io.github.fletchmckee.liquid.samples.app.data.network.ApiResponse
import io.github.fletchmckee.liquid.samples.app.data.network.dto.DailyBriefingDto
import io.github.fletchmckee.liquid.samples.app.data.network.dto.MeetingDto

/**
 * API service interface for calendar-related endpoints.
 * Implementation will use Ktor client when enabled.
 */
interface CalendarApiService {
    /**
     * Get meetings for a specific date
     * @param date Date in ISO format (yyyy-MM-dd)
     */
    suspend fun getMeetingsForDate(date: String): ApiResponse<List<MeetingDto>>

    /**
     * Get daily briefing for a specific date
     * @param date Date in ISO format (yyyy-MM-dd)
     */
    suspend fun getDailyBriefing(date: String): ApiResponse<DailyBriefingDto>

    /**
     * Get meeting details by ID
     */
    suspend fun getMeetingById(meetingId: String): ApiResponse<MeetingDto>

    /**
     * Update meeting (e.g., toggle pin status)
     */
    suspend fun updateMeeting(meetingId: String, update: MeetingUpdateRequest): ApiResponse<MeetingDto>

    /**
     * Get meetings within a date range
     * @param startDate Start date in ISO format
     * @param endDate End date in ISO format
     */
    suspend fun getMeetingsInRange(startDate: String, endDate: String): ApiResponse<List<MeetingDto>>
}

/**
 * Request body for meeting updates
 */
data class MeetingUpdateRequest(
    val isPinned: Boolean? = null,
    val isArchived: Boolean? = null,
    val transcript: String? = null
)
