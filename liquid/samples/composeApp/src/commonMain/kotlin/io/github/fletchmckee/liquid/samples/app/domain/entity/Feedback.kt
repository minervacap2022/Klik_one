// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.entity

/**
 * Domain entity representing user feedback for AI corrections.
 */
data class Feedback(
  val id: String = "",
  val elementType: FeedbackElementType,
  val elementId: String,
  val originalText: String,
  val correction: String? = null,
  val isMarkedWrong: Boolean = false,
  val screen: String,
  val timestamp: Long,
  val type: FeedbackType = FeedbackType.NEUTRAL,
  val rating: Int = 0,
)

enum class FeedbackElementType {
  MEETING_TITLE,
  MEETING_SUMMARY,
  MEETING_MINUTE,
  PERSON_NAME,
  PERSON_ROLE,
  PROJECT_NAME,
  PROJECT_STATUS,
  TASK_TITLE,
  ORGANIZATION_NAME,
  BRIEFING_TEXT,
  CHAT_RESPONSE,
  AI_RESPONSE,
  OTHER,
}

/**
 * Type of feedback (positive, negative, neutral)
 */
enum class FeedbackType {
  POSITIVE,
  NEGATIVE,
  NEUTRAL,
}

/**
 * Feedback data from the UI component
 */
data class FeedbackData(
  val type: FeedbackType = FeedbackType.NEUTRAL,
  val comment: String? = null,
  val rating: Int = 0,
  val originalText: String = "",
  val correction: String? = null,
  val isMarkedWrong: Boolean = false,
)
