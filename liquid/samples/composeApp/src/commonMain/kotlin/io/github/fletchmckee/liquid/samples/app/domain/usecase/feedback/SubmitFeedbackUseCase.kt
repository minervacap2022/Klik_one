// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.feedback

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Feedback
import io.github.fletchmckee.liquid.samples.app.domain.entity.FeedbackData
import io.github.fletchmckee.liquid.samples.app.domain.entity.FeedbackElementType
import io.github.fletchmckee.liquid.samples.app.domain.repository.FeedbackRepository

/**
 * Use case for submitting feedback.
 */
class SubmitFeedbackUseCase(
  private val feedbackRepository: FeedbackRepository,
) {
  suspend operator fun invoke(
    elementType: FeedbackElementType,
    elementId: String,
    feedbackData: FeedbackData,
    screen: String,
  ): Result<Feedback> = feedbackRepository.submitFeedback(
    elementType = elementType,
    elementId = elementId,
    feedbackData = feedbackData,
    screen = screen,
  )
}
