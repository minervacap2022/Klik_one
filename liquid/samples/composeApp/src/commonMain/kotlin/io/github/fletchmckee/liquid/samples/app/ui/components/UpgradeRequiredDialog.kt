// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1ButtonPrimary
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Eyebrow
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Sp
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type
import io.github.fletchmckee.liquid.samples.app.ui.klikone.k1Clickable

@Composable
fun UpgradeRequiredDialog(
  featureName: String,
  currentTier: String,
  onUpgrade: () -> Unit,
  onDismiss: () -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.32f))
      .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) },
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 360.dp)
        .padding(horizontal = 24.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(KlikPaperCard)
        .border(0.75.dp, KlikLineHairline, RoundedCornerShape(20.dp))
        .pointerInput(Unit) { detectTapGestures { /* swallow */ } }
        .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
      K1Eyebrow("Klik · Plans")
      Spacer(Modifier.height(K1Sp.m))
      Text(
        "Upgrade required.",
        style = K1Type.h1,
      )
      Spacer(Modifier.height(K1Sp.s))
      Text(
        "$featureName is a Pro feature. Your current plan is ${currentTier.replaceFirstChar { it.uppercase() }} — upgrade to unlock it.",
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )

      Spacer(Modifier.height(K1Sp.xl))

      K1ButtonPrimary(
        label = "View plans",
        onClick = onUpgrade,
        modifier = Modifier.fillMaxWidth(),
      )

      Spacer(Modifier.height(K1Sp.m))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(KlikPaperSoft)
          .k1Clickable(onClick = onDismiss)
          .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          "Not now",
          style = K1Type.bodyMd.copy(color = KlikInkPrimary),
        )
      }
    }
  }
}
