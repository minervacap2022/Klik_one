package io.github.fletchmckee.liquid.samples.app.data.network.dto

import io.github.fletchmckee.liquid.samples.app.domain.entity.DailyBriefing
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.MeetingMinute
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.TodoItem
import io.github.fletchmckee.liquid.samples.app.domain.entity.TodoType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Data Transfer Object for Meeting from API
 */
data class MeetingDto(
    val id: String,
    val title: String,
    val date: String,
    val time: String,
    val participants: List<PersonDto>,
    val summary: String,
    val actionItems: List<TodoItemDto>,
    val minutes: List<MeetingMinuteDto>? = null,
    val transcript: String? = null,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val pinnedAt: Long? = null,
    val dropboxUrl: String? = null
) {
    fun toDomain(): Meeting = Meeting(
        id = id,
        title = title,
        date = parseDate(date),
        time = time,
        participants = participants.map { it.toDomain() },
        summary = summary,
        actionItems = actionItems.map { it.toDomain() },
        minutes = minutes?.map { it.toDomain() } ?: emptyList(),
        transcript = transcript,
        isPinned = isPinned,
        isArchived = isArchived,
        pinnedAt = pinnedAt,
        dropboxUrl = dropboxUrl
    )

    private fun parseDate(dateStr: String): kotlinx.datetime.LocalDate {
        return try {
            kotlinx.datetime.LocalDate.parse(dateStr)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Invalid date format '$dateStr'. Backend must provide dates in ISO-8601 format (YYYY-MM-DD). Error: ${e.message}"
            )
        }
    }

    companion object {
        fun fromDomain(meeting: Meeting): MeetingDto = MeetingDto(
            id = meeting.id,
            title = meeting.title,
            date = meeting.date.toString(),
            time = meeting.time,
            participants = meeting.participants.map { PersonDto.fromDomain(it) },
            summary = meeting.summary,
            actionItems = meeting.actionItems.map { TodoItemDto.fromDomain(it) },
            minutes = meeting.minutes.map { MeetingMinuteDto.fromDomain(it) },
            transcript = meeting.transcript,
            isPinned = meeting.isPinned,
            isArchived = meeting.isArchived,
            pinnedAt = meeting.pinnedAt,
            dropboxUrl = meeting.dropboxUrl
        )
    }
}

/**
 * DTO for Person - matches Person domain entity
 */
data class PersonDto(
    val id: String,
    val name: String,
    val role: String,
    val avatarUrl: String? = null,
    val influenceTier: String = "B",
    val email: String = "",
    val phone: String = "",
    val relatedProjects: List<String> = emptyList(),
    val relatedOrganizations: List<String> = emptyList(),
    val characteristics: List<String> = emptyList(),
    val relationshipStatus: String = "DEVELOPING",
    val lastInteraction: String = "No recent contact"
) {
    fun toDomain(): Person = Person(
        id = id,
        name = name,
        role = role,
        avatarUrl = avatarUrl,
        email = email,
        phone = phone,
        relatedProjects = relatedProjects,
        relatedOrganizations = relatedOrganizations,
        characteristics = characteristics,
        lastInteraction = lastInteraction
    )

    companion object {
        fun fromDomain(person: Person): PersonDto = PersonDto(
            id = person.id,
            name = person.name,
            role = person.role,
            avatarUrl = person.avatarUrl,
            influenceTier = person.influenceTier.name,
            email = person.email,
            phone = person.phone,
            relatedProjects = person.relatedProjects,
            relatedOrganizations = person.relatedOrganizations,
            characteristics = person.characteristics,
            relationshipStatus = person.relationshipStatus.name,
            lastInteraction = person.lastInteraction
        )
    }
}

/**
 * DTO for TodoItem - matches TodoItem domain entity
 */
data class TodoItemDto(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false,
    val type: String = "TODO"
) {
    fun toDomain(): TodoItem = TodoItem(
        id = id,
        text = text,
        isCompleted = isCompleted,
        type = TodoType.fromString(type)
    )

    companion object {
        fun fromDomain(item: TodoItem): TodoItemDto = TodoItemDto(
            id = item.id,
            text = item.text,
            isCompleted = item.isCompleted,
            type = item.type.name
        )
    }
}

/**
 * DTO for MeetingMinute
 */
data class MeetingMinuteDto(
    val category: String,
    val items: List<String>
) {
    fun toDomain(): MeetingMinute = MeetingMinute(
        category = category,
        items = items
    )

    companion object {
        fun fromDomain(minute: MeetingMinute): MeetingMinuteDto = MeetingMinuteDto(
            category = minute.category,
            items = minute.items
        )
    }
}

/**
 * DTO for daily briefing
 */
data class DailyBriefingDto(
    val summary: String,
    val meetingCount: Int,
    val focusAreas: List<String>,
    val topPriority: String?,
    val generatedAt: Long
) {
    fun toDomain(): DailyBriefing = DailyBriefing(
        summary = summary,
        meetingCount = meetingCount,
        focusAreas = focusAreas,
        topPriority = topPriority,
        generatedAt = generatedAt
    )

    companion object {
        fun fromDomain(briefing: DailyBriefing): DailyBriefingDto = DailyBriefingDto(
            summary = briefing.summary,
            meetingCount = briefing.meetingCount,
            focusAreas = briefing.focusAreas,
            topPriority = briefing.topPriority,
            generatedAt = briefing.generatedAt
        )
    }
}
