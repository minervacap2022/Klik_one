// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import io.github.fletchmckee.liquid.samples.app.theme.KlikCommitmentAccent
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionBg
import io.github.fletchmckee.liquid.samples.app.theme.KlikDecisionSubtext
import io.github.fletchmckee.liquid.samples.app.theme.KlikDotOrg
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
import io.github.fletchmckee.liquid.samples.app.theme.KlikWarn
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
  xpHistoryItems: List<io.github.fletchmckee.liquid.samples.app.data.source.remote.XpHistoryItem> = emptyList(),
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
  var searchActive by remember { mutableStateOf(false) }
  var searchQuery by remember { mutableStateOf("") }

  // Search filter — case-insensitive match against display name +
  // canonical name + aliases for each entity type. Empty query passes
  // everything through unchanged.
  fun matches(name: String, canonical: String, aliases: List<String>): Boolean {
    if (searchQuery.isBlank()) return true
    val q = searchQuery.trim()
    if (name.contains(q, ignoreCase = true)) return true
    if (canonical.contains(q, ignoreCase = true)) return true
    return aliases.any { it.contains(q, ignoreCase = true) }
  }

  val people = uiState.people.filter { matches(it.name, it.canonicalName, it.aliases) }
  val projects = uiState.projects.filter { matches(it.name, it.canonicalName, it.aliases) }
  val orgs = uiState.organizations.filter { matches(it.name, it.canonicalName, it.aliases) }
  // No silent fallback to `people.take(5)` — that masked an empty backend
  // response with arbitrary contacts. Section is gated below; if the backend
  // hasn't computed top voices, the strip just doesn't render.
  val topInfluencers = uiState.topInfluencers

  // Needs-attention heuristic: tasks that are follow-ups/lapsed for a person
  val needsAttention = tasks
    .filter { it.needsConfirmation || it.status.name == "IN_REVIEW" }
    .mapNotNull { t ->
      val personName = t.relatedPeople.firstOrNull()
      if (personName.isNullOrBlank()) {
        null
      } else {
        people.find { it.name.equals(personName, ignoreCase = true) }?.let { p ->
          Triple(p, t.title, t.dueInfo.ifBlank { "Follow-up needed" })
        }
      }
    }
    .take(3)

  val ptrState = rememberPullToRefreshState()
  PullToRefreshBox(
    isRefreshing = isLoading,
    state = ptrState,
    onRefresh = onRefresh,
    modifier = Modifier.fillMaxSize().background(KlikPaperApp),
    indicator = {
      K1PullRefreshIndicator(
        state = ptrState,
        isRefreshing = isLoading,
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
                  Text(
                    "Search people, projects, orgs…",
                    style = K1Type.bodyMd.copy(color = KlikInkMuted),
                  )
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
          title = "Network",
          trailing = {
            Box(Modifier.k1Clickable { searchActive = true }) { SearchIcon() }
          },
        )
      }
      Spacer(Modifier.height(K1Sp.md))

      // Growth · user level + XP progress + streak
      userLevelData?.let { level ->
        Column(Modifier.padding(horizontal = 20.dp)) {
          Row(
            Modifier
              .fillMaxWidth()
              .clip(K1R.card)
              .background(KlikPaperCard)
              .k1Clickable(onClick = onGrowthTreeClick)
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(Modifier.weight(1f)) {
              val title = level.levelTitle.takeIf { it.isNotBlank() } ?: "Level ${level.level}"
              K1Eyebrow("Growth · $title")
              Spacer(Modifier.height(4.dp))
              Text(
                if (level.xpToNextLevel > 0) {
                  "${level.xpToNextLevel} XP to next level"
                } else {
                  "Max level reached"
                },
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
        // XP log moved to the You → XP logs sub-screen. The Network screen
        // now stays focused on level/streak + goals/people network.
        Spacer(Modifier.height(K1Sp.xl))
      }

      // ── GOALS & MILESTONES — sits with the XP/streak card so the answer
      // to "where am I going" is right next to "where am I now". Reads
      // from goalsState (KK_goal API). Always render the section so the
      // user can never lose the entry point if the API blinks (NO SILENT
      // SWALLOW). Empty state = paper card with "(none yet)".
      run {
        val goals = goalsData?.goals.orEmpty()
        Column(Modifier.padding(horizontal = 20.dp)) {
          K1SectionHeader("Goals & Milestones", count = goals.size)
          Spacer(Modifier.height(K1Sp.s))
          Column(
            Modifier
              .fillMaxWidth()
              .clip(K1R.card)
              .background(KlikPaperCard)
              .padding(16.dp),
          ) {
            if (goals.isEmpty()) {
              Text(
                "No goals yet. Klik will surface them here as you record more sessions.",
                style = K1Type.bodySm.copy(color = KlikInkTertiary),
              )
            } else {
              goals.take(3).forEachIndexed { idx, g ->
                if (idx > 0) {
                  Spacer(Modifier.height(K1Sp.m))
                  Box(Modifier.fillMaxWidth().height(0.5.dp).background(KlikLineHairline))
                  Spacer(Modifier.height(K1Sp.m))
                }
                K1GoalBlock(goal = g)
              }
            }
          }
        }
        Spacer(Modifier.height(K1Sp.xl))
      }

      // ── HIGHLIGHTS — encourage message + worklife recommendations ────
      val encourageMsg = encourageData?.message?.trim()?.takeIf { it.isNotBlank() }
      val recommendations = worklifeData?.insights
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?.distinctBy { it.lowercase() }
        ?.take(4)
        ?.takeIf { it.isNotEmpty() }
      if (encourageMsg == null && recommendations == null && isLlmDataLoading) {
        Column(Modifier.padding(horizontal = 20.dp)) {
          K1SectionHeader("Highlights")
          Spacer(Modifier.height(K1Sp.s))
          K1SkeletonCard(lines = 3)
        }
        Spacer(Modifier.height(K1Sp.xl))
      }
      if (encourageMsg != null || recommendations != null) {
        Column(Modifier.padding(horizontal = 20.dp)) {
          K1SectionHeader("Highlights")
          Spacer(Modifier.height(K1Sp.s))
          // Encourage + worklife recommendations can run multiple
          // paragraphs — collapse behind a tap so the section header
          // stays scannable.
          K1ExpandableCard { expanded ->
            if (encourageMsg != null) {
              io.github.fletchmckee.liquid.samples.app.ui.components.EntityHighlightedText(
                text = encourageMsg,
                tasks = tasks,
                meetings = meetings,
                projects = projects,
                people = people,
                organizations = orgs,
                onEntityClick = onEntityClick,
                style = K1Type.bodySm,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
              )
            }
            if (expanded && encourageMsg != null && recommendations != null) {
              Spacer(Modifier.height(K1Sp.m))
              Box(
                Modifier
                  .fillMaxWidth()
                  .height(0.5.dp)
                  .background(KlikLineHairline),
              )
              Spacer(Modifier.height(K1Sp.m))
            }
            if (expanded && recommendations != null) {
              recommendations.forEach { line ->
                Row {
                  Text("·  ", style = K1Type.caption)
                  io.github.fletchmckee.liquid.samples.app.ui.components.EntityHighlightedText(
                    text = line,
                    tasks = tasks,
                    meetings = meetings,
                    projects = projects,
                    people = people,
                    organizations = orgs,
                    onEntityClick = onEntityClick,
                    style = K1Type.bodySm,
                  )
                }
                Spacer(Modifier.height(4.dp))
              }
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
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        SegmentTab("People · ${people.size}", segment == "people", Modifier.weight(1f)) { segment = "people" }
        SegmentTab("Projects · ${projects.size}", segment == "projects", Modifier.weight(1f)) { segment = "projects" }
        SegmentTab("Orgs · ${orgs.size}", segment == "orgs", Modifier.weight(1f)) { segment = "orgs" }
      }

      Spacer(Modifier.height(K1Sp.xl))

      when (segment) {
        "people" -> PeoplePanel(
          people = people,
          topInfluencers = topInfluencers,
          needsAttention = needsAttention,
          meetings = meetings,
          onEntityClick = onEntityClick,
        )

        "projects" -> ProjectsPanel(projects, onEntityClick)

        else -> OrgsPanel(orgs, onEntityClick)
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
      .k1Clickable(onClick = onClick)
      .padding(vertical = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      label,
      style = K1Type.caption.copy(
        fontSize = 12.sp,
        color = if (selected) KlikInkPrimary else KlikInkTertiary,
        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
      ),
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
  // Top voices — sourced from the backend voice-dimension ranking
  // (PersonRepository.getTopInfluencers). Previously mislabeled "Seen this
  // week" — that was a lie since no time filter was ever applied.
  if (topInfluencers.isNotEmpty()) {
    Column(Modifier.padding(horizontal = 20.dp)) {
      K1SectionHeader("Top voices", count = topInfluencers.size)
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
      },
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
      .k1Clickable(onClick = onClick)
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
    Modifier.k1Clickable {
      onEntityClick(EntityNavigationData(EntityType.PERSON, p.id))
    },
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
          .k1Clickable {
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

private fun initialsOfPerson(p: Person): String = p.name.trim().split(" ").filter { it.isNotEmpty() }.take(2)
  .joinToString("") { it.take(1).uppercase() }

private fun firstName(full: String): String = full.trim().split(" ").firstOrNull() ?: full

// ─── Goal block (used by Goals & Milestones card) ──────────────────────
//
// One goal: title + category eyebrow, % progress bar, then its inline
// milestone checklist. Completed milestones get a filled circle + ✓; the
// rest are an outlined dot. Always render up to 4 milestones so the user
// sees what's next, not just what's done.

@Composable
private fun K1GoalBlock(goal: io.github.fletchmckee.liquid.samples.app.data.source.remote.GoalDto) {
  Column {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Column(Modifier.weight(1f)) {
        if (goal.category.isNotBlank()) {
          K1Eyebrow(goal.category.uppercase())
          Spacer(Modifier.height(2.dp))
        }
        Text(goal.goal, style = K1Type.bodyMd)
      }
      val pct = (goal.currentProgress * 100).toInt().coerceIn(0, 100)
      Text("$pct%", style = K1Type.bodyMd)
    }
    Spacer(Modifier.height(8.dp))
    Box(
      Modifier
        .fillMaxWidth()
        .height(3.dp)
        .clip(K1R.pill)
        .background(KlikLineHairline),
    ) {
      Box(
        Modifier
          .fillMaxHeight()
          .fillMaxWidth(goal.currentProgress.coerceIn(0f, 1f))
          .background(KlikInkPrimary),
      )
    }
    if (goal.targetEndDate.isNotBlank()) {
      Spacer(Modifier.height(4.dp))
      Text("Target · ${goal.targetEndDate}", style = K1Type.metaSm)
    }

    if (goal.milestones.isNotEmpty()) {
      Spacer(Modifier.height(K1Sp.m))
      val sorted = goal.milestones.sortedBy { it.sequenceOrder }
      sorted.take(4).forEach { m -> K1MilestoneRow(m) }
      if (sorted.size > 4) {
        Spacer(Modifier.height(4.dp))
        Text("+${sorted.size - 4} more", style = K1Type.metaSm)
      }
    }
  }
}

@Composable
private fun K1MilestoneRow(m: io.github.fletchmckee.liquid.samples.app.data.source.remote.GoalMilestoneDto) {
  val done = m.status.equals("completed", ignoreCase = true) ||
    m.status.equals("done", ignoreCase = true)
  Row(
    Modifier.fillMaxWidth().padding(vertical = 5.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      Modifier.size(16.dp).clip(CircleShape)
        .background(if (done) KlikInkPrimary else KlikLineHairline),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        if (done) "✓" else "·",
        style = K1Type.metaSm.copy(
          color = if (done) KlikPaperCard else KlikInkTertiary,
          fontWeight = FontWeight.Medium,
        ),
      )
    }
    Spacer(Modifier.width(K1Sp.m))
    Column(Modifier.weight(1f)) {
      Text(
        m.title,
        style = K1Type.bodySm.copy(
          color = if (done) KlikInkPrimary else KlikInkTertiary,
        ),
      )
      if (m.targetDate.isNotBlank()) {
        Spacer(Modifier.height(2.dp))
        Text(m.targetDate, style = K1Type.metaSm)
      }
    }
  }
}
