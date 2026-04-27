// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.source.json

import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatAction
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatActionType
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatMessage
import io.github.fletchmckee.liquid.samples.app.domain.entity.SuggestedQuestion
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * DTO for chat JSON file
 */
@Serializable
data class ChatJsonDto(
  val chatMessages: List<ChatMessageDto>,
  val suggestedQuestions: List<SuggestedQuestionDto>,
)

@Serializable
data class ChatMessageDto(
  val id: String,
  val content: String,
  val isUser: Boolean,
  val timestamp: Long,
  val actions: List<MessageActionDto> = emptyList(),
  val relatedEntities: List<RelatedEntityDto> = emptyList(),
  val feedbackId: String? = null,
)

@Serializable
data class MessageActionDto(
  val type: String,
  val label: String,
  val data: Map<String, JsonElement> = emptyMap(),
)

@Serializable
data class RelatedEntityDto(
  val type: String,
  val id: String,
  val name: String,
)

@Serializable
data class SuggestedQuestionDto(
  val id: String,
  val text: String,
  val category: String,
  val icon: String? = null,
)

/**
 * Extension functions to convert DTOs to domain entities
 */
fun ChatMessageDto.toDomain(): ChatMessage = ChatMessage(
  id = id,
  text = content,
  isUser = isUser,
  timestamp = timestamp,
  suggestions = emptyList(),
  actions = actions.map { it.toDomain() },
)

fun MessageActionDto.toDomain(): ChatAction {
  val actionType = when (type.uppercase()) {
    "NAVIGATE" -> ChatActionType.NAVIGATE
    "CREATE_TASK" -> ChatActionType.CREATE_TASK
    "SEND_EMAIL", "OPEN_MEETING" -> ChatActionType.OPEN_MEETING
    "SHOW_CALENDAR" -> ChatActionType.SHOW_CALENDAR
    else -> ChatActionType.OTHER
  }

  val payload = data.mapValues { (_, value) ->
    try {
      value.jsonPrimitive.content
    } catch (e: Exception) {
      value.toString()
    }
  }

  return ChatAction(
    type = actionType,
    payload = payload,
  )
}

fun SuggestedQuestionDto.toDomain(): SuggestedQuestion = SuggestedQuestion(
  text = text,
  category = category.lowercase(),
)
