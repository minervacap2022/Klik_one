// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.presentation.rules

import io.github.fletchmckee.liquid.samples.app.core.BaseViewModel
import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RuleDto
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RulePreviewDto
import io.github.fletchmckee.liquid.samples.app.domain.repository.RulesRepository

/**
 * UI state for the "Teach Klik a rule" sheet.
 *
 *  - `nlText` — what the user typed.
 *  - `preview` — parsed preview from KK_suggest after `runPreview()` succeeds.
 *  - `createdRuleId` — id of the persisted rule once `confirm()` succeeds.
 *  - `existingRuleId` — non-null while editing an existing rule. Drives
 *    `confirm()` to PATCH instead of POST, and lets the sheet show the
 *    "Edit this rule" / "Save changes" copy.
 *  - `originalNlText` — captured at the start of an edit so `confirm()` can
 *    short-circuit when the user opens Edit but doesn't actually change text.
 */
data class NewRuleUiState(
  val nlText: String = "",
  val preview: RulePreviewDto? = null,
  val isLoading: Boolean = false,
  val error: String? = null,
  val createdRuleId: String? = null,
  val existingRuleId: String? = null,
  val originalNlText: String = "",
)

sealed class NewRuleEvent {
  data object Dismiss : NewRuleEvent()
}

/**
 * Two-step flow: preview → confirm. The preview round-trip is mandatory so
 * KK_suggest can show the user the parsed trigger/action before persistence.
 *
 * Edit mode (entered via [beginEdit]) skips the create path and PATCHes the
 * existing rule's `nl_text`; the backend re-parses and re-binds the signal.
 */
class NewRuleViewModel(
  private val repo: RulesRepository,
) : BaseViewModel<NewRuleUiState, NewRuleEvent>() {

  override val initialState = NewRuleUiState()

  fun updateNl(text: String) {
    updateState { copy(nlText = text, error = null) }
  }

  /**
   * Begin editing an existing rule. Pre-fills [NewRuleUiState.nlText] and
   * remembers the original so `confirm()` can detect a no-op edit.
   */
  fun beginEdit(rule: RuleDto) {
    updateState {
      NewRuleUiState(
        nlText = rule.nlText,
        existingRuleId = rule.id,
        originalNlText = rule.nlText,
      )
    }
  }

  /** Clear all state — call between create/edit cycles. */
  fun reset() {
    updateState { NewRuleUiState() }
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
    val state = currentState
    val editingId = state.existingRuleId
    if (editingId != null) {
      // Edit path. If the user opened Edit but didn't change the text,
      // there's nothing to persist — just close the sheet.
      if (state.nlText == state.originalNlText) {
        sendEvent(NewRuleEvent.Dismiss)
        return@launch
      }
      when (val r = repo.edit(id = editingId, nlText = state.nlText)) {
        is Result.Success -> {
          updateState { copy(createdRuleId = r.data.id) }
          sendEvent(NewRuleEvent.Dismiss)
        }
        is Result.Error -> updateState { copy(error = r.exception.message) }
        is Result.Loading -> Unit
      }
      return@launch
    }
    // Create path. Preview round-trip is mandatory.
    val p = state.preview ?: return@launch
    when (val r = repo.create(state.nlText, p, isRecurring = true)) {
      is Result.Success -> {
        updateState { copy(createdRuleId = r.data.id) }
        sendEvent(NewRuleEvent.Dismiss)
      }
      is Result.Error -> updateState { copy(error = r.exception.message) }
      is Result.Loading -> Unit
    }
  }
}
