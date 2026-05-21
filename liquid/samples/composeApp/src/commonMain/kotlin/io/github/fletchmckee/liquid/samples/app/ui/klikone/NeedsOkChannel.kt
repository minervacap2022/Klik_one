// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata

/**
 * Channel a Need-attention card represents. Derived from the backend's
 * `selected_sub_categories` array (exposed on iOS as
 * `TaskMetadata.toolCategoriesNeeded`), which KK_LLM emits on every
 * todo. Backend taxonomy source-of-truth: `KK_exec/config/tools.yaml`
 * has 17 canonical sub_category names; every one maps to a concrete
 * channel here. Falling through to DECISION on a *known* canonical
 * value would render a wrong verb — that's a regression and the test
 * suite enforces non-DECISION for every canonical name.
 *
 * Provider details (Slack vs Teams vs Google Chat for `messaging`,
 * Gmail vs Outlook for `email`, etc.) are chosen at execution time
 * by KK_exec and stay off the card. The chip shows the *action class*
 * — "Send message" — not the provider — "Send Slack."
 *
 * Labels and verbs are NOT stored on enum constants — they live in
 * KlikStrings and resolve via `LocalKlikStrings.current` at render
 * time so all 8 languages get the right text.
 *
 * Empty `toolCategoriesNeeded` → [DECISION] (high-stakes judgment call
 * with no routable channel — verb is "Confirm," never a fake "Send").
 */
enum class NeedsOkChannel {
  // Outbound action channels
  EMAIL,      // email
  MESSAGE,    // messaging (Slack / Teams / Google Chat — provider chosen at exec time)
  CALENDAR,   // calendar
  DOC,        // documentation (Notion / Google Docs / etc.)
  FILE,       // file_storage (Drive / OneDrive)
  TASK,       // task_management (ClickUp / Linear / Asana / Jira / Monday)
  CODE,       // code_repository (GitHub)

  // Read / compute channels
  RESEARCH,   // web_search + web_browse + external_data
  COMPUTE,    // calculation + text_processing + data_transformation + data_query + simple_tools
  ANALYZE,    // audio_understanding + image_understanding
  GENERATE,   // image_generation
  MEMORY,     // memory

  // Last-resort
  DECISION,   // empty or unknown — user judgment required, nothing to send
}

/**
 * Map [TaskMetadata.toolCategoriesNeeded] to a single [NeedsOkChannel].
 *
 * Precedence when multiple categories present mirrors KK_exec's dispatch
 * order at execution time: outbound channels (email > message > calendar)
 * beat read-only ones (research / compute / analyze). Unknown values
 * fall through to DECISION rather than guessing a verb — better to say
 * "Confirm" than to surface a fake "Send" button on a card with no
 * real outbound.
 */
fun channelOf(t: TaskMetadata): NeedsOkChannel {
  val cats = t.toolCategoriesNeeded.map { it.lowercase() }.toSet()

  // Outbound (highest fidelity wins)
  if ("email" in cats) return NeedsOkChannel.EMAIL
  if ("calendar" in cats) return NeedsOkChannel.CALENDAR
  if ("messaging" in cats) return NeedsOkChannel.MESSAGE
  if ("task_management" in cats) return NeedsOkChannel.TASK
  if ("code_repository" in cats) return NeedsOkChannel.CODE
  if ("documentation" in cats) return NeedsOkChannel.DOC
  if ("file_storage" in cats) return NeedsOkChannel.FILE

  // Generation
  if ("image_generation" in cats) return NeedsOkChannel.GENERATE

  // Analyze (perception class)
  if ("audio_understanding" in cats || "image_understanding" in cats) {
    return NeedsOkChannel.ANALYZE
  }

  // Research (information retrieval)
  if ("web_search" in cats || "web_browse" in cats || "external_data" in cats) {
    return NeedsOkChannel.RESEARCH
  }

  // Compute (pure data / text work; `data_query` is a legacy alias
  // for `data_transformation` — 4 prod rows still carry the old name)
  if ("calculation" in cats || "text_processing" in cats ||
      "data_transformation" in cats || "data_query" in cats ||
      "simple_tools" in cats) {
    return NeedsOkChannel.COMPUTE
  }

  if ("memory" in cats) return NeedsOkChannel.MEMORY

  return NeedsOkChannel.DECISION
}

/**
 * Recipient label for the channel chip — e.g. `"to Marc Devereux"`,
 * `"with Maya Singh"`, `"to Alex + 2"`. Null = no recipient line.
 *
 * - [NeedsOkChannel.DECISION]: never has a recipient.
 * - Channels with no human counterparty (RESEARCH, COMPUTE, ANALYZE,
 *   GENERATE, MEMORY): no recipient even if `relatedPeople` is set —
 *   those people are context, not addressees.
 * - All `relatedPeople` ids fail to resolve: drop the line rather than
 *   rendering raw `vp_xxx` ids.
 *
 * Preposition: "with" for CALENDAR (you meet *with* someone), "to" for
 * everything else outbound.
 */
fun needsOkRecipientLabel(
  t: TaskMetadata,
  channel: NeedsOkChannel,
  resolve: (String) -> String?,
): String? {
  val outbound = channel in setOf(
    NeedsOkChannel.EMAIL,
    NeedsOkChannel.MESSAGE,
    NeedsOkChannel.CALENDAR,
    NeedsOkChannel.TASK,
    NeedsOkChannel.CODE,
    NeedsOkChannel.DOC,
    NeedsOkChannel.FILE,
  )
  if (!outbound) return null

  val names = t.relatedPeople.mapNotNull(resolve).filter { it.isNotBlank() }
  if (names.isEmpty()) return null
  val preposition = if (channel == NeedsOkChannel.CALENDAR) "with" else "to"
  val body = when {
    names.size == 1 -> names[0]
    names.size == 2 -> "${names[0]}, ${names[1]}"
    else -> "${names[0]} + ${names.size - 1}"
  }
  return "$preposition $body"
}
