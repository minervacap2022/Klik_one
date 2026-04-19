// Copyright 2025, Klik — Klik One redesign of the Today tab.
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.domain.entity.DailyBriefing
import io.github.fletchmckee.liquid.samples.app.domain.entity.Insights
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.theme.KlikAlert
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineMute
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikWarn
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData
import io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Klik One — Today.
 *
 * Drop-in replacement for the legacy `CalendarScreen`. Same parameter list so
 * MainApp.kt's routing `"today" -> CalendarScreen(...)` can switch to
 * `"today" -> TodayScreen(...)` with no call-site edits.
 */
@Composable
fun TodayScreen(
    selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    isCalendarExpanded: Boolean = false,
    isLoading: Boolean = false,
    isRefreshing: Boolean = false,
    isLlmDataLoading: Boolean = false,
    meetings: List<Meeting> = emptyList(),
    dailyBriefing: DailyBriefing? = null,
    insights: Insights? = null,
    speakerMap: Map<String, String> = emptyMap(),
    onRefreshMeetings: () -> Unit = {},
    onArchiveMeeting: (String) -> Unit = {},
    expandSessionId: String? = null,
    expandSegmentId: String? = null,
    expandSegmentText: String? = null,
    onSessionExpanded: () -> Unit = {},
    onDateChange: (LocalDate) -> Unit = {},
    onContentOverlap: (Boolean) -> Unit = {},
    topDockHeightPx: Int = 0,
    tasks: List<TaskMetadata> = emptyList(),
    projects: List<Project> = emptyList(),
    people: List<Person> = emptyList(),
    organizations: List<Organization> = emptyList(),
    onEntityClick: (EntityNavigationData) -> Unit = {},
    onSegmentClick: (TracedSegmentNavigation) -> Unit = {},
    onMeetingClick: (Meeting) -> Unit = {},
) {
    val scroll = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .background(KlikPaperApp)
            .verticalScroll(scroll)
            .padding(top = 52.dp, bottom = 120.dp)
    ) {
        K1Header(
            title = "Today",
            trailing = { K1Chip(label = dateLabel(selectedDate)) }
        )
        Spacer(Modifier.height(K1Sp.lg))

        // ── RIGHT NOW ─────────────────────────────────────────────────────
        val nowMeetings = meetings.filter { !it.isPast && !it.isArchived }.take(1)
        Column(Modifier.padding(horizontal = 20.dp)) {
            K1SectionHeader(
                "Right now",
                dotColor = if (nowMeetings.isNotEmpty()) KlikAlert else KlikLineMute
            )
            Spacer(Modifier.height(K1Sp.s))
            if (nowMeetings.isNotEmpty()) LiveSessionCard(nowMeetings.first())
            else QuietCard()
        }

        Spacer(Modifier.height(K1Sp.xxl))

        // ── UP NEXT ───────────────────────────────────────────────────────
        val upcoming = meetings.filter { !it.isPast && !it.isArchived }.drop(1).take(4)
        if (upcoming.isNotEmpty()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                K1SectionHeader("Up next", count = upcoming.size)
                Spacer(Modifier.height(K1Sp.s))
                upcoming.forEach { m ->
                    MeetingRow(m, onClick = { onMeetingClick(m) })
                    Spacer(Modifier.height(6.dp))
                }
            }
            Spacer(Modifier.height(K1Sp.xxl))
        }

        // ── MOVES FOR YOU ─────────────────────────────────────────────────
        val pending = tasks.filter { it.needsConfirmation }.take(3)
        if (pending.isNotEmpty()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                K1SectionHeader("Moves for you", count = pending.size, dotColor = KlikWarn)
                Spacer(Modifier.height(K1Sp.s))
                pending.forEach { t ->
                    MoveMiniCard(t)
                    Spacer(Modifier.height(6.dp))
                }
            }
            Spacer(Modifier.height(K1Sp.xxl))
        }

        // ── BRIEF ─────────────────────────────────────────────────────────
        dailyBriefing?.let { brief ->
            Column(Modifier.padding(horizontal = 20.dp)) {
                K1SectionHeader("Brief")
                Spacer(Modifier.height(K1Sp.s))
                K1Card(soft = true) {
                    Text(brief.summary, style = K1Type.bodySm)
                    if (brief.topPriority != null) {
                        Spacer(Modifier.height(K1Sp.s))
                        Text("Top priority — ${brief.topPriority}", style = K1Type.caption)
                    }
                }
            }
            Spacer(Modifier.height(K1Sp.xxl))
        }

        // ── INSIGHTS ──────────────────────────────────────────────────────
        insights?.let { ins ->
            if (ins.highlights.isNotEmpty() || ins.summary.isNotBlank()) {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    K1SectionHeader("This week")
                    Spacer(Modifier.height(K1Sp.s))
                    K1Card {
                        if (ins.summary.isNotBlank()) {
                            Text(ins.summary, style = K1Type.bodySm)
                            Spacer(Modifier.height(K1Sp.s))
                        }
                        ins.highlights.take(3).forEach { h ->
                            Row {
                                Text("·  ", style = K1Type.caption)
                                Text(h, style = K1Type.bodySm)
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveSessionCard(m: Meeting) {
    K1Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            K1RecDot()
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(m.title.ifBlank { "In conversation" }, style = K1Type.bodyMd)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${m.participants.size} speakers · ${m.time}",
                    style = K1Type.meta
                )
            }
            K1WaveformLive(Modifier.size(width = 40.dp, height = 24.dp))
        }
    }
}

@Composable
private fun QuietCard() {
    K1Card(soft = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            K1Waveform(heights = listOf(6f, 10f, 4f, 8f), color = KlikInkMuted)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Quiet.", style = K1Type.bodyMd)
                Text("Klik is listening when you're ready.", style = K1Type.caption)
            }
        }
    }
}

@Composable
private fun MeetingRow(m: Meeting, onClick: () -> Unit = {}) {
    K1Card(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(64.dp)) {
                Text(m.time, style = K1Type.bodyMd.copy(fontSize = 13.sp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(m.title.ifBlank { "Untitled meeting" }, style = K1Type.bodyMd)
                if (m.summary.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(m.summary.take(60), style = K1Type.caption)
                }
            }
            if (m.participants.isNotEmpty()) {
                K1AvatarStack(
                    initialsList = m.participants.take(3).map { p -> initialsOf(p.name) },
                    size = 20.dp
                )
            }
        }
    }
}

@Composable
private fun MoveMiniCard(t: TaskMetadata) {
    K1Card(soft = true) {
        Column {
            Text(t.title, style = K1Type.bodyMd)
            if (t.subtitle.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(t.subtitle, style = K1Type.meta)
            }
            if (!t.description.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(t.description!!.take(80), style = K1Type.caption)
            }
            Spacer(Modifier.height(K1Sp.s))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                K1Chip(label = "Approve", selected = true, onClick = {})
                K1Chip(label = "Edit", onClick = {})
                K1Chip(label = "Skip", onClick = {})
            }
        }
    }
}

private fun initialsOf(name: String): String =
    name.trim().split(" ").filter { it.isNotEmpty() }
        .take(2).joinToString("") { it.take(1).uppercase() }

private fun dateLabel(d: LocalDate): String {
    val dayName = d.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    val month = d.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "$dayName · $month ${d.dayOfMonth}"
}
