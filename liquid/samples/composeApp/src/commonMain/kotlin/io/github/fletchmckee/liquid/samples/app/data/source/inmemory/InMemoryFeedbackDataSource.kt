// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.source.inmemory

import io.github.fletchmckee.liquid.samples.app.domain.entity.Feedback
import io.github.fletchmckee.liquid.samples.app.domain.entity.FeedbackData
import io.github.fletchmckee.liquid.samples.app.domain.entity.FeedbackElementType
import io.github.fletchmckee.liquid.samples.app.domain.repository.FeedbackStats
import kotlinx.datetime.Clock

/**
 * In-memory data source for feedback data.
 * Data is provided by the backend via RemoteDataFetcher.
 */
class InMemoryFeedbackDataSource {

  private val feedbackList = mutableListOf<Feedback>()
  private var feedbackIdCounter = 0

  /**
   * Set feedback data from backend.
   */
  fun setFeedback(feedback: List<Feedback>) {
    feedbackList.clear()
    feedbackList.addAll(feedback)
    feedbackIdCounter = feedbackList.size
  }

  fun getFeedback(): List<Feedback> = feedbackList.toList()

  fun getFeedbackById(id: String): Feedback? = feedbackList.find { it.id == id }

  fun getFeedbackForElement(
    elementType: FeedbackElementType,
    elementId: String,
  ): List<Feedback> = feedbackList.filter {
    it.elementType == elementType && it.elementId == elementId
  }

  fun getFeedbackForScreen(screen: String): List<Feedback> = feedbackList.filter { it.screen == screen }

  fun submitFeedback(
    elementType: FeedbackElementType,
    elementId: String,
    feedbackData: FeedbackData,
    screen: String,
  ): Feedback {
    val feedback = Feedback(
      id = "fb${++feedbackIdCounter}",
      elementType = elementType,
      elementId = elementId,
      originalText = feedbackData.originalText,
      correction = feedbackData.correction,
      isMarkedWrong = feedbackData.isMarkedWrong,
      screen = screen,
      timestamp = Clock.System.now().toEpochMilliseconds(),
    )
    feedbackList.add(feedback)
    return feedback
  }

  fun updateFeedback(feedback: Feedback): Feedback? {
    val index = feedbackList.indexOfFirst { it.id == feedback.id }
    if (index == -1) return null

    feedbackList[index] = feedback
    return feedback
  }

  fun deleteFeedback(feedbackId: String): Boolean = feedbackList.removeAll { it.id == feedbackId }

  fun getPendingFeedback(): List<Feedback> {
    // In a real implementation, this would return feedback not yet synced
    return feedbackList.takeLast(5)
  }

  fun syncFeedback(): Int {
    // In a real implementation, this would sync to server
    return feedbackList.size
  }

  fun getFeedbackStats(): FeedbackStats {
    val byType = feedbackList.groupBy { it.elementType }
      .mapValues { it.value.size }

    return FeedbackStats(
      totalFeedback = feedbackList.size,
      correctionsCount = feedbackList.count { it.correction != null },
      markedWrongCount = feedbackList.count { it.isMarkedWrong },
      feedbackByType = byType,
    )
  }
}
