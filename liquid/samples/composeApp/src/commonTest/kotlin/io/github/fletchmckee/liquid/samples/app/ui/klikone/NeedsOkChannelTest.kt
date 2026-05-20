// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Guards the mapping `TaskMetadata.toolCategoriesNeeded` → `NeedsOkChannel`.
 *
 * Backend KK_LLM emits `selected_sub_categories` as a sorted/dedup'd list
 * of canonical capability names (`email`, `messaging`, `calendar`,
 * `file_storage`, …) on every todo. The Heidi card (todo 3111,
 * e_complex_level3) deliberately carries an empty list because a $300M
 * valuation isn't channel-routable; that case must resolve to DECISION
 * so the card renders ⚖ Decision / Confirm — never a fake "Send email"
 * verb.
 *
 * No backend change is implied by these tests; the field exists in
 * production today (~85 rows on user_627db9bc alone) and reaches iOS
 * via RemoteDataFetcher.kt:1828.
 */
class NeedsOkChannelTest {

  private fun task(cats: List<String> = emptyList(), people: List<String> = emptyList()) =
    TaskMetadata(
      id = "t1",
      title = "Title",
      subtitle = "",
      context = "",
      relatedProject = "",
      relatedPeople = people,
      dueInfo = "",
      toolCategoriesNeeded = cats,
    )

  // ─── channel derivation ───────────────────────────────────────────────

  @Test fun emptyCategoriesIsDecision() =
    assertEquals(NeedsOkChannel.DECISION, channelOf(task(emptyList())))

  @Test fun emailCategoryMapsEmail() =
    assertEquals(NeedsOkChannel.EMAIL, channelOf(task(listOf("email"))))

  @Test fun messagingCategoryMapsSlack() =
    assertEquals(NeedsOkChannel.SLACK, channelOf(task(listOf("messaging"))))

  @Test fun calendarCategoryMapsCalendar() =
    assertEquals(NeedsOkChannel.CALENDAR, channelOf(task(listOf("calendar"))))

  @Test fun fileStorageMapsDoc() =
    assertEquals(NeedsOkChannel.DOC, channelOf(task(listOf("file_storage"))))

  @Test fun documentationMapsDoc() =
    assertEquals(NeedsOkChannel.DOC, channelOf(task(listOf("documentation"))))

  /** Email wins over messaging when both present — picking the higher-
   *  fidelity outbound channel matches what KK_exec does at execution
   *  time (gmail dispatch precedes slack). Tested against prod combos
   *  like `["email", "messaging"]` which appear on 19 rows for one user. */
  @Test fun emailWinsOverMessagingWhenBothPresent() =
    assertEquals(NeedsOkChannel.EMAIL, channelOf(task(listOf("email", "messaging"))))

  /** Unknown sub-category falls through to DECISION rather than
   *  pretending to know a channel we don't have a verb for. The
   *  alternative — guessing "Send" — would put a fake action button on
   *  a card with no real outbound. */
  @Test fun unknownCategoryFallsThroughToDecision() =
    assertEquals(NeedsOkChannel.DECISION, channelOf(task(listOf("web_search"))))

  /** Production values are lowercase but the validator at todo_item.py
   *  doesn't case-normalize; defensive lowercasing here keeps us safe
   *  if the upstream prompt changes. */
  @Test fun categoryCasingIgnored() =
    assertEquals(NeedsOkChannel.EMAIL, channelOf(task(listOf("Email"))))

  // ─── enum verb labels — what the action button says ───────────────────

  @Test fun decisionVerbIsConfirm() = assertEquals("Confirm", NeedsOkChannel.DECISION.verb)
  @Test fun emailVerbIsSendEmail() = assertEquals("Send email", NeedsOkChannel.EMAIL.verb)
  @Test fun slackVerbIsSendSlack() = assertEquals("Send Slack", NeedsOkChannel.SLACK.verb)
  @Test fun calendarVerbIsSendInvite() = assertEquals("Send invite", NeedsOkChannel.CALENDAR.verb)
  @Test fun docVerbIsSaveDoc() = assertEquals("Save doc", NeedsOkChannel.DOC.verb)

  // ─── enum chip labels — what the chip pill says ───────────────────────

  @Test fun decisionChipLabel() = assertEquals("Decision", NeedsOkChannel.DECISION.label)
  @Test fun emailChipLabel() = assertEquals("Email", NeedsOkChannel.EMAIL.label)
  @Test fun slackChipLabel() = assertEquals("Slack", NeedsOkChannel.SLACK.label)
  @Test fun calendarChipLabel() = assertEquals("Calendar", NeedsOkChannel.CALENDAR.label)
  @Test fun docChipLabel() = assertEquals("Doc", NeedsOkChannel.DOC.label)

  // ─── recipient line — "to <name(s)>" ──────────────────────────────────

  /** Decision cards never carry a recipient line — there's no "to" when
   *  the action is a judgment call. Even if relatedPeople is populated
   *  (LLM pulled in stakeholders for context), the chip's recipient
   *  slot must stay empty. */
  @Test fun decisionHasNoRecipientEvenWhenPeoplePopulated() {
    val t = task(emptyList(), people = listOf("vp_marc_devereux"))
    val resolve: (String) -> String? = { "Marc Devereux" }
    assertNull(needsOkRecipientLabel(t, channelOf(t), resolve))
  }

  @Test fun emailWithOnePersonReadsToName() {
    val t = task(listOf("email"), people = listOf("vp_marc_devereux"))
    val resolve: (String) -> String? = { id -> if (id == "vp_marc_devereux") "Marc Devereux" else null }
    assertEquals("to Marc Devereux", needsOkRecipientLabel(t, NeedsOkChannel.EMAIL, resolve))
  }

  @Test fun emailWithTwoPeopleReadsBothNames() {
    val t = task(listOf("email"), people = listOf("vp_a", "vp_b"))
    val resolve: (String) -> String? = { id ->
      when (id) { "vp_a" -> "Alex Rivera"; "vp_b" -> "Maya Singh"; else -> null }
    }
    assertEquals("to Alex Rivera, Maya Singh", needsOkRecipientLabel(t, NeedsOkChannel.EMAIL, resolve))
  }

  /** Three+ recipients collapse to "Alex Rivera + 2" to keep the chip
   *  on one line at iPhone widths. Picked over "Alex et al." which
   *  reads vaguer. */
  @Test fun emailWithThreeOrMorePeopleCollapses() {
    val t = task(listOf("email"), people = listOf("vp_a", "vp_b", "vp_c"))
    val resolve: (String) -> String? = { id ->
      when (id) { "vp_a" -> "Alex"; "vp_b" -> "Maya"; "vp_c" -> "Marc"; else -> null }
    }
    assertEquals("to Alex + 2", needsOkRecipientLabel(t, NeedsOkChannel.EMAIL, resolve))
  }

  /** When relatedPeople contains an id the resolver doesn't know, drop
   *  the unknown entries silently rather than rendering "to vp_xxx"
   *  (raw IDs are ugly and leak schema). Empty after filter → no line. */
  @Test fun unresolvableIdsAreDropped() {
    val t = task(listOf("email"), people = listOf("vp_unknown_1", "vp_unknown_2"))
    val resolve: (String) -> String? = { null }
    assertNull(needsOkRecipientLabel(t, NeedsOkChannel.EMAIL, resolve))
  }

  @Test fun calendarRecipientUsesSameFormat() {
    val t = task(listOf("calendar"), people = listOf("vp_a"))
    val resolve: (String) -> String? = { "Maya Singh" }
    assertEquals("with Maya Singh", needsOkRecipientLabel(t, NeedsOkChannel.CALENDAR, resolve))
  }
}
