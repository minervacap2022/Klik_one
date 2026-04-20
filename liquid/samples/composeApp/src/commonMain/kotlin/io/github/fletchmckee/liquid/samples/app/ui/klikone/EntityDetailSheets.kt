// Copyright 2025, Klik — drill-in detail sheets for Task / Person / Project / Org.
// These are presented as full-screen K1 surfaces so the legacy chrome stays hidden.
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.theme.*

// ─── Shared detail scaffold ───────────────────────────────────────────────

@Composable
private fun DetailScaffold(
    eyebrow: String,
    title: String,
    onBack: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
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
            Box(
                Modifier.size(32.dp).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { BackChevron() }
            Spacer(Modifier.weight(1f))
            if (trailing != null) trailing()
        }

        // Hero
        Column(Modifier.padding(horizontal = 20.dp)) {
            K1Eyebrow(eyebrow)
            Spacer(Modifier.height(6.dp))
            Text(title, style = K1Type.h2)
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

// ─── TASK DETAIL ─────────────────────────────────────────────────────────
@Composable
fun TaskDetailScreen(
    task: TaskMetadata,
    meetings: List<Meeting> = emptyList(),
    onBack: () -> Unit,
    onApprove: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
) {
    DetailScaffold(
        eyebrow = "Move",
        title = task.title,
        onBack = onBack,
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
                    task.needsConfirmation -> "Needs OK"
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
                            val detail = step.errorMessage ?: step.output
                            if (!detail.isNullOrBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    detail.take(120),
                                    style = K1Type.metaSm.copy(
                                        color = if (step.success) KlikInkTertiary else KlikRiskSubtext,
                                    ),
                                )
                            }
                            step.durationMs?.let {
                                Spacer(Modifier.height(2.dp))
                                Text("${it} ms", style = K1Type.metaSm)
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
                    Text(task.taskResult!!, style = K1Type.bodySm)
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
                K1Card(soft = true) {
                    task.relatedProject.takeIf { it.isNotBlank() }?.let {
                        RelatedLine("Project", it, KlikDotProject)
                    }
                    task.relatedProjects.filter { it != task.relatedProject }.forEach {
                        RelatedLine("Project", it, KlikDotProject)
                    }
                    task.relatedPeople.forEach { RelatedLine("Person", it, KlikDotPerson) }
                    task.relatedOrganizations.forEach { RelatedLine("Org", it, KlikDotOrg) }
                }
            }

            // Source session
            val sessionId = task.relatedMeetingId
            if (!sessionId.isNullOrBlank()) {
                val meetingMatch = meetings.find { it.id == sessionId }
                Spacer(Modifier.height(K1Sp.xl))
                K1Eyebrow("Source session")
                Spacer(Modifier.height(K1Sp.s))
                K1Card(soft = true) {
                    Text(meetingMatch?.title?.ifBlank { null } ?: sessionId.take(24),
                        style = K1Type.bodyMd)
                    if (meetingMatch != null) {
                        Spacer(Modifier.height(2.dp))
                        Text("${meetingMatch.time} · ${meetingMatch.participants.size} people",
                            style = K1Type.meta)
                    }
                }
            }

            // Action row for needs-confirmation tasks
            if (task.needsConfirmation && (onApprove != null || onReject != null)) {
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
                    if (onReject != null) {
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(K1R.pill)
                                .border(0.5.dp, KlikInkMuted, K1R.pill)
                                .clickable(onClick = onReject)
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Skip", style = K1Type.bodyMd.copy(color = KlikInkPrimary))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RelatedLine(kind: String, label: String, dot: androidx.compose.ui.graphics.Color) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(5.dp).clip(CircleShape).background(dot))
        Spacer(Modifier.width(K1Sp.s))
        Text(kind, style = K1Type.metaSm)
        Spacer(Modifier.width(K1Sp.s))
        Text(label, style = K1Type.bodySm, modifier = Modifier.weight(1f))
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
    onBack: () -> Unit,
) {
    val personMeetings = meetings.filter { m -> m.participants.any { it.id == person.id } }
        .sortedByDescending { it.date }
        .take(5)
    val personTasks = tasks.filter { t -> t.relatedPeople.any { it.equals(person.name, true) } }.take(5)
    val personProjects = projects.filter { p -> p.id in person.relatedProjects || p.relatedPeople.contains(person.name) }
    val personOrgs = organizations.filter { o -> o.id in person.relatedOrganizations || o.employees.contains(person.name) }

    DetailScaffold(
        eyebrow = "Person",
        title = person.name,
        onBack = onBack,
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

        // Recent sessions
        if (personMeetings.isNotEmpty()) {
            Spacer(Modifier.height(K1Sp.xl))
            Column(Modifier.padding(horizontal = 20.dp)) {
                K1SectionHeader("Recent sessions", count = personMeetings.size)
                Spacer(Modifier.height(K1Sp.s))
                personMeetings.forEach { m ->
                    K1Card(soft = true) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(m.title.ifBlank { "Untitled" }, style = K1Type.bodyMd)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "${m.date} · ${m.time}",
                                    style = K1Type.meta,
                                )
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

        // Open commitments / related tasks
        if (personTasks.isNotEmpty()) {
            Spacer(Modifier.height(K1Sp.xl))
            Column(Modifier.padding(horizontal = 20.dp)) {
                K1SectionHeader("Open commitments", count = personTasks.size, dotColor = KlikWarn)
                Spacer(Modifier.height(K1Sp.s))
                personTasks.forEach { t ->
                    K1Card(soft = true) {
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

        // Projects + Orgs
        if (personProjects.isNotEmpty() || personOrgs.isNotEmpty()) {
            Spacer(Modifier.height(K1Sp.xl))
            Column(Modifier.padding(horizontal = 20.dp)) {
                K1Eyebrow("Also connected to")
                Spacer(Modifier.height(K1Sp.s))
                K1Card(soft = true) {
                    personProjects.take(5).forEach { p ->
                        RelatedLine("Project", p.name, KlikDotProject)
                    }
                    personOrgs.take(5).forEach { o ->
                        RelatedLine("Org", o.name, KlikDotOrg)
                    }
                }
            }
        }
    }
}

// ─── PROJECT DETAIL ──────────────────────────────────────────────────────
@Composable
fun ProjectDetailScreen(
    project: Project,
    meetings: List<Meeting> = emptyList(),
    tasks: List<TaskMetadata> = emptyList(),
    onBack: () -> Unit,
) {
    val projectTasks = tasks.filter { t ->
        t.relatedProject.equals(project.name, true) || t.relatedProjects.contains(project.name)
    }.take(8)
    val projectMeetings = meetings.filter { m -> m.id in project.relatedMeetings }
        .sortedByDescending { it.date }
        .take(5)

    DetailScaffold(
        eyebrow = "Project",
        title = project.name,
        onBack = onBack,
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
                    K1Card(soft = true) {
                        Text(t.title, style = K1Type.bodyMd)
                        if (t.subtitle.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(t.subtitle, style = K1Type.meta)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }

            // Related meetings
            if (projectMeetings.isNotEmpty()) {
                Spacer(Modifier.height(K1Sp.xl))
                K1SectionHeader("Related sessions", count = projectMeetings.size)
                Spacer(Modifier.height(K1Sp.s))
                projectMeetings.forEach { m ->
                    K1Card(soft = true) {
                        Text(m.title.ifBlank { "Untitled" }, style = K1Type.bodyMd)
                        Spacer(Modifier.height(2.dp))
                        Text("${m.date} · ${m.time}", style = K1Type.meta)
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
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
    onBack: () -> Unit,
) {
    val orgEmployees = people.filter {
        it.id in org.employees || it.organizationId == org.id || org.employees.contains(it.name)
    }.take(8)
    val orgProjects = projects.filter { p ->
        p.id in org.relatedProjects || p.relatedOrganizations.contains(org.name)
    }.take(6)

    DetailScaffold(
        eyebrow = "Organization",
        title = org.name,
        onBack = onBack,
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
                    K1Card(soft = true) {
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
                        Modifier.fillMaxWidth().padding(vertical = 10.dp),
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
    }
}

// ─── helpers ─────────────────────────────────────────────────────────────
private fun initialsOf(name: String): String =
    name.trim().split(" ").filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.take(1).uppercase() }
