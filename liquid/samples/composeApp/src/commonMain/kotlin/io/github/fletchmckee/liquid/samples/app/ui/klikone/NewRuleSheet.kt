// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.presentation.rules.NewRuleViewModel
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft

/**
 * "Teach Klik a rule" sheet. Two-step flow driven by [NewRuleViewModel]:
 * type natural-language → Preview (KK_suggest parses) → Add to Featured.
 *
 * K1 editorial only — paper-and-ink palette, hairline rules, sparkle, no
 * Material3 leakage beyond Text/Icon primitives.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewRuleSheet(
  viewModel: NewRuleViewModel,
  onDismiss: () -> Unit,
) {
  val ui by viewModel.state.collectAsState()

  Column(
    Modifier
      .fillMaxWidth()
      .background(KlikPaperCard)
      .statusBarsPadding()
      .imePadding()
      .navigationBarsPadding()
      .padding(horizontal = 24.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    val editing = ui.existingRuleId != null
    Text(if (editing) "Edit this rule" else "Teach Klik a rule", style = K1Type.h2)
    Text(
      if (editing) {
        "Klik will re-parse and re-bind the signal when you save."
      } else {
        "Klik will run this for you on schedule or in context. Write it like you'd ask a teammate."
      },
      style = K1Type.bodySm.copy(color = KlikInkTertiary),
    )

    Column(
      Modifier
        .fillMaxWidth()
        .clip(K1R.card)
        .background(KlikPaperSoft)
        .border(0.5.dp, KlikLineHairline, K1R.card)
        .padding(14.dp),
    ) {
      BasicTextField(
        value = ui.nlText,
        onValueChange = viewModel::updateNl,
        textStyle = K1Type.bodyMd,
        cursorBrush = SolidColor(KlikInkPrimary),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        // Cap height so the input doesn't balloon — typical rule fits 1-3 lines.
        maxLines = 4,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
      )
    }

    // FlowRow wraps chips onto multiple lines instead of clipping the third one.
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      listOf(
        "After every 1:1, draft a recap email",
        "Every Monday 9 AM, summarize last week",
        "When I haven't talked to a key contact in 14 days",
      ).forEach { ex ->
        K1Chip(label = ex, onClick = { viewModel.updateNl(ex) })
      }
    }

    ui.preview?.let { p ->
      Column(
        Modifier
          .fillMaxWidth()
          .clip(K1R.card)
          .background(KlikPaperSoft)
          .border(0.5.dp, KlikLineHairline, K1R.card)
          .padding(14.dp),
      ) {
        Text("Klik will: ${p.actionLabel}", style = K1Type.bodyMd)
        Text(
          "When: ${p.triggerLabel}",
          style = K1Type.bodyMd.copy(color = KlikInkSecondary),
        )
        if (!p.approximationNote.isNullOrBlank()) {
          Spacer(Modifier.height(6.dp))
          Text(
            "⚠ ${p.approximationNote}",
            style = K1Type.metaSm.copy(color = KlikInkTertiary),
          )
        }
      }
    }

    ui.error?.let {
      Text(
        "Couldn't parse: $it. Try rephrasing.",
        style = K1Type.metaSm.copy(color = KlikInkSecondary),
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      K1ButtonGhost(label = "Cancel", onClick = onDismiss, modifier = Modifier.weight(1f))
      // In edit mode the user already has a parsed rule; we don't need to
      // gate Save behind a preview round-trip — the backend re-parses on PATCH.
      if (editing) {
        K1ButtonPrimary(
          label = "Save changes",
          enabled = !ui.isLoading && ui.nlText.trim().isNotEmpty(),
          onClick = { viewModel.confirm() },
          modifier = Modifier.weight(1f),
        )
      } else if (ui.preview == null) {
        K1ButtonPrimary(
          label = if (ui.isLoading) "Parsing…" else "Preview",
          enabled = !ui.isLoading && ui.nlText.trim().isNotEmpty(),
          onClick = { viewModel.runPreview() },
          modifier = Modifier.weight(1f),
        )
      } else {
        K1ButtonPrimary(
          label = "Add to Featured",
          onClick = { viewModel.confirm() },
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}
