// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskStatus
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.model.markTaskSeen
import io.github.fletchmckee.liquid.samples.app.model.seenTaskIdsState
import io.github.fletchmckee.liquid.samples.app.theme.KlikAlert
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineSoft
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineTick
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperChip
import io.github.fletchmckee.liquid.samples.app.theme.KlikRunning
import io.github.fletchmckee.liquid.samples.app.theme.KlikWarn
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityType
import io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation

/** Klik One — Moves. Drop-in replacement for `EventsScreen`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovesScreen(
  isLoading: Boolean = false,
  isRefreshing: Boolean = false,
  // When set, the matching task renders with an extra accent band so the
  // user can spot it after deep-linking from session detail. Should be
  // cleared via onHighlightConsumed once the screen has rendered it.
  highlightedTaskId: String? = null,
  onHighlightConsumed: () -> Unit = {},
  featuredTasks: List<TaskMetadata> = emptyList(),
  sensitiveTasks: List<TaskMetadata> = emptyList(),
  dailyTasks: List<TaskMetadata> = emptyList(),
  dailyTasksGrouped: Map<String, List<TaskMetadata>> = emptyMap(),
  onRefresh: () -> Unit = {},
  onApproveTask: (String) -> Unit = {},
  onRejectTask: (String) -> Unit = {},
  onRejectTaskWithReason: (String, String) -> Unit = { _, _ -> },
  onRetryTask: (String) -> Unit = {},
  onArchiveTaskOnBackend: (String) -> Unit = {},
  onEntityClick: (EntityNavigationData) -> Unit = {},
  onSegmentClick: (TracedSegmentNavigation) -> Unit = {},
) {
  var filter by remember { mutableStateOf("all") }
  var searchActive by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }

  // Auto-clear the deep-link highlight after a short dwell so the row
  // doesn't keep tinting across navigations the user makes later.
  if (highlightedTaskId != null) {
    androidx.compose.runtime.LaunchedEffect(highlightedTaskId) {
      kotlinx.coroutines.delay(2500)
      onHighlightConsumed()
    }
  }

  // Completed buckets the Done list; failed terminal states get their own
  // bucket so the user can tell them apart from the green check-marked successes.
  // Everything else daily-tasks goes into Running. Needs-your-OK is the combined
  // review queue (featured AI suggestions + sensitive todos requiring confirmation).
  fun isDone(t: TaskMetadata): Boolean = t.status == TaskStatus.COMPLETED ||
    t.kkExecStatus?.uppercase() in setOf("COMPLETED", "APPROVED")

  fun isFailed(t: TaskMetadata): Boolean =
    t.kkExecStatus?.uppercase() in setOf("FAILED", "ERROR", "CANNOT_EXECUTE", "REJECTED")

  // Newest first. ISO-8601 strings sort lexically; null/blank goes last.
  val recencyKey: (TaskMetadata) -> String = { it.createdAt?.takeIf { s -> s.isNotBlank() } ?: "" }

  val needsOk = (featuredTasks + sensitiveTasks).sortedByDescending(recencyKey)
  val doneIds = dailyTasks.filter(::isDone).map { it.id }.toSet()
  val failedIds = dailyTasks.filter(::isFailed).map { it.id }.toSet()
  val running = dailyTasks
    .filter { it.id !in doneIds && it.id !in failedIds }
    .sortedByDescending(recencyKey)
  val done = dailyTasks.filter { it.id in doneIds }.sortedByDescending(recencyKey)
  val failed = dailyTasks.filter { it.id in failedIds }.sortedByDescending(recencyKey)
  val totalAll = needsOk.size + running.size + done.size + failed.size

  val seenIds by seenTaskIdsState
  fun unread(t: TaskMetadata): Boolean = t.id !in seenIds

  val baseFiltered = when (filter) {
    "needs" -> needsOk
    "running" -> running
    "failed" -> failed
    "done" -> done
    else -> needsOk + running + failed + done
  }
  val filteredAll = if (searchQuery.isBlank()) {
    baseFiltered
  } else {
    baseFiltered.filter {
      it.title.contains(searchQuery, ignoreCase = true) ||
        it.subtitle.contains(searchQuery, ignoreCase = true) ||
        it.relatedProject.contains(searchQuery, ignoreCase = true)
    }
  }

  val ptrState = rememberPullToRefreshState()
  PullToRefreshBox(
    isRefreshing = isRefreshing,
    state = ptrState,
    onRefresh = onRefresh,
    modifier = Modifier.fillMaxSize().background(KlikPaperApp),
    indicator = {
      K1PullRefreshIndicator(
        state = ptrState,
        isRefreshing = isRefreshing,
        modifier = Modifier.align(Alignment.TopCenter),
      )
    },
  ) {
    Column(
      Modifier
        .fillMaxSize()
        .background(KlikPaperApp)
        .verticalScroll(rememberScrollState())
        .padding(top = 52.dp, bottom = 120.dp),
    ) {
      // Header with search
      if (searchActive) {
        Row(
          Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 4.dp, bottom = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            Modifier
              .weight(1f)
              .clip(K1R.soft)
              .background(KlikPaperChip)
              .padding(horizontal = 12.dp, vertical = 10.dp),
          ) {
            BasicTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              singleLine = true,
              textStyle = K1Type.bodyMd.copy(color = KlikInkPrimary),
              modifier = Modifier.fillMaxWidth(),
              decorationBox = { inner ->
                if (searchQuery.isEmpty()) {
                  Text("Search moves…", style = K1Type.bodyMd.copy(color = KlikInkMuted))
                }
                inner()
              },
            )
          }
          Spacer(Modifier.width(12.dp))
          Text(
            "Cancel",
            style = K1Type.bodySm.copy(color = KlikInkSecondary),
            modifier = Modifier.k1Clickable {
              searchActive = false
              searchQuery = ""
            },
          )
        }
      } else {
        K1Header(
          title = "Moves",
          trailing = {
            Box(Modifier.k1Clickable { searchActive = true }) {
              SearchIcon()
            }
          },
        )
      }
      Spacer(Modifier.height(K1Sp.md))

      // Filter chips
      Row(
        Modifier
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        K1Chip(
          label = "All · $totalAll",
          selected = filter == "all",
          onClick = { filter = "all" },
        )
        K1Chip(
          label = "Needs attention · ${needsOk.size}",
          selected = filter == "needs",
          onClick = { filter = "needs" },
        )
        K1Chip(
          label = "Running · ${running.size}",
          selected = filter == "running",
          onClick = { filter = "running" },
        )
        if (failed.isNotEmpty()) {
          K1Chip(
            label = "Failed · ${failed.size}",
            selected = filter == "failed",
            onClick = { filter = "failed" },
          )
        }
        K1Chip(
          label = "Done",
          selected = filter == "done",
          onClick = { filter = "done" },
        )
      }

      Spacer(Modifier.height(K1Sp.xl))

      if ((filter == "all" || filter == "needs") && needsOk.isNotEmpty()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
          val needsUnread = needsOk.count(::unread)
          K1SectionHeader(
            "Needs your OK",
            count = needsOk.size,
            dotColor = KlikWarn,
            trailing = if (needsUnread > 0) ({ UnreadCountLabel(needsUnread) }) else null,
          )
          Spacer(Modifier.height(K1Sp.s))
          needsOk.forEach { t ->
            NeedsOkCard(
              t = t,
              isUnread = unread(t),
              onApprove = onApproveTask,
              onArchive = onArchiveTaskOnBackend,
              onOpen = {
                markTaskSeen(t.id)
                onEntityClick(EntityNavigationData(EntityType.TASK, t.id))
              },
            )
            Spacer(Modifier.height(8.dp))
          }
        }
        Spacer(Modifier.height(K1Sp.xxl))
      }

      if ((filter == "all" || filter == "running") && running.isNotEmpty()) {
        // Group by tool categories — derive label from toolCategoriesNeeded sorted & joined
        val grouped: Map<String, List<TaskMetadata>> = if (dailyTasksGrouped.isNotEmpty()) {
          // Use pre-grouped data from backend when available; filter to running only
          dailyTasksGrouped.mapValues { (_, list) ->
            list.filter { it.id in running.map { r -> r.id }.toSet() }
          }.filterValues { it.isNotEmpty() }
        } else {
          // Derive groups client-side from toolCategoriesNeeded
          running.groupBy { t ->
            val cats = t.toolCategoriesNeeded.sorted()
            if (cats.isEmpty()) "Uncategorized" else cats.joinToString(", ")
          }
        }
        Column(Modifier.padding(horizontal = 20.dp)) {
          val runningUnread = running.count(::unread)
          K1SectionHeader(
            "Running",
            count = running.size,
            dotColor = KlikRunning,
            trailing = if (runningUnread > 0) ({ UnreadCountLabel(runningUnread) }) else null,
          )
          Spacer(Modifier.height(K1Sp.s))
          grouped.entries.forEach { (groupLabel, tasks) ->
            TaskCategoryGroup(
              label = if (groupLabel == "Uncategorized") {
                "Uncategorized"
              } else {
                groupLabel.split(", ").joinToString(" + ") { cat ->
                  cat.split("_").joinToString(" ") { w -> w.replaceFirstChar { it.uppercaseChar() } }
                }
              },
              tasks = tasks,
              isUnread = ::unread,
              highlightedTaskId = highlightedTaskId,
              forceExpanded = highlightedTaskId != null && tasks.any { it.id == highlightedTaskId },
              onTaskClick = { t ->
                markTaskSeen(t.id)
                onEntityClick(EntityNavigationData(EntityType.TASK, t.id))
              },
              onRetryTask = onRetryTask,
            )
            Spacer(Modifier.height(K1Sp.s))
          }
        }
        Spacer(Modifier.height(K1Sp.xxl))
      }

      if ((filter == "all" || filter == "failed") && failed.isNotEmpty()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
          val failedUnread = failed.count(::unread)
          K1SectionHeader(
            "Failed",
            count = failed.size,
            dotColor = KlikAlert,
            trailing = if (failedUnread > 0) ({ UnreadCountLabel(failedUnread) }) else null,
          )
          Spacer(Modifier.height(K1Sp.s))
          failed.forEach { t ->
            FailedRow(
              t = t,
              isUnread = unread(t),
              onRetry = { onRetryTask(t.id) },
              onOpen = {
                markTaskSeen(t.id)
                onEntityClick(EntityNavigationData(EntityType.TASK, t.id))
              },
            )
            Spacer(Modifier.height(6.dp))
          }
        }
        Spacer(Modifier.height(K1Sp.xxl))
      }

      if ((filter == "all" || filter == "done") && done.isNotEmpty()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
          K1SectionHeader("Done today", count = done.size)
          Spacer(Modifier.height(K1Sp.s))
          done.forEach { t ->
            DoneRow(t = t, onRetry = { onRetryTask(t.id) })
          }
        }
        Spacer(Modifier.height(K1Sp.xxl))
      }

      if (filteredAll.isEmpty() && !isLoading) {
        Column(
          Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 48.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          K1Waveform(heights = listOf(6f, 10f, 5f, 8f), color = KlikInkMuted)
          Spacer(Modifier.height(K1Sp.s))
          Text("You're clear.", style = K1Type.h3)
          Spacer(Modifier.height(4.dp))
          Text("Klik is watching for more.", style = K1Type.caption)
        }
      }
    }
  } // end PullToRefreshBox
}

@Composable
private fun NeedsOkCard(
  t: TaskMetadata,
  isUnread: Boolean = false,
  onApprove: (String) -> Unit,
  onArchive: (String) -> Unit,
  onOpen: () -> Unit = {},
) {
  K1Card(soft = true, onClick = onOpen) {
    // Top row: unread dot + title + subtitle | time ago
    Row(verticalAlignment = Alignment.Top) {
      if (isUnread) {
        UnreadDot(KlikWarn)
        Spacer(Modifier.width(K1Sp.s))
      }
      Column(Modifier.weight(1f)) {
        Text(
          t.title,
          style = K1Type.bodyMd.copy(
            fontWeight = if (isUnread) FontWeight.Medium else FontWeight.Normal,
          ),
        )
        if (t.subtitle.isNotBlank()) {
          Spacer(Modifier.height(3.dp))
          Text(t.subtitle, style = K1Type.caption)
        }
      }
      if (t.dueInfo.isNotBlank()) {
        Text(t.dueInfo, style = K1Type.metaSm)
      }
    }

    // Italic draft preview in inner white card (matches reference)
    if (!t.description.isNullOrBlank()) {
      Spacer(Modifier.height(10.dp))
      Box(
        Modifier
          .fillMaxWidth()
          .clip(K1R.soft)
          .background(KlikPaperCard)
          .padding(horizontal = 12.dp, vertical = 10.dp),
      ) {
        val txt = t.description!!
        Text(
          "\u201C${txt.take(140)}${if (txt.length > 140) "\u2026" else ""}\u201D",
          style = K1Type.caption.copy(
            color = KlikInkSecondary,
            fontStyle = FontStyle.Italic,
          ),
        )
      }
    }

    Spacer(Modifier.height(10.dp))

    // Action row
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      ActionButton("Approve & send", primary = true) { onApprove(t.id) }
      ActionButton("Edit", primary = false) { /* edit */ }
      ActionButton("Skip", primary = false, muted = true) { onArchive(t.id) }
    }

    // Source link — "From Product sync · 9:22"
    val sessionRef = t.relatedMeetingId
    if (!sessionRef.isNullOrBlank()) {
      Spacer(Modifier.height(K1Sp.s))
      Box(
        Modifier.fillMaxWidth().height(0.5.dp).background(KlikLineSoft),
      )
      Spacer(Modifier.height(K1Sp.s))
      Row(verticalAlignment = Alignment.CenterVertically) {
        LinkChainIcon()
        Spacer(Modifier.width(4.dp))
        Text(
          "From session · ${sessionRef.take(8)}",
          style = K1Type.metaSm.copy(color = KlikInkTertiary),
        )
      }
    }
  }
}

@Composable
private fun ActionButton(
  label: String,
  primary: Boolean,
  muted: Boolean = false,
  onClick: () -> Unit,
) {
  val bg = when {
    primary -> KlikInkPrimary
    else -> androidx.compose.ui.graphics.Color.Transparent
  }
  val fg = when {
    primary -> KlikPaperCard
    muted -> KlikInkMuted
    else -> KlikInkPrimary
  }
  val borderColor = when {
    primary -> KlikInkPrimary
    muted -> KlikLineHairline
    else -> KlikInkMuted
  }
  Box(
    Modifier
      .clip(K1R.chip)
      .background(bg)
      .border(0.5.dp, borderColor, K1R.chip)
      .k1Clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      label,
      style = K1Type.meta.copy(
        color = fg,
        fontSize = 11.sp,
        fontWeight = if (primary) FontWeight.Medium else FontWeight.Normal,
      ),
    )
  }
}

@Composable
private fun TaskCategoryGroup(
  label: String,
  tasks: List<TaskMetadata>,
  isUnread: (TaskMetadata) -> Boolean = { false },
  highlightedTaskId: String? = null,
  forceExpanded: Boolean = false,
  onTaskClick: (TaskMetadata) -> Unit = {},
  onRetryTask: (String) -> Unit = {},
) {
  var expanded by remember { mutableStateOf(forceExpanded || tasks.size <= 3) }
  // Re-expand when a deep-link highlight lands inside this group so the
  // user can immediately see the row that pulled them here.
  if (forceExpanded && !expanded) expanded = true
  val unreadCount = tasks.count(isUnread)
  K1Card(soft = true, onClick = if (!expanded) ({ expanded = true }) else null) {
    Row(
      Modifier.fillMaxWidth().k1Clickable { expanded = !expanded },
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(label, style = K1Type.bodySm.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
      if (unreadCount > 0) {
        UnreadCountLabel(unreadCount)
        Spacer(Modifier.width(8.dp))
      }
      Text(
        "${tasks.size}",
        style = K1Type.metaSm.copy(color = KlikInkTertiary),
      )
      Spacer(Modifier.width(4.dp))
      Text(
        if (expanded) "▲" else "▼",
        style = K1Type.metaSm.copy(color = KlikInkMuted, fontSize = 8.sp),
      )
    }
    if (expanded) {
      Spacer(Modifier.height(K1Sp.s))
      Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikLineHairline))
      tasks.forEach { t ->
        RunningTaskRow(
          t = t,
          isUnread = isUnread(t),
          isHighlighted = t.id == highlightedTaskId,
          onClick = { onTaskClick(t) },
          onRetry = { onRetryTask(t.id) },
        )
        Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikLineHairline))
      }
    }
  }
}

/**
 * One row inside the Running section. Visual goal is to make in-flight tasks
 * obviously *active*, not at-rest:
 *   - Pulsing accent dot (vs the static green "done" check).
 *   - Stage caption ("Running", "Evaluating", "Queued") with step counter
 *     when execution-step data is available, in KlikRunning ink.
 *   - Re-do chip at the trailing edge so the user can kick off a fresh run
 *     without leaving the list.
 */
@Composable
private fun RunningTaskRow(
  t: TaskMetadata,
  isUnread: Boolean,
  isHighlighted: Boolean = false,
  onClick: () -> Unit,
  onRetry: () -> Unit,
) {
  Row(
    Modifier.fillMaxWidth()
      .then(if (isHighlighted) Modifier.background(KlikWarn.copy(alpha = 0.18f)) else Modifier)
      .k1Clickable(onClick = onClick)
      .padding(vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val infinite = rememberInfiniteTransition(label = "runningPulse")
    val pulse by infinite.animateFloat(
      initialValue = 1f,
      targetValue = 0.4f,
      animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
      label = "pulseAlpha",
    )
    Box(
      Modifier.size(7.dp).clip(CircleShape).background(KlikRunning.copy(alpha = pulse)),
    )
    Spacer(Modifier.width(10.dp))
    Column(Modifier.weight(1f)) {
      Text(
        t.title,
        style = K1Type.bodyMd.copy(
          fontWeight = if (isUnread) FontWeight.Medium else FontWeight.Normal,
        ),
      )
      // Stage line: "Running · step 3/5" or "Queued · projectName".
      val stageLabel = execStageLabel(t.kkExecStatus)
      val totalSteps = t.executionSteps.size + (if (t.currentExecutingStep != null) 1 else 0)
      val stepInfo = t.currentExecutingStep?.let { current ->
        if (totalSteps > 0) "step $current/$totalSteps" else "step $current"
      }
      val metaParts = buildList {
        add(stageLabel)
        stepInfo?.let { add(it) }
        t.relatedProject.takeIf { it.isNotBlank() }?.let { add(it) }
      }
      Spacer(Modifier.height(2.dp))
      Text(
        metaParts.joinToString(" · "),
        style = K1Type.metaSm.copy(color = KlikRunning),
      )
    }
    if (isUnread) {
      Spacer(Modifier.width(8.dp))
      Box(Modifier.size(6.dp).clip(CircleShape).background(KlikAlert))
    }
    Spacer(Modifier.width(8.dp))
    Box(
      Modifier
        .clip(K1R.chip)
        .border(0.5.dp, KlikInkMuted, K1R.chip)
        .k1Clickable(onClick = onRetry)
        .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
      Text(
        "Re-do",
        style = K1Type.meta.copy(
          color = KlikInkSecondary,
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium,
        ),
      )
    }
  }
}

@Composable
private fun RunningRow(t: TaskMetadata, onClick: () -> Unit = {}) {
  Row(
    Modifier
      .fillMaxWidth()
      .clip(K1R.card)
      .background(KlikPaperCard)
      .border(0.5.dp, KlikLineHairline, K1R.card)
      .k1Clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Clock icon
    Canvas(Modifier.size(16.dp)) {
      val w = 1.2.dp.toPx()
      drawCircle(
        color = KlikInkPrimary,
        style = Stroke(w),
        radius = 6.dp.toPx(),
        center = Offset(size.width / 2f, size.height / 2f),
      )
      drawLine(
        color = KlikInkPrimary,
        strokeWidth = w,
        cap = StrokeCap.Round,
        start = Offset(size.width / 2f, size.height / 2f),
        end = Offset(size.width / 2f, size.height / 2f - 3.dp.toPx()),
      )
      drawLine(
        color = KlikInkPrimary,
        strokeWidth = w,
        cap = StrokeCap.Round,
        start = Offset(size.width / 2f, size.height / 2f),
        end = Offset(size.width / 2f + 2.dp.toPx(), size.height / 2f + 1.2.dp.toPx()),
      )
    }
    Spacer(Modifier.width(K1Sp.m))
    val needsReauth = t.status == TaskStatus.REQUIRES_REAUTH ||
      t.kkExecStatus?.uppercase() == "REQUIRES_REAUTH" ||
      t.reauthInfo != null
    Column(Modifier.weight(1f)) {
      Text(t.title, style = K1Type.bodySm)
      val stageLabel = execStageLabel(t.kkExecStatus)
      val meta = buildList {
        add(stageLabel)
        t.relatedProject.takeIf { it.isNotBlank() }?.let { add(it) }
      }.joinToString(" · ")
      Spacer(Modifier.height(2.dp))
      Text(
        meta,
        style = K1Type.metaSm.copy(color = if (needsReauth) KlikAlert else KlikInkSecondary),
      )
      if (needsReauth) {
        val reason = t.reauthInfo?.reason?.takeIf { it.isNotBlank() }
          ?: "OAuth token revoked — reconnect to finish this todo"
        Spacer(Modifier.height(2.dp))
        Text(reason, style = K1Type.metaSm.copy(color = KlikAlert))
      }
    }
    if (needsReauth) {
      // Static red dot — same shape as the running pulse, distinct color
      // so blocked-by-reauth todos read as actionable, not in-flight.
      Box(
        Modifier.size(6.dp).background(KlikAlert, CircleShape),
      )
    } else {
      // Pulsing dot so the user sees Klik is actively on it.
      val infinite = rememberInfiniteTransition(label = "execPulse")
      val pulseAlpha by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseA",
      )
      Box(
        Modifier
          .size(6.dp)
          .background(KlikInkPrimary.copy(alpha = pulseAlpha), CircleShape),
      )
    }
  }
}

@Composable
private fun FailedRow(
  t: TaskMetadata,
  isUnread: Boolean = false,
  onRetry: () -> Unit = {},
  onOpen: () -> Unit = {},
) {
  // Failed tasks read as a paper card tinted with a hairline KlikAlert border so
  // they stay editorial but never blend into the green check-marked Done list.
  Row(
    Modifier
      .fillMaxWidth()
      .clip(K1R.card)
      .background(KlikPaperCard)
      .border(0.5.dp, KlikAlert.copy(alpha = 0.55f), K1R.card)
      .k1Clickable(onClick = onOpen)
      .padding(horizontal = 14.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // ✕-in-circle, mirrors the ✓-in-circle of DoneRow but in alert ink.
    Canvas(Modifier.size(14.dp)) {
      val w = 1.dp.toPx()
      drawCircle(
        color = KlikAlert,
        style = Stroke(w),
        radius = 5.5.dp.toPx(),
        center = Offset(size.width / 2f, size.height / 2f),
      )
      val s = size.width
      drawLine(
        color = KlikAlert,
        strokeWidth = 1.2.dp.toPx(),
        cap = StrokeCap.Round,
        start = Offset(s * 0.36f, s * 0.36f),
        end = Offset(s * 0.64f, s * 0.64f),
      )
      drawLine(
        color = KlikAlert,
        strokeWidth = 1.2.dp.toPx(),
        cap = StrokeCap.Round,
        start = Offset(s * 0.64f, s * 0.36f),
        end = Offset(s * 0.36f, s * 0.64f),
      )
    }
    Spacer(Modifier.width(K1Sp.m))
    Column(Modifier.weight(1f)) {
      Text(
        t.title,
        style = K1Type.bodySm.copy(
          color = KlikInkPrimary,
          fontWeight = if (isUnread) FontWeight.Medium else FontWeight.Normal,
        ),
      )
      val reason = execStageLabel(t.kkExecStatus)
      val meta = buildList {
        add(reason)
        t.relatedProject.takeIf { it.isNotBlank() }?.let { add(it) }
      }.joinToString(" · ")
      Spacer(Modifier.height(2.dp))
      Text(meta, style = K1Type.metaSm.copy(color = KlikAlert))
    }
    Spacer(Modifier.width(K1Sp.s))
    Box(
      Modifier
        .clip(K1R.chip)
        .border(0.5.dp, KlikAlert, K1R.chip)
        .k1Clickable(onClick = onRetry)
        .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
      Text(
        "Retry",
        style = K1Type.meta.copy(color = KlikAlert, fontSize = 11.sp, fontWeight = FontWeight.Medium),
      )
    }
    if (isUnread) {
      Spacer(Modifier.width(8.dp))
      UnreadDot(KlikAlert)
    }
  }
}

@Composable
private fun UnreadDot(color: androidx.compose.ui.graphics.Color = KlikAlert) {
  Box(Modifier.size(6.dp).clip(CircleShape).background(color))
}

@Composable
private fun UnreadCountLabel(count: Int) {
  Box(
    Modifier
      .clip(K1R.pill)
      .background(KlikAlert)
      .padding(horizontal = 6.dp, vertical = 1.dp),
  ) {
    Text(
      "$count new",
      style = K1Type.meta.copy(color = KlikPaperCard, fontSize = 10.sp, fontWeight = FontWeight.Medium),
    )
  }
}

@Composable
private fun DoneRow(t: TaskMetadata, onRetry: () -> Unit = {}) {
  Row(
    Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Check-in-circle
    Canvas(Modifier.size(14.dp)) {
      val w = 1.dp.toPx()
      drawCircle(
        color = KlikLineTick,
        style = Stroke(w),
        radius = 5.5.dp.toPx(),
        center = Offset(size.width / 2f, size.height / 2f),
      )
      val s = size.width
      drawLine(
        color = KlikInkTertiary,
        strokeWidth = 1.2.dp.toPx(),
        cap = StrokeCap.Round,
        start = Offset(s * 0.32f, s * 0.5f),
        end = Offset(s * 0.46f, s * 0.64f),
      )
      drawLine(
        color = KlikInkTertiary,
        strokeWidth = 1.2.dp.toPx(),
        cap = StrokeCap.Round,
        start = Offset(s * 0.46f, s * 0.64f),
        end = Offset(s * 0.7f, s * 0.36f),
      )
    }
    Spacer(Modifier.width(K1Sp.m))
    Text(
      t.title,
      style = K1Type.caption.copy(
        color = KlikInkTertiary,
        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
      ),
      modifier = Modifier.weight(1f),
    )
    if (t.completedInfo != null) {
      Text(t.completedInfo!!, style = K1Type.metaSm)
      Spacer(Modifier.width(8.dp))
    }
    Box(
      Modifier
        .clip(K1R.chip)
        .border(0.5.dp, KlikLineHairline, K1R.chip)
        .k1Clickable(onClick = onRetry)
        .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
      Text(
        "Re-do",
        style = K1Type.meta.copy(
          color = KlikInkTertiary,
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium,
        ),
      )
    }
  }
}

@Composable
private fun SearchIcon() {
  Canvas(Modifier.size(18.dp)) {
    val w = 1.2.dp.toPx()
    drawCircle(
      color = KlikInkPrimary,
      style = Stroke(w),
      radius = 5.dp.toPx(),
      center = Offset(7.dp.toPx(), 7.dp.toPx()),
    )
    drawLine(
      color = KlikInkPrimary,
      strokeWidth = w,
      cap = StrokeCap.Round,
      start = Offset(11.dp.toPx(), 11.dp.toPx()),
      end = Offset(14.dp.toPx(), 14.dp.toPx()),
    )
  }
}

@Composable
private fun LinkChainIcon() {
  Canvas(Modifier.size(10.dp)) {
    val w = 1.dp.toPx()
    drawLine(
      color = KlikInkTertiary,
      strokeWidth = w,
      cap = StrokeCap.Round,
      start = Offset(4.dp.toPx(), 6.dp.toPx()),
      end = Offset(6.dp.toPx(), 4.dp.toPx()),
    )
    drawCircle(
      color = KlikInkTertiary,
      style = Stroke(w),
      radius = 1.8.dp.toPx(),
      center = Offset(3.5.dp.toPx(), 6.5.dp.toPx()),
    )
    drawCircle(
      color = KlikInkTertiary,
      style = Stroke(w),
      radius = 1.8.dp.toPx(),
      center = Offset(6.5.dp.toPx(), 3.5.dp.toPx()),
    )
  }
}

/** Human-readable K1 stage label for a KK_exec task status string. */
private fun execStageLabel(rawStatus: String?): String {
  val s = rawStatus?.uppercase()?.trim().orEmpty()
  return when (s) {
    "PENDING", "QUEUED" -> "Queued"
    "EVALUATING" -> "Evaluating"
    "RUNNING", "IN_PROGRESS" -> "Running"
    "IN_REVIEW" -> "Needs your OK"
    "APPROVED" -> "Approved"
    "COMPLETED" -> "Done"
    "REJECTED", "CANNOT_EXECUTE" -> "Blocked"
    "FAILED", "ERROR" -> "Failed"
    "ARCHIVED" -> "Archived"
    "REQUIRES_REAUTH" -> "Reconnect needed"
    "" -> "Running"
    else -> s.lowercase().replaceFirstChar { it.uppercase() }
  }
}
