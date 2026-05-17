// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.presentation.rules

import io.github.fletchmckee.liquid.samples.app.core.BaseViewModel
import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RulePreviewDto
import io.github.fletchmckee.liquid.samples.app.domain.repository.RulesRepository

/**
 * UI state for the "Teach Klik a rule" sheet.
 *
 *  - `nlText` — what the user typed.
 *  - `preview` — parsed preview from KK_suggest after `runPreview()` succeeds.
 *  - `createdRuleId` — id of the persisted rule once `confirm()` succeeds.
 */
data class NewRuleUiState(
  val nlText: String = "",
  val preview: RulePreviewDto? = null,
  val isLoading: Boolean = false,
  val error: String? = null,
  val createdRuleId: String? = null,
)

sealed class NewRuleEvent {
  data object Dismiss : NewRuleEvent()
}

/**
 * Two-step flow: preview → confirm. The preview round-trip is mandatory so
 * KK_suggest can show the user the parsed trigger/action before persistence.
 */
class NewRuleViewModel(
  private val repo: RulesRepository,
) : BaseViewModel<NewRuleUiState, NewRuleEvent>() {

  override val initialState = NewRuleUiState()

  fun updateNl(text: String) {
    updateState { copy(nlText = text, error = null) }
  }

  fun runPreview() = launch {
    updateState { copy(isLoading = true, error = null) }
    when (val r = repo.preview(currentState.nlText)) {
      is Result.Success -> updateState { copy(preview = r.data, isLoading = false) }
      is Result.Error -> updateState { copy(isLoading = false, error = r.exception.message) }
      is Result.Loading -> Unit
    }
  }

  fun confirm() = launch {
    val p = currentState.preview ?: return@launch
    when (val r = repo.create(currentState.nlText, p, isRecurring = true)) {
      is Result.Success -> {
        updateState { copy(createdRuleId = r.data.id) }
        sendEvent(NewRuleEvent.Dismiss)
      }
      is Result.Error -> updateState { copy(error = r.exception.message) }
      is Result.Loading -> Unit
    }
  }
}
