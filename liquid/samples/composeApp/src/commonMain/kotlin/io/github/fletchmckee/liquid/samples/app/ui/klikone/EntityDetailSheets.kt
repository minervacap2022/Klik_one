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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.theme.KlikCommitmentAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikDotOrg
import io.github.fletchmckee.liquid.samples.app.theme.KlikDotPerson
import io.github.fletchmckee.liquid.samples.app.theme.KlikDotProject
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperApp
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperChip
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperSoft
import io.github.fletchmckee.liquid.samples.app.theme.KlikRiskAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikRiskSubtext
import io.github.fletchmckee.liquid.samples.app.theme.KlikRunning
import io.github.fletchmckee.liquid.samples.app.theme.KlikWarn
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityType

// ─── Shared detail scaffold ───────────────────────────────────────────────

@Composable
private fun DetailScaffold(
  eyebrow: String,
  title: String,
  onBack: () -> Unit,
  onTitleLongPress: (() -> Unit)? = null,
  trailing: (@Composable () -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
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
      Box(
        Modifier.size(32.dp).k1Clickable(onClick = onBack),
        contentAlignment = Alignment.Center,
      ) { BackChevron() }
      Spacer(Modifier.weight(1f))
      if (trailing != null) trailing()
    }

    // Hero — long-press the title to rename the entity (matches old liquid app).
    Column(Modifier.padding(horizontal = 20.dp)) {
      K1Eyebrow(eyebrow)
      Spacer(Modifier.height(6.dp))
      val titleMod = if (onTitleLongPress != null) {
        Modifier.k1LongClickable(onLongClick = onTitleLongPress)
      } else {
        Modifier
      }
      Text(title, style = K1Type.h2, modifier = titleMod)
    }
    Spacer(Modifier.height(K1Sp.lg))

    content()

    Spacer(Modifier.height(120.dp))
  }
}

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

// ─── TASK DETAIL ─────────────────────────────────────────────────────────
@Composable
fun TaskDetailScreen(
  task: TaskMetadata,
  meetings: List<Meeting> = emptyList(),
  projects: List<Project> = emptyList(),
  people: List<Person> = emptyList(),
  organizations: List<Organization> = emptyList(),
  onBack: () -> Unit,
  onApprove: (() -> Unit)? = null,
  onReject: (() -> Unit)? = null,
  onRetry: (() -> Unit)? = null,
  onRejectWithReason: ((String) -> Unit)? = null,
  onRename: ((String) -> Unit)? = null,
  // Triple of (entityType, entityId, newName). Used when the user
  // long-presses a Related row to correct the linked entity's display name.
  onRenameRelatedEntity: ((EntityType, String, String) -> Unit)? = null,
  // (sessionId, anchorText). Used when the user taps Source Session — the
  // host should open SessionDetail's Transcript tab and scroll to the line
  // matching anchorText. Falls back to a plain MEETING entity-click when null.
  onJumpToTranscript: ((String, String) -> Unit)? = null,
  onEntityClick: (EntityNavigationData) -> Unit = {},
) {
  var showRejectReasonDialog by remember { mutableStateOf(false) }
  var rejectReason by remember { mutableStateOf("") }
  var showRename by remember { mutableStateOf(false) }
  // Active rename target: (entityType, entityId, currentName). Null when no
  // rename dialog is open.
  var relatedRenameTarget by remember { mutableStateOf<Triple<EntityType, String, String>?>(null) }
  DetailScaffold(
    eyebrow = "Move",
    title = task.title,
    onBack = onBack,
    onTitleLongPress = if (onRename != null) ({ showRename = true }) else null,
  ) {
    Column(Modifier.padding(horizontal = 20.dp)) {
      // Subtitle / context
      if (task.subtitle.isNotBlank()) {
        Text(task.subtitle, style = K1Type.bodySm.copy(color = KlikInkSecondary))
        Spacer(Modifier.height(K1Sp.m))
      }

      // Status + due chip row
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        val statusLabel = when {
          task.kkExecStatus?.uppercase() in setOf("COMPLETED", "APPROVED") -> "Done"
          task.kkExecStatus?.uppercase() in setOf("RUNNING", "IN_PROGRESS") -> "Running"
          task.kkExecStatus?.uppercase() == "PENDING" -> "Pending"
          task.kkExecStatus?.uppercase() == "FAILED" -> "Failed"
          task.needsConfirmation -> "Needs attention"
          else -> task.status.name.lowercase().replaceFirstChar { it.uppercase() }
        }
        K1Chip(label = statusLabel, selected = true)
        if (task.dueInfo.isNotBlank()) K1Chip(label = task.dueInfo)
        if (task.priority.isNotBlank() && task.priority != "Normal") K1Chip(label = task.priority)
      }

      // Description / full body
      if (!task.description.isNullOrBlank()) {
        Spacer(Modifier.height(K1Sp.xl))
        K1Eyebrow("Description")
        Spacer(Modifier.height(K1Sp.s))
        K1Card(soft = true) {
          Text(task.description!!, style = K1Type.bodySm)
        }
      }

      // Integrations this task uses
      if (task.toolCategoriesNeeded.isNotEmpty()) {
        Spacer(Modifier.height(K1Sp.xl))
        K1Eyebrow("Integrations")
        Spacer(Modifier.height(K1Sp.s))
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          task.toolCategoriesNeeded.take(6).forEach { cat ->
            val (iconUrl, label) = integrationInfo(cat)
            IntegrationChip(iconUrl = iconUrl, label = label)
          }
        }
      }

      // Execution steps
      if (task.executionSteps.isNotEmpty()) {
        Spacer(Modifier.height(K1Sp.xl))
        K1SectionHeader("Execution", count = task.executionSteps.size, dotColor = KlikRunning)
        Spacer(Modifier.height(K1Sp.s))
        task.executionSteps.forEach { step ->
          Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.Top,
          ) {
            val dotColor = when {
              !step.success -> KlikRiskAccent
              else -> KlikCommitmentAccent
            }
            Box(Modifier.size(6.dp).clip(CircleShape).background(dotColor))
            Spacer(Modifier.width(K1Sp.m))
            Column(Modifier.weight(1f)) {
              Text(
                "Step ${step.stepNumber} · ${step.toolName}",
                style = K1Type.bodySm,
              )
              val rawDetail = step.errorMessage ?: step.output
              val detail = rawDetail?.let { normalizeMarkdown(it) }
              if (!detail.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Markdown(
                  content = detail,
                  colors = markdownColor(
                    text = if (step.success) KlikInkTertiary else KlikRiskSubtext,
                    codeBackground = KlikPaperChip,
                  ),
                  typography = markdownTypography(
                    paragraph = K1Type.metaSm,
                    code = K1Type.metaSm,
                    list = K1Type.metaSm,
                  ),
                )
              }
              step.durationMs?.let {
                Spacer(Modifier.height(2.dp))
                Text("$it ms", style = K1Type.metaSm)
              }
            }
          }
        }
      }

      // Task result / outcome
      if (!task.taskResult.isNullOrBlank()) {
        Spacer(Modifier.height(K1Sp.xl))
        K1Eyebrow("Result")
        Spacer(Modifier.height(K1Sp.s))
        K1Card(soft = true) {
          Markdown(
            content = normalizeMarkdown(task.taskResult!!),
            colors = markdownColor(text = KlikInkPrimary, codeBackground = KlikPaperChip),
            typography = markdownTypography(paragraph = K1Type.bodySm, list = K1Type.bodySm),
          )
        }
      }

      // Related entities
      val hasRelated = task.relatedProject.isNotBlank() ||
        task.relatedProjects.isNotEmpty() ||
        task.relatedPeople.isNotEmpty() ||
        task.relatedOrganizations.isNotEmpty()
      if (hasRelated) {
        Spacer(Modifier.height(K1Sp.xl))
        K1Eyebrow("Related")
        Spacer(Modifier.height(K1Sp.s))
        // Resolve a related-entity reference to a real entity. The backend
        // emits names ("蒙哥"), canonical names, voiceprint placeholders
        // ("VP_D5780BD8AD1A"), and aliases interchangeably — so we match in
        // that order: id, canonicalName, name, aliases, case-insensitive.
        // Returns Triple(matchedId, displayLabel, onClick) or null when the
        // reference can't be resolved against loaded data.
        fun resolveProject(ref: String): Triple<String, String, () -> Unit>? {
          val match = projects.firstOrNull {
            it.id == ref ||
              it.canonicalName.equals(ref, true) ||
              it.name.equals(ref, true) ||
              it.aliases.any { a -> a.equals(ref, true) }
          } ?: return null
          val label = match.canonicalName.ifBlank { match.name }
          return Triple(match.id, label) {
            onEntityClick(EntityNavigationData(EntityType.PROJECT, match.id))
          }
        }
        fun resolvePerson(ref: String): Triple<String, String, () -> Unit>? {
          val match = people.firstOrNull {
            it.id == ref ||
              it.canonicalName.equals(ref, true) ||
              it.name.equals(ref, true) ||
              it.aliases.any { a -> a.equals(ref, true) }
          } ?: return null
          val label = match.canonicalName.ifBlank { match.name }
          return Triple(match.id, label) {
            onEntityClick(EntityNavigationData(EntityType.PERSON, match.id))
          }
        }
        fun resolveOrg(ref: String): Triple<String, String, () -> Unit>? {
          val match = organizations.firstOrNull {
            it.id == ref ||
              it.canonicalName.equals(ref, true) ||
              it.name.equals(ref, true) ||
              it.aliases.any { a -> a.equals(ref, true) }
          } ?: return null
          val label = match.canonicalName.ifBlank { match.name }
          return Triple(match.id, label) {
            onEntityClick(EntityNavigationData(EntityType.ORGANIZATION, match.id))
          }
        }
        // Helper: produce the long-press handler that opens the rename dialog
        // for a resolved entity. Returns null when caller didn't supply an
        // onRenameRelatedEntity sink, so RelatedLine falls back to tap-only.
        fun renameLongPress(type: EntityType, id: String, label: String): (() -> Unit)? =
          if (onRenameRelatedEntity != null) {
            { relatedRenameTarget = Triple(type, id, label) }
          } else {
            null
          }
        K1Card(soft = true) {
          // Local resolution can miss when the loaded entity list is a subset
          // of what the backend references — pass the raw ref to navigation
          // so the destination screen can re-resolve against its own state.
          fun rawRefClick(type: EntityType, ref: String): () -> Unit = {
            onEntityClick(EntityNavigationData(type, ref))
          }
          task.relatedProject.takeIf { it.isNotBlank() }?.let { ref ->
            val r = resolveProject(ref)
            RelatedLine(
              "Project", r?.second ?: ref, KlikDotProject,
              onClick = r?.third ?: rawRefClick(EntityType.PROJECT, ref),
              onLongClick = r?.let { renameLongPress(EntityType.PROJECT, it.first, it.second) },
            )
          }
          task.relatedProjects.filter { it != task.relatedProject }.forEach { ref ->
            val r = resolveProject(ref)
            RelatedLine(
              "Project", r?.second ?: ref, KlikDotProject,
              onClick = r?.third ?: rawRefClick(EntityType.PROJECT, ref),
              onLongClick = r?.let { renameLongPress(EntityType.PROJECT, it.first, it.second) },
            )
          }
          task.relatedPeople.forEach { ref ->
            val r = resolvePerson(ref)
            RelatedLine(
              "Person", r?.second ?: ref, KlikDotPerson,
              onClick = r?.third ?: rawRefClick(EntityType.PERSON, ref),
              onLongClick = r?.let { renameLongPress(EntityType.PERSON, it.first, it.second) },
            )
          }
          task.relatedOrganizations.forEach { ref ->
            val r = resolveOrg(ref)
            RelatedLine(
              "Org", r?.second ?: ref, KlikDotOrg,
              onClick = r?.third ?: rawRefClick(EntityType.ORGANIZATION, ref),
              onLongClick = r?.let { renameLongPress(EntityType.ORGANIZATION, it.first, it.second) },
            )
          }
        }
      }

      // Source session — tap opens the originating meeting and (when the
      // task carries a transcript snippet via [task.subtitle] / description)
      // jumps the Transcript tab to that line.
      val sessionId = task.relatedMeetingId
      if (!sessionId.isNullOrBlank()) {
        val meetingMatch = meetings.find { it.id == sessionId }
        Spacer(Modifier.height(K1Sp.xl))
        K1Eyebrow("Source session")
        Spacer(Modifier.height(K1Sp.s))
        K1Card(
          soft = true,
          onClick = {
            // Pass the subtitle as the jump-anchor text — TranscriptPanel
            // does a substring match against transcript bodies. Fall back to
            // a plain meeting nav when there's no anchor text.
            val anchor = task.subtitle.ifBlank { task.description.orEmpty() }
            if (anchor.isNotBlank() && onJumpToTranscript != null) {
              onJumpToTranscript(sessionId, anchor)
            } else {
              onEntityClick(EntityNavigationData(EntityType.MEETING, sessionId))
            }
          },
        ) {
          Text(
            meetingMatch?.title?.ifBlank { null } ?: sessionId.take(24),
            style = K1Type.bodyMd,
          )
          if (meetingMatch != null) {
            Spacer(Modifier.height(2.dp))
            Text(
              "${meetingMatch.time} · ${meetingMatch.participants.size} people",
              style = K1Type.meta,
            )
          }
        }
      }

      // Failed tasks get a Retry primary action.
      val isFailed = task.kkExecStatus?.uppercase() == "FAILED" ||
        task.kkExecStatus?.uppercase() == "ERROR"
      if (isFailed && onRetry != null) {
        Spacer(Modifier.height(K1Sp.xl))
        K1ButtonPrimary(
          label = "Retry",
          onClick = onRetry,
          pill = true,
          modifier = Modifier.fillMaxWidth(),
        )
      }

      // Needs-confirmation tasks get Approve + Reject-with-reason.
      if (task.needsConfirmation && (onApprove != null || onReject != null || onRejectWithReason != null)) {
        Spacer(Modifier.height(K1Sp.xl))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          if (onApprove != null) {
            K1ButtonPrimary(
              label = "Approve & send",
              onClick = onApprove,
              pill = true,
              modifier = Modifier.weight(1f),
            )
          }
          val rejectHandler: () -> Unit = {
            if (onRejectWithReason != null) {
              showRejectReasonDialog = true
            } else {
              onReject?.invoke()
            }
          }
          Box(
            Modifier
              .weight(1f)
              .clip(K1R.pill)
              .border(0.5.dp, KlikInkMuted, K1R.pill)
              .k1Clickable(onClick = rejectHandler)
              .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              if (onRejectWithReason != null) "Reject…" else "Skip",
              style = K1Type.bodyMd.copy(color = KlikInkPrimary),
            )
          }
        }
      }
    }
  }

  if (showRejectReasonDialog && onRejectWithReason != null) {
    RejectReasonDialog(
      reason = rejectReason,
      onReasonChange = { rejectReason = it },
      onCancel = {
        showRejectReasonDialog = false
        rejectReason = ""
      },
      onSubmit = {
        onRejectWithReason(rejectReason.trim())
        showRejectReasonDialog = false
        rejectReason = ""
      },
    )
  }

  if (showRename && onRename != null) {
    RenameEntityDialog(
      kind = "move",
      currentName = task.title,
      onCancel = { showRename = false },
      onSave = { newName ->
        onRename(newName)
        showRename = false
      },
    )
  }

  relatedRenameTarget?.let { target ->
    val (type, id, current) = target
    RenameEntityDialog(
      kind = when (type) {
        EntityType.PERSON -> "person"
        EntityType.PROJECT -> "project"
        EntityType.ORGANIZATION -> "organization"
        else -> "entity"
      },
      currentName = current,
      onCancel = { relatedRenameTarget = null },
      onSave = { newName ->
        onRenameRelatedEntity?.invoke(type, id, newName)
        relatedRenameTarget = null
      },
    )
  }
}

@Composable
private fun RejectReasonDialog(
  reason: String,
  onReasonChange: (String) -> Unit,
  onCancel: () -> Unit,
  onSubmit: () -> Unit,
) {
  androidx.compose.foundation.layout.Box(
    androidx.compose.ui.Modifier
      .fillMaxSize()
      .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
      .k1Clickable(onClick = onCancel),
    contentAlignment = androidx.compose.ui.Alignment.Center,
  ) {
    androidx.compose.foundation.layout.Column(
      androidx.compose.ui.Modifier
        .padding(horizontal = 24.dp)
        .fillMaxWidth()
        .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
        .background(KlikPaperCard)
        .k1Clickable(enabled = false) {}
        .padding(24.dp),
    ) {
      Text("Why skip this move?", style = K1Type.h3)
      Spacer(androidx.compose.ui.Modifier.height(K1Sp.s))
      Text(
        "Your reason helps Klik refine future suggestions.",
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )
      Spacer(androidx.compose.ui.Modifier.height(K1Sp.m))
      androidx.compose.foundation.layout.Box(
        androidx.compose.ui.Modifier
          .fillMaxWidth()
          .clip(K1R.card)
          .background(KlikPaperChip)
          .padding(horizontal = 12.dp, vertical = 12.dp),
      ) {
        if (reason.isBlank()) {
          Text(
            "Optional — e.g. wrong person, already handled, not relevant",
            style = K1Type.bodySm.copy(color = KlikInkMuted),
          )
        }
        androidx.compose.foundation.text.BasicTextField(
          value = reason,
          onValueChange = onReasonChange,
          textStyle = K1Type.bodySm,
          maxLines = 4,
          modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        )
      }
      Spacer(androidx.compose.ui.Modifier.height(K1Sp.xl))
      androidx.compose.foundation.layout.Row(
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
      ) {
        androidx.compose.foundation.layout.Box(
          androidx.compose.ui.Modifier.weight(1f).clip(K1R.pill).background(KlikPaperChip)
            .k1Clickable(onClick = onCancel).padding(vertical = 14.dp),
          contentAlignment = androidx.compose.ui.Alignment.Center,
        ) { Text("Cancel", style = K1Type.bodyMd) }
        androidx.compose.foundation.layout.Box(
          androidx.compose.ui.Modifier.weight(1f).clip(K1R.pill).background(KlikInkPrimary)
            .k1Clickable(onClick = onSubmit).padding(vertical = 14.dp),
          contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
          Text(
            "Send",
            style = K1Type.bodyMd.copy(
              color = KlikPaperCard,
              fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            ),
          )
        }
      }
    }
  }
}

@Composable
private fun RelatedLine(
  kind: String,
  label: String,
  dot: androidx.compose.ui.graphics.Color,
  onClick: (() -> Unit)? = null,
  onLongClick: (() -> Unit)? = null,
) {
  // Tap → navigate; long-press → rename the linked entity (matches the
  // long-press-on-title pattern on Person/Project/Org detail screens).
  val tapMod = when {
    onLongClick != null -> Modifier.k1LongClickable(
      onClick = onClick ?: {},
      onLongClick = onLongClick,
    )
    onClick != null -> Modifier.k1Clickable(onClick = onClick)
    else -> Modifier
  }
  Row(
    Modifier
      .fillMaxWidth()
      .then(tapMod)
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(Modifier.size(5.dp).clip(CircleShape).background(dot))
    Spacer(Modifier.width(K1Sp.s))
    Text(kind, style = K1Type.metaSm)
    Spacer(Modifier.width(K1Sp.s))
    Text(
      label,
      style = K1Type.bodySm.copy(
        color = KlikInkPrimary,
        textDecoration = if (onClick != null) {
          androidx.compose.ui.text.style.TextDecoration.Underline
        } else {
          androidx.compose.ui.text.style.TextDecoration.None
        },
      ),
      modifier = Modifier.weight(1f),
    )
  }
}

// ─── PERSON DETAIL ───────────────────────────────────────────────────────
@Composable
fun PersonDetailScreen(
  person: Person,
  meetings: List<Meeting> = emptyList(),
  tasks: List<TaskMetadata> = emptyList(),
  projects: List<Project> = emptyList(),
  organizations: List<Organization> = emptyList(),
  allPeople: List<Person> = emptyList(),
  onBack: () -> Unit,
  onEntityClick: (EntityNavigationData) -> Unit = {},
  onRename: ((String) -> Unit)? = null,
) {
  val personMeetings = meetings.filter { m -> m.participants.any { it.id == person.id } }
    .sortedByDescending { it.date }
    .take(5)
  val personTasks = tasks.filter { t -> t.relatedPeople.any { it.equals(person.name, true) } }.take(5)
  val personProjects = projects.filter { p -> p.id in person.relatedProjects || p.relatedPeople.contains(person.name) }
  val personOrgs = organizations.filter { o -> o.id in person.relatedOrganizations || o.employees.contains(person.name) }

  var showRename by remember { mutableStateOf(false) }

  DetailScaffold(
    eyebrow = "Person",
    title = person.name,
    onBack = onBack,
    onTitleLongPress = if (onRename != null) ({ showRename = true }) else null,
  ) {
    // Hero row: big avatar + role
    Row(
      Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      K1Avatar(initialsOf(person.name), size = 72.dp)
      Spacer(Modifier.width(K1Sp.lg))
      Column(Modifier.weight(1f)) {
        val roleParts = listOfNotNull(
          person.role.ifBlank { null },
          person.title,
          person.department,
        )
        Text(
          roleParts.joinToString(" · ").ifBlank { "—" },
          style = K1Type.bodyMd,
        )
        if (person.email.isNotBlank()) {
          Spacer(Modifier.height(2.dp))
          Text(person.email, style = K1Type.caption)
        }
        Spacer(Modifier.height(K1Sp.s))
        Text(person.lastInteraction, style = K1Type.meta)
      }
    }

    // Signals — voice / connection / reliability (3-up score row)
    Spacer(Modifier.height(K1Sp.xl))
    Column(Modifier.padding(horizontal = 20.dp)) {
      K1Eyebrow("Signals")
      Spacer(Modifier.height(K1Sp.s))
      K1DimensionRow(
        keys = listOf("voice", "connection", "reliability"),
        dimensions = person.dimensions,
      )
    }

    // Skills / characteristics
    if (person.skills.isNotEmpty() || person.characteristics.isNotEmpty()) {
      Spacer(Modifier.height(K1Sp.xl))
      Column(Modifier.padding(horizontal = 20.dp)) {
        K1Eyebrow("Skills")
        Spacer(Modifier.height(K1Sp.s))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          (person.skills + person.characteristics).distinct().take(6).forEach {
            K1Chip(label = it)
          }
        }
      }
    }

    // Related sessions — meeting cards with avatar stack
    K1RelatedSessionsCards(
      title = "Related sessions",
      sessions = personMeetings,
      onClick = { m -> onEntityClick(EntityNavigationData(EntityType.MEETING, m.id)) },
    )

    // Open commitments / related tasks
    if (personTasks.isNotEmpty()) {
      Spacer(Modifier.height(K1Sp.xl))
      Column(Modifier.padding(horizontal = 20.dp)) {
        K1SectionHeader("Open commitments", count = personTasks.size, dotColor = KlikWarn)
        Spacer(Modifier.height(K1Sp.s))
        personTasks.forEach { t ->
          K1Card(
            soft = true,
            onClick = { onEntityClick(EntityNavigationData(EntityType.TASK, t.id)) },
          ) {
            Text(t.title, style = K1Type.bodyMd)
            if (t.subtitle.isNotBlank()) {
              Spacer(Modifier.height(2.dp))
              Text(t.subtitle, style = K1Type.meta)
            }
          }
          Spacer(Modifier.height(6.dp))
        }
      }
    }

    // Related projects — chips, click navigates into project_detail
    K1RelatedChipsSection(
      title = "Related projects",
      count = personProjects.size,
      dotColor = KlikDotProject,
      names = personProjects.map { it.name },
      onChipClick = { name ->
        val p = personProjects.find { it.name == name }
        if (p != null) onEntityClick(EntityNavigationData(EntityType.PROJECT, p.id))
      },
    )

    // Related organizations — chips, click navigates into org_detail
    K1RelatedChipsSection(
      title = "Related organizations",
      count = personOrgs.size,
      dotColor = KlikDotOrg,
      names = personOrgs.map { it.name },
      onChipClick = { name ->
        val o = personOrgs.find { it.name == name }
        if (o != null) onEntityClick(EntityNavigationData(EntityType.ORGANIZATION, o.id))
      },
    )

    // Related people — co-attendees from this person's meetings
    val coAttendees: List<String> = run {
      val seen = LinkedHashSet<String>()
      for (m in personMeetings) {
        for (p in m.participants) {
          if (!p.name.equals(person.name, ignoreCase = true) && p.name.isNotBlank()) {
            seen.add(p.name)
          }
        }
      }
      seen.toList()
    }
    K1RelatedChipsSection(
      title = "Related people",
      count = coAttendees.size,
      dotColor = KlikDotPerson,
      names = coAttendees,
      onChipClick = { name ->
        val match = allPeople.firstOrNull { it.name.equals(name, true) || it.canonicalName.equals(name, true) }
          ?: meetings.flatMap { it.participants }.firstOrNull { it.name.equals(name, true) }
        if (match != null) onEntityClick(EntityNavigationData(EntityType.PERSON, match.id))
      },
    )
  }

  if (showRename && onRename != null) {
    RenameEntityDialog(
      kind = "person",
      currentName = person.name,
      onCancel = { showRename = false },
      onSave = { newName ->
        onRename(newName)
        showRename = false
      },
    )
  }
}

// ─── PROJECT DETAIL ──────────────────────────────────────────────────────
@Composable
fun ProjectDetailScreen(
  project: Project,
  meetings: List<Meeting> = emptyList(),
  tasks: List<TaskMetadata> = emptyList(),
  people: List<Person> = emptyList(),
  allProjects: List<Project> = emptyList(),
  organizations: List<Organization> = emptyList(),
  onBack: () -> Unit,
  onEntityClick: (EntityNavigationData) -> Unit = {},
  onRename: ((String) -> Unit)? = null,
) {
  val projectTasks = tasks.filter { t ->
    t.relatedProject.equals(project.name, true) || t.relatedProjects.contains(project.name)
  }.take(8)
  val projectMeetings = meetings.filter { m -> m.id in project.relatedMeetings }
    .sortedByDescending { it.date }
    .take(5)

  var showRename by remember { mutableStateOf(false) }

  DetailScaffold(
    eyebrow = "Project",
    title = project.name,
    onBack = onBack,
    onTitleLongPress = if (onRename != null) ({ showRename = true }) else null,
  ) {
    Column(Modifier.padding(horizontal = 20.dp)) {
      // Status row + stage
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        K1Chip(
          label = project.status.name.lowercase().replaceFirstChar { it.uppercase() },
          selected = true,
        )
        if (project.stage.isNotBlank()) K1Chip(label = project.stage)
        project.type?.takeIf { it.isNotBlank() }?.let { K1Chip(label = it) }
      }

      Spacer(Modifier.height(K1Sp.lg))

      // Signals — clarity / weather / health (3-up score row)
      K1Eyebrow("Signals")
      Spacer(Modifier.height(K1Sp.s))
      K1DimensionRow(
        keys = listOf("clarity", "weather", "health"),
        dimensions = project.dimensions,
      )

      Spacer(Modifier.height(K1Sp.xl))

      // Progress
      K1Eyebrow("Progress")
      Spacer(Modifier.height(K1Sp.s))
      K1Card(soft = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            Modifier
              .weight(1f)
              .height(4.dp)
              .clip(K1R.pill)
              .background(KlikLineHairline),
          ) {
            Box(
              Modifier
                .fillMaxHeight()
                .fillMaxWidth(project.progress.coerceIn(0f, 1f))
                .background(KlikInkPrimary),
            )
          }
          Spacer(Modifier.width(K1Sp.m))
          Text("${(project.progress * 100).toInt()}%", style = K1Type.bodyMd)
        }
        if (project.startDate != null || project.endDate != null) {
          Spacer(Modifier.height(6.dp))
          Text(
            "${project.startDate ?: "—"} → ${project.endDate ?: "—"}",
            style = K1Type.metaSm,
          )
        }
      }

      // Lead + team
      if (project.lead.isNotBlank() || project.teamMembers.isNotEmpty()) {
        Spacer(Modifier.height(K1Sp.xl))
        K1Eyebrow("Team")
        Spacer(Modifier.height(K1Sp.s))
        K1Card(soft = true) {
          if (project.lead.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              K1Avatar(initialsOf(project.lead), size = 32.dp)
              Spacer(Modifier.width(K1Sp.m))
              Column {
                Text(project.lead, style = K1Type.bodyMd)
                Text("Lead", style = K1Type.metaSm)
              }
            }
            Spacer(Modifier.height(K1Sp.s))
          }
          if (project.teamMembers.isNotEmpty()) {
            K1AvatarStack(
              initialsList = project.teamMembers.take(6).map { initialsOf(it) },
              size = 24.dp,
            )
            Spacer(Modifier.height(4.dp))
            Text("${project.teamMembers.size} members", style = K1Type.metaSm)
          }
        }
      }

      // Goals
      if (project.goals.isNotEmpty()) {
        Spacer(Modifier.height(K1Sp.xl))
        K1SectionHeader("Goals", count = project.goals.size)
        Spacer(Modifier.height(K1Sp.s))
        K1Card(soft = true) {
          project.goals.forEach { g ->
            BulletLine(g)
          }
        }
      }

      // KPIs
      if (project.kpis.isNotEmpty()) {
        Spacer(Modifier.height(K1Sp.xl))
        K1SectionHeader("KPIs", count = project.kpis.size)
        Spacer(Modifier.height(K1Sp.s))
        K1Card(soft = true) {
          project.kpis.forEach { k -> BulletLine(k) }
        }
      }

      // Risks
      if (project.risks.isNotEmpty()) {
        Spacer(Modifier.height(K1Sp.xl))
        K1SectionHeader("Risks", count = project.risks.size, dotColor = KlikRiskAccent)
        Spacer(Modifier.height(K1Sp.s))
        project.risks.forEach { r ->
          K1SignalCard(signal = K1Signal.Risk, eyebrow = "Risk", body = r)
          Spacer(Modifier.height(6.dp))
        }
      }

      // Open tasks
      if (projectTasks.isNotEmpty()) {
        Spacer(Modifier.height(K1Sp.xl))
        K1SectionHeader("Moves", count = projectTasks.size, dotColor = KlikRunning)
        Spacer(Modifier.height(K1Sp.s))
        projectTasks.forEach { t ->
          K1Card(
            soft = true,
            onClick = { onEntityClick(EntityNavigationData(EntityType.TASK, t.id)) },
          ) {
            Text(t.title, style = K1Type.bodyMd)
            if (t.subtitle.isNotBlank()) {
              Spacer(Modifier.height(2.dp))
              Text(t.subtitle, style = K1Type.meta)
            }
          }
          Spacer(Modifier.height(6.dp))
        }
      }

    }

    // Related sessions — always shown (with placeholder when empty)
    K1RelatedSessionsCards(
      title = "Related sessions",
      sessions = projectMeetings,
      onClick = { m -> onEntityClick(EntityNavigationData(EntityType.MEETING, m.id)) },
    )

    // Related people — chips look up Person by name; rows without a match
    // remain non-clickable (no flicker, just a static chip).
    K1RelatedChipsSection(
      title = "Related people",
      count = project.relatedPeople.size,
      dotColor = KlikDotPerson,
      names = project.relatedPeople,
      onChipClick = { name ->
        val match = people.firstOrNull { it.name.equals(name, true) || it.canonicalName.equals(name, true) }
        if (match != null) onEntityClick(EntityNavigationData(EntityType.PERSON, match.id))
      },
    )

    // Related projects
    K1RelatedChipsSection(
      title = "Related projects",
      count = project.relatedProjects.size,
      dotColor = KlikDotProject,
      names = project.relatedProjects,
      onChipClick = { name ->
        val match = allProjects.firstOrNull {
          it.id != project.id && (it.name.equals(name, true) || it.canonicalName.equals(name, true))
        }
        if (match != null) onEntityClick(EntityNavigationData(EntityType.PROJECT, match.id))
      },
    )

    // Related organizations
    K1RelatedChipsSection(
      title = "Related organizations",
      count = project.relatedOrganizations.size,
      dotColor = KlikDotOrg,
      names = project.relatedOrganizations,
      onChipClick = { name ->
        val match = organizations.firstOrNull { it.name.equals(name, true) || it.canonicalName.equals(name, true) }
        if (match != null) onEntityClick(EntityNavigationData(EntityType.ORGANIZATION, match.id))
      },
    )
  }

  if (showRename && onRename != null) {
    RenameEntityDialog(
      kind = "project",
      currentName = project.name,
      onCancel = { showRename = false },
      onSave = { newName ->
        onRename(newName)
        showRename = false
      },
    )
  }
}

@Composable
private fun BulletLine(text: String) {
  Row(
    Modifier.fillMaxWidth().padding(vertical = 4.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Text("·  ", style = K1Type.bodySm)
    Text(text, style = K1Type.bodySm, modifier = Modifier.weight(1f))
  }
}

// ─── ORG DETAIL ──────────────────────────────────────────────────────────
@Composable
fun OrgDetailScreen(
  org: Organization,
  people: List<Person> = emptyList(),
  projects: List<Project> = emptyList(),
  meetings: List<Meeting> = emptyList(),
  onBack: () -> Unit,
  onEntityClick: (EntityNavigationData) -> Unit = {},
  onRename: ((String) -> Unit)? = null,
) {
  val orgEmployees = people.filter {
    it.id in org.employees || it.organizationId == org.id || org.employees.contains(it.name)
  }.take(8)
  val orgProjects = projects.filter { p ->
    p.id in org.relatedProjects || p.relatedOrganizations.contains(org.name)
  }.take(6)
  val orgSessions = meetings
    .filter { m -> m.id in org.relatedSessions }
    .sortedByDescending { it.date }
    .take(5)

  var showRename by remember { mutableStateOf(false) }

  DetailScaffold(
    eyebrow = "Organization",
    title = org.name,
    onBack = onBack,
    onTitleLongPress = if (onRename != null) ({ showRename = true }) else null,
  ) {
    Column(Modifier.padding(horizontal = 20.dp)) {
      // Hero: square monogram + industry/type
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          Modifier.size(48.dp).clip(K1R.soft).background(KlikPaperSoft),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            org.name.take(2).uppercase(),
            style = K1Type.bodyMd.copy(
              fontWeight = FontWeight.Medium,
              fontSize = 15.sp,
            ),
          )
        }
        Spacer(Modifier.width(K1Sp.m))
        Column(Modifier.weight(1f)) {
          if (org.industry.isNotBlank()) {
            Text(org.industry, style = K1Type.bodyMd)
          }
          val subline = listOfNotNull(
            org.type,
            org.country,
            org.sizeHeadcount?.let { "$it people" },
          ).joinToString(" · ")
          if (subline.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(subline, style = K1Type.meta)
          }
        }
      }

      // Signals — formation / tribe_vibe / pulse (3-up score row)
      Spacer(Modifier.height(K1Sp.xl))
      K1Eyebrow("Signals")
      Spacer(Modifier.height(K1Sp.s))
      K1DimensionRow(
        keys = listOf("formation", "tribe_vibe", "pulse"),
        dimensions = org.dimensions,
      )

      if (org.strategicFocus.isNotBlank()) {
        Spacer(Modifier.height(K1Sp.xl))
        K1Eyebrow("Strategic focus")
        Spacer(Modifier.height(K1Sp.s))
        K1Card(soft = true) {
          Text(org.strategicFocus, style = K1Type.bodySm)
        }
      }

      if (org.nextAction.isNotBlank()) {
        Spacer(Modifier.height(K1Sp.xl))
        K1SignalCard(
          signal = K1Signal.Commitment,
          eyebrow = "Next action",
          body = org.nextAction,
        )
      }

      if (org.strengths.isNotEmpty()) {
        Spacer(Modifier.height(K1Sp.xl))
        K1Eyebrow("Strengths")
        Spacer(Modifier.height(K1Sp.s))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          org.strengths.take(5).forEach { K1Chip(label = it) }
        }
      }

      if (orgProjects.isNotEmpty()) {
        Spacer(Modifier.height(K1Sp.xl))
        K1SectionHeader("Projects", count = orgProjects.size, dotColor = KlikDotProject)
        Spacer(Modifier.height(K1Sp.s))
        orgProjects.forEach { p ->
          K1Card(
            soft = true,
            onClick = { onEntityClick(EntityNavigationData(EntityType.PROJECT, p.id)) },
          ) {
            Text(p.name, style = K1Type.bodyMd)
            Spacer(Modifier.height(2.dp))
            Text("${p.stage} · ${(p.progress * 100).toInt()}%", style = K1Type.meta)
          }
          Spacer(Modifier.height(6.dp))
        }
      }

      if (orgEmployees.isNotEmpty()) {
        Spacer(Modifier.height(K1Sp.xl))
        K1SectionHeader("People", count = orgEmployees.size)
        Spacer(Modifier.height(K1Sp.s))
        orgEmployees.forEach { p ->
          Row(
            Modifier.fillMaxWidth().k1Clickable { onEntityClick(EntityNavigationData(EntityType.PERSON, p.id)) }.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            K1Avatar(initialsOf(p.name), size = 32.dp)
            Spacer(Modifier.width(K1Sp.m))
            Column(Modifier.weight(1f)) {
              Text(p.name, style = K1Type.bodyMd)
              Spacer(Modifier.height(2.dp))
              Text(
                listOfNotNull(p.role.ifBlank { null }, p.title)
                  .joinToString(" · ")
                  .ifBlank { "—" },
                style = K1Type.meta,
              )
            }
          }
          Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikPaperChip))
        }
      }
    }

    // Related sessions for the org — always shown
    K1RelatedSessionsCards(
      title = "Related sessions",
      sessions = orgSessions,
      onClick = { m -> onEntityClick(EntityNavigationData(EntityType.MEETING, m.id)) },
    )
  }

  if (showRename && onRename != null) {
    RenameEntityDialog(
      kind = "organization",
      currentName = org.name,
      onCancel = { showRename = false },
      onSave = { newName ->
        onRename(newName)
        showRename = false
      },
    )
  }
}

// ─── Rename dialog ───────────────────────────────────────────────────────
//
// Long-press on an entity title (Person/Project/Org) opens this dialog.
// The K1 redesign restores the rename affordance the old liquidglass app
// had — corrections route through EntityFeedbackClient (port 8339), which
// trains the user_id-scoped backend memory and updates the canonical name
// across sessions, moves and the network graph.

@Composable
private fun RenameEntityDialog(
  kind: String,
  currentName: String,
  onCancel: () -> Unit,
  onSave: (String) -> Unit,
) {
  var draft by remember(currentName) { mutableStateOf(currentName) }
  Box(
    Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.45f))
      .k1Clickable(onClick = onCancel),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      Modifier
        .padding(horizontal = 24.dp)
        .fillMaxWidth()
        .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
        .background(KlikPaperCard)
        .k1Clickable(enabled = false) {}
        .padding(24.dp),
    ) {
      Text("Rename $kind", style = K1Type.h3)
      Spacer(Modifier.height(K1Sp.s))
      Text(
        "Klik will use this name everywhere it shows up.",
        style = K1Type.bodySm.copy(color = KlikInkSecondary),
      )
      Spacer(Modifier.height(K1Sp.lg))
      Box(
        Modifier
          .fillMaxWidth()
          .clip(K1R.card)
          .background(KlikPaperChip)
          .padding(horizontal = 12.dp, vertical = 12.dp),
      ) {
        androidx.compose.foundation.text.BasicTextField(
          value = draft,
          onValueChange = { draft = it },
          singleLine = true,
          textStyle = K1Type.bodyMd,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      Spacer(Modifier.height(K1Sp.xl))
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
          Modifier.weight(1f).clip(K1R.pill).background(KlikPaperChip)
            .k1Clickable(onClick = onCancel).padding(vertical = 14.dp),
          contentAlignment = Alignment.Center,
        ) { Text("Cancel", style = K1Type.bodyMd) }
        val canSave = draft.trim().isNotEmpty() && draft.trim() != currentName.trim()
        Box(
          Modifier.weight(1f).clip(K1R.pill)
            .background(if (canSave) KlikInkPrimary else KlikInkMuted)
            .k1Clickable(enabled = canSave) { onSave(draft.trim()) }
            .padding(vertical = 14.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            "Save",
            style = K1Type.bodyMd.copy(
              color = KlikPaperCard,
              fontWeight = FontWeight.Medium,
            ),
          )
        }
      }
    }
  }
}

// ─── helpers ─────────────────────────────────────────────────────────────

/** Normalises a raw backend string for Markdown rendering.
 *  The backend stores outputs as JSON string literals so they arrive
 *  with escape sequences still encoded (\n, \", \\).  Strip the outer
 *  quotes and unescape before passing to the Markdown renderer.
 */
private fun normalizeMarkdown(raw: String): String {
  var s = raw.trim()
  if (s.startsWith("\"") && s.endsWith("\"") && s.length > 1) {
    s = s.substring(1, s.length - 1)
  }
  s = s.replace("\\n", "\n")
    .replace("\\\"", "\"")
    .replace("\\\\", "\\")
    .replace("\\t", "\t")
  return s.trim()
}

private fun initialsOf(name: String): String = name.trim().split(" ").filter { it.isNotEmpty() }.take(2)
  .joinToString("") { it.take(1).uppercase() }

/** Returns (Simple Icons CDN URL, display label) for a backend tool category slug.
 *  Icons served from cdn.simpleicons.org — real brand logos in SVG. */
private fun integrationInfo(slug: String): Pair<String?, String> {
  val key = slug.lowercase()
  fun icon(name: String) = "https://cdn.simpleicons.org/$name/1C1D21"
  return when {
    "gmail" in key || ("email" in key && "google" in key) -> icon("gmail") to "Gmail"
    "email" in key -> icon("maildotru") to "Email"
    "calendar" in key && "google" in key -> icon("googlecalendar") to "Google Calendar"
    "calendar" in key && "apple" in key -> icon("apple") to "Apple Calendar"
    "calendar" in key -> icon("googlecalendar") to "Calendar"
    "slack" in key -> icon("slack") to "Slack"
    "notion" in key -> icon("notion") to "Notion"
    "linear" in key -> icon("linear") to "Linear"
    "github" in key -> icon("github") to "GitHub"
    "gitlab" in key -> icon("gitlab") to "GitLab"
    "jira" in key -> icon("jira") to "Jira"
    "confluence" in key -> icon("confluence") to "Confluence"
    "drive" in key -> icon("googledrive") to "Google Drive"
    "docs" in key && "google" in key -> icon("googledocs") to "Google Docs"
    "sheets" in key -> icon("googlesheets") to "Google Sheets"
    "meet" in key && "google" in key -> icon("googlemeet") to "Google Meet"
    "zoom" in key -> icon("zoom") to "Zoom"
    "teams" in key -> icon("microsoftteams") to "Teams"
    "outlook" in key -> icon("microsoftoutlook") to "Outlook"
    "onedrive" in key -> icon("microsoftonedrive") to "OneDrive"
    "sharepoint" in key -> icon("microsoftsharepoint") to "SharePoint"
    "web_search" in key || "search" in key -> icon("google") to "Search"
    "browser" in key -> icon("googlechrome") to "Browser"
    "github" in key -> icon("github") to "GitHub"
    "trello" in key -> icon("trello") to "Trello"
    "asana" in key -> icon("asana") to "Asana"
    "clickup" in key -> icon("clickup") to "ClickUp"
    "figma" in key -> icon("figma") to "Figma"
    "twitter" in key || "x.com" in key -> icon("x") to "X"
    "linkedin" in key -> icon("linkedin") to "LinkedIn"
    "whatsapp" in key -> icon("whatsapp") to "WhatsApp"
    "telegram" in key -> icon("telegram") to "Telegram"
    "discord" in key -> icon("discord") to "Discord"
    "hubspot" in key -> icon("hubspot") to "HubSpot"
    "salesforce" in key -> icon("salesforce") to "Salesforce"
    "airtable" in key -> icon("airtable") to "Airtable"
    "dropbox" in key -> icon("dropbox") to "Dropbox"
    "box" in key -> icon("box") to "Box"
    "stripe" in key -> icon("stripe") to "Stripe"
    "openai" in key || "gpt" in key -> icon("openai") to "OpenAI"
    "anthropic" in key || "claude" in key -> icon("anthropic") to "Anthropic"
    else -> null to slug.split("_").joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
  }
}

// ─── Related-X sections ──────────────────────────────────────────────────
//
// Small editorial sections used by the entity detail screens to surface
// the related-people / related-projects / related-orgs / related-sessions
// chips that the old liquid-glass WorkLifeScreen showed inline.  Always
// rendered (even when empty) with a "(none)" placeholder so the
// information layout stays consistent across entities.

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun K1RelatedChipsSection(
  title: String,
  count: Int,
  dotColor: Color,
  names: List<String>,
  onChipClick: ((String) -> Unit)? = null,
  maxVisible: Int = 8,
) {
  Spacer(Modifier.height(K1Sp.xl))
  Column(Modifier.padding(horizontal = 20.dp)) {
    K1SectionHeader(title, count = count, dotColor = dotColor)
    Spacer(Modifier.height(K1Sp.s))
    if (names.isEmpty()) {
      Box(
        Modifier
          .clip(K1R.chip)
          .background(KlikPaperChip)
          .padding(horizontal = 9.dp, vertical = 4.dp),
      ) {
        Text("(none)", style = K1Type.metaSm.copy(color = KlikInkTertiary))
      }
    } else {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        names.take(maxVisible).forEach { name ->
          K1Chip(
            label = name,
            onClick = onChipClick?.let { cb -> { cb(name) } },
          )
        }
        if (names.size > maxVisible) {
          K1Chip(label = "+${names.size - maxVisible}")
        }
      }
    }
  }
}

@Composable
private fun K1RelatedSessionsCards(
  title: String,
  sessions: List<Meeting>,
  onClick: (Meeting) -> Unit,
) {
  Spacer(Modifier.height(K1Sp.xl))
  Column(Modifier.padding(horizontal = 20.dp)) {
    K1SectionHeader(title, count = sessions.size)
    Spacer(Modifier.height(K1Sp.s))
    if (sessions.isEmpty()) {
      Box(
        Modifier
          .clip(K1R.chip)
          .background(KlikPaperChip)
          .padding(horizontal = 9.dp, vertical = 4.dp),
      ) {
        Text("(none)", style = K1Type.metaSm.copy(color = KlikInkTertiary))
      }
    } else {
      sessions.forEach { m ->
        K1Card(
          soft = true,
          onClick = { onClick(m) },
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
              Text(m.title.ifBlank { "Untitled" }, style = K1Type.bodyMd)
              Spacer(Modifier.height(2.dp))
              Text("${m.date} · ${m.time}", style = K1Type.meta)
            }
            if (m.participants.isNotEmpty()) {
              K1AvatarStack(
                initialsList = m.participants.take(3).map { initialsOf(it.name) },
                size = 20.dp,
              )
            }
          }
        }
        Spacer(Modifier.height(6.dp))
      }
    }
  }
}

@Composable
private fun IntegrationChip(iconUrl: String?, label: String) {
  Row(
    Modifier
      .clip(K1R.chip)
      .background(KlikPaperChip)
      .padding(horizontal = 9.dp, vertical = 5.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(5.dp),
  ) {
    if (iconUrl != null) {
      AsyncImage(
        model = iconUrl,
        contentDescription = label,
        modifier = Modifier.size(14.dp),
      )
    }
    Text(label, style = K1Type.meta.copy(color = KlikInkSecondary, fontSize = 11.sp))
  }
}
