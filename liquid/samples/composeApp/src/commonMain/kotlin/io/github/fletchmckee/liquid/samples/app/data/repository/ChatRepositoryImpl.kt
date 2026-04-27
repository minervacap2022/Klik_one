// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser
import io.github.fletchmckee.liquid.samples.app.data.network.HttpClient
import io.github.fletchmckee.liquid.samples.app.data.network.dto.AskKlikChatRequest
import io.github.fletchmckee.liquid.samples.app.data.network.dto.AskKlikChatResponse
import io.github.fletchmckee.liquid.samples.app.data.storage.SecureStorage
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatMessage
import io.github.fletchmckee.liquid.samples.app.domain.entity.SuggestedQuestion
import io.github.fletchmckee.liquid.samples.app.domain.repository.ChatContext
import io.github.fletchmckee.liquid.samples.app.domain.repository.ChatRepository
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Implementation of ChatRepository.
 * Uses AskKlik RAG API (port 8333) for real chat functionality.
 * Uses HttpClient singleton which handles automatic token refresh on 401.
 * Persists chat history to SecureStorage across sessions.
 */
class ChatRepositoryImpl : ChatRepository {

  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
  }

  // Local message storage
  private val _messages = mutableListOf<ChatMessage>()
  private val _messagesFlow = MutableStateFlow<Result<List<ChatMessage>>>(Result.Success(emptyList()))

  // Session ID for multi-turn conversations
  private var currentSessionId: String? = null

  // Persistence
  private val storage = SecureStorage()

  init {
    loadPersistedHistory()
    refreshMessagesInternal()
  }

  // ==================== Persistence ====================

  @Serializable
  private data class PersistedChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long,
  )

  @Serializable
  private data class PersistedChatHistory(
    val messages: List<PersistedChatMessage> = emptyList(),
    val sessionId: String? = null,
  )

  private fun loadPersistedHistory() {
    try {
      val jsonString = storage.getString(CHAT_HISTORY_KEY) ?: return
      val history = json.decodeFromString<PersistedChatHistory>(jsonString)
      _messages.clear()
      _messages.addAll(
        history.messages.map { persisted ->
          ChatMessage(
            id = persisted.id,
            text = persisted.text,
            isUser = persisted.isUser,
            timestamp = persisted.timestamp,
          )
        },
      )
      currentSessionId = history.sessionId
      KlikLogger.i("ChatRepo", "Loaded ${_messages.size} messages from storage, sessionId=$currentSessionId")
    } catch (e: Exception) {
      KlikLogger.e("ChatRepo", "Failed to load chat history: ${e.message}", e)
    }
  }

  private fun persistHistory() {
    try {
      val history = PersistedChatHistory(
        messages = _messages.takeLast(MAX_PERSISTED_MESSAGES).map { msg ->
          PersistedChatMessage(
            id = msg.id,
            text = msg.text,
            isUser = msg.isUser,
            timestamp = msg.timestamp,
          )
        },
        sessionId = currentSessionId,
      )
      storage.saveString(CHAT_HISTORY_KEY, json.encodeToString(history))
    } catch (e: Exception) {
      KlikLogger.e("ChatRepo", "Failed to persist chat history: ${e.message}", e)
    }
  }

  private fun refreshMessagesInternal() {
    _messagesFlow.value = Result.Success(_messages.toList())
  }

  override fun getChatMessagesFlow(): Flow<Result<List<ChatMessage>>> = _messagesFlow

  override suspend fun getChatMessages(): Result<List<ChatMessage>> = Result.Success(_messages.toList())

  override suspend fun sendMessage(message: String): Result<ChatMessage> {
    return try {
      // Create user message and add to local list
      val userMessage = ChatMessage(
        id = "user_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}",
        text = message,
        isUser = true,
        timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
        suggestions = emptyList(),
        actions = emptyList(),
      )
      _messages.add(userMessage)
      refreshMessagesInternal()
      persistHistory()

      // Call AskKlik RAG API
      val request = AskKlikChatRequest(
        query = message,
        chat_session_id = currentSessionId, // Use chat_session_id (backend field name)
        user_id = CurrentUser.userId,
      )
      val requestBody = json.encodeToString(request)
      val url = "${ApiConfig.ASKKLIK_BASE_URL}${ApiConfig.Endpoints.ASKKLIK_CHAT}"

      KlikLogger.d("AskKlik", "POST $url with chat_session_id: $currentSessionId, user_id: ${CurrentUser.userId}")

      // Use HttpClient.postUrl which handles automatic token refresh on 401
      val responseText = HttpClient.postUrl(url, requestBody)

      if (responseText != null) {
        // Check if response is an error (contains "detail" field)
        if (responseText.contains("\"detail\"")) {
          // Parse error response
          val errorDetail = try {
            val errorJson = json.parseToJsonElement(responseText)
            errorJson.jsonObject["detail"]?.jsonPrimitive?.content ?: responseText
          } catch (e: Exception) {
            responseText
          }
          KlikLogger.e("AskKlik", "API Error: $errorDetail")

          // Create error message to show user
          val errorMessage = when {
            errorDetail.contains("401") || errorDetail.contains("无效的令牌") ->
              "Server AI service temporarily unavailable. Please try again later."

            errorDetail.contains("403") ->
              "Access denied. Please check your credentials."

            else ->
              "Chat service error. Please try again."
          }
          return Result.Error(Exception(errorDetail), errorMessage)
        }

        val response = json.decodeFromString<AskKlikChatResponse>(responseText)

        // Update session ID for multi-turn conversations
        currentSessionId = response.session_id
        KlikLogger.i("AskKlik", "Session ID updated to: $currentSessionId")

        // Create AI response message
        val aiMessage = response.toAiMessage()
        _messages.add(aiMessage)
        refreshMessagesInternal()
        persistHistory()

        Result.Success(aiMessage)
      } else {
        Result.Error(Exception("Empty response from AskKlik API"), "Failed to get response")
      }
    } catch (e: Exception) {
      KlikLogger.e("AskKlik", "Error: ${e.message}", e)
      Result.Error(e, "Failed to send message: ${e.message}")
    }
  }

  override fun streamResponse(message: String): Flow<Result<String>> = flow {
    // AskKlik API uses request-response, not streaming
    try {
      val result = sendMessage(message)
      when (result) {
        is Result.Success -> {
          emit(Result.Success(result.data.text))
        }

        is Result.Error -> {
          emit(Result.Error(result.exception, result.message))
        }

        is Result.Loading -> {
          // No-op
        }
      }
    } catch (e: Exception) {
      emit(Result.Error(e, "Failed to stream response"))
    }
  }

  @Serializable
  private data class SuggestionsApiResponse(
    val suggestions: List<SuggestionItem>,
  )

  @Serializable
  private data class SuggestionItem(
    val text: String,
    val category: String = "general",
  )

  override suspend fun getSuggestedQuestions(): Result<List<SuggestedQuestion>> {
    // Try fetching from backend first
    return try {
      val responseText = HttpClient.get(ApiConfig.Endpoints.SUGGESTED_QUESTIONS)
      if (responseText != null) {
        val response = json.decodeFromString<SuggestionsApiResponse>(responseText)
        if (response.suggestions.isNotEmpty()) {
          KlikLogger.i("ChatRepo", "Loaded ${response.suggestions.size} suggestions from backend")
          return Result.Success(response.suggestions.map { SuggestedQuestion(it.text, it.category) })
        }
      }
      Result.Success(defaultSuggestions())
    } catch (e: Exception) {
      KlikLogger.d("ChatRepo", "Backend suggestions unavailable, using defaults: ${e.message}")
      Result.Success(defaultSuggestions())
    }
  }

  private fun defaultSuggestions() = listOf(
    SuggestedQuestion("What's on my schedule today?", "calendar"),
    SuggestedQuestion("Summarize my last meeting", "meetings"),
    SuggestedQuestion("Who should I follow up with?", "people"),
    SuggestedQuestion("What tasks need my attention?", "tasks"),
    SuggestedQuestion("Tell me about recent projects", "projects"),
  )

  override suspend fun getContextualSuggestions(context: ChatContext): Result<List<SuggestedQuestion>> = getSuggestedQuestions()

  override suspend fun clearChatHistory(): Result<Unit> = try {
    _messages.clear()
    currentSessionId = null // Reset session for new conversation
    refreshMessagesInternal()
    storage.remove(CHAT_HISTORY_KEY)
    KlikLogger.i("ChatRepo", "Chat history cleared")
    Result.Success(Unit)
  } catch (e: Exception) {
    Result.Error(e, "Failed to clear chat history")
  }

  override suspend fun deleteMessage(messageId: String): Result<Unit> = try {
    val removed = _messages.removeAll { it.id == messageId }
    if (removed) {
      refreshMessagesInternal()
      persistHistory()
      Result.Success(Unit)
    } else {
      Result.Error(NoSuchElementException("Message not found"), "Message not found")
    }
  } catch (e: Exception) {
    Result.Error(e, "Failed to delete message")
  }

  override suspend fun getChatHistory(startTime: Long, endTime: Long): Result<List<ChatMessage>> = try {
    val filtered = _messages.filter { it.timestamp in startTime..endTime }
    Result.Success(filtered)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get chat history")
  }

  override suspend fun exportChatHistory(): Result<String> = try {
    val export = _messages.joinToString("\n\n") { msg ->
      val sender = if (msg.isUser) "You" else "Klik"
      "[$sender]: ${msg.text}"
    }
    Result.Success(export)
  } catch (e: Exception) {
    Result.Error(e, "Failed to export chat history")
  }

  /**
   * Get current session ID (for debugging/display)
   */
  fun getCurrentSessionId(): String? = currentSessionId

  /**
   * Start a new conversation (clears session)
   */
  fun startNewConversation() {
    currentSessionId = null
  }

  companion object {
    private const val CHAT_HISTORY_KEY = "chat_history"
    private const val MAX_PERSISTED_MESSAGES = 100
  }
}
