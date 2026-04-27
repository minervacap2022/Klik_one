// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.theme.KlikAlert
import io.github.fletchmckee.liquid.samples.app.theme.KlikAvatarBg
import io.github.fletchmckee.liquid.samples.app.theme.KlikAvatarFg
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionBg
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionSubtext
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionText
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineSoft
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft

/** A single transcript turn shown in the Live Capture list. */
data class LiveCaptureTurn(
  val id: String,
  val speakerInitials: String,
  val speakerLabel: String,
  val time: String,
  val text: String,
)

/** An AI-detected moment flagged during capture — shown in a decision wash. */
data class LiveKlikDetection(
  val id: String,
  val kind: String,
  val summary: String,
)

/** Klik One Live Recording — full-screen capture surface. */
@Composable
fun LiveRecordingScreen(
  title: String = "In conversation",
  startedAgo: String = "Just started",
  speakerCount: Int = 1,
  elapsed: String,
  recentTurns: List<LiveCaptureTurn>,
  detections: List<LiveKlikDetection> = emptyList(),
  isPaused: Boolean = false,
  onMinimize: () -> Unit = {},
  onPauseResume: () -> Unit = {},
  onStop: () -> Unit = {},
  onAddContext: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  Column(
    modifier.fillMaxSize().background(KlikPaperCard).statusBarsPadding(),
  ) {
    Column(
      Modifier.weight(1f).verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 16.dp),
    ) {
      // RECORDING eyebrow + Minimize.
      Row(
        Modifier.fillMaxWidth().padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          K1RecDot(color = KlikAlert, size = 8.dp)
          Text(
            "RECORDING",
            style = K1Type.eyebrow.copy(color = KlikAlert, letterSpacing = 0.6.sp),
          )
        }
        Text(
          "Minimize",
          style = K1Type.bodySm.copy(color = KlikInkTertiary),
          modifier = Modifier.k1Clickable(onClick = onMinimize),
        )
      }

      // Session info.
      Text(title, style = K1Type.h2)
      Spacer(Modifier.height(4.dp))
      Text(
        "$startedAgo · $speakerCount speaker${if (speakerCount == 1) "" else "s"} detected",
        style = K1Type.bodySm.copy(color = KlikInkTertiary),
      )
      Spacer(Modifier.height(32.dp))

      // Live waveform card.
      Box(
        Modifier.fillMaxWidth().height(120.dp)
          .clip(RoundedCornerShape(14.dp)).background(KlikPaperSoft),
        contentAlignment = Alignment.Center,
      ) {
        if (isPaused) {
          K1Waveform(
            heights = listOf(12f, 20f, 8f, 16f, 10f),
            barWidth = 3.dp,
            color = KlikInkMuted,
          )
        } else {
          K1WaveformLive(color = KlikInkPrimary, barWidth = 3.dp, gap = 3.dp)
        }
      }
      Spacer(Modifier.height(24.dp))

      // Timer.
      Text(
        elapsed,
        style = K1Type.timer,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(32.dp))

      // LIVE CAPTURE.
      K1Eyebrow("LIVE CAPTURE")
      Spacer(Modifier.height(10.dp))

      if (recentTurns.isEmpty()) {
        Box(
          Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(KlikPaperSoft).padding(14.dp),
        ) {
          Text(
            if (isPaused) {
              "Paused — tap resume to continue capturing."
            } else {
              "Listening… transcription starts as soon as someone speaks."
            },
            style = K1Type.bodySm.copy(color = KlikInkTertiary),
          )
        }
      } else {
        recentTurns.takeLast(3).forEach { turn ->
          TurnBubble(turn)
          Spacer(Modifier.height(8.dp))
        }
      }

      detections.forEach { det ->
        Spacer(Modifier.height(8.dp))
        DetectionBubble(det)
      }
    }

    // Fixed control row.
    ControlRow(
      isPaused = isPaused,
      onStop = onStop,
      onPauseResume = onPauseResume,
      onAddContext = onAddContext,
    )
  }
}

@Composable
private fun TurnBubble(turn: LiveCaptureTurn) {
  Column(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
      .background(KlikPaperSoft).padding(horizontal = 14.dp, vertical = 12.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      K1Avatar(initials = turn.speakerInitials.take(2), size = 18.dp)
      Text(turn.speakerLabel, style = K1Type.bodySm.copy(fontWeight = FontWeight.Medium))
      Text(turn.time, style = K1Type.metaSm)
    }
    Spacer(Modifier.height(6.dp))
    Text(turn.text, style = K1Type.caption.copy(color = KlikInkPrimary))
  }
}

@Composable
private fun DetectionBubble(det: LiveKlikDetection) {
  Row(
    Modifier.fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(KlikDecisionBg)
      .height(IntrinsicSize.Min),
  ) {
    Box(Modifier.width(2.dp).fillMaxHeight().background(KlikDecisionAccent))
    Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
      ) {
        Box(
          Modifier.size(9.dp)
            .border(1.1.dp, KlikDecisionSubtext, RoundedCornerShape(1.dp)),
        )
        Text(
          "KLIK DETECTED · ${det.kind}",
          style = K1Type.eyebrow.copy(
            color = KlikDecisionSubtext,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp,
          ),
        )
      }
      Spacer(Modifier.height(4.dp))
      Text(det.summary, style = K1Type.meta.copy(color = KlikDecisionText, fontSize = 11.sp))
    }
  }
}

@Composable
private fun ControlRow(
  isPaused: Boolean,
  onStop: () -> Unit,
  onPauseResume: () -> Unit,
  onAddContext: () -> Unit,
) {
  Column {
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikLineSoft))
    Row(
      Modifier.fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 20.dp)
        .navigationBarsPadding(),
      horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      CircleOutline(onClick = onStop) {
        Box(Modifier.size(12.dp).clip(RoundedCornerShape(1.dp)).background(KlikInkPrimary))
      }
      PauseResumePill(isPaused = isPaused, onClick = onPauseResume)
      CircleOutline(onClick = onAddContext) {
        Text(
          "+",
          style = K1Type.h2.copy(fontSize = 22.sp),
        )
      }
    }
  }
}

@Composable
private fun CircleOutline(onClick: () -> Unit, content: @Composable () -> Unit) {
  Box(
    Modifier.size(48.dp).clip(CircleShape)
      .border(0.5.dp, KlikLineHairline, CircleShape)
      .k1Clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) { content() }
}

@Composable
private fun PauseResumePill(isPaused: Boolean, onClick: () -> Unit) {
  Row(
    Modifier.height(48.dp).clip(CircleShape).background(KlikInkPrimary)
      .k1Clickable(onClick = onClick).padding(horizontal = 24.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (!isPaused) {
      Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Box(Modifier.size(width = 3.dp, height = 12.dp).background(KlikPaperCard))
        Box(Modifier.size(width = 3.dp, height = 12.dp).background(KlikPaperCard))
      }
    } else {
      Text("▶", style = K1Type.bodySm.copy(color = KlikPaperCard))
    }
    Text(
      if (isPaused) "Resume" else "Pause",
      style = K1Type.bodyMd.copy(color = KlikPaperCard),
    )
  }
}
