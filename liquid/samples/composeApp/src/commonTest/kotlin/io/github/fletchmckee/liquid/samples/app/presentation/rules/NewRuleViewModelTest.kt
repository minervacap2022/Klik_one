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
  override suspend fun edit(id: String, nlText: String?, isRecurring: Boolean?, status: String?) =
    Result.Error(NotImplementedError())
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
}
