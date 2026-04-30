// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
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
import io.github.fletchmckee.liquid.samples.app.theme.KlikAlert
import io.github.fletchmckee.liquid.samples.app.theme.KlikAvatarBg
import io.github.fletchmckee.liquid.samples.app.theme.KlikAvatarFg
import io.github.fletchmckee.liquid.samples.app.theme.KlikCommitmentAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikCommitmentSubtext
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperChip
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.theme.KlikWarn

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
  allMeetings: List<Meeting> = emptyList(),
  projects: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Project> = emptyList(),
  people: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Person> = emptyList(),
  organizations: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Organization> = emptyList(),
  onBack: () -> Unit,
  onShare: () -> Unit = {},
  onMore: () -> Unit = {},
  onEntityClick: (io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData) -> Unit = {},
) {
  var tab by remember { mutableStateOf(SessionTab.Summary) }

  // KK_exec todos carry the SESSION_… id, while the meetings table primary key is
  // a separate UUID. Match against either so a Meeting links to its own todos
  // regardless of which identifier the backend exposed first.
  val linked = tasks.filter { task ->
    val target = task.relatedMeetingId ?: task.kkExecSessionId
    target != null && (target == meeting.sessionId || target == meeting.id)
  }

  Column(
    Modifier
      .fillMaxSize()
      .background(KlikPaperApp)
      .k1SwipeBack(onBack)
      .verticalScroll(rememberScrollState()),
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
        Modifier.k1Clickable(onClick = onBack).padding(end = 8.dp),
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
          ),
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

    // Participants pill — collapsed shows count + avatar stack; tap to
    // expand into a wrap of clickable name chips that route to person_detail.
    if (meeting.participants.isNotEmpty()) {
      var participantsExpanded by remember { mutableStateOf(false) }
      Column(
        Modifier
          .padding(horizontal = 20.dp)
          .fillMaxWidth()
          .clip(K1R.soft)
          .background(KlikPaperSoft)
          .k1Clickable { participantsExpanded = !participantsExpanded }
          .padding(horizontal = 12.dp, vertical = 10.dp),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          K1AvatarStack(
            initialsList = meeting.participants.take(4).map { p -> initialsOf(p.name) },
            size = 24.dp,
          )
          Spacer(Modifier.width(10.dp))
          val countLabel = if (meeting.participants.size == 1) "1 person" else "${meeting.participants.size} people"
          Text(countLabel, style = K1Type.meta, modifier = Modifier.weight(1f))
          Text(
            if (participantsExpanded) "▴" else "▾",
            style = K1Type.metaSm.copy(color = KlikInkMuted, fontSize = 9.sp),
          )
        }
        if (participantsExpanded) {
          Spacer(Modifier.height(K1Sp.s))
          Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikLineHairline))
          Spacer(Modifier.height(K1Sp.s))
          FlowRowCompat(horizontalGap = 5.dp, verticalGap = 5.dp) {
            meeting.participants.forEach { p ->
              K1Chip(
                label = p.name,
                onClick = if (p.id.isNotBlank()) {
                  {
                    onEntityClick(
                      io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData(
                        io.github.fletchmckee.liquid.samples.app.ui.components.EntityType.PERSON,
                        p.id,
                      ),
                    )
                  }
                } else {
                  null
                },
                leading = {
                  val idx = p.name.hashCode().let { if (it < 0) -it else it }
                  Box(
                    Modifier.size(14.dp).clip(CircleShape)
                      .background(KlikAvatarBg[idx % KlikAvatarBg.size]),
                    contentAlignment = Alignment.Center,
                  ) {
                    Text(
                      initialsOf(p.name),
                      style = K1Type.metaSm.copy(
                        color = KlikAvatarFg[idx % KlikAvatarFg.size],
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Medium,
                      ),
                    )
                  }
                },
              )
            }
          }
        }
      }
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
        .background(KlikLineHairline),
    )

    Spacer(Modifier.height(K1Sp.lg))

    when (tab) {
      SessionTab.Summary -> SummaryPanel(
        m = meeting,
        tasks = tasks,
        allMeetings = allMeetings,
        projects = projects,
        people = people,
        organizations = organizations,
        onEntityClick = onEntityClick,
      )

      SessionTab.Todos -> TodosPanel(
        linked = linked,
        actionItems = meeting.actionItems,
        onEntityClick = onEntityClick,
      )

      SessionTab.Transcript -> TranscriptPanel(meeting)

      SessionTab.Highlights -> HighlightsPanel(meeting)
    }

    Spacer(Modifier.height(140.dp))
  }
}

@Composable
private fun TabItem(label: String, active: Boolean, badge: Int? = null, onClick: () -> Unit) {
  // IntrinsicSize.Max prevents the active underline's fillMaxWidth from expanding
  // beyond the label text width and breaking the sibling tabs in the Row.
  Column(
    Modifier
      .width(IntrinsicSize.Max)
      .k1Clickable(onClick = onClick)
      .padding(vertical = 8.dp),
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
private fun SummaryPanel(
  m: Meeting,
  tasks: List<TaskMetadata>,
  allMeetings: List<Meeting>,
  projects: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Project>,
  people: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Person>,
  organizations: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Organization>,
  onEntityClick: (io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData) -> Unit,
) {
  Column(Modifier.padding(horizontal = 20.dp)) {
    // ── In 3 lines ──────────────────────────────────────────────────────
    if (m.summary.isNotBlank()) {
      K1Eyebrow("In 3 lines")
      Spacer(Modifier.height(K1Sp.s))
      io.github.fletchmckee.liquid.samples.app.ui.components.EntityHighlightedText(
        text = m.summary,
        tasks = tasks,
        meetings = allMeetings,
        projects = projects,
        people = people,
        organizations = organizations,
        onEntityClick = onEntityClick,
        style = K1Type.bodySm,
      )
      Spacer(Modifier.height(K1Sp.xxl))
    }

    // ── Meeting minutes by category (Discussion, Decisions, etc.) ────────
    m.minutes.filter { it.items.isNotEmpty() }.forEach { minute ->
      K1SectionHeader(minute.category, count = minute.items.size)
      Spacer(Modifier.height(K1Sp.s))
      minute.items.take(8).forEach { item ->
        K1Card(soft = true) {
          io.github.fletchmckee.liquid.samples.app.ui.components.EntityHighlightedText(
            text = item,
            tasks = tasks,
            meetings = allMeetings,
            projects = projects,
            people = people,
            organizations = organizations,
            onEntityClick = onEntityClick,
            style = K1Type.caption,
          )
        }
        Spacer(Modifier.height(6.dp))
      }
      Spacer(Modifier.height(K1Sp.xxl))
    }

    // ── Action items / decisions (fallback if minutes not populated) ─────
    if (m.actionItems.isNotEmpty() && m.minutes.isEmpty()) {
      K1SectionHeader("Action items", count = m.actionItems.size)
      Spacer(Modifier.height(K1Sp.s))
      m.actionItems.take(8).forEach { todo ->
        K1Card(soft = true) {
          io.github.fletchmckee.liquid.samples.app.ui.components.EntityHighlightedText(
            text = todo.text,
            tasks = tasks,
            meetings = allMeetings,
            projects = projects,
            people = people,
            organizations = organizations,
            onEntityClick = onEntityClick,
            style = K1Type.caption,
          )
        }
        Spacer(Modifier.height(6.dp))
      }
      Spacer(Modifier.height(K1Sp.xxl))
    }

    // ── Mentioned participants ───────────────────────────────────────────
    if (m.participants.isNotEmpty()) {
      K1Eyebrow("Mentioned")
      Spacer(Modifier.height(K1Sp.s))
      FlowRowCompat(horizontalGap = 5.dp, verticalGap = 5.dp) {
        m.participants.take(6).forEach { p ->
          K1Chip(
            label = p.name,
            onClick = if (p.id.isNotBlank()) {
              { onEntityClick(io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData(io.github.fletchmckee.liquid.samples.app.ui.components.EntityType.PERSON, p.id)) }
            } else {
              null
            },
            leading = {
              val idx = p.name.hashCode().let { if (it < 0) -it else it }
              Box(
                Modifier.size(12.dp).clip(CircleShape)
                  .background(KlikAvatarBg[idx % KlikAvatarBg.size]),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  initialsOf(p.name),
                  style = K1Type.metaSm.copy(
                    color = KlikAvatarFg[idx % KlikAvatarFg.size],
                    fontSize = 6.sp,
                  ),
                )
              }
            },
          )
        }
      }
    }
  }
}

@Composable
private fun TodosPanel(
  linked: List<TaskMetadata>,
  actionItems: List<io.github.fletchmckee.liquid.samples.app.domain.entity.TodoItem> = emptyList(),
  onEntityClick: (io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData) -> Unit = {},
) {
  // Two sources of follow-ups:
  //   1. KK_exec todos linked to this meeting's session_id (`linked`).
  //   2. Action items extracted from the transcript (`actionItems`) — these
  //      always exist for meetings the AI summarised, even before any KK_exec
  //      todo has been generated, so a freshly-recorded session never reads
  //      as empty.
  // We render KK_exec todos first (clickable into task_detail) and any
  // remaining transcript-only items as plain rows below.
  val linkedTexts = linked.map { it.title.lowercase().trim() }.toSet()
  val transcriptOnly = actionItems.filter { it.text.lowercase().trim() !in linkedTexts }

  Column(Modifier.padding(horizontal = 20.dp)) {
    if (linked.isEmpty() && transcriptOnly.isEmpty()) {
      Text(
        "No follow-ups captured yet.",
        style = K1Type.caption.copy(color = KlikInkTertiary),
      )
      return@Column
    }

    if (linked.isNotEmpty()) {
      K1Eyebrow("From Klik")
      Spacer(Modifier.height(K1Sp.s))
      linked.forEach { t ->
        Row(
          Modifier.fillMaxWidth()
            .k1Clickable { onEntityClick(io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData(io.github.fletchmckee.liquid.samples.app.ui.components.EntityType.TASK, t.id)) }
            .padding(vertical = 12.dp),
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

    if (transcriptOnly.isNotEmpty()) {
      if (linked.isNotEmpty()) Spacer(Modifier.height(K1Sp.xl))
      K1Eyebrow("From transcript")
      Spacer(Modifier.height(K1Sp.s))
      transcriptOnly.forEach { item ->
        Row(
          Modifier.fillMaxWidth().padding(vertical = 12.dp),
          verticalAlignment = Alignment.Top,
        ) {
          Box(
            Modifier.size(14.dp).clip(CircleShape).background(KlikInkMuted),
          )
          Spacer(Modifier.width(10.dp))
          Text(item.text, style = K1Type.bodyMd, modifier = Modifier.weight(1f))
        }
        Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikPaperChip))
      }
    }
  }
}

@Composable
private fun TranscriptPanel(m: Meeting) {
  // Per session-detail spec: each transcript turn renders with a coloured
  // initials avatar + speaker name (medium weight) + optional timestamp +
  // body paragraph. We collapse consecutive same-speaker turns visually by
  // only repeating the avatar when the speaker changes — gives a chat feel.
  Column(Modifier.padding(horizontal = 20.dp)) {
    val rawLines = m.transcript?.lines()?.filter { it.isNotBlank() }.orEmpty()
    if (rawLines.isEmpty()) {
      Text(
        "Transcript not available yet.",
        style = K1Type.caption.copy(color = KlikInkTertiary),
      )
      return@Column
    }
    var lastSpeaker: String? = null
    rawLines.forEach { raw ->
      val parsed = parseTranscriptLine(raw)
      val showAvatar = parsed.speaker != lastSpeaker
      Row(
        Modifier.fillMaxWidth().padding(vertical = if (showAvatar) 8.dp else 2.dp),
      ) {
        // Avatar column — fixed 22dp + 9dp gap so body text aligns when we
        // suppress the avatar on continuation turns.
        Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
          if (showAvatar && parsed.speaker.isNotBlank()) {
            val idx = parsed.speaker.hashCode().let { if (it < 0) -it else it }
            Box(
              Modifier.size(22.dp).clip(CircleShape)
                .background(KlikAvatarBg[idx % KlikAvatarBg.size]),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                initialsOf(parsed.speaker),
                style = K1Type.metaSm.copy(
                  color = KlikAvatarFg[idx % KlikAvatarFg.size],
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Medium,
                ),
              )
            }
          }
        }
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
          if (showAvatar && parsed.speaker.isNotBlank()) {
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                parsed.speaker,
                style = K1Type.bodySm.copy(
                  color = KlikInkPrimary,
                  fontWeight = FontWeight.Medium,
                ),
              )
              if (!parsed.timestamp.isNullOrBlank()) {
                Spacer(Modifier.width(6.dp))
                Text(
                  parsed.timestamp,
                  style = K1Type.metaSm.copy(color = KlikInkTertiary),
                )
              }
            }
            Spacer(Modifier.height(2.dp))
          }
          Text(
            parsed.body,
            style = K1Type.bodySm.copy(
              color = KlikInkPrimary,
              lineHeight = 18.sp,
            ),
          )
        }
      }
      lastSpeaker = parsed.speaker
    }
  }
}

private data class TranscriptLine(val speaker: String, val timestamp: String?, val body: String)

/**
 * Parse a transcript line of the form "Speaker: body" or
 * "Speaker (HH:MM:SS): body". Falls back to a body-only line when no colon
 * separator is present, attributing it to the previous speaker.
 */
private fun parseTranscriptLine(raw: String): TranscriptLine {
  val colon = raw.indexOf(':')
  if (colon <= 0) return TranscriptLine(speaker = "", timestamp = null, body = raw.trim())
  val head = raw.substring(0, colon).trim()
  val body = raw.substring(colon + 1).trim()
  // Strip a trailing "(timestamp)" off the speaker token.
  val parenStart = head.indexOf('(')
  val parenEnd = head.indexOf(')')
  return if (parenStart > 0 && parenEnd > parenStart) {
    val speaker = head.substring(0, parenStart).trim()
    val ts = head.substring(parenStart + 1, parenEnd).trim()
    TranscriptLine(speaker = speaker, timestamp = ts.takeIf { it.isNotBlank() }, body = body)
  } else {
    TranscriptLine(speaker = head, timestamp = null, body = body)
  }
}

@Composable
private fun HighlightsPanel(m: Meeting) {
  // Show every flagged moment by default — no collapse. The session-detail
  // spec calls for a single scannable list, not a fold.
  Column(Modifier.padding(horizontal = 20.dp)) {
    Text(
      "${m.actionItems.size} moments Klik flagged.",
      style = K1Type.caption,
    )
    Spacer(Modifier.height(K1Sp.m))
    m.actionItems.forEach { a ->
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
private fun ShareIcon(onClick: () -> Unit) {
  Canvas(
    Modifier
      .size(16.dp)
      .k1Clickable(onClick = onClick),
  ) {
    val w = 1.2.dp.toPx()
    drawLine(
      color = KlikInkPrimary,
      strokeWidth = w,
      cap = StrokeCap.Round,
      start = Offset(8.dp.toPx(), 2.5.dp.toPx()),
      end = Offset(8.dp.toPx(), 10.5.dp.toPx()),
    )
    drawLine(
      color = KlikInkPrimary,
      strokeWidth = w,
      cap = StrokeCap.Round,
      start = Offset(8.dp.toPx(), 2.5.dp.toPx()),
      end = Offset(5.5.dp.toPx(), 5.dp.toPx()),
    )
    drawLine(
      color = KlikInkPrimary,
      strokeWidth = w,
      cap = StrokeCap.Round,
      start = Offset(8.dp.toPx(), 2.5.dp.toPx()),
      end = Offset(10.5.dp.toPx(), 5.dp.toPx()),
    )
    // frame
    drawLine(
      color = KlikInkPrimary,
      strokeWidth = w,
      cap = StrokeCap.Round,
      start = Offset(3.5.dp.toPx(), 10.dp.toPx()),
      end = Offset(3.5.dp.toPx(), 13.5.dp.toPx()),
    )
    drawLine(
      color = KlikInkPrimary,
      strokeWidth = w,
      cap = StrokeCap.Round,
      start = Offset(12.5.dp.toPx(), 10.dp.toPx()),
      end = Offset(12.5.dp.toPx(), 13.5.dp.toPx()),
    )
    drawLine(
      color = KlikInkPrimary,
      strokeWidth = w,
      cap = StrokeCap.Round,
      start = Offset(3.5.dp.toPx(), 13.5.dp.toPx()),
      end = Offset(12.5.dp.toPx(), 13.5.dp.toPx()),
    )
  }
}

@Composable
private fun MoreDotsIcon(onClick: () -> Unit) {
  Canvas(
    Modifier.size(16.dp).k1Clickable(onClick = onClick),
  ) {
    val r = 1.1.dp.toPx()
    val cx = size.width / 2f
    drawCircle(color = KlikInkPrimary, radius = r, center = Offset(cx, 3.5.dp.toPx()))
    drawCircle(color = KlikInkPrimary, radius = r, center = Offset(cx, 8.dp.toPx()))
    drawCircle(color = KlikInkPrimary, radius = r, center = Offset(cx, 12.5.dp.toPx()))
  }
}

private fun initialsOf(name: String): String = name.trim().split(" ").filter { it.isNotEmpty() }
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
