// Copyright 2025, Klik — Klik One redesign of the Today tab.
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
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
    isRecording: Boolean = false,
    onStartRecording: () -> Unit = {},
    onStopRecording: () -> Unit = {},
    onOpenLiveRecording: () -> Unit = {},
    onCalendarToggle: () -> Unit = {},
) {
    val scroll = rememberScrollState()
    val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    // Picker view state — tracks the displayed month/year separately from selectedDate
    // so the user can browse forward/back without changing the selection.
    var pickerMonth by remember(selectedDate.monthNumber) {
        mutableStateOf(selectedDate.monthNumber)
    }
    var pickerYear by remember(selectedDate.year) {
        mutableStateOf(selectedDate.year)
    }
    val meetingsCountByDay = remember(meetings, pickerMonth, pickerYear) {
        io.github.fletchmckee.liquid.samples.app.model.getMeetingsCountForMonth(
            meetings, pickerYear, pickerMonth
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(KlikPaperApp)
            .verticalScroll(scroll)
            .padding(top = 52.dp, bottom = 120.dp)
    ) {
        // Header with date navigation — prev / date-chip / next. Tap the chip to
        // expand an inline mini calendar; long-tap jumps to today.
        val prevDate = selectedDate.minus(DatePeriod(days = 1))
        val nextDate = selectedDate.plus(DatePeriod(days = 1))
        K1Header(
            title = "Today",
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DateChevron(pointRight = false, onClick = { onDateChange(prevDate) })
                    Spacer(Modifier.width(4.dp))
                    K1Chip(
                        label = dateLabel(selectedDate),
                        selected = isCalendarExpanded,
                        onClick = onCalendarToggle,
                    )
                    Spacer(Modifier.width(4.dp))
                    DateChevron(pointRight = true, onClick = { onDateChange(nextDate) })
                }
            }
        )

        // Expanded mini calendar picker
        androidx.compose.animation.AnimatedVisibility(
            visible = isCalendarExpanded,
            enter = androidx.compose.animation.expandVertically(
                animationSpec = androidx.compose.animation.core.tween(220),
                expandFrom = Alignment.Top,
            ) + androidx.compose.animation.fadeIn(
                animationSpec = androidx.compose.animation.core.tween(220),
            ),
            exit = androidx.compose.animation.shrinkVertically(
                animationSpec = androidx.compose.animation.core.tween(180),
                shrinkTowards = Alignment.Top,
            ) + androidx.compose.animation.fadeOut(
                animationSpec = androidx.compose.animation.core.tween(180),
            ),
        ) {
            Box(
                Modifier
                    .padding(horizontal = 20.dp, vertical = K1Sp.s)
                    .fillMaxWidth()
                    .clip(K1R.card)
                    .background(
                        io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard,
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                io.github.fletchmckee.liquid.samples.app.ui.components.MiniCalendar(
                    currentMonth = pickerMonth,
                    currentYear = pickerYear,
                    selectedDate = selectedDate,
                    todayDate = todayDate,
                    meetingsCountByDay = meetingsCountByDay,
                    onDateSelected = { date ->
                        onDateChange(date)
                        onCalendarToggle() // collapse picker after pick
                    },
                    onMonthChange = { m, y ->
                        pickerMonth = m
                        pickerYear = y
                    },
                )
            }
        }

        Spacer(Modifier.height(K1Sp.lg))

        // ── RIGHT NOW ─────────────────────────────────────────────────────
        val nowMeetings = meetings.filter { !it.isPast && !it.isArchived }.take(1)
        Column(Modifier.padding(horizontal = 20.dp)) {
            K1SectionHeader(
                "Right now",
                dotColor = when {
                    isRecording -> KlikAlert
                    nowMeetings.isNotEmpty() -> KlikAlert
                    else -> KlikLineMute
                }
            )
            Spacer(Modifier.height(K1Sp.s))
            when {
                isRecording -> RecordingActiveCard(
                    onOpen = onOpenLiveRecording,
                    onStop = onStopRecording,
                )
                nowMeetings.isNotEmpty() -> LiveSessionCard(nowMeetings.first())
                else -> QuietCard(onStart = onStartRecording)
            }
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
        // Backend often echoes the summary as the first highlight — de-dupe so
        // the card doesn't read the same line twice.
        insights?.let { ins ->
            val summaryTrim = ins.summary.trim()
            val extraHighlights = ins.highlights
                .asSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.equals(summaryTrim, ignoreCase = true) }
                .distinctBy { it.lowercase() }
                .take(3)
                .toList()
            if (summaryTrim.isNotBlank() || extraHighlights.isNotEmpty()) {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    K1SectionHeader("This week")
                    Spacer(Modifier.height(K1Sp.s))
                    K1Card {
                        if (summaryTrim.isNotBlank()) {
                            Text(summaryTrim, style = K1Type.bodySm)
                            if (extraHighlights.isNotEmpty()) Spacer(Modifier.height(K1Sp.s))
                        }
                        extraHighlights.forEach { h ->
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
private fun QuietCard(onStart: () -> Unit) {
    K1Card(soft = true, onClick = onStart) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            K1Waveform(heights = listOf(6f, 10f, 4f, 8f), color = KlikInkMuted)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Quiet.", style = K1Type.bodyMd)
                Text("Tap to start listening.", style = K1Type.caption)
            }
            // Record dot glyph — tap-hint
            Box(
                Modifier
                    .size(28.dp)
                    .background(
                        io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary,
                        androidx.compose.foundation.shape.CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(
                            io.github.fletchmckee.liquid.samples.app.theme.KlikAlert,
                            androidx.compose.foundation.shape.CircleShape,
                        )
                )
            }
        }
    }
}

@Composable
private fun RecordingActiveCard(
    onOpen: () -> Unit,
    onStop: () -> Unit,
) {
    K1Card(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            K1RecDot()
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "RECORDING",
                    style = K1Type.eyebrow.copy(
                        color = KlikAlert,
                        letterSpacing = 0.6.sp,
                        fontSize = 10.sp,
                    ),
                )
                Spacer(Modifier.height(2.dp))
                Text("Tap to open live view", style = K1Type.caption)
            }
            K1WaveformLive(Modifier.size(width = 48.dp, height = 28.dp))
            Spacer(Modifier.width(10.dp))
            // Stop pill
            Box(
                Modifier
                    .clip(K1R.chip)
                    .background(io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary)
                    .clickable(onClick = onStop)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Stop",
                    style = K1Type.meta.copy(
                        color = io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        fontSize = 11.sp,
                    ),
                )
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

@Composable
private fun DateChevron(pointRight: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(28.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(12.dp)) {
            val w = 1.4.dp.toPx()
            val tip = if (pointRight) 9.dp.toPx() else 3.dp.toPx()
            val tail = if (pointRight) 3.dp.toPx() else 9.dp.toPx()
            drawLine(
                color = io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary,
                strokeWidth = w,
                cap = StrokeCap.Round,
                start = Offset(tail, 2.dp.toPx()),
                end = Offset(tip, 6.dp.toPx()),
            )
            drawLine(
                color = io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary,
                strokeWidth = w,
                cap = StrokeCap.Round,
                start = Offset(tip, 6.dp.toPx()),
                end = Offset(tail, 10.dp.toPx()),
            )
        }
    }
}
