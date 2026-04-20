// Copyright 2025, Klik — Klik One Network tab. Matches klik_one_5_screens.html screen 05.
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.fletchmckee.liquid.samples.app.core.rememberViewModel
import io.github.fletchmckee.liquid.samples.app.data.source.remote.EncourageData
import io.github.fletchmckee.liquid.samples.app.data.source.remote.GoalListResponse
import io.github.fletchmckee.liquid.samples.app.data.source.remote.UserLevelData
import io.github.fletchmckee.liquid.samples.app.data.source.remote.WorklifeData
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.domain.entity.SubscriptionFeatures
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.presentation.worklife.WorkLifeViewModel
import io.github.fletchmckee.liquid.samples.app.theme.*
import io.github.fletchmckee.liquid.samples.app.theme.KlikCommitmentAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikLineHairline
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityType
import io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation

/** Klik One — Network. Drop-in replacement for `WorkLifeScreen`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    viewModel: WorkLifeViewModel = rememberViewModel { WorkLifeViewModel() },
    isLoading: Boolean = false,
    isLlmDataLoading: Boolean = false,
    encourageData: EncourageData? = null,
    worklifeData: WorklifeData? = null,
    goalsData: GoalListResponse? = null,
    userLevelData: UserLevelData? = null,
    onArchiveProject: (String) -> Unit = {},
    onArchivePerson: (String) -> Unit = {},
    onArchiveOrganization: (String) -> Unit = {},
    tasks: List<TaskMetadata> = emptyList(),
    meetings: List<Meeting> = emptyList(),
    onEntityClick: (EntityNavigationData) -> Unit = {},
    onSegmentClick: (TracedSegmentNavigation) -> Unit = {},
    highlightProjectId: String? = null,
    highlightPersonId: String? = null,
    highlightOrganizationId: String? = null,
    onEntityHighlighted: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onGrowthTreeClick: () -> Unit = {},
    subscriptionFeatures: SubscriptionFeatures? = null,
    onUpgradeRequired: (String) -> Unit = {},
) {
    val uiState by viewModel.state.collectAsState()
    var segment by remember { mutableStateOf("people") }

    val people = uiState.people
    val projects = uiState.projects
    val orgs = uiState.organizations
    val topInfluencers = uiState.topInfluencers.ifEmpty { people.take(5) }

    // Needs-attention heuristic: tasks that are follow-ups/lapsed for a person
    val needsAttention = tasks
        .filter { it.needsConfirmation || it.status.name == "IN_REVIEW" }
        .mapNotNull { t ->
            val personName = t.relatedPeople.firstOrNull()
            if (personName.isNullOrBlank()) null
            else people.find { it.name.equals(personName, ignoreCase = true) }?.let { p ->
                Triple(p, t.title, t.dueInfo.ifBlank { "Follow-up needed" })
            }
        }
        .take(3)

    PullToRefreshBox(
        isRefreshing = isLoading,
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
        K1Header(title = "Network", trailing = { SearchIcon() })
        Spacer(Modifier.height(K1Sp.md))

        // Growth · user level + XP progress + streak
        userLevelData?.let { level ->
            Column(Modifier.padding(horizontal = 20.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(K1R.card)
                        .background(KlikPaperCard)
                        .clickable(onClick = onGrowthTreeClick)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        val title = level.levelTitle.takeIf { it.isNotBlank() } ?: "Level ${level.level}"
                        K1Eyebrow("Growth · $title")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (level.xpToNextLevel > 0) "${level.xpToNextLevel} XP to next level"
                            else "Max level reached",
                            style = K1Type.bodyMd,
                        )
                        Spacer(Modifier.height(8.dp))
                        val totalForLevel = (level.currentXp + level.xpToNextLevel).coerceAtLeast(1)
                        val pct = (level.currentXp.toFloat() / totalForLevel).coerceIn(0f, 1f)
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(K1R.pill)
                                .background(KlikLineHairline),
                        ) {
                            Box(
                                Modifier.fillMaxHeight().fillMaxWidth(pct).background(KlikCommitmentAccent),
                            )
                        }
                        if (level.streakDays > 0) {
                            Spacer(Modifier.height(6.dp))
                            Text("${level.streakDays}-day streak", style = K1Type.metaSm)
                        }
                    }
                    Spacer(Modifier.width(K1Sp.m))
                    // Right-pointing chevron to hint this opens the growth tree
                    androidx.compose.foundation.Canvas(Modifier.size(14.dp)) {
                        val w = 1.4.dp.toPx()
                        drawLine(
                            color = KlikInkMuted,
                            strokeWidth = w,
                            cap = StrokeCap.Round,
                            start = Offset(4.dp.toPx(), 3.dp.toPx()),
                            end = Offset(9.dp.toPx(), 7.dp.toPx()),
                        )
                        drawLine(
                            color = KlikInkMuted,
                            strokeWidth = w,
                            cap = StrokeCap.Round,
                            start = Offset(9.dp.toPx(), 7.dp.toPx()),
                            end = Offset(4.dp.toPx(), 11.dp.toPx()),
                        )
                    }
                }
            }
            Spacer(Modifier.height(K1Sp.xl))
        }

        // Goals list
        goalsData?.goals?.takeIf { it.isNotEmpty() }?.let { goalList ->
            Column(Modifier.padding(horizontal = 20.dp)) {
                K1SectionHeader("Goals", count = goalList.size, dotColor = KlikCommitmentAccent)
                Spacer(Modifier.height(K1Sp.s))
                goalList.take(4).forEach { g ->
                    K1Card(soft = true) {
                        Text(g.goal.ifBlank { "Untitled goal" }, style = K1Type.bodyMd)
                        // GoalDto.currentProgress is 0..1 on the backend schema
                        val progress = g.currentProgress.coerceIn(0f, 1f)
                        if (g.category.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(g.category, style = K1Type.metaSm)
                        }
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier.fillMaxWidth().height(3.dp).clip(K1R.pill)
                                .background(KlikLineHairline),
                        ) {
                            Box(
                                Modifier.fillMaxHeight().fillMaxWidth(progress)
                                    .background(KlikInkPrimary),
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("${(progress * 100).toInt()}%", style = K1Type.metaSm)
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
            Spacer(Modifier.height(K1Sp.xl))
        }

        // Segmented control: People / Projects / Orgs
        Row(
            Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(K1R.card)
                .background(KlikPaperChip)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SegmentTab("People · ${people.size}", segment == "people", Modifier.weight(1f)) { segment = "people" }
            SegmentTab("Projects · ${projects.size}", segment == "projects", Modifier.weight(1f)) { segment = "projects" }
            SegmentTab("Orgs · ${orgs.size}", segment == "orgs", Modifier.weight(1f)) { segment = "orgs" }
        }

        Spacer(Modifier.height(K1Sp.xl))

        when (segment) {
            "people"   -> PeoplePanel(
                people = people,
                topInfluencers = topInfluencers,
                needsAttention = needsAttention,
                meetings = meetings,
                onEntityClick = onEntityClick,
            )
            "projects" -> ProjectsPanel(projects, onEntityClick)
            else       -> OrgsPanel(orgs, onEntityClick)
        }

        if (isLoading) {
            Spacer(Modifier.height(24.dp))
            Text(
                "Loading…",
                style = K1Type.caption,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            )
        }
    }
    } // end PullToRefreshBox
}

@Composable
private fun SegmentTab(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(K1R.chip)
            .background(if (selected) KlikPaperCard else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = K1Type.caption.copy(
                fontSize = 12.sp,
                color = if (selected) KlikInkPrimary else KlikInkTertiary,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            )
        )
    }
}

@Composable
private fun PeoplePanel(
    people: List<Person>,
    topInfluencers: List<Person>,
    needsAttention: List<Triple<Person, String, String>>,
    meetings: List<Meeting>,
    onEntityClick: (EntityNavigationData) -> Unit,
) {
    // SEEN THIS WEEK horizontal avatar strip
    if (topInfluencers.isNotEmpty()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            K1SectionHeader("Seen this week", count = topInfluencers.size)
            Spacer(Modifier.height(K1Sp.m))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(topInfluencers.take(8)) { p ->
                    Column(
                        Modifier.width(72.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        K1Avatar(initialsOfPerson(p), size = 56.dp)
                        Spacer(Modifier.height(K1Sp.s))
                        Text(
                            firstName(p.name),
                            style = K1Type.meta.copy(
                                color = KlikInkPrimary,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                        Spacer(Modifier.height(2.dp))
                        val sessionCount = meetings.count { m -> m.participants.any { it.id == p.id } }
                        Text(
                            if (sessionCount > 0) "$sessionCount sessions" else p.role.ifBlank { "—" },
                            style = K1Type.metaSm,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(K1Sp.xxl))
    }

    // NEEDS ATTENTION — amber cards, matches reference
    if (needsAttention.isNotEmpty()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            K1SectionHeader("Needs attention", count = needsAttention.size, dotColor = KlikWarn)
            Spacer(Modifier.height(K1Sp.s))
            needsAttention.forEach { (person, _, lapsed) ->
                NeedsAttentionRow(person, lapsed)
                Spacer(Modifier.height(6.dp))
            }
        }
        Spacer(Modifier.height(K1Sp.xxl))
    }

    // ALL PEOPLE
    Column(Modifier.padding(horizontal = 20.dp)) {
        K1SectionHeader(
            "All people",
            count = people.size,
            trailing = {
                Text("Sort ↓", style = K1Type.metaSm.copy(color = KlikInkTertiary))
            }
        )
        Spacer(Modifier.height(K1Sp.s))
        people.forEach { p -> PersonRow(p, onEntityClick) }
    }
}

@Composable
private fun NeedsAttentionRow(person: Person, lapsed: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(K1R.soft)
            .background(KlikDecisionBg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        K1Avatar(initialsOfPerson(person), size = 36.dp)
        Spacer(Modifier.width(K1Sp.m))
        Column(Modifier.weight(1f)) {
            Text(person.name, style = K1Type.bodyMd)
            Spacer(Modifier.height(2.dp))
            Text(lapsed, style = K1Type.meta.copy(color = KlikDecisionSubtext))
        }
        KlikItButton(onClick = {})
    }
}

@Composable
private fun KlikItButton(onClick: () -> Unit) {
    Box(
        Modifier
            .clip(K1R.chip)
            .background(KlikInkPrimary)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Klik it",
            style = K1Type.meta.copy(
                color = KlikPaperCard,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            ),
        )
    }
}

@Composable
private fun PersonRow(p: Person, onEntityClick: (EntityNavigationData) -> Unit) {
    Column(
        Modifier.clickable {
            onEntityClick(EntityNavigationData(EntityType.PERSON, p.id))
        }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            K1Avatar(initialsOfPerson(p), size = 36.dp)
            Spacer(Modifier.width(K1Sp.m))
            Column(Modifier.weight(1f)) {
                Text(p.name, style = K1Type.bodyMd)
                Spacer(Modifier.height(2.dp))
                Text(
                    listOfNotNull(p.role.ifBlank { null }, p.title).joinToString(" · ").ifBlank { "—" },
                    style = K1Type.meta,
                )
            }
            Text(p.lastInteraction.take(8), style = K1Type.metaSm)
        }
        Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikPaperChip))
    }
}

@Composable
private fun ProjectsPanel(projects: List<Project>, onEntityClick: (EntityNavigationData) -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        K1SectionHeader("All projects", count = projects.size, dotColor = KlikDotProject)
        Spacer(Modifier.height(K1Sp.s))
        projects.forEach { pr ->
            K1Card(
                onClick = {
                    onEntityClick(EntityNavigationData(EntityType.PROJECT, pr.id))
                },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(KlikDotProject))
                    Spacer(Modifier.width(K1Sp.s))
                    Text(pr.name, style = K1Type.bodyMd, modifier = Modifier.weight(1f))
                    Text(pr.stage, style = K1Type.metaSm)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(K1R.pill)
                            .background(KlikLineHairline),
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(pr.progress.coerceIn(0f, 1f))
                                .background(KlikInkPrimary),
                        )
                    }
                    Spacer(Modifier.width(K1Sp.s))
                    Text("${(pr.progress * 100).toInt()}%", style = K1Type.metaSm)
                }
                if (pr.teamMembers.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        K1AvatarStack(
                            initialsList = pr.teamMembers.take(3).map { n ->
                                n.trim().split(" ").filter { it.isNotEmpty() }
                                    .take(2).joinToString("") { it.take(1).uppercase() }
                            },
                            size = 20.dp,
                        )
                        Spacer(Modifier.width(K1Sp.s))
                        Text("${pr.teamMembers.size} people", style = K1Type.metaSm)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun OrgsPanel(orgs: List<Organization>, onEntityClick: (EntityNavigationData) -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        K1SectionHeader("Organizations", count = orgs.size, dotColor = KlikDotOrg)
        Spacer(Modifier.height(K1Sp.s))
        orgs.forEach { org ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        onEntityClick(EntityNavigationData(EntityType.ORGANIZATION, org.id))
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(36.dp).clip(K1R.soft).background(KlikPaperSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        org.name.take(2).uppercase(),
                        style = K1Type.bodyMd.copy(fontSize = 12.sp),
                    )
                }
                Spacer(Modifier.width(K1Sp.m))
                Column(Modifier.weight(1f)) {
                    Text(org.name, style = K1Type.bodyMd)
                    Spacer(Modifier.height(2.dp))
                    Text(org.industry.ifBlank { "—" }, style = K1Type.meta)
                }
            }
            Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikPaperChip))
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

private fun initialsOfPerson(p: Person): String =
    p.name.trim().split(" ").filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.take(1).uppercase() }

private fun firstName(full: String): String = full.trim().split(" ").firstOrNull() ?: full
