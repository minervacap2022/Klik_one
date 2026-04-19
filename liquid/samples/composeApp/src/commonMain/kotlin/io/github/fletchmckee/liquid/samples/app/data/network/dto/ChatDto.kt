package io.github.fletchmckee.liquid.samples.app.data.network.dto

import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatAction
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatActionType
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatMessage
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatSource
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatSourceType
import io.github.fletchmckee.liquid.samples.app.domain.entity.SuggestedQuestion

/**
 * DTO for ChatMessage from API
 */
data class ChatMessageDto(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long,
    val suggestions: List<String>? = null,
    val actions: List<ChatActionDto>? = null
) {
    fun toDomain(): ChatMessage = ChatMessage(
        id = id,
        text = text,
        isUser = isUser,
        timestamp = timestamp,
        suggestions = suggestions ?: emptyList(),
        actions = actions?.map { it.toDomain() } ?: emptyList()
    )

    companion object {
        fun fromDomain(message: ChatMessage): ChatMessageDto = ChatMessageDto(
            id = message.id,
            text = message.text,
            isUser = message.isUser,
            timestamp = message.timestamp,
            suggestions = message.suggestions.takeIf { it.isNotEmpty() },
            actions = message.actions.takeIf { it.isNotEmpty() }?.map { ChatActionDto.fromDomain(it) }
        )
    }
}

/**
 * DTO for ChatAction
 */
data class ChatActionDto(
    val type: String,
    val payload: Map<String, String>? = null
) {
    fun toDomain(): ChatAction = ChatAction(
        type = parseActionType(type),
        payload = payload ?: emptyMap()
    )

    private fun parseActionType(type: String): ChatActionType = when (type.uppercase()) {
        "NAVIGATE" -> ChatActionType.NAVIGATE
        "OPEN_MEETING" -> ChatActionType.OPEN_MEETING
        "CREATE_TASK" -> ChatActionType.CREATE_TASK
        "SHOW_CALENDAR" -> ChatActionType.SHOW_CALENDAR
        else -> ChatActionType.OTHER
    }

    companion object {
        fun fromDomain(action: ChatAction): ChatActionDto = ChatActionDto(
            type = action.type.name,
            payload = action.payload.takeIf { it.isNotEmpty() }
        )
    }
}

/**
 * DTO for SuggestedQuestion
 */
data class SuggestedQuestionDto(
    val text: String,
    val category: String? = null
) {
    fun toDomain(): SuggestedQuestion = SuggestedQuestion(
        text = text,
        category = category ?: "general"
    )

    companion object {
        fun fromDomain(question: SuggestedQuestion): SuggestedQuestionDto = SuggestedQuestionDto(
            text = question.text,
            category = question.category
        )
    }
}

/**
 * Request body for sending a chat message
 */
data class SendChatMessageRequest(
    val message: String,
    val context: ChatContextDto? = null
)

/**
 * DTO for chat context
 */
data class ChatContextDto(
    val currentScreen: String? = null,
    val recentMeetingId: String? = null,
    val timeOfDay: String? = null,
    val userId: String? = null
)

/**
 * Response for chat message send
 */
data class SendChatMessageResponse(
    val userMessage: ChatMessageDto,
    val aiResponse: ChatMessageDto,
    val suggestions: List<SuggestedQuestionDto>? = null
)

// ============================================
// AskKlik RAG Chat API DTOs (port 8333)
// ============================================

/**
 * Request body for AskKlik RAG chat API
 * POST /api/chat/v1/chat
 *
 * IMPORTANT: Backend expects chat_session_id (not session_id)
 * REMOVED: history field (deprecated - backend loads from PostgreSQL)
 */
@kotlinx.serialization.Serializable
data class AskKlikChatRequest(
    val query: String,
    val chat_session_id: String? = null,  // Multi-turn conversation session
    val user_id: String? = null
)

/**
 * Response from AskKlik RAG chat API
 */
@kotlinx.serialization.Serializable
data class AskKlikChatResponse(
    val response: String,
    val session_id: String,
    val user_id: String? = null,
    val sources: List<String>? = null,
    val retrieval_context: AskKlikRetrievalContext? = null,
    val matched_segments: List<AskKlikMatchedSegment>? = null
) {
    /**
     * Convert to domain ChatMessage (AI response only)
     */
    fun toAiMessage(): ChatMessage {
        val chatSources = mutableListOf<ChatSource>()

        // Add matched segments first (highest priority - specific navigable segments)
        matched_segments?.forEach { segment ->
            // PRODUCTION: meeting_title is required from backend - no fallback
            require(!segment.meeting_title.isNullOrBlank()) {
                "Backend must provide meeting_title for segment ${segment.segment_id}. " +
                "session_id=${segment.session_id}"
            }
            val displayTitle = if (!segment.meeting_time.isNullOrBlank()) {
                "${segment.meeting_title} (${segment.meeting_time})"
            } else {
                segment.meeting_title
            }

            chatSources.add(
                ChatSource(
                    id = segment.segment_id,
                    type = ChatSourceType.MEETING_SEGMENT,
                    title = displayTitle,
                    content = segment.text,
                    sessionId = segment.session_id,
                    score = segment.score + 100f, // Boost matched segments priority
                    metadata = mapOf(
                        "segment_id" to segment.segment_id,
                        "speaker" to (segment.speaker_name ?: "Unknown")
                    )
                )
            )
        }

        // Add related chunks as sources (meeting summaries)
        retrieval_context?.related_chunks?.forEach { chunk ->
            chatSources.add(
                ChatSource(
                    id = chunk.chunk_id,
                    type = ChatSourceType.SESSION_SUMMARY,
                    title = chunk.entity_names?.joinToString(", ") ?: "Meeting Summary",
                    content = chunk.content,
                    sessionId = chunk.session_id,
                    score = chunk.score,
                    metadata = mapOf(
                        "chunk_type" to chunk.chunk_type,
                        "keywords" to (chunk.keywords?.joinToString(", ") ?: "")
                    )
                )
            )
        }

        // Add related entities as sources
        retrieval_context?.related_entities?.forEach { entity ->
            chatSources.add(
                ChatSource(
                    id = entity.entity_id,
                    type = ChatSourceType.ENTITY,
                    title = entity.canonical_name,
                    content = entity.profile_text,
                    score = entity.score,
                    metadata = mapOf("entity_type" to entity.entity_type)
                )
            )
        }

        // Add related sessions as sources
        retrieval_context?.related_sessions?.forEach { session ->
            chatSources.add(
                ChatSource(
                    id = session.session_id,
                    type = ChatSourceType.SESSION_SUMMARY,
                    title = session.topics?.firstOrNull() ?: "Meeting Session",
                    content = session.summary_text,
                    sessionId = session.session_id,
                    score = session.score,
                    metadata = mapOf("topics" to (session.topics?.joinToString(", ") ?: ""))
                )
            )
        }

        // Sort by score (highest first) and take top sources
        val topSources = chatSources.sortedByDescending { it.score }.take(5)

        return ChatMessage(
            id = "ai_${session_id}_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}",
            text = response,
            isUser = false,
            timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            suggestions = emptyList(),
            actions = emptyList(),
            sources = topSources
        )
    }
}

/**
 * Retrieval context from AskKlik response
 */
@kotlinx.serialization.Serializable
data class AskKlikRetrievalContext(
    val related_entities: List<AskKlikRelatedEntity>? = null,
    val related_chunks: List<AskKlikRelatedChunk>? = null,
    val related_sessions: List<AskKlikRelatedSession>? = null,
    val related_segments: List<AskKlikMatchedSegment>? = null
)

@kotlinx.serialization.Serializable
data class AskKlikRelatedEntity(
    val entity_id: String,
    val entity_type: String,
    val canonical_name: String,
    val score: Float,
    val profile_text: String? = null
)

@kotlinx.serialization.Serializable
data class AskKlikRelatedChunk(
    val chunk_id: String,
    val session_id: String,
    val chunk_type: String,
    val content: String,
    val score: Float,
    val entity_names: List<String>? = null,
    val keywords: List<String>? = null
)

@kotlinx.serialization.Serializable
data class AskKlikRelatedSession(
    val session_id: String,
    val summary_text: String? = null,
    val topics: List<String>? = null,
    val score: Float
)

@kotlinx.serialization.Serializable
data class AskKlikMatchedSegment(
    val segment_id: String,
    val session_id: String,
    val speaker_name: String? = null,
    val text: String,
    val score: Float,
    val meeting_title: String? = null,
    val meeting_time: String? = null
)
