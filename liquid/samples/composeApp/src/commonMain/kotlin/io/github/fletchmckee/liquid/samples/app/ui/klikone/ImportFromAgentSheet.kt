// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.core.rememberViewModel
import io.github.fletchmckee.liquid.samples.app.presentation.importcode.ImportCodeViewModel
import io.github.fletchmckee.liquid.samples.app.presentation.importcode.formatCountdown
import io.github.fletchmckee.liquid.samples.app.presentation.importcode.formatImportCode
import io.github.fletchmckee.liquid.samples.app.theme.KlikAlert
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperChip

/**
 * "Import from Agent" — generates a 6-digit code that AI agents (Claude Code,
 * OpenClaw, Hermes, etc.) use with the public klik-import skill to upload
 * their memory + scheduled tasks into Klik.
 *
 * Flow on first open: auto-generates a code; user reads it to the agent;
 * countdown ticks; once zero (or already used) user taps "Generate new code".
 */
@Composable
fun ImportFromAgentSheet(
  onDismiss: () -> Unit,
  viewModel: ImportCodeViewModel = rememberViewModel { ImportCodeViewModel() },
) {
  val ui by viewModel.state.collectAsState()
  LaunchedEffect(Unit) {
    if (ui.code == null && !ui.isLoading) viewModel.generateCode()
  }

  Box(
    Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.45f))
      .k1Clickable(onClick = onDismiss),
    contentAlignment = Alignment.BottomCenter,
  ) {
    Column(
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        .background(KlikPaperCard)
        .k1Clickable(enabled = false) {}
        .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
      Box(
        Modifier
          .padding(bottom = K1Sp.m)
          .fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        Box(
          Modifier
            .height(4.dp)
            .background(KlikInkMuted.copy(alpha = 0.35f), K1R.pill)
            .padding(horizontal = 28.dp),
        )
      }
      Text("Import from Agent", style = K1Type.h3)
      Spacer(Modifier.height(K1Sp.s))
      Text(
        "Share this 6-digit code with your AI agent. The agent will upload its memory and scheduled tasks into Klik.",
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )

      Spacer(Modifier.height(K1Sp.xl))

      Column(
        Modifier
          .fillMaxWidth()
          .clip(K1R.card)
          .background(KlikPaperChip)
          .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        when {
          ui.isLoading && ui.code == null -> {
            Text(
              "—  —  —    —  —  —",
              style = K1Type.timer.copy(color = KlikInkMuted),
            )
            Spacer(Modifier.height(K1Sp.s))
            Text("Generating…", style = K1Type.metaSm)
          }
          ui.code != null -> {
            Text(
              formatImportCode(ui.code!!),
              style = K1Type.timer.copy(letterSpacing = 6.sp, fontWeight = FontWeight.Medium),
            )
            Spacer(Modifier.height(K1Sp.s))
            Text(
              "Expires in ${formatCountdown(ui.secondsRemaining)}",
              style = K1Type.metaSm.copy(color = KlikInkTertiary),
            )
          }
          ui.error != null -> {
            Text("Couldn't generate a code", style = K1Type.bodyMd.copy(color = KlikAlert))
            Spacer(Modifier.height(4.dp))
            Text(
              ui.error ?: "",
              style = K1Type.metaSm.copy(color = KlikInkTertiary),
            )
          }
          else -> {
            Text(
              "—  —  —    —  —  —",
              style = K1Type.timer.copy(color = KlikInkMuted),
            )
            Spacer(Modifier.height(K1Sp.s))
            Text("Tap below to generate a code", style = K1Type.metaSm)
          }
        }
      }

      Spacer(Modifier.height(K1Sp.xl))
      K1Eyebrow("How to use it")
      Spacer(Modifier.height(K1Sp.s))
      ImportStep("1", "Open your AI agent (Claude Code, OpenClaw, Hermes…).")
      ImportStep("2", "Tell it: \"import my memory to klik\".")
      ImportStep("3", "Paste this 6-digit code when prompted.")

      Spacer(Modifier.height(K1Sp.xl))

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
          Modifier
            .clip(K1R.pill)
            .background(KlikPaperChip)
            .k1Clickable(onClick = {
              viewModel.clear()
              onDismiss()
            })
            .padding(horizontal = 18.dp, vertical = 14.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text("Done", style = K1Type.bodyMd)
        }
        Box(
          Modifier
            .clip(K1R.pill)
            .background(if (ui.isLoading) KlikInkMuted else KlikInkPrimary)
            .k1Clickable(enabled = !ui.isLoading) { viewModel.generateCode() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            if (ui.code == null && !ui.isLoading) "Generate code" else "New code",
            style = K1Type.bodyMd.copy(
              color = KlikPaperCard,
              fontWeight = FontWeight.Medium,
            ),
          )
        }
      }
      Spacer(Modifier.height(K1Sp.lg))
    }
  }
}

@Composable
private fun ImportStep(num: String, body: String) {
  Row(
    Modifier.fillMaxWidth().padding(vertical = 4.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      "$num.",
      style = K1Type.bodyMd.copy(color = KlikInkPrimary),
      modifier = Modifier.padding(end = 8.dp),
    )
    Text(body, style = K1Type.bodySm.copy(color = KlikInkSecondary))
  }
}

