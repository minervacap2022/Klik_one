package io.github.fletchmckee.liquid.samples.app.domain.usecase.chat

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatMessage
import io.github.fletchmckee.liquid.samples.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing chat messages as a reactive flow.
 */
class ObserveChatMessagesUseCase(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(): Flow<Result<List<ChatMessage>>> {
        return chatRepository.getChatMessagesFlow()
    }
}
