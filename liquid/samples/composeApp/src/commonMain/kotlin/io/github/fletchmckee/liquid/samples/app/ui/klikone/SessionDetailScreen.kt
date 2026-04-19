// Copyright 2025, Klik — Klik One Session Detail. Matches klik_one_session_detail.html.
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.theme.*

private enum class SessionTab { Summary, Todos, Transcript, Highlights }

/**
 * Klik One — Session Detail.
 *
 * Renders a completed/live meeting with Summary / To-dos / Transcript / Highlights tabs.
 * Designed as a full-screen replacement you can route to from Today.
 */
@Composable
fun SessionDetailScreen(
    meeting: Meeting,
    tasks: List<TaskMetadata> = emptyList(),
    onBack: () -> Unit,
    onShare: () -> Unit = {},
    onMore: () -> Unit = {},
) {
    var tab by remember { mutableStateOf(SessionTab.Summary) }

    // Derive linked tasks
    val linked = tasks.filter { it.relatedMeetingId == meeting.id }

    Column(
        Modifier
            .fillMaxSize()
            .background(KlikPaperApp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier.clickable(onClick = onBack).padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BackChevron()
                Spacer(Modifier.width(4.dp))
                Text("Today", style = K1Type.bodySm)
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ShareIcon(onClick = onShare)
                MoreDotsIcon(onClick = onMore)
            }
        }

        // Status + date
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val completed = meeting.isPast
                Box(
                    Modifier.size(5.dp).clip(CircleShape)
                        .background(if (completed) KlikCommitmentAccent else KlikAlert),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (completed) "COMPLETED" else "LIVE",
                    style = K1Type.eyebrow.copy(
                        color = if (completed) KlikCommitmentSubtext else KlikAlert,
                        letterSpacing = 0.6.sp,
                        fontWeight = FontWeight.Medium,
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text("· ${formatDate(meeting)}", style = K1Type.metaSm)
            }
            Spacer(Modifier.height(6.dp))
            Text(meeting.title.ifBlank { "Untitled session" }, style = K1Type.h3)
            Spacer(Modifier.height(4.dp))
            Text(meeting.time, style = K1Type.caption)
        }

        Spacer(Modifier.height(K1Sp.m))

        // Participants pill
        Row(
            Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(K1R.soft)
                .background(KlikPaperSoft)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            K1AvatarStack(
                initialsList = meeting.participants.take(4).map { p -> initialsOf(p.name) },
                size = 24.dp,
            )
            Spacer(Modifier.width(10.dp))
            Text("${meeting.participants.size} people", style = K1Type.meta)
            Spacer(Modifier.weight(1f))
            K1Chip(label = "+ Add", onClick = {})
        }

        Spacer(Modifier.height(K1Sp.lg))

        // Tabs
        Row(
            Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            TabItem("Summary", tab == SessionTab.Summary) { tab = SessionTab.Summary }
            TabItem(
                "To-dos",
                tab == SessionTab.Todos,
                badge = linked.size.takeIf { it > 0 },
            ) { tab = SessionTab.Todos }
            TabItem("Transcript", tab == SessionTab.Transcript) { tab = SessionTab.Transcript }
            TabItem("Highlights", tab == SessionTab.Highlights) { tab = SessionTab.Highlights }
        }
        Box(
            Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(0.5.dp)
                .background(KlikLineHairline)
        )

        Spacer(Modifier.height(K1Sp.lg))

        when (tab) {
            SessionTab.Summary    -> SummaryPanel(meeting)
            SessionTab.Todos      -> TodosPanel(linked)
            SessionTab.Transcript -> TranscriptPanel(meeting)
            SessionTab.Highlights -> HighlightsPanel(meeting)
        }

        Spacer(Modifier.height(140.dp))
    }
}

@Composable
private fun TabItem(label: String, active: Boolean, badge: Int? = null, onClick: () -> Unit) {
    Column(
        Modifier.clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = K1Type.caption.copy(
                    fontSize = 12.sp,
                    color = if (active) KlikInkPrimary else KlikInkMuted,
                    fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                ),
            )
            if (badge != null && badge > 0) {
                Spacer(Modifier.width(3.dp))
                Text(
                    "$badge",
                    style = K1Type.metaSm.copy(
                        color = KlikAlert,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
        if (active) {
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(1.5.dp).background(KlikInkPrimary))
        }
    }
}

@Composable
private fun SummaryPanel(m: Meeting) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        if (m.summary.isNotBlank()) {
            K1Eyebrow("In 3 lines")
            Spacer(Modifier.height(K1Sp.s))
            Text(m.summary, style = K1Type.bodySm)
            Spacer(Modifier.height(K1Sp.xxl))
        }

        if (m.actionItems.isNotEmpty()) {
            K1SectionHeader("Decisions", count = m.actionItems.size)
            Spacer(Modifier.height(K1Sp.s))
            m.actionItems.take(5).forEach { todo ->
                K1Card(soft = true) {
                    Text(todo.text, style = K1Type.caption)
                    Spacer(Modifier.height(3.dp))
                    Text(todo.type.name.lowercase().replace('_', ' '), style = K1Type.metaSm)
                }
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.height(K1Sp.xxl))
        }

        if (m.participants.isNotEmpty()) {
            K1Eyebrow("Mentioned")
            Spacer(Modifier.height(K1Sp.s))
            FlowRowCompat(horizontalGap = 5.dp, verticalGap = 5.dp) {
                m.participants.take(6).forEach { p ->
                    K1Chip(label = p.name, leading = {
                        Box(
                            Modifier.size(12.dp).clip(CircleShape)
                                .background(KlikAvatarBg[p.name.hashCode().let { if (it < 0) -it else it } % KlikAvatarBg.size]),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                initialsOf(p.name),
                                style = K1Type.metaSm.copy(
                                    color = KlikAvatarFg[p.name.hashCode().let { if (it < 0) -it else it } % KlikAvatarFg.size],
                                    fontSize = 6.sp,
                                ),
                            )
                        }
                    })
                }
            }
        }
    }
}

@Composable
private fun TodosPanel(linked: List<TaskMetadata>) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        if (linked.isEmpty()) {
            Text(
                "No follow-ups captured yet.",
                style = K1Type.caption.copy(color = KlikInkTertiary),
            )
        } else {
            linked.forEach { t ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(Modifier.size(14.dp).clip(CircleShape).background(KlikWarn))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t.title, style = K1Type.bodyMd)
                        if (t.subtitle.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(t.subtitle, style = K1Type.caption)
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikPaperChip))
            }
        }
    }
}

@Composable
private fun TranscriptPanel(m: Meeting) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        val lines = m.transcript?.lines()?.filter { it.isNotBlank() }.orEmpty()
        if (lines.isEmpty()) {
            Text(
                "Transcript not available yet.",
                style = K1Type.caption.copy(color = KlikInkTertiary),
            )
        } else {
            lines.take(40).forEach { line ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text(line.take(240), style = K1Type.bodySm)
                }
            }
        }
    }
}

@Composable
private fun HighlightsPanel(m: Meeting) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        Text(
            "${m.actionItems.size} moments Klik flagged. Tap a decision to jump to transcript.",
            style = K1Type.caption,
        )
        Spacer(Modifier.height(K1Sp.m))
        m.actionItems.take(5).forEach { a ->
            K1SignalCard(
                signal = K1Signal.Decision,
                eyebrow = "Decision",
                body = a.text,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── Small icon helpers ───────────────────────────────────────────────────
@Composable
private fun BackChevron() {
    Canvas(Modifier.size(16.dp)) {
        val w = 1.3.dp.toPx()
        drawLine(
            color = KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round,
            start = Offset(10.dp.toPx(), 3.5.dp.toPx()),
            end = Offset(5.5.dp.toPx(), 8.dp.toPx()),
        )
        drawLine(
            color = KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round,
            start = Offset(5.5.dp.toPx(), 8.dp.toPx()),
            end = Offset(10.dp.toPx(), 12.5.dp.toPx()),
        )
    }
}

@Composable
private fun ShareIcon(onClick: () -> Unit) {
    Canvas(
        Modifier
            .size(16.dp)
            .clickable(onClick = onClick)
    ) {
        val w = 1.2.dp.toPx()
        drawLine(
            color = KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round,
            start = Offset(8.dp.toPx(), 2.5.dp.toPx()),
            end = Offset(8.dp.toPx(), 10.5.dp.toPx()),
        )
        drawLine(
            color = KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round,
            start = Offset(8.dp.toPx(), 2.5.dp.toPx()),
            end = Offset(5.5.dp.toPx(), 5.dp.toPx()),
        )
        drawLine(
            color = KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round,
            start = Offset(8.dp.toPx(), 2.5.dp.toPx()),
            end = Offset(10.5.dp.toPx(), 5.dp.toPx()),
        )
        // frame
        drawLine(
            color = KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round,
            start = Offset(3.5.dp.toPx(), 10.dp.toPx()),
            end = Offset(3.5.dp.toPx(), 13.5.dp.toPx()),
        )
        drawLine(
            color = KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round,
            start = Offset(12.5.dp.toPx(), 10.dp.toPx()),
            end = Offset(12.5.dp.toPx(), 13.5.dp.toPx()),
        )
        drawLine(
            color = KlikInkPrimary, strokeWidth = w, cap = StrokeCap.Round,
            start = Offset(3.5.dp.toPx(), 13.5.dp.toPx()),
            end = Offset(12.5.dp.toPx(), 13.5.dp.toPx()),
        )
    }
}

@Composable
private fun MoreDotsIcon(onClick: () -> Unit) {
    Canvas(
        Modifier.size(16.dp).clickable(onClick = onClick)
    ) {
        val r = 1.1.dp.toPx()
        val cx = size.width / 2f
        drawCircle(color = KlikInkPrimary, radius = r, center = Offset(cx, 3.5.dp.toPx()))
        drawCircle(color = KlikInkPrimary, radius = r, center = Offset(cx, 8.dp.toPx()))
        drawCircle(color = KlikInkPrimary, radius = r, center = Offset(cx, 12.5.dp.toPx()))
    }
}

private fun initialsOf(name: String): String =
    name.trim().split(" ").filter { it.isNotEmpty() }
        .take(2).joinToString("") { it.take(1).uppercase() }

private fun formatDate(m: Meeting): String {
    val d = m.date
    val day = d.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    val month = d.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "$day, $month ${d.dayOfMonth}"
}

/** Tiny flow-row shim — arranges children in rows, wrapping to next row on overflow. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowCompat(
    horizontalGap: androidx.compose.ui.unit.Dp,
    verticalGap: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
        verticalArrangement = Arrangement.spacedBy(verticalGap),
    ) { content() }
}
