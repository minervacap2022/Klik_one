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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.data.source.remote.XpHistoryItem
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikRunning
import io.github.fletchmckee.liquid.samples.app.theme.KlikWarn

/**
 * Detailed XP log. One row per grant, grouped by date. Each row shows the XP
 * earned, the base XP + multiplier breakdown, and the source todo or session
 * (if known) so the user can tell *why* the grant landed.
 */
@Composable
fun XpLogsScreen(
  items: List<XpHistoryItem>,
  tasks: List<TaskMetadata> = emptyList(),
  meetings: List<Meeting> = emptyList(),
  onBack: () -> Unit,
) {
  // Group by ISO date (lexically sortable). Newest day first; within each
  // day we keep server order, which is also newest-first.
  val byDate: List<Pair<String, List<XpHistoryItem>>> = items
    .groupBy { it.createdAt?.substringBefore('T') ?: "—" }
    .toList()
    .sortedByDescending { it.first }

  Column(
    Modifier
      .fillMaxSize()
      .background(KlikPaperApp)
      .k1SwipeBack(onBack)
      .verticalScroll(rememberScrollState()),
  ) {
    Row(
      Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        Modifier.size(32.dp).k1Clickable(onClick = onBack),
        contentAlignment = Alignment.Center,
      ) { K1XpBackChevron() }
      Spacer(Modifier.weight(1f))
    }

    Column(Modifier.padding(horizontal = 20.dp)) {
      K1Eyebrow("XP")
      Spacer(Modifier.height(6.dp))
      Text("XP logs", style = K1Type.h2)
      Spacer(Modifier.height(4.dp))
      val total = items.sumOf { it.xpEarned }
      Text(
        "$total XP across ${items.size} grants",
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )
    }

    Spacer(Modifier.height(K1Sp.xl))

    if (items.isEmpty()) {
      Column(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text("No XP yet.", style = K1Type.h3)
        Spacer(Modifier.height(4.dp))
        Text(
          "Klik will log every grant here as you complete moves.",
          style = K1Type.caption,
        )
      }
    } else {
      byDate.forEach { (date, grants) ->
        Column(Modifier.padding(horizontal = 20.dp)) {
          val dayTotal = grants.sumOf { it.xpEarned }
          K1SectionHeader(date, count = grants.size, dotColor = KlikRunning)
          Spacer(Modifier.height(6.dp))
          Text(
            "+$dayTotal XP today",
            style = K1Type.metaSm.copy(color = KlikInkTertiary),
          )
          Spacer(Modifier.height(K1Sp.s))
          grants.forEach { grant ->
            XpLogRow(grant, tasks = tasks, meetings = meetings)
            Spacer(Modifier.height(6.dp))
          }
        }
        Spacer(Modifier.height(K1Sp.xxl))
      }
    }

    Spacer(Modifier.height(120.dp))
  }
}

@Composable
private fun XpLogRow(
  grant: XpHistoryItem,
  tasks: List<TaskMetadata>,
  meetings: List<Meeting>,
) {
  val time = grant.createdAt?.substringAfter('T')?.substringBefore('.')?.take(5) ?: "—"
  val multiplierLabel = if (grant.multiplier > 1.0f) " · ×${grant.multiplier}" else ""
  val sourceTodo = grant.todoId?.let { tid -> tasks.firstOrNull { it.kkExecTodoId == tid } }
  val sourceMeeting = grant.sessionId?.let { sid -> meetings.firstOrNull { it.id == sid } }
  val sourceLabel = when {
    sourceTodo != null -> "Move · ${sourceTodo.title}"
    sourceMeeting != null -> "Session · ${sourceMeeting.title.ifBlank { sourceMeeting.id.take(8) }}"
    grant.todoId != null -> "Move · #${grant.todoId}"
    grant.sessionId != null -> "Session · ${grant.sessionId.take(8)}"
    else -> null
  }

  Row(
    Modifier
      .fillMaxWidth()
      .clip(K1R.card)
      .background(KlikPaperCard)
      .padding(horizontal = 14.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(time, style = K1Type.metaSm.copy(color = KlikInkTertiary), modifier = Modifier.width(48.dp))
    Spacer(Modifier.width(K1Sp.m))
    Column(Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          "+${grant.xpEarned} XP",
          style = K1Type.bodyMd.copy(
            color = KlikInkPrimary,
            fontWeight = FontWeight.Medium,
          ),
        )
        if (grant.multiplier > 1.0f) {
          Spacer(Modifier.width(6.dp))
          Box(
            Modifier
              .clip(K1R.pill)
              .background(KlikWarn)
              .padding(horizontal = 6.dp, vertical = 1.dp),
          ) {
            Text(
              "×${grant.multiplier}",
              style = K1Type.meta.copy(color = KlikPaperCard, fontWeight = FontWeight.Medium),
            )
          }
        }
      }
      Spacer(Modifier.height(2.dp))
      Text(
        "Base ${grant.baseXp} XP$multiplierLabel",
        style = K1Type.metaSm.copy(color = KlikInkTertiary),
      )
      if (sourceLabel != null) {
        Spacer(Modifier.height(2.dp))
        Text(sourceLabel, style = K1Type.metaSm.copy(color = KlikInkSecondary))
      }
    }
  }
}

@Composable
private fun K1XpBackChevron() {
  androidx.compose.foundation.Canvas(Modifier.size(16.dp)) {
    val w = 1.3.dp.toPx()
    drawLine(
      color = KlikInkPrimary,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(10.dp.toPx(), 3.5.dp.toPx()),
      end = androidx.compose.ui.geometry.Offset(5.5.dp.toPx(), 8.dp.toPx()),
    )
    drawLine(
      color = KlikInkPrimary,
      strokeWidth = w,
      cap = androidx.compose.ui.graphics.StrokeCap.Round,
      start = androidx.compose.ui.geometry.Offset(5.5.dp.toPx(), 8.dp.toPx()),
      end = androidx.compose.ui.geometry.Offset(10.dp.toPx(), 12.5.dp.toPx()),
    )
  }
  // KlikLineHairline import-keeper in case the back-chevron is later styled
  // with a hairline divider underneath.
  @Suppress("UNUSED_EXPRESSION") KlikLineHairline
}
