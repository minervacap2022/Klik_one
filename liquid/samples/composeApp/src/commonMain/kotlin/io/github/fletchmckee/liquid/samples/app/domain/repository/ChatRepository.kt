// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatMessage
import io.github.fletchmckee.liquid.samples.app.domain.entity.SuggestedQuestion
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat/AI assistant operations.
 */
interface ChatRepository {

  /**
   * Get chat messages as a reactive flow.
   */
  fun getChatMessagesFlow(): Flow<Result<List<ChatMessage>>>

  /**
   * Get all chat messages.
   */
  suspend fun getChatMessages(): Result<List<ChatMessage>>

  /**
   * Send a message to the AI and get a response.
   */
  suspend fun sendMessage(message: String): Result<ChatMessage>

  /**
   * Stream AI response for real-time display.
   */
  fun streamResponse(message: String): Flow<Result<String>>

  /**
   * Get suggested questions for the user.
   */
  suspend fun getSuggestedQuestions(): Result<List<SuggestedQuestion>>

  /**
   * Get suggested questions based on context.
   */
  suspend fun getContextualSuggestions(context: ChatContext): Result<List<SuggestedQuestion>>

  /**
   * Clear chat history.
   */
  suspend fun clearChatHistory(): Result<Unit>

  /**
   * Delete a specific message.
   */
  suspend fun deleteMessage(messageId: String): Result<Unit>

  /**
   * Get chat history for a specific time range.
   */
  suspend fun getChatHistory(startTime: Long, endTime: Long): Result<List<ChatMessage>>

  /**
   * Export chat history.
   */
  suspend fun exportChatHistory(): Result<String>
}

/**
 * Context for generating contextual suggestions.
 */
data class ChatContext(
  val currentScreen: String,
  val recentMeetingId: String? = null,
  val recentTaskId: String? = null,
  val timeOfDay: TimeOfDay = TimeOfDay.AFTERNOON,
)

enum class TimeOfDay {
  MORNING,
  AFTERNOON,
  EVENING,
}
