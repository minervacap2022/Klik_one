// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.chat

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatMessage
import io.github.fletchmckee.liquid.samples.app.domain.repository.ChatRepository

/**
 * Use case for sending a chat message and getting AI response.
 */
class SendChatMessageUseCase(
  private val chatRepository: ChatRepository,
) {
  suspend operator fun invoke(message: String): Result<ChatMessage> {
    if (message.isBlank()) {
      return Result.Error(IllegalArgumentException("Message cannot be empty"))
    }
    return chatRepository.sendMessage(message)
  }
}
