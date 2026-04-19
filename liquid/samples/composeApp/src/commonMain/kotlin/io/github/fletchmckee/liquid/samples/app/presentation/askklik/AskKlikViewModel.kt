package io.github.fletchmckee.liquid.samples.app.presentation.askklik

import io.github.fletchmckee.liquid.samples.app.core.BaseViewModel
import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.di.AppModule
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatMessage
import io.github.fletchmckee.liquid.samples.app.domain.entity.SuggestedQuestion
import io.github.fletchmckee.liquid.samples.app.domain.entity.Feedback
import io.github.fletchmckee.liquid.samples.app.domain.entity.FeedbackData
import io.github.fletchmckee.liquid.samples.app.domain.entity.FeedbackElementType
import io.github.fletchmckee.liquid.samples.app.domain.entity.FeedbackType
import kotlinx.datetime.toLocalDateTime

/**
 * UI State for AskKlikScreen (Chat with AI)
 */
data class AskKlikUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isStreaming: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val suggestedQuestions: List<SuggestedQuestion> = emptyList(),
    val currentInput: String = "",
    val streamingResponse: String = "",
    val selectedMessage: ChatMessage? = null,
    val showFeedbackDialog: Boolean = false,
    val feedbackMessageId: String? = null,
    val showClearHistoryConfirmation: Boolean = false,
    val error: String? = null
)

/**
 * One-time events for AskKlikScreen
 */
sealed class AskKlikEvent {
    data class ShowError(val message: String) : AskKlikEvent()
    data class MessageSent(val messageId: String) : AskKlikEvent()
    data object ResponseReceived : AskKlikEvent()
    data object FeedbackSubmitted : AskKlikEvent()
    data object HistoryCleared : AskKlikEvent()
    data class ActionTriggered(val actionType: String, val actionData: Map<String, String>) : AskKlikEvent()
    data class CopyToClipboard(val text: String) : AskKlikEvent()
}

/**
 * ViewModel for AskKlikScreen (AI Chat Assistant)
 * Handles chat messages, suggestions, and feedback
 */
class AskKlikViewModel : BaseViewModel<AskKlikUiState, AskKlikEvent>() {

    override val initialState = AskKlikUiState()

    // Use Cases
    private val sendChatMessageUseCase = AppModule.sendChatMessageUseCase
    private val getSuggestedQuestionsUseCase = AppModule.getSuggestedQuestionsUseCase
    private val observeChatMessagesUseCase = AppModule.observeChatMessagesUseCase
    private val submitFeedbackUseCase = AppModule.submitFeedbackUseCase

    init {
        loadInitialData()
        observeMessages()
    }

    private fun loadInitialData() {
        loadSuggestedQuestions()
    }

    private fun observeMessages() {
        launch {
            observeChatMessagesUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        updateState {
                            copy(
                                messages = result.data,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> {
                        updateState {
                            copy(
                                error = result.message,
                                isLoading = false
                            )
                        }
                    }
                    is Result.Loading -> {
                        updateState { copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun loadSuggestedQuestions() {
        launch {
            when (val result = getSuggestedQuestionsUseCase()) {
                is Result.Success -> {
                    updateState { copy(suggestedQuestions = result.data) }
                }
                is Result.Error -> {
                    sendEvent(AskKlikEvent.ShowError(result.message ?: "Failed to load suggestions"))
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    fun updateInput(input: String) {
        updateState { copy(currentInput = input) }
    }

    fun sendMessage() {
        val message = currentState.currentInput.trim()
        if (message.isBlank()) return

        launch {
            updateState {
                copy(
                    isSending = true,
                    currentInput = ""
                )
            }

            when (val result = sendChatMessageUseCase(message)) {
                is Result.Success -> {
                    updateState { copy(isSending = false) }
                    sendEvent(AskKlikEvent.MessageSent(result.data.id))
                    sendEvent(AskKlikEvent.ResponseReceived)
                    // Refresh suggestions after sending
                    loadSuggestedQuestions()
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isSending = false,
                            currentInput = message // Restore input on error
                        )
                    }
                    sendEvent(AskKlikEvent.ShowError(result.message ?: "Failed to send message"))
                }
                is Result.Loading -> {
                    updateState { copy(isSending = true) }
                }
            }
        }
    }

    fun sendSuggestedQuestion(question: SuggestedQuestion) {
        updateState { copy(currentInput = question.text) }
        sendMessage()
    }

    fun selectMessage(messageId: String) {
        val message = currentState.messages.find { it.id == messageId }
        updateState { copy(selectedMessage = message) }
    }

    fun deselectMessage() {
        updateState { copy(selectedMessage = null) }
    }

    fun showFeedbackDialog(messageId: String) {
        updateState {
            copy(
                showFeedbackDialog = true,
                feedbackMessageId = messageId
            )
        }
    }

    fun dismissFeedbackDialog() {
        updateState {
            copy(
                showFeedbackDialog = false,
                feedbackMessageId = null
            )
        }
    }

    fun submitFeedback(isPositive: Boolean, comment: String? = null) {
        val messageId = currentState.feedbackMessageId ?: return

        launch {
            val feedbackData = FeedbackData(
                type = if (isPositive) FeedbackType.POSITIVE else FeedbackType.NEGATIVE,
                comment = comment,
                rating = if (isPositive) 5 else 1
            )

            when (val result = submitFeedbackUseCase(
                elementType = FeedbackElementType.AI_RESPONSE,
                elementId = messageId,
                feedbackData = feedbackData,
                screen = "AskKlikScreen"
            )) {
                is Result.Success -> {
                    dismissFeedbackDialog()
                    sendEvent(AskKlikEvent.FeedbackSubmitted)
                }
                is Result.Error -> {
                    sendEvent(AskKlikEvent.ShowError(result.message ?: "Failed to submit feedback"))
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    fun showClearHistoryConfirmation() {
        updateState { copy(showClearHistoryConfirmation = true) }
    }

    fun dismissClearHistoryConfirmation() {
        updateState { copy(showClearHistoryConfirmation = false) }
    }

    fun confirmClearHistory() {
        launch {
            val chatRepo = AppModule.chatRepository as io.github.fletchmckee.liquid.samples.app.data.repository.ChatRepositoryImpl
            chatRepo.clearChatHistory()
            updateState {
                copy(
                    messages = emptyList(),
                    showClearHistoryConfirmation = false
                )
            }
            sendEvent(AskKlikEvent.HistoryCleared)
        }
    }

    fun handleAction(actionType: String, actionData: Map<String, String>) {
        sendEvent(AskKlikEvent.ActionTriggered(actionType, actionData))
    }

    fun copyMessageToClipboard(messageId: String) {
        val message = currentState.messages.find { it.id == messageId } ?: return
        sendEvent(AskKlikEvent.CopyToClipboard(message.text))
    }

    fun clearError() {
        updateState { copy(error = null) }
    }

    fun refresh() {
        loadInitialData()
    }

    /**
     * Get messages grouped by date for display
     */
    fun getMessagesGroupedByDate(): Map<String, List<ChatMessage>> {
        val nowMillis = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

        return currentState.messages.groupBy { message ->
            val diffMs = nowMillis - message.timestamp
            val diffDays = diffMs / (24 * 60 * 60 * 1000L)
            when {
                diffDays == 0L -> "Today"
                diffDays == 1L -> "Yesterday"
                diffDays < 7L -> "$diffDays days ago"
                else -> {
                    val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
                    val date = kotlinx.datetime.Instant.fromEpochMilliseconds(message.timestamp)
                        .toLocalDateTime(tz).date
                    "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
                }
            }
        }
    }

    /**
     * Get the last user message
     */
    fun getLastUserMessage(): ChatMessage? {
        return currentState.messages.lastOrNull { it.isUser }
    }

    /**
     * Get the last AI response
     */
    fun getLastAiResponse(): ChatMessage? {
        return currentState.messages.lastOrNull { !it.isUser }
    }

    /**
     * Check if there are any messages
     */
    fun hasMessages(): Boolean {
        return currentState.messages.isNotEmpty()
    }

    /**
     * Get chat statistics
     */
    fun getChatStats(): ChatStats {
        val messages = currentState.messages
        return ChatStats(
            totalMessages = messages.size,
            userMessages = messages.count { it.isUser },
            aiResponses = messages.count { !it.isUser }
        )
    }
}

/**
 * Statistics about chat history
 */
data class ChatStats(
    val totalMessages: Int,
    val userMessages: Int,
    val aiResponses: Int
)
