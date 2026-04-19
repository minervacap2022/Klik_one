package io.github.fletchmckee.liquid.samples.app.data.source.json

import io.github.fletchmckee.liquid.samples.app.domain.entity.DailyBriefing
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.MeetingMinute
import io.github.fletchmckee.liquid.samples.app.domain.entity.TodoItem
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * DTO for meetings JSON file - supports KK_datamodel format (plain list with embedded participants)
 */
@Serializable
data class MeetingsJsonDto(
    val meetings: List<MeetingDto>,
    val dailyBriefing: DailyBriefingDto? = null
)

@Serializable
data class MeetingDto(
    val id: String,
    val title: String,
    val date: String,
    val time: String,
    val endTime: String? = null,
    val participantIds: List<String> = emptyList(),
    val participants: List<ParticipantDto> = emptyList(), // Embedded participants from KK_datamodel
    val summary: String? = null,
    val actionItems: List<ActionItemDto> = emptyList(),
    val minutes: List<MeetingMinuteDto> = emptyList(),
    val transcript: String? = null,
    val location: String? = null,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val pinnedAt: Long? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

@Serializable
data class ParticipantDto(
    val name: String,
    val role: String? = null,
    val influenceTier: String = "B",
    val relatedProjects: List<String> = emptyList(),
    val relatedOrgs: List<String> = emptyList(),
    val characteristics: List<String> = emptyList(),
    val relationshipStatus: String = "Neutral",
    val lastInteraction: String? = null,
    val email: String? = null,
    val phone: String? = null
)

@Serializable
data class ActionItemDto(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false,
    val assigneeId: String? = null,
    val dueDate: String? = null,
    val type: String? = null
)

@Serializable
data class MeetingMinuteDto(
    val category: String,
    val items: List<String>
)

@Serializable
data class DailyBriefingDto(
    val summary: String,
    val meetingCount: Int,
    val focusAreas: List<String>,
    val topPriority: String? = null,
    val generatedAt: Long
)

/**
 * Extension functions to convert DTOs to domain entities
 */
fun MeetingDto.toDomain(): Meeting {
    // Parse date string "2024-12-17" to LocalDate
    val localDate = try {
        val parts = date.split("-")
        LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    } catch (e: Exception) {
        throw IllegalArgumentException(
            "Invalid date format '$date' for meeting '$title' (id=$id). " +
            "Expected format: YYYY-MM-DD. Error: ${e.message}"
        )
    }

    return Meeting(
        id = id,
        title = title,
        date = localDate,
        time = time,
        participants = participants.map { it.toDomain() },
        summary = summary ?: "",
        actionItems = actionItems.map { it.toDomain() },
        minutes = minutes.map { it.toDomain() },
        transcript = transcript,
        isPinned = isPinned,
        isArchived = isArchived,
        pinnedAt = pinnedAt
    )
}

fun ParticipantDto.toDomain(): Person {
    return Person(
        id = name.lowercase().replace(" ", "_"),
        name = name,
        role = role ?: "",
        email = email ?: "",
        phone = phone ?: "",
        characteristics = characteristics,
        relatedProjects = relatedProjects,
        relatedOrganizations = relatedOrgs,
        lastInteraction = lastInteraction ?: "No recent contact",
        isPinned = false,
        pinnedAt = null
    )
}

fun ActionItemDto.toDomain(): TodoItem {
    return TodoItem(
        id = id,
        text = text,
        isCompleted = isCompleted
    )
}

fun MeetingMinuteDto.toDomain(): MeetingMinute {
    return MeetingMinute(
        category = category,
        items = items
    )
}

fun DailyBriefingDto.toDomain(): DailyBriefing {
    return DailyBriefing(
        summary = summary,
        meetingCount = meetingCount,
        focusAreas = focusAreas,
        topPriority = topPriority,
        generatedAt = generatedAt
    )
}
