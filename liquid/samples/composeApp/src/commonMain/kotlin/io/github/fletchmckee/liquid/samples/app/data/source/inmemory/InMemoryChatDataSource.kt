package io.github.fletchmckee.liquid.samples.app.data.source.inmemory

import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatMessage
import io.github.fletchmckee.liquid.samples.app.domain.entity.SuggestedQuestion
import io.github.fletchmckee.liquid.samples.app.domain.repository.ChatContext
import io.github.fletchmckee.liquid.samples.app.domain.repository.TimeOfDay
import kotlinx.datetime.Clock

/**
 * In-memory data source for chat/AI assistant data.
 * Data is provided by the backend via RemoteDataFetcher.
 */
class InMemoryChatDataSource {

    private val chatMessages = mutableListOf<ChatMessage>()
    private var messageIdCounter = 0
    private var suggestedQuestions = mutableListOf<SuggestedQuestion>()

    /**
     * Set chat messages from backend.
     */
    fun setChatMessages(messages: List<ChatMessage>) {
        chatMessages.clear()
        chatMessages.addAll(messages)
        messageIdCounter = chatMessages.size
    }

    /**
     * Set suggested questions from backend.
     */
    fun setSuggestedQuestions(questions: List<SuggestedQuestion>) {
        suggestedQuestions.clear()
        suggestedQuestions.addAll(questions)
    }

    /**
     * Generate a default AI response.
     */
    private fun generateDefaultResponse(): ChatMessage {
        return ChatMessage(
            id = "msg${++messageIdCounter}",
            text = "I'm here to help! What would you like to know?",
            isUser = false,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
    }

    fun getChatMessages(): List<ChatMessage> = chatMessages.toList()

    fun sendMessage(message: String): ChatMessage {
        // Add user message
        val userMessage = ChatMessage(
            id = "msg${++messageIdCounter}",
            text = message,
            isUser = true,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        chatMessages.add(userMessage)

        // Generate AI response based on message content
        val response = generateResponse(message)
        chatMessages.add(response)

        return response
    }

    private fun generateResponse(message: String): ChatMessage {
        // Handled by ChatRepositoryImpl via AskKlik RAG API
        return generateDefaultResponse()
    }

    fun getSuggestedQuestions(): List<SuggestedQuestion> {
        return suggestedQuestions.toList()
    }

    fun getContextualSuggestions(context: ChatContext): List<SuggestedQuestion> {
        val suggestions = mutableListOf<SuggestedQuestion>()

        // Time-based suggestions
        when (context.timeOfDay) {
            TimeOfDay.MORNING -> {
                suggestions.add(SuggestedQuestion("What's on my schedule today?", "calendar"))
                suggestions.add(SuggestedQuestion("Prepare my morning briefing", "briefing"))
            }
            TimeOfDay.AFTERNOON -> {
                suggestions.add(SuggestedQuestion("How's my day going?", "summary"))
                suggestions.add(SuggestedQuestion("What meetings do I have left?", "calendar"))
            }
            TimeOfDay.EVENING -> {
                suggestions.add(SuggestedQuestion("Summarize my day", "summary"))
                suggestions.add(SuggestedQuestion("What's tomorrow looking like?", "calendar"))
            }
        }

        // Screen-based suggestions
        when (context.currentScreen) {
            "calendar" -> {
                suggestions.add(SuggestedQuestion("Find a free slot this week", "calendar"))
            }
            "events" -> {
                suggestions.add(SuggestedQuestion("Summarize recent meetings", "meetings"))
            }
            "worklife" -> {
                suggestions.add(SuggestedQuestion("How are my relationships trending?", "people"))
            }
        }

        // Meeting-based suggestions
        if (context.recentMeetingId != null) {
            suggestions.add(SuggestedQuestion("Summarize my last meeting", "meetings"))
            suggestions.add(SuggestedQuestion("What action items came from the meeting?", "tasks"))
        }

        return suggestions.take(5)
    }

    fun clearChatHistory() {
        chatMessages.clear()
    }

    fun deleteMessage(messageId: String): Boolean {
        return chatMessages.removeAll { it.id == messageId }
    }

    fun getChatHistory(startTime: Long, endTime: Long): List<ChatMessage> {
        return chatMessages.filter { it.timestamp in startTime..endTime }
    }

    fun exportChatHistory(): String {
        return chatMessages.joinToString("\n\n") { msg ->
            val sender = if (msg.isUser) "You" else "Klik AI"
            "[$sender]: ${msg.text}"
        }
    }
}
