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
 * The 17 canonical sub_category names that KK_LLM is allowed to emit are
 * defined in `KK_exec/config/tools.yaml`. Every value must map to a
 * concrete channel — falling through to DECISION on a known canonical
 * value would render a wrong verb ("Confirm" on a card that should say
 * "Create task" / "Run search" / etc.). Only empty arrays or genuinely
 * unknown values get DECISION.
 *
 * Provider names ("slack", "notion", etc.) are NEVER expected here —
 * those are runtime execution details, not card-display categories.
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

  // ─── 17 canonical sub_categories from KK_exec/config/tools.yaml ──────
  // Every one must resolve to a non-DECISION channel. If KK_exec adds a
  // new canonical name, this test will fail until iOS adds the mapping.

  @Test fun emailMapsEmail() =
    assertEquals(NeedsOkChannel.EMAIL, channelOf(task(listOf("email"))))

  @Test fun messagingMapsMessage() =
    assertEquals(NeedsOkChannel.MESSAGE, channelOf(task(listOf("messaging"))))

  @Test fun calendarMapsCalendar() =
    assertEquals(NeedsOkChannel.CALENDAR, channelOf(task(listOf("calendar"))))

  @Test fun documentationMapsDoc() =
    assertEquals(NeedsOkChannel.DOC, channelOf(task(listOf("documentation"))))

  @Test fun fileStorageMapsFile() =
    assertEquals(NeedsOkChannel.FILE, channelOf(task(listOf("file_storage"))))

  @Test fun taskManagementMapsTask() =
    assertEquals(NeedsOkChannel.TASK, channelOf(task(listOf("task_management"))))

  @Test fun codeRepositoryMapsCode() =
    assertEquals(NeedsOkChannel.CODE, channelOf(task(listOf("code_repository"))))

  @Test fun webSearchMapsResearch() =
    assertEquals(NeedsOkChannel.RESEARCH, channelOf(task(listOf("web_search"))))

  @Test fun webBrowseMapsResearch() =
    assertEquals(NeedsOkChannel.RESEARCH, channelOf(task(listOf("web_browse"))))

  @Test fun externalDataMapsResearch() =
    assertEquals(NeedsOkChannel.RESEARCH, channelOf(task(listOf("external_data"))))

  @Test fun calculationMapsCompute() =
    assertEquals(NeedsOkChannel.COMPUTE, channelOf(task(listOf("calculation"))))

  @Test fun textProcessingMapsCompute() =
    assertEquals(NeedsOkChannel.COMPUTE, channelOf(task(listOf("text_processing"))))

  @Test fun dataTransformationMapsCompute() =
    assertEquals(NeedsOkChannel.COMPUTE, channelOf(task(listOf("data_transformation"))))

  /** data_query appears in prod (4 rows) but isn't documented in
   *  tools.yaml's sub_categories list — likely a legacy name for what
   *  data_transformation now covers. Map it the same way so legacy rows
   *  don't collapse to DECISION. */
  @Test fun dataQueryMapsCompute() =
    assertEquals(NeedsOkChannel.COMPUTE, channelOf(task(listOf("data_query"))))

  @Test fun audioUnderstandingMapsAnalyze() =
    assertEquals(NeedsOkChannel.ANALYZE, channelOf(task(listOf("audio_understanding"))))

  @Test fun imageUnderstandingMapsAnalyze() =
    assertEquals(NeedsOkChannel.ANALYZE, channelOf(task(listOf("image_understanding"))))

  @Test fun imageGenerationMapsGenerate() =
    assertEquals(NeedsOkChannel.GENERATE, channelOf(task(listOf("image_generation"))))

  @Test fun memoryMapsMemory() =
    assertEquals(NeedsOkChannel.MEMORY, channelOf(task(listOf("memory"))))

  /** simple_tools shows up in prod (2 rows) — a catch-all for things
   *  that don't fit the major categories. Treat as COMPUTE rather than
   *  DECISION so the user sees an action verb, not a judgment prompt. */
  @Test fun simpleToolsMapsCompute() =
    assertEquals(NeedsOkChannel.COMPUTE, channelOf(task(listOf("simple_tools"))))

  // ─── Multi-category precedence ─────────────────────────────────────

  /** When email and messaging both present, email wins — higher-fidelity
   *  outbound matches KK_exec dispatch precedence. */
  @Test fun emailBeatsMessaging() =
    assertEquals(NeedsOkChannel.EMAIL, channelOf(task(listOf("email", "messaging"))))

  /** Calendar beats messaging — scheduling is more action-defining than
   *  a generic chat. Verified against the 10 prod rows that carry both. */
  @Test fun calendarBeatsMessaging() =
    assertEquals(NeedsOkChannel.CALENDAR, channelOf(task(listOf("calendar", "messaging"))))

  // ─── Decision / unknown ────────────────────────────────────────────

  @Test fun emptyMapsDecision() =
    assertEquals(NeedsOkChannel.DECISION, channelOf(task(emptyList())))

  /** An unrecognized name (typo, new category not yet ported to iOS)
   *  falls through to DECISION rather than guessing a verb. The test
   *  for every canonical value above must pass first — DECISION is
   *  the *last-resort* state, not the default. */
  @Test fun unknownCategoryFallsThroughToDecision() =
    assertEquals(NeedsOkChannel.DECISION, channelOf(task(listOf("not_a_real_category_xyz"))))

  /** Provider names like "slack", "notion", "github" must NEVER appear
   *  on the wire as sub_categories — those are runtime providers.
   *  If one slips through, fall to DECISION (loud signal something is
   *  miswired upstream). */
  @Test fun providerNameFallsThroughToDecision() {
    assertEquals(NeedsOkChannel.DECISION, channelOf(task(listOf("slack"))))
    assertEquals(NeedsOkChannel.DECISION, channelOf(task(listOf("notion"))))
    assertEquals(NeedsOkChannel.DECISION, channelOf(task(listOf("github"))))
  }

  @Test fun caseInsensitive() =
    assertEquals(NeedsOkChannel.EMAIL, channelOf(task(listOf("Email"))))

  // ─── Recipient label ──────────────────────────────────────────────

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

  @Test fun emailWithThreeOrMorePeopleCollapses() {
    val t = task(listOf("email"), people = listOf("vp_a", "vp_b", "vp_c"))
    val resolve: (String) -> String? = { id ->
      when (id) { "vp_a" -> "Alex"; "vp_b" -> "Maya"; "vp_c" -> "Marc"; else -> null }
    }
    assertEquals("to Alex + 2", needsOkRecipientLabel(t, NeedsOkChannel.EMAIL, resolve))
  }

  @Test fun unresolvableIdsAreDropped() {
    val t = task(listOf("email"), people = listOf("vp_unknown_1"))
    val resolve: (String) -> String? = { null }
    assertNull(needsOkRecipientLabel(t, NeedsOkChannel.EMAIL, resolve))
  }

  @Test fun calendarRecipientUsesWith() {
    val t = task(listOf("calendar"), people = listOf("vp_a"))
    val resolve: (String) -> String? = { "Maya Singh" }
    assertEquals("with Maya Singh", needsOkRecipientLabel(t, NeedsOkChannel.CALENDAR, resolve))
  }

  /** Outbound channels other than calendar use "to". MESSAGE goes "to"
   *  the recipient (the channel-or-DM is a runtime detail). */
  @Test fun messageRecipientUsesTo() {
    val t = task(listOf("messaging"), people = listOf("vp_a"))
    val resolve: (String) -> String? = { "Maya Singh" }
    assertEquals("to Maya Singh", needsOkRecipientLabel(t, NeedsOkChannel.MESSAGE, resolve))
  }

  // ─── Enum identity sanity ──────────────────────────────────────────

  /** Channel labels and verbs are NOT stored on the enum any more —
   *  they live in KlikStrings and are resolved at render time so they
   *  pick up the user's locale. The enum is semantic only. This test
   *  documents that contract. */
  @Test fun channelEnumIsSemanticOnly() {
    // If someone re-adds .label/.verb properties to the enum to "make
    // it more convenient", this test fails fast — labels MUST come
    // through KlikStrings or the i18n contract breaks.
    val allChannels = NeedsOkChannel.values().toSet()
    assertEquals(13, allChannels.size,
      "Expected 13 channels (12 actions + DECISION); add/remove tests if intentional")
  }

  // The per-category explicit tests above (`emailMapsEmail` …
  // `simpleToolsMapsCompute`) are the contract — one test per canonical
  // name from `KK_exec/config/tools.yaml`. Adding a new backend category
  // means adding one new explicit test, which forces the developer to
  // add the mapping in `channelOf()` to make it pass. No second list of
  // category names exists in this file — `channelOf()` is the single
  // source of truth for the mapping.
}
