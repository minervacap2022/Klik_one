// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.data.network.dto.AchievementBadgeDto
import io.github.fletchmckee.liquid.samples.app.data.network.dto.AchievementsListDto
import io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.theme.KlikCommitmentAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperChip

/**
 * K1 — Achievements. Shows server-computed badge catalog with locked/unlocked
 * state and integer progress (e.g. 23/100 sessions). Backend is the source of
 * truth — we never compute or fabricate locally.
 */
@Composable
fun AchievementsScreen(onBack: () -> Unit) {
  var data by remember { mutableStateOf<AchievementsListDto?>(null) }
  var error by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    try {
      data = RemoteDataFetcher.fetchAchievementsList()
    } catch (e: Exception) {
      error = e.message ?: "Failed to load achievements"
      KlikLogger.e("Achievements", "load failed: ${e.message}", e)
    }
  }

  Column(
    Modifier
      .fillMaxSize()
      .background(KlikPaperApp)
      .k1SwipeBack(onBack)
      .verticalScroll(rememberScrollState()),
  ) {
    Row(
      Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        Modifier.size(32.dp).k1Clickable(onClick = onBack),
        contentAlignment = Alignment.Center,
      ) { K1BackChevronGlyph() }
      Spacer(Modifier.weight(1f))
    }

    Column(Modifier.padding(horizontal = 20.dp)) {
      K1Eyebrow("Growth")
      Spacer(Modifier.height(6.dp))
      Text("Achievements", style = K1Type.h2)
      Spacer(Modifier.height(4.dp))
      val totalLine = data?.let { "${it.totalEarned} of ${it.totalPossible} badges earned." }
        ?: "Loading…"
      Text(totalLine, style = K1Type.bodySm.copy(color = KlikInkSecondary))
    }
    Spacer(Modifier.height(K1Sp.xl))

    val ui = data
    when {
      ui == null && error == null -> {
        Column(Modifier.padding(20.dp)) {
          repeat(3) { K1SkeletonCard(lines = 2); Spacer(Modifier.height(8.dp)) }
        }
      }
      ui == null -> {
        Column(Modifier.padding(20.dp)) {
          K1Card(soft = true) {
            Text("Couldn't load achievements", style = K1Type.bodyMd.copy(color = KlikInkPrimary, fontWeight = FontWeight.Medium))
            Spacer(Modifier.height(4.dp))
            Text(error.orEmpty(), style = K1Type.bodySm.copy(color = KlikInkSecondary))
          }
        }
      }
      else -> AchievementsContent(ui)
    }
  }
}

@Composable
private fun AchievementsContent(ui: AchievementsListDto) {
  Column(Modifier.padding(horizontal = 20.dp)) {
    K1SectionHeader("Earned", count = ui.totalEarned, dotColor = KlikCommitmentAccent)
    Spacer(Modifier.height(K1Sp.s))
    val earned = ui.achievements.filter { it.earned }
    if (earned.isEmpty()) {
      Text("Nothing yet — finish a session to earn First Klik.", style = K1Type.metaSm.copy(color = KlikInkTertiary))
    } else {
      earned.forEach {
        BadgeRow(it)
        Spacer(Modifier.height(6.dp))
      }
    }
    Spacer(Modifier.height(K1Sp.xxl))
    val locked = ui.achievements.filter { !it.earned }
    if (locked.isNotEmpty()) {
      K1SectionHeader("Locked", count = locked.size)
      Spacer(Modifier.height(K1Sp.s))
      locked.forEach {
        BadgeRow(it)
        Spacer(Modifier.height(6.dp))
      }
      Spacer(Modifier.height(K1Sp.xxl))
    }
  }
}

@Composable
private fun K1BackChevronGlyph() {
  Canvas(Modifier.size(16.dp)) {
    val w = 1.3.dp.toPx()
    drawLine(
      color = KlikInkPrimary,
      strokeWidth = w,
      cap = StrokeCap.Round,
      start = Offset(10.dp.toPx(), 3.5.dp.toPx()),
      end = Offset(5.5.dp.toPx(), 8.dp.toPx()),
    )
    drawLine(
      color = KlikInkPrimary,
      strokeWidth = w,
      cap = StrokeCap.Round,
      start = Offset(5.5.dp.toPx(), 8.dp.toPx()),
      end = Offset(10.dp.toPx(), 12.5.dp.toPx()),
    )
  }
}

@Composable
private fun BadgeRow(b: AchievementBadgeDto) {
  Box(
    Modifier
      .fillMaxWidth()
      .clip(K1R.soft)
      .background(if (b.earned) KlikPaperCard else KlikPaperChip)
      .padding(14.dp),
  ) {
    Column {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          b.title,
          style = K1Type.bodyMd.copy(
            color = if (b.earned) KlikInkPrimary else KlikInkSecondary,
            fontWeight = FontWeight.Medium,
          ),
        )
        Spacer(Modifier.weight(1f))
        Text(
          if (b.target <= 1) (if (b.earned) "✓" else "—")
          else "${b.progress}/${b.target}",
          style = K1Type.metaSm.copy(
            color = if (b.earned) KlikInkPrimary else KlikInkTertiary,
            fontWeight = FontWeight.Medium,
          ),
        )
      }
      Spacer(Modifier.height(4.dp))
      Text(b.summary, style = K1Type.bodySm.copy(color = KlikInkSecondary))
      if (b.target > 1) {
        Spacer(Modifier.height(8.dp))
        ProgressBar(progress = b.progress, target = b.target, earned = b.earned)
      }
    }
  }
}

@Composable
private fun ProgressBar(progress: Int, target: Int, earned: Boolean) {
  val pct = if (target > 0) (progress.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
  Box(Modifier.fillMaxWidth().height(4.dp).clip(K1R.pill).background(KlikPaperChip)) {
    Box(
      Modifier
        .fillMaxWidth(pct)
        .height(4.dp)
        .clip(K1R.pill)
        .background(if (earned) KlikCommitmentAccent else KlikInkTertiary),
    )
  }
}
