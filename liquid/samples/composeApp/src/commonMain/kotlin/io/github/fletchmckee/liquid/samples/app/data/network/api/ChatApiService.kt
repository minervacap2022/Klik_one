package io.github.fletchmckee.liquid.samples.app.data.network.api

import io.github.fletchmckee.liquid.samples.app.data.network.ApiResponse
import io.github.fletchmckee.liquid.samples.app.data.network.dto.ChatMessageDto
import io.github.fletchmckee.liquid.samples.app.data.network.dto.SendChatMessageRequest
import io.github.fletchmckee.liquid.samples.app.data.network.dto.SendChatMessageResponse
import io.github.fletchmckee.liquid.samples.app.data.network.dto.SuggestedQuestionDto

/**
 * API service interface for chat-related endpoints.
 * Implementation will use Ktor client when enabled.
 */
interface ChatApiService {
    /**
     * Get chat message history
     * @param limit Maximum number of messages to return
     * @param offset Number of messages to skip
     */
    suspend fun getChatMessages(
        limit: Int = 50,
        offset: Int = 0
    ): ApiResponse<List<ChatMessageDto>>

    /**
     * Send a chat message and get AI response
     */
    suspend fun sendMessage(request: SendChatMessageRequest): ApiResponse<SendChatMessageResponse>

    /**
     * Get suggested questions based on context
     * @param currentScreen The current screen the user is viewing
     */
    suspend fun getSuggestedQuestions(
        currentScreen: String? = null
    ): ApiResponse<List<SuggestedQuestionDto>>

    /**
     * Clear chat history
     */
    suspend fun clearChatHistory(): ApiResponse<Unit>

    /**
     * Delete a specific message
     */
    suspend fun deleteMessage(messageId: String): ApiResponse<Unit>

    /**
     * Export chat history as text
     */
    suspend fun exportChatHistory(): ApiResponse<String>
}
