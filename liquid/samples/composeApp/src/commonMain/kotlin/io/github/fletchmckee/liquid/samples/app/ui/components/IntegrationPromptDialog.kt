// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationInfo
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationProviders
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Eyebrow
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Sp
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type
import io.github.fletchmckee.liquid.samples.app.ui.klikone.k1Clickable
import kotlinx.coroutines.delay

@Composable
fun IntegrationPromptDialog(
  isVisible: Boolean,
  unconnectedIntegrations: List<IntegrationInfo>,
  autoDismissSeconds: Int = 3,
  onIntegrationClick: (String) -> Unit,
  onDismiss: () -> Unit,
  onNeverShowAgain: () -> Unit,
) {
  var remainingSeconds by remember(isVisible) { mutableStateOf(autoDismissSeconds) }
  var neverShowAgain by remember(isVisible) { mutableStateOf(false) }

  // Countdown + auto-dismiss. Single effect — previously there were two, which
  // caused the displayed countdown to race the progress reset loop.
  LaunchedEffect(isVisible) {
    if (!isVisible) return@LaunchedEffect
    remainingSeconds = autoDismissSeconds
    while (remainingSeconds > 0) {
      delay(1000)
      remainingSeconds--
    }
    if (neverShowAgain) onNeverShowAgain() else onDismiss()
  }

  AnimatedVisibility(
    visible = isVisible,
    enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.96f),
    exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.96f),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.32f))
        .pointerInput(Unit) {
          detectTapGestures(onTap = {
            if (neverShowAgain) onNeverShowAgain() else onDismiss()
          })
        },
      contentAlignment = Alignment.Center,
    ) {
      // Paper card — K1 editorial: hairline border, paper fill, no shadow.
      Column(
        modifier = Modifier
          .widthIn(max = 360.dp)
          .padding(horizontal = 24.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(KlikPaperCard)
          .border(0.75.dp, KlikLineHairline, RoundedCornerShape(20.dp))
          .pointerInput(Unit) { detectTapGestures { /* swallow tap so backdrop doesn't fire */ } }
          .padding(horizontal = 24.dp, vertical = 28.dp),
      ) {
        K1Eyebrow("Klik")
        Spacer(Modifier.height(K1Sp.m))
        Text(
          "Connect your apps.",
          style = K1Type.h1,
        )
        Spacer(Modifier.height(K1Sp.s))
        Text(
          "Klik works best when it can quietly read your calendar and tasks. Tap a provider to connect.",
          style = K1Type.bodySm.copy(color = KlikInkSecondary),
        )

        Spacer(Modifier.height(K1Sp.xl))

        // Integration tiles row
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          items(
            items = unconnectedIntegrations.take(5),
            key = { it.providerId },
          ) { integration ->
            K1IntegrationTile(
              integration = integration,
              onClick = { onIntegrationClick(integration.providerId) },
            )
          }
        }

        if (unconnectedIntegrations.size > 5) {
          Spacer(Modifier.height(K1Sp.s))
          Text(
            "+${unconnectedIntegrations.size - 5} more",
            style = K1Type.metaSm.copy(color = KlikInkMuted),
          )
        }

        Spacer(Modifier.height(K1Sp.xl))

        // Countdown track — hairline rule with a filling ink bar.
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(KlikLineHairline),
        ) {
          val fraction = (remainingSeconds.toFloat() / autoDismissSeconds).coerceIn(0f, 1f)
          Box(
            Modifier
              .fillMaxWidth(fraction)
              .height(2.dp)
              .background(KlikInkPrimary),
          )
        }
        Spacer(Modifier.height(K1Sp.s))
        Text(
          "Closes in ${remainingSeconds}s",
          style = K1Type.metaSm.copy(color = KlikInkTertiary),
        )

        Spacer(Modifier.height(K1Sp.m))

        // "Don't show again" — text-only with a minimal check glyph.
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .k1Clickable { neverShowAgain = !neverShowAgain }
            .padding(vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          K1TinyCheck(checked = neverShowAgain)
          Text(
            "Don't show this again",
            style = K1Type.metaSm.copy(color = KlikInkSecondary),
          )
        }

        Spacer(Modifier.height(K1Sp.m))

        // Secondary action — text-pill on soft paper.
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KlikPaperSoft)
            .k1Clickable {
              if (neverShowAgain) onNeverShowAgain() else onDismiss()
            }
            .padding(vertical = 14.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            "Maybe later",
            style = K1Type.bodyMd.copy(color = KlikInkPrimary),
          )
        }
      }
    }
  }
}

@Composable
private fun K1IntegrationTile(
  integration: IntegrationInfo,
  onClick: () -> Unit,
) {
  val displayInfo = IntegrationProviders.getDisplayInfo(integration.providerId)
  val displayName = displayInfo?.name ?: integration.displayName

  Column(
    modifier = Modifier
      .width(80.dp)
      .clip(RoundedCornerShape(14.dp))
      .background(KlikPaperSoft)
      .k1Clickable(onClick = onClick)
      .padding(vertical = 12.dp, horizontal = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    K1ProviderIcon(providerId = integration.providerId, size = 36.dp)
    Text(
      displayName,
      style = K1Type.metaSm.copy(color = KlikInkPrimary),
      maxLines = 1,
      textAlign = TextAlign.Center,
    )
    Text(
      "Connect",
      style = K1Type.metaSm.copy(color = KlikInkTertiary),
    )
  }
}

@Composable
private fun K1TinyCheck(checked: Boolean) {
  val shape = RoundedCornerShape(3.dp)
  Box(
    Modifier
      .size(16.dp)
      .clip(shape)
      .background(if (checked) KlikInkPrimary else KlikPaperCard)
      .border(0.75.dp, if (checked) KlikInkPrimary else KlikLineHairline, shape),
    contentAlignment = Alignment.Center,
  ) {
    if (checked) {
      Text("✓", style = K1Type.metaSm.copy(color = KlikPaperCard))
    }
  }
}
