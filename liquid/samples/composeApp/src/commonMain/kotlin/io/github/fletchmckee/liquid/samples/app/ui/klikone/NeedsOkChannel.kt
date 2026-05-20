// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata

/**
 * Channel a Need-attention card represents. Derived from the backend's
 * `selected_sub_categories` array (exposed on iOS as
 * `TaskMetadata.toolCategoriesNeeded`), which KK_LLM emits on every
 * todo. **No backend change implied** — this enum just gives the UI a
 * single source of truth for what label + verb to show.
 *
 * Empty `toolCategoriesNeeded` → [DECISION]. The Heidi card
 * (todo 3111, $300M valuation) is intentionally empty because high-
 * stakes judgment calls aren't channel-routable; showing "Send email"
 * on those would be a lie.
 */
enum class NeedsOkChannel(val label: String, val verb: String) {
  EMAIL("Email", "Send email"),
  SLACK("Slack", "Send Slack"),
  CALENDAR("Calendar", "Send invite"),
  DOC("Doc", "Save doc"),
  DECISION("Decision", "Confirm"),
}

/**
 * Map [TaskMetadata.toolCategoriesNeeded] to a single [NeedsOkChannel].
 *
 * When multiple capabilities are present we pick the higher-fidelity
 * outbound channel: email beats messaging beats calendar beats doc.
 * That matches the precedence KK_exec uses at execution time (gmail
 * dispatch before slack). Unknown sub-categories fall through to
 * [DECISION] rather than guessing a verb — better to say "Confirm" than
 * to surface a fake "Send" button on a card with no real outbound.
 */
fun channelOf(t: TaskMetadata): NeedsOkChannel {
  val cats = t.toolCategoriesNeeded.map { it.lowercase() }.toSet()
  return when {
    "email" in cats -> NeedsOkChannel.EMAIL
    "messaging" in cats -> NeedsOkChannel.SLACK
    "calendar" in cats -> NeedsOkChannel.CALENDAR
    "file_storage" in cats || "documentation" in cats -> NeedsOkChannel.DOC
    else -> NeedsOkChannel.DECISION
  }
}

/**
 * Recipient label for the channel chip — e.g. `"to Marc Devereux"`,
 * `"with Maya Singh"`, `"to Alex + 2"`. Returns null when the card has
 * no recipient line to render:
 *   - [NeedsOkChannel.DECISION]: never has a recipient (no "to" on a
 *     judgment call) even when `relatedPeople` is populated for context.
 *   - all `relatedPeople` ids fail to resolve: rather than rendering
 *     raw `vp_xxx` ids (ugly + leaks schema) we drop the line entirely.
 *
 * @param resolve voiceprint_id → display name; null means unknown.
 */
fun needsOkRecipientLabel(
  t: TaskMetadata,
  channel: NeedsOkChannel,
  resolve: (String) -> String?,
): String? {
  if (channel == NeedsOkChannel.DECISION) return null
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
