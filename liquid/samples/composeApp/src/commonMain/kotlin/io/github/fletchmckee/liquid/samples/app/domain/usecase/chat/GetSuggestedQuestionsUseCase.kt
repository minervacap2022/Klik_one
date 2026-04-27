// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.chat

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.SuggestedQuestion
import io.github.fletchmckee.liquid.samples.app.domain.repository.ChatContext
import io.github.fletchmckee.liquid.samples.app.domain.repository.ChatRepository

/**
 * Use case for getting suggested questions.
 */
class GetSuggestedQuestionsUseCase(
  private val chatRepository: ChatRepository,
) {
  suspend operator fun invoke(context: ChatContext? = null): Result<List<SuggestedQuestion>> = if (context != null) {
    chatRepository.getContextualSuggestions(context)
  } else {
    chatRepository.getSuggestedQuestions()
  }
}
