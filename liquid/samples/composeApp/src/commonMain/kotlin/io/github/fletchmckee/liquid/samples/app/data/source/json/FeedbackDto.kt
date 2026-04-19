package io.github.fletchmckee.liquid.samples.app.data.source.json

import io.github.fletchmckee.liquid.samples.app.domain.entity.Feedback
import io.github.fletchmckee.liquid.samples.app.domain.entity.FeedbackElementType
import io.github.fletchmckee.liquid.samples.app.domain.entity.FeedbackType
import kotlinx.serialization.Serializable

/**
 * DTO for feedback JSON file
 */
@Serializable
data class FeedbackJsonDto(
    val feedbackEntries: List<FeedbackEntryDto> = emptyList(),
    val feedbackItems: List<FeedbackEntryDto> = emptyList()
)

@Serializable
data class FeedbackEntryDto(
    val id: String,
    val elementType: String,
    val elementId: String,
    val screen: String,
    val data: FeedbackDataDto,
    val createdAt: Long = 0
)

@Serializable
data class FeedbackDataDto(
    val type: String,
    val comment: String? = null,
    val rating: Int = 0,
    val originalText: String = "",
    val correction: String? = null,
    val isMarkedWrong: Boolean = false
)

/**
 * Extension function to convert DTO to domain entity
 */
fun FeedbackEntryDto.toDomain(): Feedback {
    val elementTypeEnum = when (elementType.uppercase()) {
        "AI_RESPONSE" -> FeedbackElementType.AI_RESPONSE
        "CHAT_RESPONSE" -> FeedbackElementType.CHAT_RESPONSE
        "MEETING_TITLE" -> FeedbackElementType.MEETING_TITLE
        "MEETING_SUMMARY" -> FeedbackElementType.MEETING_SUMMARY
        "MEETING_MINUTE" -> FeedbackElementType.MEETING_MINUTE
        "PERSON_NAME" -> FeedbackElementType.PERSON_NAME
        "PERSON_ROLE" -> FeedbackElementType.PERSON_ROLE
        "PROJECT_NAME" -> FeedbackElementType.PROJECT_NAME
        "PROJECT_STATUS" -> FeedbackElementType.PROJECT_STATUS
        "TASK_TITLE" -> FeedbackElementType.TASK_TITLE
        "ORGANIZATION_NAME" -> FeedbackElementType.ORGANIZATION_NAME
        "BRIEFING_TEXT" -> FeedbackElementType.BRIEFING_TEXT
        else -> FeedbackElementType.OTHER
    }

    val feedbackType = when (data.type.uppercase()) {
        "POSITIVE" -> FeedbackType.POSITIVE
        "NEGATIVE" -> FeedbackType.NEGATIVE
        else -> FeedbackType.NEUTRAL
    }

    return Feedback(
        id = id,
        elementType = elementTypeEnum,
        elementId = elementId,
        originalText = data.originalText,
        correction = data.correction,
        isMarkedWrong = data.isMarkedWrong,
        screen = screen,
        timestamp = createdAt,
        type = feedbackType,
        rating = data.rating
    )
}
