// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.presentation.rules

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RuleDto
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RulePreviewDto
import io.github.fletchmckee.liquid.samples.app.domain.repository.RulesRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.JsonObject

private class FakeRulesRepo(private val preview: RulePreviewDto) : RulesRepository {
  override suspend fun preview(nlText: String) = Result.Success(preview)
  override suspend fun create(nlText: String, preview: RulePreviewDto, isRecurring: Boolean) =
    Result.Success(
      RuleDto(
        id = "r1",
        source = "user_defined",
        nlText = nlText,
        triggerLabel = preview.triggerLabel,
        actionLabel = preview.actionLabel,
        status = "active",
        isRecurring = true,
        lastFiredAt = null,
      ),
    )
  override suspend fun list() = Result.Success(emptyList<RuleDto>())
  override suspend fun edit(
    id: String,
    nlText: String?,
    isRecurring: Boolean?,
    status: String?,
    snoozedUntil: String?,
  ) = Result.Error(NotImplementedError())
  override suspend fun delete(id: String) = Result.Error(NotImplementedError())
  override suspend fun accept(id: String) = Result.Error(NotImplementedError())
}

@OptIn(ExperimentalCoroutinesApi::class)
class NewRuleViewModelTest {
  private val dispatcher = StandardTestDispatcher()

  @BeforeTest fun setUp() { Dispatchers.setMain(dispatcher) }

  @AfterTest fun tearDown() { Dispatchers.resetMain() }

  @Test fun submitting_runs_preview_then_create_and_emits_done() = runTest {
    val fake = FakeRulesRepo(
      RulePreviewDto(
        triggerLabel = "every Mon 9 AM",
        actionLabel = "draft recap",
        approximationNote = null,
        parsedTrigger = JsonObject(emptyMap()),
        parsedAction = JsonObject(emptyMap()),
        signalBinding = "cron",
      ),
    )
    val vm = NewRuleViewModel(fake)
    vm.updateNl("every Mon 9, draft recap")
    vm.runPreview()
    advanceUntilIdle()
    assertEquals("every Mon 9 AM", vm.state.value.preview?.triggerLabel)
    vm.confirm()
    advanceUntilIdle()
    assertNotNull(vm.state.value.createdRuleId)
  }

  @Test fun edit_mode_calls_edit_not_create() = runTest {
    val edits = mutableListOf<Triple<String, String?, Boolean?>>()
    val fakeRepo = object : RulesRepository {
      override suspend fun preview(nlText: String) = Result.Success(
        RulePreviewDto(
          triggerLabel = "x",
          actionLabel = "x",
          approximationNote = null,
          parsedTrigger = JsonObject(emptyMap()),
          parsedAction = JsonObject(emptyMap()),
          signalBinding = "cron",
        ),
      )
      override suspend fun create(nlText: String, preview: RulePreviewDto, isRecurring: Boolean): Result<RuleDto> =
        error("create() should NOT be called in edit mode")
      override suspend fun edit(
        id: String,
        nlText: String?,
        isRecurring: Boolean?,
        status: String?,
        snoozedUntil: String?,
      ): Result<RuleDto> {
        edits += Triple(id, nlText, isRecurring)
        return Result.Success(
          RuleDto(
            id = id,
            source = "user_defined",
            nlText = nlText ?: "",
            triggerLabel = "x",
            actionLabel = "x",
            status = "active",
            isRecurring = true,
            lastFiredAt = null,
          ),
        )
      }
      override suspend fun list() = Result.Success(emptyList<RuleDto>())
      override suspend fun delete(id: String) = Result.Error(NotImplementedError())
      override suspend fun accept(id: String) = Result.Error(NotImplementedError())
    }
    val vm = NewRuleViewModel(fakeRepo)
    vm.beginEdit(
      RuleDto(
        id = "r9",
        source = "user_defined",
        nlText = "old text",
        triggerLabel = "x",
        actionLabel = "x",
        status = "active",
        isRecurring = true,
        lastFiredAt = null,
      ),
    )
    vm.updateNl("new text")
    vm.runPreview()
    advanceUntilIdle()
    vm.confirm()
    advanceUntilIdle()
    assertEquals(1, edits.size)
    assertEquals("r9", edits[0].first)
    assertEquals("new text", edits[0].second)
  }

  @Test fun edit_mode_no_op_when_text_unchanged_just_dismisses() = runTest {
    val edits = mutableListOf<Triple<String, String?, Boolean?>>()
    val fakeRepo = object : RulesRepository {
      override suspend fun preview(nlText: String) = Result.Success(
        RulePreviewDto(
          triggerLabel = "x",
          actionLabel = "x",
          approximationNote = null,
          parsedTrigger = JsonObject(emptyMap()),
          parsedAction = JsonObject(emptyMap()),
          signalBinding = "cron",
        ),
      )
      override suspend fun create(nlText: String, preview: RulePreviewDto, isRecurring: Boolean): Result<RuleDto> =
        error("create() should NOT be called in edit mode")
      override suspend fun edit(
        id: String,
        nlText: String?,
        isRecurring: Boolean?,
        status: String?,
        snoozedUntil: String?,
      ): Result<RuleDto> {
        edits += Triple(id, nlText, isRecurring)
        return Result.Success(
          RuleDto(id, "user_defined", nlText ?: "", "x", "x", "active", true, null),
        )
      }
      override suspend fun list() = Result.Success(emptyList<RuleDto>())
      override suspend fun delete(id: String) = Result.Error(NotImplementedError())
      override suspend fun accept(id: String) = Result.Error(NotImplementedError())
    }
    val vm = NewRuleViewModel(fakeRepo)
    vm.beginEdit(
      RuleDto("r9", "user_defined", "same text", "x", "x", "active", true, null),
    )
    // user does not change the text; runPreview unnecessary, confirm should
    // be a no-op (no edit call) and just emit Dismiss.
    vm.confirm()
    advanceUntilIdle()
    assertTrue(edits.isEmpty(), "edit() should not be called when text unchanged")
  }

  @Test fun reset_clears_edit_state() = runTest {
    val fake = FakeRulesRepo(
      RulePreviewDto(
        triggerLabel = "x",
        actionLabel = "x",
        approximationNote = null,
        parsedTrigger = JsonObject(emptyMap()),
        parsedAction = JsonObject(emptyMap()),
        signalBinding = "cron",
      ),
    )
    val vm = NewRuleViewModel(fake)
    vm.beginEdit(
      RuleDto("r9", "user_defined", "old", "x", "x", "active", true, null),
    )
    assertEquals("r9", vm.state.value.existingRuleId)
    vm.reset()
    assertNull(vm.state.value.existingRuleId)
    assertEquals("", vm.state.value.nlText)
  }
}
