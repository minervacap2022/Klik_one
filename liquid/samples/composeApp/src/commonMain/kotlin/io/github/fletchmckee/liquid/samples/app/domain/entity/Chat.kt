package io.github.fletchmckee.liquid.samples.app.domain.entity

/**
 * Domain entity representing a chat message.
 */
data class ChatMessage(
    val id: String = "",
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = 0,
    val suggestions: List<String> = emptyList(),
    val actions: List<ChatAction> = emptyList(),
    val sources: List<ChatSource> = emptyList()
)

/**
 * Source reference for AI response - allows navigation to original content
 */
data class ChatSource(
    val id: String,
    val type: ChatSourceType,
    val title: String,
    val content: String? = null,
    val sessionId: String? = null,
    val score: Float = 0f,
    val metadata: Map<String, String> = emptyMap()
)

enum class ChatSourceType {
    MEETING_SEGMENT,
    ENTITY,
    SESSION_SUMMARY,
    OTHER
}

/**
 * Actionable item returned by AI
 */
data class ChatAction(
    val type: ChatActionType,
    val payload: Map<String, String> = emptyMap()
)

enum class ChatActionType {
    NAVIGATE,
    OPEN_MEETING,
    CREATE_TASK,
    SHOW_CALENDAR,
    OTHER
}

/**
 * Suggested questions for quick access
 */
data class SuggestedQuestion(
    val text: String,
    val category: String = "general"
)

val defaultSuggestedQuestions = listOf(
    SuggestedQuestion("What's on my schedule today?", "calendar"),
    SuggestedQuestion("Summarize my last meeting", "meetings"),
    SuggestedQuestion("What tasks need my attention?", "tasks"),
    SuggestedQuestion("Who should I follow up with?", "people"),
    SuggestedQuestion("What's my next meeting about?", "calendar")
)
