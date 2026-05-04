// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.domain.entity.Insights
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.MeetingSource
import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.model.archiveMeeting
import io.github.fletchmckee.liquid.samples.app.model.archiveTask
import io.github.fletchmckee.liquid.samples.app.model.archivedMeetingIdsState
import io.github.fletchmckee.liquid.samples.app.model.archivedTaskIdsState
import io.github.fletchmckee.liquid.samples.app.model.pinnedMeetingIdsState
import io.github.fletchmckee.liquid.samples.app.model.pinnedTaskIdsState
import io.github.fletchmckee.liquid.samples.app.model.toggleMeetingPin
import io.github.fletchmckee.liquid.samples.app.model.toggleTaskPin
import io.github.fletchmckee.liquid.samples.app.theme.KlikAlert
import io.github.fletchmckee.liquid.samples.app.theme.KlikCommitmentAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineMute
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikWarn
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData
import io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Klik One — Today.
 *
 * Drop-in replacement for the legacy `CalendarScreen`. Same parameter list so
 * MainApp.kt's routing `"today" -> CalendarScreen(...)` can switch to
 * `"today" -> TodayScreen(...)` with no call-site edits.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
  selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
  isCalendarExpanded: Boolean = false,
  isLoading: Boolean = false,
  isRefreshing: Boolean = false,
  isLlmDataLoading: Boolean = false,
  meetings: List<Meeting> = emptyList(),
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
  processingStartedAtMillis: Long? = null,
  processingStage: String? = null,
  processingStatus: String? = null,
  processingMessage: String? = null,
  processingProgressPct: Double? = null,
  processingError: String? = null,
  onDismissProcessing: () -> Unit = {},
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
  // For the month grid dots, always merge the `meetings` param with the global
  // meetingsState (populated by AppModule after API load). The two sources are
  // usually the same set, but reading the global state guarantees we see the
  // 300-ish meetings even when the per-screen flow hasn't re-emitted yet.
  val globalMeetings by io.github.fletchmckee.liquid.samples.app.model.meetingsState
  val mergedMeetings = remember(meetings, globalMeetings) {
    val byId = (meetings + globalMeetings).associateBy { it.id }
    byId.values.toList()
  }
  val meetingsCountByDay = remember(mergedMeetings, pickerMonth, pickerYear) {
    io.github.fletchmckee.liquid.samples.app.model.getMeetingsCountForMonth(
      mergedMeetings,
      pickerYear,
      pickerMonth,
    )
  }

  val ptrState = rememberPullToRefreshState()
  PullToRefreshBox(
    isRefreshing = isRefreshing,
    state = ptrState,
    onRefresh = onRefreshMeetings,
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
        .verticalScroll(scroll)
        .padding(top = 52.dp, bottom = 120.dp),
    ) {
      // Header with date navigation — prev / date-chip / next. Tap the chip to
      // expand an inline mini calendar; long-tap jumps to today.
      val prevDate = selectedDate.minus(DatePeriod(days = 1))
      val nextDate = selectedDate.plus(DatePeriod(days = 1))
      K1Header(
        title = "Today",
        onTitleClick = { if (selectedDate != todayDate) onDateChange(todayDate) },
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
        },
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

      // Post-session processing banner — reflects KK_orchestrator's REAL
      // pipeline state: stage (denoise → asr → diarization → speaker_names
      // → knowledge_graph → meeting_minutes → tasks → dropbox), progress %
      // and any terminal error. Falls back to orchestrator's `message`
      // when we don't know the stage yet, or to a short "uploading" copy
      // during the first couple seconds before the first poll.
      processingStartedAtMillis?.let { startedAt ->
        var nowMs by remember(startedAt) {
          mutableStateOf(kotlinx.datetime.Clock.System.now().toEpochMilliseconds())
        }
        androidx.compose.runtime.LaunchedEffect(startedAt) {
          while (true) {
            kotlinx.coroutines.delay(1_000)
            nowMs = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
          }
        }
        val elapsed = (nowMs - startedAt).coerceAtLeast(0L)
        val elapsedLabel = run {
          val secs = elapsed / 1_000
          if (secs < 60) "${secs}s" else "${secs / 60}m ${secs % 60}s"
        }
        val statusLc = processingStatus?.lowercase().orEmpty()
        val humanStage =
          io.github.fletchmckee.liquid.samples.app.humaniseOrchestratorStage(processingStage)
        val copy: String = when {
          statusLc == "failed" -> {
            processingError?.takeIf { it.isNotBlank() }
              ?.let { "Pipeline error — $it" }
              ?: processingMessage?.let { "Pipeline error — $it" }
              ?: "Pipeline ran into an error."
          }

          statusLc == "cancelled" -> "Cancelled."

          statusLc == "succeeded" || statusLc == "completed" ->
            "Done — your session is ready."

          humanStage != null -> humanStage

          !processingMessage.isNullOrBlank() -> processingMessage

          statusLc == "queued" -> "Uploading your session…"

          else -> "Connecting to Klik…"
        }
        // Only allow dismissing when the pipeline is in a terminal state —
        // otherwise a stray tap would hide the status the user wants to see.
        val statusIsTerminal = statusLc in setOf("succeeded", "completed", "failed", "cancelled")
        Column(Modifier.padding(horizontal = 20.dp)) {
          K1ProcessingBanner(
            stage = copy,
            elapsedLabel = elapsedLabel,
            progressFraction = processingProgressPct?.let { (it / 100.0).coerceIn(0.0, 1.0).toFloat() },
            onTap = if (statusIsTerminal) onDismissProcessing else null,
          )
        }
        Spacer(Modifier.height(K1Sp.lg))
      }

      val archivedMeetingIds by archivedMeetingIdsState
      val pinnedMeetingIds by pinnedMeetingIdsState
      val dayMeetings = run {
        val raw = mergedMeetings.filter {
          it.date == selectedDate &&
            !it.isArchived &&
            it.id !in archivedMeetingIds
        }
        // Dedup: same title+time = same real event. Keep KLIK (recorded) over APPLE_CALENDAR.
        val grouped = raw.groupBy { "${it.title.trim().lowercase()}|${it.time}" }
        grouped.values.map { group ->
          group.firstOrNull { it.source == MeetingSource.KLIK } ?: group.first()
        }.sortedByDescending { it.time }
      }
      val isTodaySelected = selectedDate == todayDate
      val isPastDaySelected = selectedDate < todayDate

      // Current time-of-day for splitting today's meetings into past vs upcoming.
      val nowTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time

      // ── RIGHT NOW ─────────────────────────────────────────────────────
      // Always visible on today — shows the recording affordance. If the
      // user is actively recording, the stop card; otherwise the quiet
      // record prompt so the tap target never moves around on the user.
      if (isTodaySelected) {
        Column(Modifier.padding(horizontal = 20.dp)) {
          K1SectionHeader(
            "Right now",
            dotColor = if (isRecording) KlikAlert else KlikLineMute,
          )
          Spacer(Modifier.height(K1Sp.s))
          if (isRecording) {
            RecordingActiveCard(
              onOpen = onOpenLiveRecording,
              onStop = onStopRecording,
            )
          } else {
            QuietCard(onStart = onStartRecording)
          }
        }
        Spacer(Modifier.height(K1Sp.xxl))
      }

      // ── INSIGHTS ──────────────────────────────────────────────────────
      // Real AI-generated insights from KK_tools. Surfaced near the top so
      // the day's read precedes the meeting list — entity mentions are
      // underlined and route to detail via onEntityClick.
      val hasInsights = !insights?.summary.isNullOrBlank()
      if (hasInsights || isLlmDataLoading || isLoading) {
        Column(Modifier.padding(horizontal = 20.dp)) {
          K1SectionHeader("Insights")
          Spacer(Modifier.height(K1Sp.s))
          if (hasInsights) {
            val ins = insights!!
            K1ExpandableCard(soft = true) { expanded ->
              io.github.fletchmckee.liquid.samples.app.ui.components.EntityHighlightedText(
                text = ins.summary,
                tasks = tasks,
                meetings = meetings,
                projects = projects,
                people = people,
                organizations = organizations,
                onEntityClick = onEntityClick,
                style = K1Type.bodySm,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
              )
            }
          } else {
            K1SkeletonCard(lines = 3)
          }
        }
        Spacer(Modifier.height(K1Sp.xxl))
      }

      // ── DAY MEETINGS ──────────────────────────────────────────────────
      // While the initial meetings list is in flight, show a couple of
      // breathing rows so the page reads as "loading" instead of "empty".
      if (isLoading && dayMeetings.isEmpty() && mergedMeetings.isEmpty()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
          K1SectionHeader(if (isTodaySelected) "Sessions" else "Scheduled")
          Spacer(Modifier.height(K1Sp.s))
          repeat(3) {
            K1SkeletonCard(lines = 2)
            Spacer(Modifier.height(6.dp))
          }
        }
        Spacer(Modifier.height(K1Sp.xxl))
      }
      // Today: split into "Processed" (already happened) and "Up next" (upcoming).
      // Past day: all under "Sessions". Future day: "Scheduled".
      when {
        isTodaySelected -> {
          // Pinned-first ordering keeps user-promoted sessions at the top of
          // their bucket without crossing the past/upcoming boundary.
          val processed = dayMeetings
            .filter { it.startTime <= nowTime }
            .sortedByDescending { pinnedMeetingIds[it.id] ?: 0L }
          val upcoming = dayMeetings
            .filter { it.startTime > nowTime }
            .sortedWith(
              compareByDescending<Meeting> { pinnedMeetingIds[it.id] ?: 0L }.thenBy { it.time },
            )

          if (processed.isNotEmpty()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
              K1SectionHeader("Processed", count = processed.size, dotColor = KlikCommitmentAccent)
              Spacer(Modifier.height(K1Sp.s))
              processed.forEach { m ->
                SwipeableMeetingRow(
                  m = m,
                  pinned = m.id in pinnedMeetingIds,
                  onClick = { onMeetingClick(m) },
                )
                Spacer(Modifier.height(6.dp))
              }
            }
            Spacer(Modifier.height(K1Sp.xxl))
          }

          if (upcoming.isNotEmpty()) {
            Column(Modifier.padding(horizontal = 20.dp)) {
              K1SectionHeader("Up next", count = upcoming.size)
              Spacer(Modifier.height(K1Sp.s))
              upcoming.forEach { m ->
                SwipeableMeetingRow(
                  m = m,
                  pinned = m.id in pinnedMeetingIds,
                  onClick = { onMeetingClick(m) },
                )
                Spacer(Modifier.height(6.dp))
              }
            }
            Spacer(Modifier.height(K1Sp.xxl))
          }

          // Right Now is rendered above unconditionally, so no
          // separate empty-state prompt here.
        }

        isPastDaySelected -> {
          if (dayMeetings.isNotEmpty()) {
            val ordered = dayMeetings.sortedByDescending { pinnedMeetingIds[it.id] ?: 0L }
            Column(Modifier.padding(horizontal = 20.dp)) {
              K1SectionHeader("Sessions", count = ordered.size, dotColor = KlikCommitmentAccent)
              Spacer(Modifier.height(K1Sp.s))
              ordered.forEach { m ->
                SwipeableMeetingRow(
                  m = m,
                  pinned = m.id in pinnedMeetingIds,
                  onClick = { onMeetingClick(m) },
                )
                Spacer(Modifier.height(6.dp))
              }
            }
            Spacer(Modifier.height(K1Sp.xxl))
          }
        }

        else -> {
          if (dayMeetings.isNotEmpty()) {
            val ordered = dayMeetings.sortedByDescending { pinnedMeetingIds[it.id] ?: 0L }
            Column(Modifier.padding(horizontal = 20.dp)) {
              K1SectionHeader("Scheduled", count = ordered.size)
              Spacer(Modifier.height(K1Sp.s))
              ordered.forEach { m ->
                SwipeableMeetingRow(
                  m = m,
                  pinned = m.id in pinnedMeetingIds,
                  onClick = { onMeetingClick(m) },
                )
                Spacer(Modifier.height(6.dp))
              }
            }
            Spacer(Modifier.height(K1Sp.xxl))
          }
        }
      }

      // ── MOVES FOR YOU ─────────────────────────────────────────────────
      val archivedTaskIds by archivedTaskIdsState
      val pinnedTaskIds by pinnedTaskIdsState
      val pending = tasks
        .filter { it.needsConfirmation && it.id !in archivedTaskIds }
        .sortedByDescending { pinnedTaskIds[it.id] ?: 0L }
        .take(3)
      if (pending.isNotEmpty()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
          K1SectionHeader("Moves for you", count = pending.size, dotColor = KlikWarn)
          Spacer(Modifier.height(K1Sp.s))
          pending.forEach { t ->
            K1SwipeRow(
              isPinned = t.id in pinnedTaskIds,
              onPin = { toggleTaskPin(t.id) },
              onArchive = { archiveTask(t.id, t) },
            ) {
              MoveMiniCard(t)
            }
            Spacer(Modifier.height(6.dp))
          }
        }
        Spacer(Modifier.height(K1Sp.xxl))
      }

      // Weekly/worklife insights are rendered on the Network screen, not Today.
      // Today stays focused on the daily brief + today's actions.
    }
  } // end PullToRefreshBox
}

@Composable
private fun LiveSessionCard(m: Meeting, onClick: () -> Unit = {}) {
  // Focal card per spec §2 / §7 — Today's primary session uses #F6F7F9
  // with radius 14 and padding 18 to separate it from the raised rows below.
  K1Card(focal = true, onClick = onClick) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      K1RecDot()
      Spacer(Modifier.width(8.dp))
      Column(Modifier.weight(1f)) {
        Text(m.title.ifBlank { "In conversation" }, style = K1Type.bodyMd)
        Spacer(Modifier.height(2.dp))
        Text(
          "${m.participants.size} speakers · ${m.time}",
          style = K1Type.meta,
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
      K1Waveform(
        heights = listOf(10f, 18f, 6f, 14f, 8f, 12f),
        barWidth = 3.dp,
        gap = 2.5.dp,
        color = KlikInkMuted,
      )
      Spacer(Modifier.width(14.dp))
      Column(Modifier.weight(1f)) {
        Text("Quiet.", style = K1Type.bodyMd)
        Spacer(Modifier.height(2.dp))
        Text("Tap to start listening.", style = K1Type.caption)
      }
      // Record dot glyph — tap-hint
      Box(
        Modifier
          .size(32.dp)
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
            ),
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
          .k1Clickable(onClick = onStop)
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
  val isCalendar = m.source == MeetingSource.APPLE_CALENDAR
  if (isCalendar) {
    // Calendar event — lightweight, no transcript/summary expected
    Row(
      Modifier
        .fillMaxWidth()
        .k1Clickable(onClick = onClick)
        .padding(vertical = 10.dp, horizontal = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Thin left accent line
      Box(
        Modifier
          .width(3.dp)
          .height(36.dp)
          .clip(K1R.pill)
          .background(
            if (m.sourceColor != null) {
              androidx.compose.ui.graphics.Color(m.sourceColor)
            } else {
              KlikInkMuted
            },
          ),
      )
      Spacer(Modifier.width(12.dp))
      Column(Modifier.weight(1f)) {
        Text(
          m.title.ifBlank { "Event" },
          style = K1Type.bodySm.copy(color = KlikInkPrimary),
        )
        Text(
          m.time,
          style = K1Type.meta.copy(color = KlikInkTertiary),
        )
      }
      if (m.participants.isNotEmpty()) {
        K1AvatarStack(
          initialsList = m.participants.take(3).map { p -> initialsOf(p.name) },
          size = 18.dp,
        )
      }
    }
    Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikLineHairline))
  } else {
    // Recorded Klik session — full card with summary
    K1Card(onClick = onClick) {
      Row(verticalAlignment = Alignment.Top) {
        Column(Modifier.width(72.dp)) {
          Text(m.time, style = K1Type.meta.copy(color = KlikInkSecondary, fontSize = 12.sp))
        }
        Column(Modifier.weight(1f)) {
          Text(m.title.ifBlank { "Untitled session" }, style = K1Type.bodyMd)
          if (m.summary.isNotBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(
              m.summary.take(80),
              style = K1Type.caption.copy(color = KlikInkTertiary),
            )
          }
        }
        if (m.participants.isNotEmpty()) {
          Spacer(Modifier.width(8.dp))
          K1AvatarStack(
            initialsList = m.participants.take(3).map { p -> initialsOf(p.name) },
            size = 20.dp,
          )
        }
      }
    }
  }
}

// SwipeableMeetingRow wires the global pin/archive helpers to a MeetingRow
// so the editorial swipe gestures are consistent across Today's three
// session buckets (processed, upcoming, sessions).
@Composable
private fun SwipeableMeetingRow(
  m: Meeting,
  pinned: Boolean,
  onClick: () -> Unit,
) {
  K1SwipeRow(
    isPinned = pinned,
    onPin = { toggleMeetingPin(m.id) },
    onArchive = { archiveMeeting(m.id, m) },
  ) {
    MeetingRow(m, onClick = onClick)
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

private fun initialsOf(name: String): String = name.trim().split(" ").filter { it.isNotEmpty() }
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
      .k1Clickable(onClick = onClick),
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
