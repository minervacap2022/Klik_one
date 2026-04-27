// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Feedback
import io.github.fletchmckee.liquid.samples.app.domain.entity.FeedbackData
import io.github.fletchmckee.liquid.samples.app.domain.entity.FeedbackElementType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for feedback operations.
 * Handles user corrections and feedback on AI-generated content.
 */
interface FeedbackRepository {

  /**
   * Get all feedback as a reactive flow.
   */
  fun getFeedbackFlow(): Flow<Result<List<Feedback>>>

  /**
   * Get all feedback.
   */
  suspend fun getFeedback(): Result<List<Feedback>>

  /**
   * Get feedback by ID.
   */
  suspend fun getFeedbackById(id: String): Result<Feedback>

  /**
   * Get feedback for a specific element.
   */
  suspend fun getFeedbackForElement(
    elementType: FeedbackElementType,
    elementId: String,
  ): Result<List<Feedback>>

  /**
   * Get feedback for a specific screen.
   */
  suspend fun getFeedbackForScreen(screen: String): Result<List<Feedback>>

  /**
   * Submit new feedback.
   */
  suspend fun submitFeedback(
    elementType: FeedbackElementType,
    elementId: String,
    feedbackData: FeedbackData,
    screen: String,
  ): Result<Feedback>

  /**
   * Update existing feedback.
   */
  suspend fun updateFeedback(feedback: Feedback): Result<Feedback>

  /**
   * Delete feedback.
   */
  suspend fun deleteFeedback(feedbackId: String): Result<Unit>

  /**
   * Get pending feedback (not yet synced to server).
   */
  suspend fun getPendingFeedback(): Result<List<Feedback>>

  /**
   * Sync pending feedback to server.
   */
  suspend fun syncFeedback(): Result<Int>

  /**
   * Get feedback statistics.
   */
  suspend fun getFeedbackStats(): Result<FeedbackStats>
}

/**
 * Feedback statistics.
 */
data class FeedbackStats(
  val totalFeedback: Int,
  val correctionsCount: Int,
  val markedWrongCount: Int,
  val feedbackByType: Map<FeedbackElementType, Int>,
)
