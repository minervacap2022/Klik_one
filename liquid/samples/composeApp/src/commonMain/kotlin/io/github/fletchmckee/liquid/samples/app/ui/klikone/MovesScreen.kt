// Copyright 2025, Klik — Klik One Moves tab. Matches klik_one_5_screens.html screen 04.
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskStatus
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.theme.*
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityType
import io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation

/** Klik One — Moves. Drop-in replacement for `EventsScreen`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovesScreen(
    isLoading: Boolean = false,
    isRefreshing: Boolean = false,
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

    // Completed buckets the Done list; everything else daily-tasks goes into Running.
    // Needs-your-OK is the combined review queue (featured AI suggestions + sensitive
    // todos that require explicit user confirmation).
    fun isDone(t: TaskMetadata): Boolean =
        t.status == TaskStatus.COMPLETED ||
            t.kkExecStatus?.uppercase() in setOf("COMPLETED", "APPROVED")

    val needsOk = featuredTasks + sensitiveTasks
    val doneIds = dailyTasks.filter(::isDone).map { it.id }.toSet()
    val running = dailyTasks.filter { it.id !in doneIds }
    val done = dailyTasks.filter { it.id in doneIds }
    val totalAll = needsOk.size + running.size + done.size

    val filteredAll = when (filter) {
        "needs" -> needsOk
        "running" -> running
        "done" -> done
        else -> needsOk + running + done
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        state = rememberPullToRefreshState(),
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize().background(KlikPaperApp),
    ) {
    Column(
        Modifier
            .fillMaxSize()
            .background(KlikPaperApp)
            .verticalScroll(rememberScrollState())
            .padding(top = 52.dp, bottom = 120.dp)
    ) {
        // Header with search icon on the right — matches reference
        K1Header(
            title = "Moves",
            trailing = { SearchIcon() }
        )
        Spacer(Modifier.height(K1Sp.md))

        // Filter chips
        Row(
            Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            K1Chip(
                label = "All · $totalAll",
                selected = filter == "all",
                onClick = { filter = "all" }
            )
            K1Chip(
                label = "Needs OK · ${needsOk.size}",
                selected = filter == "needs",
                onClick = { filter = "needs" }
            )
            K1Chip(
                label = "Running · ${running.size}",
                selected = filter == "running",
                onClick = { filter = "running" }
            )
            K1Chip(
                label = "Done",
                selected = filter == "done",
                onClick = { filter = "done" }
            )
        }

        Spacer(Modifier.height(K1Sp.xl))

        if ((filter == "all" || filter == "needs") && needsOk.isNotEmpty()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                K1SectionHeader("Needs your OK", count = needsOk.size, dotColor = KlikWarn)
                Spacer(Modifier.height(K1Sp.s))
                needsOk.forEach { t ->
                    NeedsOkCard(
                        t = t,
                        onApprove = onApproveTask,
                        onArchive = onArchiveTaskOnBackend,
                        onOpen = { onEntityClick(EntityNavigationData(EntityType.TASK, t.id)) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(K1Sp.xxl))
        }

        if ((filter == "all" || filter == "running") && running.isNotEmpty()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                K1SectionHeader("Running", count = running.size, dotColor = KlikRunning)
                Spacer(Modifier.height(K1Sp.s))
                running.forEach { t ->
                    RunningRow(t, onClick = {
                        onEntityClick(EntityNavigationData(EntityType.TASK, t.id))
                    })
                    Spacer(Modifier.height(6.dp))
                }
            }
            Spacer(Modifier.height(K1Sp.xxl))
        }

        if ((filter == "all" || filter == "done") && done.isNotEmpty()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                K1SectionHeader("Done today", count = done.size)
                Spacer(Modifier.height(K1Sp.s))
                done.forEach { t -> DoneRow(t) }
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
    onApprove: (String) -> Unit,
    onArchive: (String) -> Unit,
    onOpen: () -> Unit = {},
) {
    K1Card(soft = true, onClick = onOpen) {
        // Top row: title + subtitle | time ago
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(t.title, style = K1Type.bodyMd)
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
                    .padding(horizontal = 12.dp, vertical = 10.dp)
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
                Modifier.fillMaxWidth().height(0.5.dp).background(KlikLineSoft)
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
        muted   -> KlikInkMuted
        else    -> KlikInkPrimary
    }
    val borderColor = when {
        primary -> KlikInkPrimary
        muted   -> KlikLineHairline
        else    -> KlikInkMuted
    }
    Box(
        Modifier
            .clip(K1R.chip)
            .background(bg)
            .border(0.5.dp, borderColor, K1R.chip)
            .clickable(onClick = onClick)
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
private fun RunningRow(t: TaskMetadata, onClick: () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(K1R.card)
            .background(KlikPaperCard)
            .border(0.5.dp, KlikLineHairline, K1R.card)
            .clickable(onClick = onClick)
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
                color = KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round,
                start = Offset(size.width / 2f, size.height / 2f),
                end = Offset(size.width / 2f, size.height / 2f - 3.dp.toPx()),
            )
            drawLine(
                color = KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round,
                start = Offset(size.width / 2f, size.height / 2f),
                end = Offset(size.width / 2f + 2.dp.toPx(), size.height / 2f + 1.2.dp.toPx()),
            )
        }
        Spacer(Modifier.width(K1Sp.m))
        // OAuth reconnect: a todo whose KK_exec execution returned 401 and
        // whose reactive refresh failed surfaces here as a status of
        // REQUIRES_REAUTH carrying provider/reason. We render the meta line
        // in red and append the reason so the user can act on it without
        // diving into the integrations screen first.
        val needsReauth = t.status == TaskStatus.REQUIRES_REAUTH ||
            t.kkExecStatus?.uppercase() == "REQUIRES_REAUTH" ||
            t.reauthInfo != null
        Column(Modifier.weight(1f)) {
            Text(t.title, style = K1Type.bodySm)
            val meta = buildList {
                if (needsReauth) {
                    add("Reconnect needed")
                } else {
                    t.kkExecStatus?.takeIf { it.isNotBlank() }?.let {
                        add("ETA ${it.lowercase().replaceFirstChar { c -> c.uppercase() }}")
                    }
                }
                t.relatedProject.takeIf { it.isNotBlank() }?.let { add(it) }
                if (isEmpty()) add("Running")
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
    }
}

@Composable
private fun DoneRow(t: TaskMetadata) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Check-in-circle
        Canvas(Modifier.size(14.dp)) {
            val w = 1.dp.toPx()
            drawCircle(
                color = KlikLineTick, style = Stroke(w),
                radius = 5.5.dp.toPx(),
                center = Offset(size.width / 2f, size.height / 2f),
            )
            val s = size.width
            drawLine(
                color = KlikInkTertiary, strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round,
                start = Offset(s * 0.32f, s * 0.5f),
                end = Offset(s * 0.46f, s * 0.64f),
            )
            drawLine(
                color = KlikInkTertiary, strokeWidth = 1.2.dp.toPx(),
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
        }
    }
}

@Composable
private fun SearchIcon() {
    Canvas(Modifier.size(18.dp)) {
        val w = 1.2.dp.toPx()
        drawCircle(
            color = KlikInkPrimary, style = Stroke(w),
            radius = 5.dp.toPx(),
            center = Offset(7.dp.toPx(), 7.dp.toPx()),
        )
        drawLine(
            color = KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round,
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
            color = KlikInkTertiary, strokeWidth = w, cap = StrokeCap.Round,
            start = Offset(4.dp.toPx(), 6.dp.toPx()),
            end = Offset(6.dp.toPx(), 4.dp.toPx()),
        )
        drawCircle(
            color = KlikInkTertiary, style = Stroke(w),
            radius = 1.8.dp.toPx(),
            center = Offset(3.5.dp.toPx(), 6.5.dp.toPx()),
        )
        drawCircle(
            color = KlikInkTertiary, style = Stroke(w),
            radius = 1.8.dp.toPx(),
            center = Offset(6.5.dp.toPx(), 3.5.dp.toPx()),
        )
    }
}
