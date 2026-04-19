package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import io.github.fletchmckee.liquid.samples.app.ui.components.AiGeneratedBadge
import io.github.fletchmckee.liquid.samples.app.ui.components.AiContentDisclaimer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateDpAsState
import kotlinx.datetime.Clock
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.core.rememberViewModel
import io.github.fletchmckee.liquid.samples.app.platform.OAuthBrowser
import io.github.fletchmckee.liquid.samples.app.domain.entity.Achievements
import io.github.fletchmckee.liquid.samples.app.domain.entity.DimensionScore
import io.github.fletchmckee.liquid.samples.app.domain.entity.InfluenceTier
import io.github.fletchmckee.liquid.samples.app.domain.entity.RelationshipStatus
import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.domain.entity.ProjectStatus
import io.github.fletchmckee.liquid.samples.app.presentation.worklife.WorkLifeViewModel
import io.github.fletchmckee.liquid.samples.app.theme.EngageColor
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import io.github.fletchmckee.liquid.samples.app.theme.LiveColor
import io.github.fletchmckee.liquid.samples.app.theme.LocalInsightCardColors
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings
import io.github.fletchmckee.liquid.samples.app.theme.StriveColor
import io.github.fletchmckee.liquid.samples.app.ui.components.FeedbackPopup
import io.github.fletchmckee.liquid.samples.app.ui.icons.CustomIcons
import io.github.fletchmckee.liquid.samples.app.ui.icons.PushPin
import io.github.fletchmckee.liquid.samples.app.ui.icons.Archive
import io.github.fletchmckee.liquid.samples.app.utils.onLongPress
import io.github.fletchmckee.liquid.samples.app.data.source.remote.EncourageData
import io.github.fletchmckee.liquid.samples.app.ui.components.FeedbackPopup
import io.github.fletchmckee.liquid.samples.app.ui.components.FeedbackData
import io.github.fletchmckee.liquid.samples.app.data.network.EntityFeedbackClient
import androidx.compose.ui.text.input.KeyboardType
import io.github.fletchmckee.liquid.samples.app.data.source.remote.WorklifeData
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import io.github.fletchmckee.liquid.samples.app.ui.components.LiquidPullToRefreshIndicator
import io.github.fletchmckee.liquid.samples.app.model.archivedProjectIdsState
import io.github.fletchmckee.liquid.samples.app.model.archivedPersonIdsState
import io.github.fletchmckee.liquid.samples.app.model.archivedOrganizationIdsState
import io.github.fletchmckee.liquid.samples.app.model.pinnedProjectIdsState
import io.github.fletchmckee.liquid.samples.app.model.pinnedPersonIdsState
import io.github.fletchmckee.liquid.samples.app.model.pinnedOrganizationIdsState
import io.github.fletchmckee.liquid.samples.app.model.archiveProject
import io.github.fletchmckee.liquid.samples.app.model.archivePerson
import io.github.fletchmckee.liquid.samples.app.model.archiveOrganization
import io.github.fletchmckee.liquid.samples.app.model.pinProject
import io.github.fletchmckee.liquid.samples.app.model.pinPerson
import io.github.fletchmckee.liquid.samples.app.model.pinOrganization
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.TracedSegment
import io.github.fletchmckee.liquid.samples.app.ui.components.AvatarImage
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityHighlightedText
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityType
import io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation
import io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentsSection
import io.github.fletchmckee.liquid.samples.app.ui.components.ShimmerTextBlock
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import liquid_root.samples.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Insights screen that displays people, projects, and organizations.
 * Redesigned with horizontal carousels and visual dimension displays.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkLifeScreen(
    viewModel: WorkLifeViewModel = rememberViewModel { WorkLifeViewModel() },
    isLoading: Boolean = false,
    isLlmDataLoading: Boolean = false,
    encourageData: EncourageData? = null,
    worklifeData: WorklifeData? = null,
    goalsData: io.github.fletchmckee.liquid.samples.app.data.source.remote.GoalListResponse? = null,
    userLevelData: io.github.fletchmckee.liquid.samples.app.data.source.remote.UserLevelData? = null,
    onArchiveProject: (String) -> Unit = {},
    onArchivePerson: (String) -> Unit = {},
    onArchiveOrganization: (String) -> Unit = {},
    // Entity data for highlighting
    tasks: List<TaskMetadata> = emptyList(),
    meetings: List<Meeting> = emptyList(),
    onEntityClick: (EntityNavigationData) -> Unit = {},
    onSegmentClick: (TracedSegmentNavigation) -> Unit = {},
    // Entity highlight/scroll parameters
    highlightProjectId: String? = null,
    highlightPersonId: String? = null,
    highlightOrganizationId: String? = null,
    onEntityHighlighted: () -> Unit = {}, // Called after highlight is processed
    onRefresh: () -> Unit = {},
    onGrowthTreeClick: () -> Unit = {},
    subscriptionFeatures: io.github.fletchmckee.liquid.samples.app.domain.entity.SubscriptionFeatures? = null,
    onUpgradeRequired: (String) -> Unit = {}
) {
    val liquidState = rememberLiquidState()
    val uiState by viewModel.state.collectAsState()
    // achievements usage removed
    val coroutineScope = rememberCoroutineScope()

    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    // Clear refreshing when data changes
    LaunchedEffect(uiState.projects, uiState.people, uiState.organizations, encourageData, worklifeData) {
        if (isRefreshing) {
            isRefreshing = false
        }
    }

    // Shared expanded state - only one category can be expanded at a time
    // null = none, "projects", "people", "organizations"
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    // LazyListState for scroll-to-top functionality
    val listState = rememberLazyListState()

    // Use global archive/pin state from Models
    val archivedProjectIds by archivedProjectIdsState
    val archivedPersonIds by archivedPersonIdsState
    val archivedOrganizationIds by archivedOrganizationIdsState
    val pinnedProjects by pinnedProjectIdsState
    val pinnedPeople by pinnedPersonIdsState
    val pinnedOrganizations by pinnedOrganizationIdsState

    // Initialize global pinned state with pre-pinned items (only once per item)
    LaunchedEffect(uiState.projects, uiState.people, uiState.organizations) {
        val now = Clock.System.now().toEpochMilliseconds()
        // Projects
        val currentPinnedProjects = pinnedProjectIdsState.value.toMutableMap()
        uiState.projects.filter { it.isPinned && !currentPinnedProjects.containsKey(it.id) }.forEachIndexed { idx, item ->
            currentPinnedProjects[item.id] = now - idx * 1000
        }
        if (currentPinnedProjects != pinnedProjectIdsState.value) {
            pinnedProjectIdsState.value = currentPinnedProjects
        }
        // People
        val currentPinnedPeople = pinnedPersonIdsState.value.toMutableMap()
        uiState.people.filter { it.isPinned && !currentPinnedPeople.containsKey(it.id) }.forEachIndexed { idx, item ->
            currentPinnedPeople[item.id] = now - idx * 1000
        }
        if (currentPinnedPeople != pinnedPersonIdsState.value) {
            pinnedPersonIdsState.value = currentPinnedPeople
        }
        // Organizations
        val currentPinnedOrgs = pinnedOrganizationIdsState.value.toMutableMap()
        uiState.organizations.filter { it.isPinned && !currentPinnedOrgs.containsKey(it.id) }.forEachIndexed { idx, item ->
            currentPinnedOrgs[item.id] = now - idx * 1000
        }
        if (currentPinnedOrgs != pinnedOrganizationIdsState.value) {
            pinnedOrganizationIdsState.value = currentPinnedOrgs
        }
    }

    // Track highlighted entity for visual feedback
    var highlightedEntityId by remember { mutableStateOf<String?>(null) }

    // Handle entity highlight navigation - scroll to section and expand
    LaunchedEffect(highlightProjectId) {
        if (highlightProjectId != null) {
            KlikLogger.i("WorkLifeScreen", "Navigating to project: $highlightProjectId")

            // Clear any existing highlight immediately to prevent race conditions
            highlightedEntityId = null

            // First expand the category (this triggers recomposition)
            expandedCategory = "projects"

            // Wait for expansion animation to complete
            delay(300)

            // Then scroll to Projects section (index 2: after HighlightCard and GoalsCard)
            KlikLogger.d("WorkLifeScreen", "Scrolling to Projects section (index 2)")
            listState.animateScrollToItem(2)
            delay(200)  // Wait for scroll to complete

            // Set highlight AFTER scroll and expansion
            highlightedEntityId = highlightProjectId
            KlikLogger.d("WorkLifeScreen", "Highlighting project: $highlightProjectId")

            onEntityHighlighted()

            // Clear highlight after 3 seconds
            delay(3000)
            highlightedEntityId = null
            KlikLogger.d("WorkLifeScreen", "Project highlight cleared")
        }
    }

    LaunchedEffect(highlightPersonId) {
        if (highlightPersonId != null) {
            KlikLogger.i("WorkLifeScreen", "Navigating to person: $highlightPersonId")

            // Clear any existing highlight immediately to prevent race conditions
            highlightedEntityId = null

            // First expand the category
            expandedCategory = "people"
            delay(300)

            // Then scroll to People section (index 3)
            KlikLogger.d("WorkLifeScreen", "Scrolling to People section (index 3)")
            listState.animateScrollToItem(3)
            delay(200)

            highlightedEntityId = highlightPersonId
            KlikLogger.d("WorkLifeScreen", "Highlighting person: $highlightPersonId")

            onEntityHighlighted()
            delay(3000)
            highlightedEntityId = null
            KlikLogger.d("WorkLifeScreen", "Person highlight cleared")
        }
    }

    LaunchedEffect(highlightOrganizationId) {
        if (highlightOrganizationId != null) {
            KlikLogger.i("WorkLifeScreen", "Navigating to organization: $highlightOrganizationId")

            // Clear any existing highlight immediately to prevent race conditions
            highlightedEntityId = null

            // First expand the category
            expandedCategory = "organizations"
            delay(300)

            // Then scroll to Organizations section (index 4)
            KlikLogger.d("WorkLifeScreen", "Scrolling to Organizations section (index 4)")
            listState.animateScrollToItem(4)
            delay(200)

            highlightedEntityId = highlightOrganizationId
            KlikLogger.d("WorkLifeScreen", "Highlighting organization: $highlightOrganizationId")

            onEntityHighlighted()
            delay(3000)
            highlightedEntityId = null
            KlikLogger.d("WorkLifeScreen", "Organization highlight cleared")
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        state = pullRefreshState,
        onRefresh = {
            isRefreshing = true
            onRefresh()
        },
        modifier = Modifier.fillMaxSize(),
        indicator = {
            LiquidPullToRefreshIndicator(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {
        // Loading indicator
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = KlikPrimary
            )
        }

        // Error display with retry
        uiState.error?.let { error ->
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 160.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = error,
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.worklife_tap_to_retry),
                    color = KlikPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        viewModel.clearError()
                        viewModel.refresh()
                    }
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 68.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    HighlightEncouragementCard(
                       liquidState = liquidState,
                       title = stringResource(Res.string.worklife_highlights),
                       encourageMessage = encourageData?.message,
                       insights = worklifeData?.insights,
                       isLoading = isLoading,
                       isLlmDataLoading = isLlmDataLoading,
                       primaryColor = KlikPrimary,
                       // Traced segments from both encourage and worklife
                       encourageTracedSegments = encourageData?.tracedSegments,
                       worklifeTracedSegments = worklifeData?.tracedSegments,
                       // Entity data for highlighting
                       tasks = tasks,
                       meetings = meetings,
                       projects = uiState.projects,
                       people = uiState.people,
                       organizations = uiState.organizations,
                       onEntityClick = onEntityClick,
                       onSegmentClick = onSegmentClick
                    )
                }
            }

            item {
                val goalsEnabled = subscriptionFeatures?.goalsEnabled ?: true
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    GoalsandProgressCard(
                        liquidState = liquidState,
                        primaryColor = KlikPrimary,
                        goalsData = if (goalsEnabled) goalsData else null,
                        userLevelData = userLevelData,
                        isLoading = isLoading,
                        onGrowthTreeClick = {
                            if (goalsEnabled) onGrowthTreeClick()
                            else onUpgradeRequired("Goals & Progress")
                        },
                        isLocked = !goalsEnabled
                    )
                }
            }

            // Projects Section (Stacked) - Always show, with loading/empty state
            item {
                val sortedProjects = uiState.projects
                    .filter { it.id !in archivedProjectIds }
                    .sortedByDescending { pinnedProjects[it.id] ?: 0L }
                EntityStackSection(
                    liquidState = liquidState,
                    title = stringResource(Res.string.worklife_projects),
                    subtitle = when {
                        isLoading || uiState.isLoading -> stringResource(Res.string.loading)
                        sortedProjects.isNotEmpty() -> "${sortedProjects.size} active"
                        else -> "(none)"
                    },
                    color = LiveColor,
                    icon = Icons.Filled.PlayArrow
                ) {
                    when {
                        isLoading || uiState.isLoading -> {
                            LoadingEntityCard(
                                liquidState = liquidState,
                                color = LiveColor,
                                delayMillis = 0
                            )
                        }
                        sortedProjects.isNotEmpty() -> {
                            ProjectStack(
                                liquidState = liquidState,
                                projects = sortedProjects,
                                pinnedId = sortedProjects.firstOrNull()?.let {
                                    if (pinnedProjects[it.id] != null) it.id else null
                                },
                                highlightedId = highlightedEntityId,
                                expanded = expandedCategory == "projects",
                                onExpandedChange = { isExpanded ->
                                    expandedCategory = if (isExpanded) "projects" else null
                                },
                                onPin = { id ->
                                    pinProject(id)
                                },
                                onArchive = { id ->
                                    KlikLogger.i("WorkLifeScreen", "Archive project: $id")
                                    archiveProject(id)
                                },
                                onRefreshProjects = {
                                    viewModel.refreshProjects()
                                }
                            )
                        }
                        else -> {
                            EmptyEntityCard(
                                liquidState = liquidState,
                                entityType = "Projects",
                                message = stringResource(Res.string.worklife_no_projects),
                                color = LiveColor,
                                onRefresh = { viewModel.refreshProjects() }
                            )
                        }
                    }
                }
            }

            // People Section (Stacked) - Always show, with loading/empty state
            item {
                val sortedPeople = uiState.people
                    .filter { it.id !in archivedPersonIds }
                    .sortedByDescending { pinnedPeople[it.id] ?: 0L }
                EntityStackSection(
                    liquidState = liquidState,
                    title = stringResource(Res.string.worklife_people),
                    subtitle = when {
                        isLoading || uiState.isLoading -> stringResource(Res.string.loading)
                        sortedPeople.isNotEmpty() -> "${sortedPeople.size} connections"
                        else -> "(none)"
                    },
                    color = StriveColor,
                    icon = Icons.Filled.Person
                ) {
                    when {
                        isLoading || uiState.isLoading -> {
                            LoadingEntityCard(
                                liquidState = liquidState,
                                color = StriveColor,
                                delayMillis = 100
                            )
                        }
                        sortedPeople.isNotEmpty() -> {
                            PersonStack(
                                liquidState = liquidState,
                                people = sortedPeople,
                                pinnedId = sortedPeople.firstOrNull()?.let {
                                    if (pinnedPeople[it.id] != null) it.id else null
                                },
                                highlightedId = highlightedEntityId,
                                expanded = expandedCategory == "people",
                                onExpandedChange = { isExpanded ->
                                    expandedCategory = if (isExpanded) "people" else null
                                },
                                onPin = { id ->
                                    pinPerson(id)
                                },
                                onArchive = { id ->
                                    KlikLogger.i("WorkLifeScreen", "Archive person: $id")
                                    archivePerson(id)
                                },
                                onRefreshPeople = {
                                    viewModel.refreshPeople()
                                }
                            )
                        }
                        else -> {
                            EmptyEntityCard(
                                liquidState = liquidState,
                                entityType = "People",
                                message = stringResource(Res.string.worklife_no_people),
                                color = StriveColor,
                                onRefresh = { viewModel.refreshPeople() }
                            )
                        }
                    }
                }
            }

            // Organizations Section (Stacked) - Always show, with loading/empty state
            item {
                val sortedOrgs = uiState.organizations
                    .filter { it.id !in archivedOrganizationIds }
                    .sortedByDescending { pinnedOrganizations[it.id] ?: 0L }
                EntityStackSection(
                    liquidState = liquidState,
                    title = stringResource(Res.string.worklife_organizations),
                    subtitle = when {
                        isLoading || uiState.isLoading -> stringResource(Res.string.loading)
                        sortedOrgs.isNotEmpty() -> "${sortedOrgs.size} companies"
                        else -> "(none)"
                    },
                    color = EngageColor,
                    icon = Icons.Filled.Home
                ) {
                    when {
                        isLoading || uiState.isLoading -> {
                            LoadingEntityCard(
                                liquidState = liquidState,
                                color = EngageColor,
                                delayMillis = 200
                            )
                        }
                        sortedOrgs.isNotEmpty() -> {
                            OrganizationStack(
                                liquidState = liquidState,
                                organizations = sortedOrgs,
                                pinnedId = sortedOrgs.firstOrNull()?.let {
                                    if (pinnedOrganizations[it.id] != null) it.id else null
                                },
                                highlightedId = highlightedEntityId,
                                expanded = expandedCategory == "organizations",
                                onExpandedChange = { isExpanded ->
                                    expandedCategory = if (isExpanded) "organizations" else null
                                },
                                onPin = { id ->
                                    pinOrganization(id)
                                },
                                onArchive = { id ->
                                    KlikLogger.i("WorkLifeScreen", "Archive organization: $id")
                                    archiveOrganization(id)
                                },
                                onRefresh = { viewModel.refreshOrganizations() }
                            )
                        }
                        else -> {
                            EmptyEntityCard(
                                liquidState = liquidState,
                                entityType = "Organizations",
                                message = stringResource(Res.string.worklife_no_organizations),
                                color = EngageColor,
                                onRefresh = { viewModel.refreshOrganizations() }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section container for entity stacks with header (vertical layout)
 */
@Composable
fun EntityStackSection(
    liquidState: LiquidState,
    title: String,
    subtitle: String,
    color: Color,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header with icon and count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier
                        .size(width = 4.dp, height = 24.dp)
                        .liquid(liquidState) {
                            edge = 0.01f
                            shape = RoundedCornerShape(2.dp)
                            tint = color
                        }
                ) {}
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Stacked content
        content()
    }
}

/**
 * Empty state card for entity sections when no data is available
 */
@Composable
fun EmptyEntityCard(
    liquidState: LiquidState,
    entityType: String,
    message: String,
    color: Color,
    onRefresh: (() -> Unit)? = null
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                cardShape
            )
            .border(
                BorderStroke(0.5.dp, color.copy(alpha = 0.2f)),
                cardShape
            )
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                tint = color.copy(alpha = 0.02f)
            }
            .clip(cardShape)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        if (onRefresh != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Refresh",
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onRefresh() }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

/**
 * Loading card for entities - shows pulsing animation while data is being fetched.
 */
@Composable
fun LoadingEntityCard(
    liquidState: LiquidState,
    color: Color,
    delayMillis: Int = 0
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, delayMillis = delayMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                cardShape
            )
            .border(
                BorderStroke(0.5.dp, color.copy(alpha = 0.2f)),
                cardShape
            )
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                tint = color.copy(alpha = 0.02f)
            }
            .clip(cardShape)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(20.dp)
                .background(color.copy(alpha = alpha * 0.3f), RoundedCornerShape(4.dp))
        )
        // Subtitle placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(14.dp)
                .background(Color.Gray.copy(alpha = alpha * 0.2f), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Content placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(12.dp)
                .background(Color.Gray.copy(alpha = alpha * 0.15f), RoundedCornerShape(4.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(12.dp)
                .background(Color.Gray.copy(alpha = alpha * 0.15f), RoundedCornerShape(4.dp))
        )
    }
}

/**
 * Swipable wrapper for entity cards - handles pin/archive swipe gestures
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipableEntityCardWrapper(
    isSwipeable: Boolean,
    entityId: String,
    entityType: String,
    color: Color,
    liquidState: LiquidState,
    onPinToggle: (String) -> Unit,
    onArchive: (String) -> Unit,
    content: @Composable () -> Unit
) {
    if (!isSwipeable) {
        Box(modifier = Modifier.fillMaxWidth()) {
            content()
        }
        return
    }

    // Guard against duplicate archive calls during swipe animation settling
    var hasArchived by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onPinToggle(entityId)
                    false // Don't dismiss, just toggle pin
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Only call onArchive once per instance
                    if (!hasArchived) {
                        hasArchived = true
                        onArchive(entityId)
                    }
                    true // Dismiss on archive
                }
                else -> false
            }
        }
    )

    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    SwipeToDismissBox(
        modifier = Modifier.fillMaxWidth(),
        state = dismissState,
        backgroundContent = {
            val (bgColor, icon) = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Pair(Color(0xFFFFCC00), CustomIcons.PushPin)
                SwipeToDismissBoxValue.EndToStart -> Pair(Color(0xFFFF3B30), CustomIcons.Archive)
                else -> Pair(Color.Transparent, CustomIcons.PushPin)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .liquid(liquidState) {
                        edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                        shape = cardShape
                        if (glassSettings.applyToCards) {
                            frost = glassSettings.frost
                            curve = glassSettings.curve
                            refraction = glassSettings.refraction
                        }
                        tint = bgColor.copy(alpha = 0.2f)
                    },
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                    Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Icon(icon, null, tint = bgColor, modifier = Modifier.size(24.dp))
                }
            }
        },
        content = {
            Box(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    )
}

/**
 * Project stack with stacking animation and swipe functionality
 * First project is at surface (pinned = surfaced, same concept)
 * Swipe to pin surfaces that item to top
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProjectStack(
    liquidState: LiquidState,
    projects: List<Project>,
    pinnedId: String?,  // The surfaced/pinned item ID (null = first item is default surface)
    highlightedId: String? = null,  // Entity to visually highlight (from navigation)
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    onPin: (String) -> Unit = {},
    onArchive: (String) -> Unit = {},
    onRefreshProjects: () -> Unit = {}
) {
    if (projects.isEmpty()) return

    val surfacedProject = projects.first()
    val stackedProjects = projects.drop(1)
    val isSurfacePinned = pinnedId == surfacedProject.id
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)
    val expandedStackShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
    val projectListState = rememberLazyListState()

    // Stackable if there are other projects
    val isStackable = stackedProjects.isNotEmpty()

    // Scroll to highlighted project card when highlightedId changes
    LaunchedEffect(highlightedId, expanded) {
        if (highlightedId != null && expanded && stackedProjects.isNotEmpty()) {
            // Wait for expansion animation to complete
            delay(400)

            // CRITICAL: EntityListWithIndex sorts alphabetically AND adds headers!
            // We need to calculate the index in the flat list structure

            // 1. Sort stacked projects alphabetically (matching EntityListWithIndex)
            val sortedProjects = stackedProjects.sortedBy { it.name.uppercase() }

            // 2. Group by first letter
            val groupedProjects = sortedProjects.groupBy {
                it.name.uppercase().firstOrNull() ?: '#'
            }

            // 3. Build flat list (same as EntityListWithIndex)
            val flatItems = buildList {
                groupedProjects.keys.sorted().forEach { letter ->
                    add(letter to null) // Header
                    groupedProjects[letter]?.forEach { project ->
                        add(letter to project)
                    }
                }
            }

            // 4. Find index in flat list
            val flatIndex = flatItems.indexOfFirst { (_, project) ->
                project?.id == highlightedId
            }

            if (flatIndex >= 0) {
                KlikLogger.d("ProjectStack", "Scrolling to project at flatIndex $flatIndex (id=$highlightedId)")

                try {
                    projectListState.animateScrollToItem(
                        index = flatIndex,
                        scrollOffset = -100  // Show some context
                    )
                    delay(200)
                    KlikLogger.i("ProjectStack", "Scrolled to project card")
                } catch (e: Exception) {
                    KlikLogger.e("ProjectStack", "Scroll failed: ${e.message}", e)
                }
            } else {
                KlikLogger.w("ProjectStack", "Project $highlightedId not found in list (${stackedProjects.size} stacked projects)")
            }
        }
    }

    // Animation Values (Deck visual)
    val dummyAlpha by animateFloatAsState(targetValue = if (expanded) 0f else 1f)
    val dummyScaleBottom by animateFloatAsState(targetValue = if (expanded) 1f else 0.92f)
    val dummyOffsetBottom by animateDpAsState(targetValue = if (expanded) 0.dp else 16.dp)
    val dummyScaleMiddle by animateFloatAsState(targetValue = if (expanded) 1f else 0.96f)
    val dummyOffsetMiddle by animateDpAsState(targetValue = if (expanded) 0.dp else 8.dp)

    // Outer Swipe Wrapper: Disabled - swiping only allowed in expanded state
    SwipableEntityCardWrapper(
        isSwipeable = false,
        entityId = surfacedProject.id,
        entityType = "Project",
        color = LiveColor,
        liquidState = liquidState,
        onPinToggle = onPin,
        onArchive = onArchive
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isStackable && !expanded) dummyOffsetBottom else 0.dp)
        ) {
            // --- Dummy Deck Cards (only if stackable) ---
            if (isStackable) {
                if (!expanded || dummyAlpha > 0f) {
                    Box(modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer { scaleX = dummyScaleBottom; translationY = dummyOffsetBottom.toPx(); alpha = dummyAlpha * 0.9f }
                        .background(Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.9f), cardShape)
                        .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
                        .liquid(liquidState) {
                            edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                            shape = cardShape
                            if (glassSettings.applyToCards) {
                                frost = glassSettings.frost
                                curve = glassSettings.curve
                                refraction = glassSettings.refraction
                            }
                            tint = Color.Black.copy(alpha = 0.05f)
                        }
                    )
                }
                if (!expanded || dummyAlpha > 0f) {
                    Box(modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer { scaleX = dummyScaleMiddle; translationY = dummyOffsetMiddle.toPx(); alpha = dummyAlpha * 0.95f }
                        .background(Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.9f), cardShape)
                        .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
                        .liquid(liquidState) {
                            edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                            shape = cardShape
                            if (glassSettings.applyToCards) {
                                frost = glassSettings.frost
                                curve = glassSettings.curve
                                refraction = glassSettings.refraction
                            }
                            tint = Color.Black.copy(alpha = 0.03f)
                        }
                    )
                }
            }

            // --- Main Content Column ---
            Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                // 1. The Surfaced Card (pinned = at surface)
                key(surfacedProject.id) {
                    Box(modifier = Modifier.zIndex(1f)) {
                        SwipableEntityCardWrapper(
                        isSwipeable = (!isStackable || expanded),
                        entityId = surfacedProject.id,
                        entityType = "Project",
                        color = LiveColor,
                        liquidState = liquidState,
                        onPinToggle = onPin,
                        onArchive = onArchive
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ProjectCardContent(
                                liquidState = liquidState,
                                project = surfacedProject,
                                isFeatured = true,
                                isPinned = isSurfacePinned,
                                isHighlighted = highlightedId == surfacedProject.id,
                                onClick = if (isStackable) { { onExpandedChange(!expanded) } } else { {} },
                                onRefresh = onRefreshProjects
                            )
                        }
                    }
                    }
                }

                // 2. The Expanded Stack (other projects) with alphabetical index
                // Project cards: Header + Budget/Lead + 3 Dimensions + Risks + Goals + Related People + Related Projects ≈ 540dp
                if (expanded && isStackable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 560.dp)
                            .offset(y = (-24).dp)
                            .clip(expandedStackShape)
                    ) {
                        EntityListWithIndex(
                            items = stackedProjects,
                            getName = { it.name },
                            getId = { it.id },
                            listState = projectListState,
                            activeColor = LiveColor,
                            modifier = Modifier.fillMaxWidth()
                        ) { project ->
                            SwipableEntityCardWrapper(
                                isSwipeable = true,
                                entityId = project.id,
                                entityType = "Project",
                                color = LiveColor,
                                liquidState = liquidState,
                                onPinToggle = onPin,
                                onArchive = onArchive
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    ProjectCardContent(
                                        liquidState = liquidState,
                                        project = project,
                                        isFeatured = false,
                                        isPinned = false,
                                        isHighlighted = highlightedId == project.id,
                                        onClick = { },
                                        onRefresh = onRefreshProjects
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Simple related item card for expanded view
 */
@Composable
fun RelatedItemCard(
    liquidState: LiquidState,
    title: String,
    subtitle: String,
    color: Color
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f), cardShape)
            .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                if (glassSettings.applyToCards) {
                    frost = glassSettings.frost
                    curve = glassSettings.curve
                    refraction = glassSettings.refraction
                }
                tint = Color.Transparent
            }
            .clip(cardShape)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    title.take(1).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = KlikBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * Person stack with stacking animation and swipe functionality
 * First person is at surface (pinned = surfaced, same concept)
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PersonStack(
    liquidState: LiquidState,
    people: List<Person>,
    pinnedId: String?,
    highlightedId: String? = null,  // Entity to visually highlight (from navigation)
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    onPin: (String) -> Unit = {},
    onArchive: (String) -> Unit = {},
    onRefreshPeople: () -> Unit = {}
) {
    if (people.isEmpty()) return

    val surfacedPerson = people.first()
    val stackedPeople = people.drop(1)
    val isSurfacePinned = pinnedId == surfacedPerson.id
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)
    val expandedStackShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
    val peopleListState = rememberLazyListState()

    val isStackable = stackedPeople.isNotEmpty()

    // Scroll to highlighted person card when highlightedId changes
    LaunchedEffect(highlightedId, expanded) {
        if (highlightedId != null && expanded && stackedPeople.isNotEmpty()) {
            delay(400)  // Wait for expansion animation

            // CRITICAL: EntityListWithIndex sorts alphabetically AND adds headers!
            // Calculate index in flat list structure

            // 1. Sort stacked people alphabetically
            val sortedPeople = stackedPeople.sortedBy { it.name.uppercase() }

            // 2. Group by first letter
            val groupedPeople = sortedPeople.groupBy {
                it.name.uppercase().firstOrNull() ?: '#'
            }

            // 3. Build flat list (headers + items)
            val flatItems = buildList {
                groupedPeople.keys.sorted().forEach { letter ->
                    add(letter to null)  // Header
                    groupedPeople[letter]?.forEach { person ->
                        add(letter to person)
                    }
                }
            }

            // 4. Find index in flat list
            val flatIndex = flatItems.indexOfFirst { (_, person) ->
                person?.id == highlightedId || person?.name == highlightedId
            }

            if (flatIndex >= 0) {
                KlikLogger.d("PersonStack", "Scrolling to person at flatIndex $flatIndex (id=$highlightedId)")

                try {
                    peopleListState.animateScrollToItem(
                        index = flatIndex,
                        scrollOffset = -100
                    )
                    delay(200)
                    KlikLogger.i("PersonStack", "Scrolled to person card")
                } catch (e: Exception) {
                    KlikLogger.e("PersonStack", "Scroll failed: ${e.message}", e)
                }
            } else {
                KlikLogger.w("PersonStack", "Person $highlightedId not found in list (${stackedPeople.size} stacked people)")
            }
        }
    }

    val dummyAlpha by animateFloatAsState(targetValue = if (expanded) 0f else 1f)
    val dummyScaleBottom by animateFloatAsState(targetValue = if (expanded) 1f else 0.92f)
    val dummyOffsetBottom by animateDpAsState(targetValue = if (expanded) 0.dp else 16.dp)
    val dummyScaleMiddle by animateFloatAsState(targetValue = if (expanded) 1f else 0.96f)
    val dummyOffsetMiddle by animateDpAsState(targetValue = if (expanded) 0.dp else 8.dp)

    // Outer Swipe Wrapper: Disabled - swiping only allowed in expanded state
    SwipableEntityCardWrapper(
        isSwipeable = false,
        entityId = surfacedPerson.id,
        entityType = "Person",
        color = StriveColor,
        liquidState = liquidState,
        onPinToggle = onPin,
        onArchive = onArchive
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isStackable && !expanded) dummyOffsetBottom else 0.dp)
        ) {
            if (isStackable) {
                if (!expanded || dummyAlpha > 0f) {
                    Box(modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer { scaleX = dummyScaleBottom; translationY = dummyOffsetBottom.toPx(); alpha = dummyAlpha * 0.9f }
                        .background(Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.9f), cardShape)
                        .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
                        .liquid(liquidState) {
                            edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                            shape = cardShape
                            tint = Color.Black.copy(alpha = 0.05f)
                        }
                    )
                }
                if (!expanded || dummyAlpha > 0f) {
                    Box(modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer { scaleX = dummyScaleMiddle; translationY = dummyOffsetMiddle.toPx(); alpha = dummyAlpha * 0.95f }
                        .background(Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.9f), cardShape)
                        .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
                        .liquid(liquidState) {
                            edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                            shape = cardShape
                            tint = Color.Black.copy(alpha = 0.03f)
                        }
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                // 1. The Surfaced Card
                key(surfacedPerson.id) {
                    Box(modifier = Modifier.zIndex(1f)) {
                        SwipableEntityCardWrapper(
                            isSwipeable = (!isStackable || expanded),
                            entityId = surfacedPerson.id,
                            entityType = "Person",
                            color = StriveColor,
                            liquidState = liquidState,
                            onPinToggle = onPin,
                            onArchive = onArchive
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                PersonCardContent(
                                    liquidState = liquidState,
                                    person = surfacedPerson,
                                    isFeatured = true,
                                    isPinned = isSurfacePinned,
                                    isHighlighted = highlightedId == surfacedPerson.id || highlightedId == surfacedPerson.name,
                                    onClick = if (isStackable) { { onExpandedChange(!expanded) } } else { {} },
                                    onRefresh = onRefreshPeople
                                )
                            }
                        }
                    }
                }

                // 2. The Expanded Stack (other people) with alphabetical index
                // Person cards: Header + Email + 3 Dimensions + Skills + Characteristics + Projects + Organizations ≈ 480dp
                if (expanded && isStackable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp)
                            .offset(y = (-24).dp)
                            .clip(expandedStackShape)
                    ) {
                        EntityListWithIndex(
                            items = stackedPeople,
                            getName = { it.name },
                            getId = { it.id },
                            listState = peopleListState,
                            activeColor = StriveColor,
                            modifier = Modifier.fillMaxWidth()
                        ) { person ->
                            SwipableEntityCardWrapper(
                                isSwipeable = true,
                                entityId = person.id,
                                entityType = "Person",
                                color = StriveColor,
                                liquidState = liquidState,
                                onPinToggle = onPin,
                                onArchive = onArchive
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    PersonCardContent(
                                        liquidState = liquidState,
                                        person = person,
                                        isFeatured = false,
                                        isPinned = false,
                                        isHighlighted = highlightedId == person.id || highlightedId == person.name,
                                        onClick = { },
                                        onRefresh = onRefreshPeople
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Organization stack with stacking animation and swipe functionality
 * First organization is at surface (pinned = surfaced, same concept)
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OrganizationStack(
    liquidState: LiquidState,
    organizations: List<Organization>,
    pinnedId: String?,
    highlightedId: String? = null,  // Entity to visually highlight (from navigation)
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    onPin: (String) -> Unit = {},
    onArchive: (String) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    if (organizations.isEmpty()) return

    val surfacedOrg = organizations.first()
    val stackedOrgs = organizations.drop(1)
    val isSurfacePinned = pinnedId == surfacedOrg.id
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)
    val expandedStackShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
    val orgListState = rememberLazyListState()

    val isStackable = stackedOrgs.isNotEmpty()

    // Scroll to highlighted organization card when highlightedId changes
    LaunchedEffect(highlightedId, expanded) {
        if (highlightedId != null && expanded && stackedOrgs.isNotEmpty()) {
            // If the highlighted org is the surfaced card (first in list), it's already
            // visible at the top — no need to scroll the stacked list.
            if (highlightedId == surfacedOrg.id || highlightedId == surfacedOrg.name) {
                KlikLogger.d("OrganizationStack", "Highlighted organization $highlightedId is the surfaced card, no scroll needed")
                return@LaunchedEffect
            }

            delay(400)  // Wait for expansion animation

            // CRITICAL: EntityListWithIndex sorts alphabetically AND adds letter headers!
            // We need to calculate the flat list index matching that structure.

            // Step 1: Sort organizations alphabetically (matching EntityListWithIndex behavior)
            val sortedOrgs = stackedOrgs.sortedBy { it.name.uppercase() }

            // Step 2: Group by first letter
            val groupedOrgs = sortedOrgs.groupBy {
                it.name.uppercase().firstOrNull() ?: '#'
            }

            // Step 3: Build flat list structure (header + items for each letter)
            val flatItems = buildList {
                groupedOrgs.keys.sorted().forEach { letter ->
                    add(letter to null)  // Header row
                    groupedOrgs[letter]?.forEach { org ->
                        add(letter to org)  // Organization row
                    }
                }
            }

            // Step 4: Find the target organization in the flat list
            val flatIndex = flatItems.indexOfFirst { (_, org) ->
                org?.id == highlightedId || org?.name == highlightedId
            }

            if (flatIndex >= 0) {
                KlikLogger.d("OrganizationStack", "Scrolling to organization at flat index $flatIndex (id=$highlightedId)")

                try {
                    orgListState.animateScrollToItem(
                        index = flatIndex,
                        scrollOffset = -100
                    )
                    delay(200)
                    KlikLogger.i("OrganizationStack", "Scrolled to organization card")
                } catch (e: Exception) {
                    KlikLogger.e("OrganizationStack", "Scroll failed: ${e.message}", e)
                }
            } else {
                KlikLogger.w("OrganizationStack", "Organization $highlightedId not found in flat list")
            }
        }
    }

    val dummyAlpha by animateFloatAsState(targetValue = if (expanded) 0f else 1f)
    val dummyScaleBottom by animateFloatAsState(targetValue = if (expanded) 1f else 0.92f)
    val dummyOffsetBottom by animateDpAsState(targetValue = if (expanded) 0.dp else 16.dp)
    val dummyScaleMiddle by animateFloatAsState(targetValue = if (expanded) 1f else 0.96f)
    val dummyOffsetMiddle by animateDpAsState(targetValue = if (expanded) 0.dp else 8.dp)

    // Outer Swipe Wrapper: Disabled - swiping only allowed in expanded state
    SwipableEntityCardWrapper(
        isSwipeable = false,
        entityId = surfacedOrg.id,
        entityType = "Organization",
        color = EngageColor,
        liquidState = liquidState,
        onPinToggle = onPin,
        onArchive = onArchive
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isStackable && !expanded) dummyOffsetBottom else 0.dp)
        ) {
            if (isStackable) {
                if (!expanded || dummyAlpha > 0f) {
                    Box(modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer { scaleX = dummyScaleBottom; translationY = dummyOffsetBottom.toPx(); alpha = dummyAlpha * 0.9f }
                        .background(Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.9f), cardShape)
                        .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
                        .liquid(liquidState) {
                            edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                            shape = cardShape
                            tint = Color.Black.copy(alpha = 0.05f)
                        }
                    )
                }
                if (!expanded || dummyAlpha > 0f) {
                    Box(modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer { scaleX = dummyScaleMiddle; translationY = dummyOffsetMiddle.toPx(); alpha = dummyAlpha * 0.95f }
                        .background(Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.9f), cardShape)
                        .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
                        .liquid(liquidState) {
                            edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                            shape = cardShape
                            tint = Color.Black.copy(alpha = 0.03f)
                        }
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                // 1. The Surfaced Card
                key(surfacedOrg.id) {
                    Box(modifier = Modifier.zIndex(1f)) {
                        SwipableEntityCardWrapper(
                            isSwipeable = (!isStackable || expanded),
                            entityId = surfacedOrg.id,
                            entityType = "Organization",
                            color = EngageColor,
                            liquidState = liquidState,
                            onPinToggle = onPin,
                            onArchive = onArchive
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                OrganizationCardContent(
                                    liquidState = liquidState,
                                    organization = surfacedOrg,
                                    isFeatured = true,
                                    isPinned = isSurfacePinned,
                                    isHighlighted = highlightedId == surfacedOrg.id || highlightedId == surfacedOrg.name,
                                    onClick = if (isStackable) { { onExpandedChange(!expanded) } } else { {} },
                                    onRefresh = onRefresh
                                )
                            }
                        }
                    }
                }

                // 2. The Expanded Stack (other organizations) with alphabetical index
                // Organization cards: Header + Strategic Focus/Next Action + 3 Dimensions + Departments + Strengths + Related People + Projects ≈ 520dp
                if (expanded && isStackable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 540.dp)
                            .offset(y = (-24).dp)
                            .clip(expandedStackShape)
                    ) {
                        EntityListWithIndex(
                            items = stackedOrgs,
                            getName = { it.name },
                            getId = { it.id },
                            listState = orgListState,
                            activeColor = EngageColor,
                            modifier = Modifier.fillMaxWidth()
                        ) { org ->
                            SwipableEntityCardWrapper(
                                isSwipeable = true,
                                entityId = org.id,
                                entityType = "Organization",
                                color = EngageColor,
                                liquidState = liquidState,
                                onPinToggle = onPin,
                                onArchive = onArchive
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    OrganizationCardContent(
                                        liquidState = liquidState,
                                        organization = org,
                                        isFeatured = false,
                                        isPinned = false,
                                        isHighlighted = highlightedId == org.id || highlightedId == org.name,
                                        onClick = { },
                                        onRefresh = onRefresh
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Project card content - Shows: Name, Stage, Budget, Lead Person
 * Dimensions: Clarity, Weather, Health
 * Related: Risks, Goals, Meetings, Projects
 * Design: Progress circle on left (matching People avatar size) + content on right
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectCardContent(
    liquidState: LiquidState,
    project: Project,
    isFeatured: Boolean = false,
    isPinned: Boolean = false,
    isHighlighted: Boolean = false,  // Visual highlight from entity navigation
    onClick: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)
    val coroutineScope = rememberCoroutineScope()

    // Entity feedback state (same pattern as Meetings feedback)
    var showFeedbackPopup by remember { mutableStateOf(false) }
    var feedbackTargetText by remember { mutableStateOf("") }
    var feedbackFieldKey by remember { mutableStateOf("") }
    var feedbackFieldLabel by remember { mutableStateOf("") }

    // Refresh animation state
    var isRefreshing by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Clear loading state when project data actually changes (dynamic)
    LaunchedEffect(project.name, project.budget, project.lead) {
        if (isRefreshing) {
            delay(300)
            isRefreshing = false
        }
    }

    // Safety timeout: Reset loading state after 10 seconds to prevent infinite spinner
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(10000)
            isRefreshing = false
            KlikLogger.w("WorkLifeScreen", "Refresh timeout reached - loading state reset")
        }
    }

    val onShowFeedback: (String, String, String) -> Unit = { text, fieldKey, fieldLabel ->
        feedbackTargetText = text
        feedbackFieldKey = fieldKey
        feedbackFieldLabel = fieldLabel
        showFeedbackPopup = true
    }

    // Highlight animation for navigation
    val highlightBorderWidth by animateDpAsState(
        targetValue = if (isHighlighted) 2.dp else 0.5.dp,
        animationSpec = tween(durationMillis = 300)
    )
    val highlightBorderAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0.12f,
        animationSpec = if (isHighlighted) infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ) else tween(300)
    )

    val cardBackgroundColor = Color.White

    val borderStroke = if (isHighlighted) {
        BorderStroke(highlightBorderWidth, LiveColor.copy(alpha = highlightBorderAlpha))
    } else {
        BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f))
    }

    // Animated progress value
    val progressPercent = (project.progress * 100).toInt()
    val animatedProgress by animateFloatAsState(
        targetValue = project.progress,
        animationSpec = tween(durationMillis = 800)
    )

    Box {
        Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                cardBackgroundColor.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                cardShape
            )
            .border(borderStroke, cardShape)
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.02f
                shape = cardShape
                tint = LiveColor.copy(alpha = if (isFeatured) 0.08f else 0.05f)
            }
            .clip(cardShape)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        // Header Row: Progress Circle + Name/Details + Pin indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress Circle (matching People avatar size)
            Box(
                modifier = Modifier.size(if (isFeatured) 48.dp else 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = if (isFeatured) 5.dp.toPx() else 4.dp.toPx()
                    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

                    // Background arc
                    drawArc(
                        color = LiveColor.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = stroke
                    )

                    // Progress arc
                    drawArc(
                        color = LiveColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = stroke
                    )
                }
                Text(
                    "${progressPercent}%",
                    style = if (isFeatured) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = LiveColor
                )
            }

            Spacer(Modifier.width(12.dp))

            // Title and metadata column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        project.canonicalName.ifEmpty { project.name },
                        style = if (isFeatured) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = KlikBlack.copy(alpha = if (isRefreshing) pulseAlpha else 1f),
                        modifier = Modifier
                            .onLongPress {
                                onShowFeedback(project.name, "name", "Project Name")
                            }
                            .then(
                                if (isRefreshing) {
                                    Modifier.background(
                                        KlikPrimary.copy(alpha = pulseAlpha * 0.1f),
                                        RoundedCornerShape(4.dp)
                                    )
                                } else Modifier
                            )
                    )
                }
                // Stage as subtitle - always show
                Text(
                    project.stage.ifEmpty { "(none)" },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (project.stage.isEmpty()) Color.Gray.copy(alpha = 0.6f) else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Status badge based on progress
            val statusColor = when {
                project.progress >= 0.8f -> Color(0xFF4CAF50) // Green for 80%+
                project.progress >= 0.5f -> LiveColor // Blue for 50-79%
                project.progress >= 0.25f -> Color(0xFFFF9800) // Orange for 25-49%
                else -> Color(0xFFE57373) // Red for <25%
            }
            val statusText = when {
                project.progress >= 0.8f -> stringResource(Res.string.worklife_on_track)
                project.progress >= 0.5f -> stringResource(Res.string.worklife_in_progress)
                project.progress >= 0.25f -> stringResource(Res.string.worklife_behind)
                else -> stringResource(Res.string.worklife_at_risk)
            }
            Text(
                statusText,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Budget & Lead row (compact)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (project.budget.isNotEmpty()) {
                Column {
                    Text(stringResource(Res.string.worklife_budget), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        project.budget,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = KlikBlack.copy(alpha = if (isRefreshing) pulseAlpha else 1f),
                        modifier = Modifier
                            .onLongPress {
                                onShowFeedback(project.budget, "budget", "Budget")
                            }
                            .then(
                                if (isRefreshing) {
                                    Modifier.background(
                                        LiveColor.copy(alpha = pulseAlpha * 0.1f),
                                        RoundedCornerShape(4.dp)
                                    )
                                } else Modifier
                            )
                            .padding(vertical = 2.dp)
                    )
                }
            }
            if (project.lead.isNotEmpty()) {
                Column {
                    Text(stringResource(Res.string.worklife_lead), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        project.lead,
                        style = MaterialTheme.typography.bodySmall,
                        color = StriveColor.copy(alpha = if (isRefreshing) pulseAlpha else 1f),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .onLongPress {
                                onShowFeedback(project.lead, "lead", "Lead")
                            }
                            .then(
                                if (isRefreshing) {
                                    Modifier.background(
                                        LiveColor.copy(alpha = pulseAlpha * 0.1f),
                                        RoundedCornerShape(4.dp)
                                    )
                                } else Modifier
                            )
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Dimensions section (Clarity, Weather, Health) - always show
        val projectDimensionNames = listOf("clarity", "weather", "health")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            projectDimensionNames.forEach { dimName ->
                val dim = project.dimensions.find { it.dimension.lowercase() == dimName }
                if (dim != null) {
                    DimensionScoreBox(dim, LiveColor, modifier = Modifier.weight(1f))
                } else {
                    EmptyDimensionBox(dimName, LiveColor, modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Risks - always show, even if empty
        Text(stringResource(Res.string.worklife_risks), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (project.risks.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                project.risks.take(3).forEach { risk ->
                    Text(
                        risk,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF555555),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(
                "(none)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Goals - always show, even if empty
        Text(stringResource(Res.string.worklife_goals), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (project.goals.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                project.goals.take(3).forEach { goal ->
                    Text(
                        goal,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF555555),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(
                "(none)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Related People - always show, even if empty (using relatedPeople field)
        Text(stringResource(Res.string.worklife_related_people), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (project.relatedPeople.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                project.relatedPeople.take(3).forEach { person ->
                    Text(
                        person,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF555555),
                        modifier = Modifier
                            .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(
                "(none)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Related Projects - always show, even if empty
        Text(stringResource(Res.string.worklife_related_projects), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (project.relatedProjects.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                project.relatedProjects.take(3).forEach { relatedProject ->
                    Text(
                        relatedProject,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF555555),
                        modifier = Modifier
                            .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(
                "(none)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Related Organizations - always show
        Text(stringResource(Res.string.worklife_related_organizations), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (project.relatedOrganizations.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                project.relatedOrganizations.take(3).forEach { org ->
                    Text(
                        org,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF555555),
                        modifier = Modifier
                            .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(
                "(none)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Related Meetings - always show
        Text(stringResource(Res.string.worklife_related_meetings), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (project.relatedMeetings.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                project.relatedMeetings.take(3).forEach { meeting ->
                    Text(
                        meeting,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF555555),
                        modifier = Modifier
                            .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(
                "(none)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        }

        // Entity Feedback Popup (same pattern as Meetings feedback)
        FeedbackPopup(
            isVisible = showFeedbackPopup,
            originalText = feedbackTargetText,
            onDismiss = { showFeedbackPopup = false },
            onSubmitFeedback = { feedback ->
                KlikLogger.d("EntityFeedback", "Feedback received: field=$feedbackFieldKey, originalText=${feedback.originalText}, correction=${feedback.correction}, isWrong=${feedback.isMarkedWrong}")
                showFeedbackPopup = false

                // Submit feedback to backend
                val hasCorrection = !feedback.correction.isNullOrBlank() && feedback.correction != feedback.originalText
                val isMarkedWrong = feedback.isMarkedWrong

                if (hasCorrection || isMarkedWrong) {
                    // Start loading animation immediately
                    isRefreshing = true

                    coroutineScope.launch {
                        try {
                            val correctedValue = if (hasCorrection) feedback.correction else feedback.originalText
                            val updates = mapOf(feedbackFieldKey to correctedValue)

                            val result = EntityFeedbackClient.updateEntity(
                                "project",
                                project.id,
                                updates
                            )

                            result.onSuccess {
                                KlikLogger.i("EntityFeedback", "Entity feedback success for project ${project.id}")
                                KlikLogger.d("EntityFeedback", "About to call onRefresh")
                                try {
                                    onRefresh()
                                    KlikLogger.i("EntityFeedback", "onRefresh() call completed successfully")
                                } catch (e: Exception) {
                                    KlikLogger.e("EntityFeedback", "Error calling onRefresh: ${e.message}", e)
                                    isRefreshing = false
                                }
                            }.onFailure { error ->
                                KlikLogger.e("EntityFeedback", "Entity feedback submission failed: ${error.message}")
                                isRefreshing = false
                            }
                        } catch (e: Exception) {
                            KlikLogger.e("EntityFeedback", "Entity feedback submission error: ${e.message}", e)
                            isRefreshing = false
                        }
                    }
                }
            },
            overlayShape = cardShape,
            modifier = Modifier.matchParentSize()
        )
    }
}

/**
 * Person card content - Shows: Name, Title, Email, Relationship, Influence Tier
 * Dimensions: Focus Orbit, Flow, Energy
 * Related: Skills, Characteristics, Projects, Meetings
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonCardContent(
    liquidState: LiquidState,
    person: Person,
    isFeatured: Boolean = false,
    isPinned: Boolean = false,
    isHighlighted: Boolean = false,  // Visual highlight from entity navigation
    onClick: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)
    val coroutineScope = rememberCoroutineScope()

    // Entity feedback state (same pattern as Meetings feedback)
    var showFeedbackPopup by remember { mutableStateOf(false) }
    var feedbackTargetText by remember { mutableStateOf("") }
    var feedbackFieldKey by remember { mutableStateOf("") }
    var feedbackFieldLabel by remember { mutableStateOf("") }

    // Refresh animation state
    var isRefreshing by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Clear loading state when person data actually changes (dynamic)
    LaunchedEffect(person.name, person.email, person.phone, person.role) {
        if (isRefreshing) {
            delay(300)
            isRefreshing = false
        }
    }

    // Safety timeout: Reset loading state after 10 seconds to prevent infinite spinner
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(10000)
            isRefreshing = false
            KlikLogger.w("WorkLifeScreen", "Refresh timeout reached - loading state reset")
        }
    }

    val onShowFeedback: (String, String, String) -> Unit = { text, fieldKey, fieldLabel ->
        feedbackTargetText = text
        feedbackFieldKey = fieldKey
        feedbackFieldLabel = fieldLabel
        showFeedbackPopup = true
    }

    // Highlight animation for navigation
    val highlightBorderWidth by animateDpAsState(
        targetValue = if (isHighlighted) 2.dp else 0.5.dp,
        animationSpec = tween(durationMillis = 300)
    )
    val highlightBorderAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0.12f,
        animationSpec = if (isHighlighted) infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ) else tween(300)
    )

    val cardBackgroundColor = Color.White

    val borderStroke = if (isHighlighted) {
        BorderStroke(highlightBorderWidth, StriveColor.copy(alpha = highlightBorderAlpha))
    } else {
        BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f))
    }

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    cardBackgroundColor.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                    cardShape
                )
                .border(borderStroke, cardShape)
                .liquid(liquidState) {
                    edge = if (glassSettings.applyToCards) glassSettings.edge else 0.02f
                    shape = cardShape
                    tint = StriveColor.copy(alpha = if (isFeatured) 0.08f else 0.05f)
                }
                .clip(cardShape)
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            // Header Row: Avatar + Name
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            AvatarImage(
                avatarUrl = person.displayAvatarUrl,
                initials = person.name.take(1).uppercase(),
                size = if (isFeatured) 48.dp else 40.dp,
                backgroundColor = StriveColor.copy(alpha = 0.1f),
                initialsColor = StriveColor,
                initialsStyle = if (isFeatured) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    person.name,
                    style = if (isFeatured) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = KlikBlack.copy(alpha = if (isRefreshing) pulseAlpha else 1f),
                    modifier = Modifier
                        .onLongPress {
                            onShowFeedback(person.name, "name", "Person Name")
                        }
                        .then(
                            if (isRefreshing) {
                                Modifier.background(
                                    StriveColor.copy(alpha = pulseAlpha * 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                            } else Modifier
                        )
                )
                // Title right under name - always show
                Text(
                    person.role.ifEmpty { "(none)" },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (person.role.isEmpty()) Color.Gray.copy(alpha = 0.6f) else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Influence Tier badge
            Text(
                "Tier ${person.influenceTier.label}",
                style = MaterialTheme.typography.labelSmall,
                color = when (person.influenceTier) {
                    InfluenceTier.S -> Color(0xFFFFB020)
                    InfluenceTier.A -> StriveColor
                    else -> Color.Gray
                },
                modifier = Modifier
                    .background(
                        when (person.influenceTier) {
                            InfluenceTier.S -> Color(0xFFFFB020)
                            InfluenceTier.A -> StriveColor
                            else -> Color.Gray
                        }.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Email - always show
        Spacer(Modifier.height(8.dp))
        Column {
            Text(stringResource(Res.string.worklife_email), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(
                person.email.ifEmpty { "(none)" },
                style = MaterialTheme.typography.bodySmall,
                color = if (person.email.isEmpty()) Color.Gray.copy(alpha = 0.6f) else Color(0xFF4A90E2).copy(alpha = if (isRefreshing) pulseAlpha else 1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .then(
                        if (person.email.isNotEmpty()) {
                            Modifier.clickable { OAuthBrowser.openUrl("mailto:${person.email}") }
                        } else Modifier
                    )
                    .onLongPress {
                        if (person.email.isNotEmpty()) {
                            onShowFeedback(person.email, "email", "Email")
                        }
                    }
                    .then(
                        if (isRefreshing) {
                            Modifier.background(
                                StriveColor.copy(alpha = pulseAlpha * 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                        } else Modifier
                    )
                    .padding(vertical = 2.dp)
            )
        }

        // Phone - always show
        Spacer(Modifier.height(4.dp))
        Column {
            Text(stringResource(Res.string.worklife_phone), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(
                person.phone.ifEmpty { "(none)" },
                style = MaterialTheme.typography.bodySmall,
                color = if (person.phone.isEmpty()) Color.Gray.copy(alpha = 0.6f) else Color(0xFF4A90E2).copy(alpha = if (isRefreshing) pulseAlpha else 1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .then(
                        if (person.phone.isNotEmpty()) {
                            Modifier.clickable { OAuthBrowser.openUrl("tel:${person.phone}") }
                        } else Modifier
                    )
                    .onLongPress {
                        if (person.phone.isNotEmpty()) {
                            onShowFeedback(person.phone, "phone", "Phone")
                        }
                    }
                    .padding(vertical = 2.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Dimensions section (Voice, Connection, Reliability) - always show
        val personDimensionNames = listOf("voice", "connection", "reliability")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            personDimensionNames.forEach { dimName ->
                val dim = person.dimensions.find { it.dimension.lowercase() == dimName }
                if (dim != null) {
                    DimensionScoreBox(dim, StriveColor, modifier = Modifier.weight(1f))
                } else {
                    // Empty placeholder box
                    EmptyDimensionBox(dimName, StriveColor, modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Skills - always show
        Text(stringResource(Res.string.worklife_skills), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (person.skills.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                person.skills.take(4).forEach { skill ->
                    Text(
                        skill,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF555555),
                        modifier = Modifier
                            .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(
                "(none)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Characteristics - always show
        Text(stringResource(Res.string.worklife_characteristics), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (person.characteristics.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                person.characteristics.take(4).forEach { char ->
                    Text(
                        char,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF555555),
                        modifier = Modifier
                            .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(
                "(none)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Related Projects - always show
        Text(stringResource(Res.string.worklife_projects), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (person.relatedProjects.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                person.relatedProjects.take(3).forEach { project ->
                    Text(
                        project,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF555555),
                        modifier = Modifier
                            .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(
                "(none)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Related Organizations - always show
        Text(stringResource(Res.string.worklife_organizations), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (person.relatedOrganizations.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                person.relatedOrganizations.take(3).forEach { org ->
                    Text(
                        org,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF555555),
                        modifier = Modifier
                            .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(
                "(none)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        }

        // Entity Feedback Popup (same pattern as Meetings feedback)
        FeedbackPopup(
            isVisible = showFeedbackPopup,
            originalText = feedbackTargetText,
            onDismiss = { showFeedbackPopup = false },
            onSubmitFeedback = { feedback ->
                KlikLogger.d("EntityFeedback", "Feedback received: field=$feedbackFieldKey, originalText=${feedback.originalText}, correction=${feedback.correction}, isWrong=${feedback.isMarkedWrong}")
                showFeedbackPopup = false

                // Submit feedback to backend
                val hasCorrection = !feedback.correction.isNullOrBlank() && feedback.correction != feedback.originalText
                val isMarkedWrong = feedback.isMarkedWrong

                if (hasCorrection || isMarkedWrong) {
                    // Start loading animation immediately
                    isRefreshing = true

                    coroutineScope.launch {
                        try {
                            val correctedValue = if (hasCorrection) feedback.correction else feedback.originalText
                            val updates = mapOf(feedbackFieldKey to correctedValue)

                            val result = EntityFeedbackClient.updateEntity(
                                "person",
                                person.id,
                                updates
                            )

                            result.onSuccess {
                                KlikLogger.i("EntityFeedback", "Entity feedback success for person ${person.id}")
                                KlikLogger.d("EntityFeedback", "About to call onRefresh")
                                try {
                                    onRefresh()
                                    KlikLogger.i("EntityFeedback", "onRefresh() call completed successfully")
                                } catch (e: Exception) {
                                    KlikLogger.e("EntityFeedback", "Error calling onRefresh: ${e.message}", e)
                                    isRefreshing = false
                                }
                            }.onFailure { error ->
                                KlikLogger.e("EntityFeedback", "Entity feedback submission failed: ${error.message}")
                                isRefreshing = false
                            }
                        } catch (e: Exception) {
                            KlikLogger.e("EntityFeedback", "Entity feedback submission error: ${e.message}", e)
                            isRefreshing = false
                        }
                    }
                }
            },
            overlayShape = cardShape,
            modifier = Modifier.matchParentSize()
        )
    }
}

/**
 * Organization card content - Shows: Name, Industry, Size
 * Dimensions: Formation, Tribe Vibe, Pulse
 * Related: Departments, People, Projects
 * Design: Score circle on left (matching People avatar size) + content on right
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrganizationCardContent(
    liquidState: LiquidState,
    organization: Organization,
    isFeatured: Boolean = false,
    isPinned: Boolean = false,
    isHighlighted: Boolean = false,  // Visual highlight from entity navigation
    onClick: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)
    val coroutineScope = rememberCoroutineScope()

    // Entity feedback state (same pattern as Project/Person feedback)
    var showFeedbackPopup by remember { mutableStateOf(false) }
    var feedbackTargetText by remember { mutableStateOf("") }
    var feedbackFieldKey by remember { mutableStateOf("") }
    var feedbackFieldLabel by remember { mutableStateOf("") }

    // Refresh animation state
    var isRefreshing by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Clear loading state when organization data changes
    LaunchedEffect(organization.name, organization.industry, organization.strategicFocus) {
        if (isRefreshing) {
            delay(300)
            isRefreshing = false
        }
    }

    // Safety timeout: Reset loading state after 10 seconds
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(10000)
            isRefreshing = false
            KlikLogger.w("WorkLifeScreen", "Refresh timeout reached - loading state reset")
        }
    }

    val onShowFeedback: (String, String, String) -> Unit = { text, fieldKey, fieldLabel ->
        feedbackTargetText = text
        feedbackFieldKey = fieldKey
        feedbackFieldLabel = fieldLabel
        showFeedbackPopup = true
    }

    // Highlight animation for navigation
    val highlightBorderWidth by animateDpAsState(
        targetValue = if (isHighlighted) 2.dp else 0.5.dp,
        animationSpec = tween(durationMillis = 300)
    )
    val highlightBorderAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0.12f,
        animationSpec = if (isHighlighted) infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ) else tween(300)
    )

    val cardBackgroundColor = Color.White

    val borderStroke = if (isHighlighted) {
        BorderStroke(highlightBorderWidth, EngageColor.copy(alpha = highlightBorderAlpha))
    } else {
        BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f))
    }

    // Animated score value
    val animatedScore by animateFloatAsState(
        targetValue = organization.relationshipScore / 100f,
        animationSpec = tween(durationMillis = 800)
    )

    // Score-based color
    val scoreColor = when {
        organization.relationshipScore >= 80 -> Color(0xFF4CAF50)
        organization.relationshipScore >= 50 -> EngageColor
        organization.relationshipScore >= 30 -> Color(0xFFFF9800)
        else -> Color(0xFFE57373)
    }

    Box {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                cardBackgroundColor.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                cardShape
            )
            .border(borderStroke, cardShape)
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.02f
                shape = cardShape
                tint = EngageColor.copy(alpha = if (isFeatured) 0.08f else 0.05f)
            }
            .clip(cardShape)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        // Header Row: Score Circle + Name/Details
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score Circle (matching People avatar size)
            Box(
                modifier = Modifier.size(if (isFeatured) 48.dp else 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = if (isFeatured) 5.dp.toPx() else 4.dp.toPx()
                    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

                    // Background arc
                    drawArc(
                        color = scoreColor.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = stroke
                    )

                    // Score arc
                    drawArc(
                        color = scoreColor,
                        startAngle = -90f,
                        sweepAngle = animatedScore * 360f,
                        useCenter = false,
                        style = stroke
                    )
                }
                Text(
                    "${organization.relationshipScore}",
                    style = if (isFeatured) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }

            Spacer(Modifier.width(12.dp))

            // Title and metadata column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    organization.canonicalName.ifEmpty { organization.name },
                    style = if (isFeatured) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.onLongPress {
                        onShowFeedback(organization.canonicalName.ifEmpty { organization.name }, "name", "Organization Name")
                    }
                )
                // Industry right under name - always show
                Text(
                    organization.industry.ifEmpty { "(none)" },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (organization.industry.isEmpty()) Color.Gray.copy(alpha = 0.6f) else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.onLongPress {
                        onShowFeedback(organization.industry, "industry", "Industry")
                    }
                )
            }

            // Size badge - always show
            val sizeText = organization.sizeHeadcount?.let { "${it}+" } ?: "(none)"
            Text(
                sizeText,
                style = MaterialTheme.typography.labelSmall,
                color = if (organization.sizeHeadcount != null) EngageColor else Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(
                        if (organization.sizeHeadcount != null) EngageColor.copy(alpha = 0.1f)
                        else Color.Gray.copy(alpha = 0.05f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Strategic Focus & Next Action row
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.worklife_strategic_focus), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(
                    organization.strategicFocus.ifEmpty { "(none)" },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (organization.strategicFocus.isEmpty()) Color.Gray.copy(alpha = 0.6f) else EngageColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.onLongPress {
                        onShowFeedback(organization.strategicFocus, "strategic_focus", "Strategic Focus")
                    }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(Res.string.worklife_next_action), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(
                    organization.nextAction.ifEmpty { "(none)" },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (organization.nextAction.isEmpty()) Color.Gray.copy(alpha = 0.6f) else Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.onLongPress {
                        onShowFeedback(organization.nextAction, "next_action", "Next Action")
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Dimensions section (Formation, Tribe Vibe, Pulse) - always show
        val orgDimensionNames = listOf("formation", "tribe_vibe", "pulse")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            orgDimensionNames.forEach { dimName ->
                val dim = organization.dimensions.find { it.dimension.lowercase() == dimName }
                if (dim != null) {
                    DimensionScoreBox(dim, EngageColor, modifier = Modifier.weight(1f))
                } else {
                    EmptyDimensionBox(dimName, EngageColor, modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Departments - always show
        Text(stringResource(Res.string.worklife_departments), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (organization.departments.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                organization.departments.take(4).forEach { dept ->
                    Text(
                        dept,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF555555),
                        modifier = Modifier
                            .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(
                "(none)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Strengths - always show
        Text(stringResource(Res.string.worklife_strengths), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (organization.strengths.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                organization.strengths.take(3).forEach { strength ->
                    Text(
                        strength,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF555555),
                        modifier = Modifier
                            .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(
                "(none)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Related People - always show
        Text(stringResource(Res.string.worklife_related_people), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (organization.employees.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                organization.employees.take(3).forEach { employee ->
                    Text(
                        employee,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF555555),
                        modifier = Modifier
                            .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(
                "(none)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(Modifier.height(8.dp))

        // Related Projects - always show
        Text(stringResource(Res.string.worklife_projects), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        if (organization.relatedProjects.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                organization.relatedProjects.take(3).forEach { project ->
                    Text(
                        project,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF555555),
                        modifier = Modifier
                            .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Text(
                "(none)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }

        // Entity Feedback Popup (same pattern as Project/Person feedback)
        FeedbackPopup(
            isVisible = showFeedbackPopup,
            originalText = feedbackTargetText,
            onDismiss = { showFeedbackPopup = false },
            onSubmitFeedback = { feedback ->
                KlikLogger.d("EntityFeedback", "Feedback received: field=$feedbackFieldKey, originalText=${feedback.originalText}, correction=${feedback.correction}, isWrong=${feedback.isMarkedWrong}")
                showFeedbackPopup = false

                val hasCorrection = !feedback.correction.isNullOrBlank() && feedback.correction != feedback.originalText
                val isMarkedWrong = feedback.isMarkedWrong

                if (hasCorrection || isMarkedWrong) {
                    isRefreshing = true

                    coroutineScope.launch {
                        try {
                            val correctedValue = if (hasCorrection) feedback.correction else feedback.originalText
                            val updates = mapOf(feedbackFieldKey to correctedValue)

                            val result = EntityFeedbackClient.updateEntity(
                                "organization",
                                organization.id,
                                updates
                            )

                            result.onSuccess {
                                KlikLogger.i("EntityFeedback", "Entity feedback success for organization ${organization.id}")
                                try {
                                    onRefresh()
                                    KlikLogger.i("EntityFeedback", "onRefresh() call completed successfully")
                                } catch (e: Exception) {
                                    KlikLogger.e("EntityFeedback", "Error calling onRefresh: ${e.message}", e)
                                    isRefreshing = false
                                }
                            }.onFailure { error ->
                                KlikLogger.e("EntityFeedback", "Entity feedback submission failed: ${error.message}")
                                isRefreshing = false
                            }
                        } catch (e: Exception) {
                            KlikLogger.e("EntityFeedback", "Entity feedback submission error: ${e.message}", e)
                            isRefreshing = false
                        }
                    }
                }
            },
            overlayShape = cardShape,
            modifier = Modifier.matchParentSize()
        )
    }
}

/**
 * Radial score indicator ring
 */
@Composable
fun ScoreRing(
    score: Float,
    color: Color,
    size: androidx.compose.ui.unit.Dp,
    strokeWidth: Float = 4f
) {
    val animatedScore by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 800)
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sweepAngle = animatedScore * 360f
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

            // Background arc
            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )

            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = stroke
            )
        }

        Text(
            text = "${score.toInt()}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun AchievementsCard(liquidState: LiquidState, achievements: Achievements?) {
    if (achievements == null) return

    val primaryColor = MaterialTheme.colorScheme.primary
    var expanded by remember { mutableStateOf(false) }
    val glassSettings = LocalLiquidGlassSettings.current
    val insightColors = LocalInsightCardColors.current
    val cardShape = RoundedCornerShape(24.dp)

    val baseColor = if (glassSettings.applyToCards) {
        insightColors.growthTint.copy(alpha = 0.12f).compositeOver(Color.White)
    } else {
        Color.White
    }

    val progressPercent = achievements.progressPercent
    val title = achievements.title
    val summary = achievements.summary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(baseColor.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f), cardShape)
            .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                tint = Color.Transparent
            }
            .clip(cardShape)
            .clickable { expanded = !expanded }
            .padding(20.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(88.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                drawArc(
                    color = primaryColor.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = (progressPercent / 100f) * 360f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                "${progressPercent}%",
                style = MaterialTheme.typography.titleSmall,
                color = primaryColor
            )
        }

        Spacer(Modifier.width(20.dp))

        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = primaryColor
            )
            Spacer(Modifier.height(8.dp))
            Text(
                summary,
                style = MaterialTheme.typography.bodyMedium,
                color = KlikBlack,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.padding(bottom = 2.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = KlikBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Compact chip showing related item count
 */
@Composable
fun RelatedCountChip(
    icon: ImageVector,
    count: Int,
    label: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Info chip showing title and count for related items
 * Shows the label prominently with count
 */
@Composable
fun RelatedInfoChip(
    icon: ImageVector,
    count: Int,
    label: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.7f)
        )
    }
}

/**
 * Dimension score box display - Shows dimension name, score/status, and period
 * Used for Projects (Clarity/Weather/Health), People (Focus Orbit/Flow/Energy),
 * Organizations (Formation/Tribe Vibe/Pulse)
 *
 * Special handling for focus_orbit: displays keywords from orbits array
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DimensionScoreBox(
    dimension: DimensionScore,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Parse details JSON for status-based dimensions
    val detailsJson = remember(dimension.details) {
        try {
            Json.parseToJsonElement(dimension.details).jsonObject
        } catch (e: Exception) {
            KlikLogger.w("WorkLifeDimension", "Failed to parse dimension details JSON: ${e.message}", e)
            null
        }
    }

    // Format dimension name for display
    val displayName = dimension.dimension.split("_").joinToString(" ") {
        it.replaceFirstChar { char -> char.uppercase() }
    }

    // Special handling for focus_orbit - extract keywords from orbits array
    if (dimension.dimension.lowercase() == "focus_orbit") {
        val keywords = remember(detailsJson) {
            try {
                detailsJson!!.get("orbits")!!.jsonArray.flatMap { orbit ->
                    orbit.jsonObject["keywords"]!!.jsonArray.mapNotNull {
                        it.jsonPrimitive.content
                    }
                }.distinct().take(5)
            } catch (e: Exception) {
                KlikLogger.w("WorkLifeDimension", "Failed to parse focus_orbit keywords: ${e.message}", e)
                emptyList()
            }
        }

        Column(
            modifier = modifier
                .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                displayName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            if (keywords.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    keywords.forEach { keyword ->
                        Text(
                            keyword,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = color,
                            modifier = Modifier
                                .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            } else {
                Text(
                    "(none)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    // Get status text for status-based dimensions
    val statusText = detailsJson?.get("status")?.jsonPrimitive?.content
    val level = detailsJson?.get("level")?.jsonPrimitive?.content
    val state = detailsJson?.get("state")?.jsonPrimitive?.content

    // Determine display value (status text or score)
    val displayValue = statusText ?: level ?: state ?: dimension.score?.toInt()?.toString() ?: "-"

    // Color based on dimension type and status
    val boxColor = when (dimension.dimension) {
        "health" -> when (statusText?.lowercase()) {
            "green" -> Color(0xFF4CAF50)
            "yellow" -> Color(0xFFFF9800)
            "red" -> Color(0xFFE57373)
            else -> color
        }
        "weather" -> when (statusText?.lowercase()) {
            "sunny" -> Color(0xFFFFB020)
            "partly cloudy" -> Color(0xFF90CAF9)
            "foggy" -> Color(0xFF9E9E9E)
            else -> color
        }
        "energy" -> when {
            (dimension.score ?: 0f) >= 70f -> Color(0xFF4CAF50)
            (dimension.score ?: 0f) >= 40f -> Color(0xFFFF9800)
            else -> Color(0xFFE57373)
        }
        else -> color
    }

    Column(
        modifier = modifier
            .background(boxColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            displayName,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
        Text(
            displayValue,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = boxColor,
            maxLines = 1
        )
        if (dimension.periodType.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                dimension.periodType,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = Color.Gray.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

/**
 * Empty dimension box placeholder - Shows dimension name with "(none)" when no data available
 */
@Composable
fun EmptyDimensionBox(
    dimensionName: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Format dimension name for display
    val displayName = dimensionName.split("_").joinToString(" ") {
        it.replaceFirstChar { char -> char.uppercase() }
    }

    Column(
        modifier = modifier
            .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            displayName,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "(none)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray.copy(alpha = 0.6f),
            maxLines = 1
        )
    }
}

/**
 * Insights Section - Displays Encourage (highlighted) and Worklife (full content) insights
 * Always shows both cards, with placeholder content when data is not yet available
 */
@Composable
fun InsightsSection(
    liquidState: LiquidState,
    isLoading: Boolean = false,
    encourageData: EncourageData?,
    worklifeData: WorklifeData?
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    // Pulsing animation for loading state
    val infiniteTransition = rememberInfiniteTransition(label = "insights_loading")
    val loadingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encourage Card (Highlights) - Always show
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    KlikPrimary.copy(alpha = 0.08f),
                    cardShape
                )
                .border(
                    BorderStroke(1.dp, KlikPrimary.copy(alpha = 0.2f)),
                    cardShape
                )
                .liquid(liquidState) {
                    edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                    shape = cardShape
                    tint = KlikPrimary.copy(alpha = 0.05f)
                }
                .clip(cardShape)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = KlikPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Highlights",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = KlikPrimary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    when {
                        isLoading -> "Loading..."
                        encourageData != null -> "${encourageData.activeSessionsCount} sessions"
                        else -> "(none)"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = KlikPrimary.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(12.dp))
            if (isLoading) {
                // Loading placeholder for highlights
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(14.dp)
                            .background(KlikPrimary.copy(alpha = loadingAlpha * 0.2f), RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(14.dp)
                            .background(KlikPrimary.copy(alpha = loadingAlpha * 0.15f), RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(14.dp)
                            .background(KlikPrimary.copy(alpha = loadingAlpha * 0.1f), RoundedCornerShape(4.dp))
                    )
                }
            } else {
                Text(
                    encourageData?.message ?: "No highlights available yet. Start recording sessions to see your personalized summary.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (encourageData != null) KlikBlack else Color.Gray,
                    lineHeight = 22.sp
                )
            }
        }

        // Worklife Card - Always show
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                    cardShape
                )
                .border(
                    BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)),
                    cardShape
                )
                .liquid(liquidState) {
                    edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                    shape = cardShape
                    tint = Color.Transparent
                }
                .clip(cardShape)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Worklife Insights",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = KlikBlack
                )
            }

            Spacer(Modifier.height(8.dp))

            // Stats row - show loading or actual counts
            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(20.dp)
                            .background(LiveColor.copy(alpha = loadingAlpha * 0.2f), RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(20.dp)
                            .background(StriveColor.copy(alpha = loadingAlpha * 0.2f), RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(20.dp)
                            .background(EngageColor.copy(alpha = loadingAlpha * 0.2f), RoundedCornerShape(4.dp))
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "${worklifeData?.projectsFound ?: 0} projects",
                        style = MaterialTheme.typography.labelSmall,
                        color = LiveColor,
                        modifier = Modifier
                            .background(LiveColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                        "${worklifeData?.relationshipsFound ?: 0} relationships",
                        style = MaterialTheme.typography.labelSmall,
                        color = StriveColor,
                        modifier = Modifier
                            .background(StriveColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                        "${worklifeData?.organizationsFound ?: 0} orgs",
                        style = MaterialTheme.typography.labelSmall,
                        color = EngageColor,
                        modifier = Modifier
                            .background(EngageColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Insights list, loading placeholders, or empty message
            when {
                isLoading -> {
                    // Loading placeholders for worklife insights
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(3) { index ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(KlikPrimary.copy(alpha = loadingAlpha * 0.3f), CircleShape)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f - index * 0.1f)
                                            .height(12.dp)
                                            .background(Color.Gray.copy(alpha = loadingAlpha * 0.15f), RoundedCornerShape(4.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f - index * 0.1f)
                                            .height(12.dp)
                                            .background(Color.Gray.copy(alpha = loadingAlpha * 0.1f), RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }
                }
                worklifeData != null && worklifeData.insights.isNotEmpty() -> {
                    worklifeData.insights.forEach { insight ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                "\u2022",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KlikPrimary,
                                modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                            )
                            Text(
                                insight,
                                style = MaterialTheme.typography.bodyMedium,
                                color = KlikBlack,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
                else -> {
                    Text(
                        "No worklife insights available yet. Your activity patterns will appear here as you use the app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

/**
 * Alphabetical index scrubber for quick scrolling through entity lists.
 * Similar to iOS Contacts app - shows letters on the right side that can be tapped or dragged.
 */
@Composable
fun AlphabeticalIndexScrubber(
    availableLetters: List<Char>,
    letterToIndex: Map<Char, Int>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    activeColor: Color = KlikPrimary
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    var scrubberHeight by remember { mutableStateOf(0) }
    var currentLetter by remember { mutableStateOf<Char?>(null) }
    var isDragging by remember { mutableStateOf(false) }

    // All possible letters for display
    val allLetters = ('A'..'Z').toList() + listOf('#')

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(24.dp)
            .onSizeChanged { scrubberHeight = it.height }
            .pointerInput(availableLetters, letterToIndex) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val letterIndex = ((offset.y / scrubberHeight) * allLetters.size).toInt()
                            .coerceIn(0, allLetters.size - 1)
                        val letter = allLetters[letterIndex]
                        currentLetter = letter

                        // Find the closest available letter
                        val targetLetter = findClosestAvailableLetter(letter, availableLetters)
                        targetLetter?.let { target ->
                            letterToIndex[target]?.let { index ->
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            }
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        currentLetter = null
                    },
                    onDragCancel = {
                        isDragging = false
                        currentLetter = null
                    },
                    onVerticalDrag = { change, _ ->
                        change.consume()
                        val letterIndex = ((change.position.y / scrubberHeight) * allLetters.size).toInt()
                            .coerceIn(0, allLetters.size - 1)
                        val letter = allLetters[letterIndex]

                        if (letter != currentLetter) {
                            currentLetter = letter
                            val targetLetter = findClosestAvailableLetter(letter, availableLetters)
                            targetLetter?.let { target ->
                                letterToIndex[target]?.let { index ->
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                }
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            allLetters.forEach { letter ->
                val isAvailable = letter in availableLetters
                val isActive = currentLetter == letter && isDragging

                Text(
                    text = letter.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isActive -> activeColor
                        isAvailable -> KlikBlack.copy(alpha = 0.8f)
                        else -> Color.Gray.copy(alpha = 0.3f)
                    },
                    modifier = Modifier
                        .clickable(enabled = isAvailable) {
                            letterToIndex[letter]?.let { index ->
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            }
                        }
                        .padding(vertical = 1.dp)
                )
            }
        }

        // Show current letter indicator when dragging
        if (isDragging && currentLetter != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 40.dp)
                    .size(50.dp)
                    .background(activeColor.copy(alpha = 0.9f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentLetter.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Find the closest available letter in the list, preferring the exact match
 * or the next available letter in alphabetical order.
 */
private fun findClosestAvailableLetter(target: Char, availableLetters: List<Char>): Char? {
    if (target in availableLetters) return target

    // Find next available letter after target
    val nextLetter = availableLetters.firstOrNull { it >= target }
    if (nextLetter != null) return nextLetter

    // If no next letter, return last available letter
    return availableLetters.lastOrNull()
}

/**
 * Helper function to extract the first letter of a name for grouping
 */
private fun getFirstLetter(name: String): Char {
    val firstChar = name.trim().firstOrNull()?.uppercaseChar() ?: '#'
    return if (firstChar.isLetter()) firstChar else '#'
}

/**
 * Generic entity list with alphabetical index scrubber
 */
@Composable
fun <T> EntityListWithIndex(
    items: List<T>,
    getName: (T) -> String,
    getId: (T) -> String,  // Add getId function for unique keys
    listState: LazyListState,
    activeColor: Color,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    // Group items by first letter
    val groupedItems = remember(items) {
        items.sortedBy { getName(it).uppercase() }
            .groupBy { getFirstLetter(getName(it)) }
    }

    // Build available letters and their indices
    val availableLetters = remember(groupedItems) {
        groupedItems.keys.sorted()
    }

    // Calculate the index of each letter's first item in the flat list
    val letterToIndex = remember(groupedItems) {
        var currentIndex = 0
        val map = mutableMapOf<Char, Int>()
        groupedItems.keys.sorted().forEach { letter ->
            map[letter] = currentIndex
            currentIndex += (groupedItems[letter]?.size ?: 0) + 1 // +1 for header
        }
        map
    }

    // Flatten the grouped items for display
    val flatItems = remember(groupedItems) {
        buildList {
            groupedItems.keys.sorted().forEach { letter ->
                add(letter to null as T?) // Header
                groupedItems[letter]?.forEach { item ->
                    add(letter to item)
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 28.dp), // Leave space for scrubber
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            flatItems.forEach { (letter, item) ->
                if (item == null) {
                    // Letter header
                    item(key = "header_$letter") {
                        Text(
                            text = letter.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = activeColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    // Item content - use unique ID as key
                    item(key = getId(item)) {
                        itemContent(item)
                    }
                }
            }
        }

        // Alphabetical index scrubber on the right
        AlphabeticalIndexScrubber(
            availableLetters = availableLetters,
            letterToIndex = letterToIndex,
            listState = listState,
            activeColor = activeColor,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
fun HighlightEncouragementCard(
    liquidState: LiquidState,
    title: String,
    encourageMessage: String?,
    insights: List<String>?,
    isLoading: Boolean,
    isLlmDataLoading: Boolean = false,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    // Traced segments for source navigation
    encourageTracedSegments: List<TracedSegment>? = null,
    worklifeTracedSegments: List<TracedSegment>? = null,
    // Entity data for highlighting
    tasks: List<TaskMetadata> = emptyList(),
    meetings: List<Meeting> = emptyList(),
    projects: List<Project> = emptyList(),
    people: List<Person> = emptyList(),
    organizations: List<Organization> = emptyList(),
    onEntityClick: (EntityNavigationData) -> Unit = {},
    onSegmentClick: (TracedSegmentNavigation) -> Unit = {}
) {
    val glassSettings = LocalLiquidGlassSettings.current
    var showTracedSegments by remember { mutableStateOf(false) }

    // Combine traced segments from both sources (null means data not yet loaded)
    val allTracedSegments = remember(encourageTracedSegments, worklifeTracedSegments) {
        val combined = listOfNotNull(encourageTracedSegments, worklifeTracedSegments).flatten()
        combined.distinctBy { it.sessionId to it.segmentId }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquid(liquidState) {
                edge = glassSettings.edge
                frost = glassSettings.frost
                shape = RoundedCornerShape(16.dp)
                tint = primaryColor.copy(alpha = 0.05f) // Subtle blue tint
            }
            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { /* Expand/collapse if needed */ }
            .onLongPress {
                if (allTracedSegments.isNotEmpty()) {
                    showTracedSegments = !showTracedSegments
                }
            }
            .padding(20.dp)
            .animateContentSize()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KlikBlack
                )
                Spacer(Modifier.width(8.dp))
                AiGeneratedBadge("AI-Generated")
                Spacer(Modifier.weight(1f))
                if (isLoading || (encourageMessage == null && isLlmDataLoading)) {
                    Text(
                        "Loading...",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor.copy(alpha = 0.7f)
                    )
                }
                if (allTracedSegments.isNotEmpty() && !isLoading && !isLlmDataLoading) {
                    Text(
                        "${allTracedSegments.size} sources",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor.copy(alpha = 0.6f)
                    )
                }
            }

            // Content
            if (isLoading) {
                // General data loading shimmer
                ShimmerTextBlock(lines = 3, color = primaryColor)
            } else if (encourageMessage != null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Encourage Message with Entity Highlighting
                    EntityHighlightedText(
                        text = encourageMessage,
                        tasks = tasks,
                        meetings = meetings,
                        projects = projects,
                        people = people,
                        organizations = organizations,
                        onEntityClick = onEntityClick,
                        style = MaterialTheme.typography.bodyMedium,
                        color = KlikBlack.copy(alpha = 0.8f)
                    )

                    if (insights != null && insights.isNotEmpty()) {
                         // Separator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(primaryColor.copy(alpha = 0.1f))
                        )

                        // Insights with Entity Highlighting
                        insights.take(2).forEach { insight -> // Limit to 2 for brevity
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(start = 4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .size(4.dp)
                                        .background(primaryColor.copy(alpha = 0.6f), CircleShape)
                                )
                                EntityHighlightedText(
                                    text = insight,
                                    tasks = tasks,
                                    meetings = meetings,
                                    projects = projects,
                                    people = people,
                                    organizations = organizations,
                                    onEntityClick = onEntityClick,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KlikBlack.copy(alpha = 0.65f)
                                )
                            }
                        }
                    } else if (isLlmDataLoading) {
                        // Encourage loaded but worklife insights still loading
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(primaryColor.copy(alpha = 0.1f))
                        )
                        ShimmerTextBlock(lines = 2, color = primaryColor)
                    }

                    // Traced segments section (shown on long press)
                    if (showTracedSegments && allTracedSegments.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        TracedSegmentsSection(
                            liquidState = liquidState,
                            segments = allTracedSegments,
                            onSegmentClick = onSegmentClick
                        )
                    }

                    AiContentDisclaimer()
                }
            } else if (isLlmDataLoading) {
                // LLM data still loading after general data loaded
                ShimmerTextBlock(lines = 3, color = primaryColor)
            }
        }
    }
}

@Composable
fun GoalsandProgressCard(
    liquidState: LiquidState,
    primaryColor: Color,
    goalsData: io.github.fletchmckee.liquid.samples.app.data.source.remote.GoalListResponse?,
    userLevelData: io.github.fletchmckee.liquid.samples.app.data.source.remote.UserLevelData?,
    isLoading: Boolean = false,
    onGrowthTreeClick: () -> Unit = {},
    isLocked: Boolean = false,
    modifier: Modifier = Modifier
) {
    val glassSettings = LocalLiquidGlassSettings.current
    var expandedGoalId by remember { mutableStateOf<Int?>(null) }

    // Pulsing animation for loading state
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val loadingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loadingAlpha"
    )

    val goals = goalsData?.goals
    // Only computed when goals data is loaded (goalsData non-null)
    val activeGoals = goals?.filter { it.status == "active" || it.status == "in_progress" }
    val completedGoals = goals?.filter { it.status == "completed" }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquid(liquidState) {
                edge = glassSettings.edge
                frost = glassSettings.frost
                shape = RoundedCornerShape(16.dp)
                tint = primaryColor.copy(alpha = 0.05f)
            }
            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onGrowthTreeClick() }
            .padding(20.dp)
            .animateContentSize()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Goals & Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KlikBlack
                )
                Spacer(Modifier.weight(1f))
                if (isLoading) {
                    Text(
                        "Loading...",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor.copy(alpha = 0.7f)
                    )
                } else if (!goals.isNullOrEmpty()) {
                    Text(
                        "${activeGoals!!.size} active",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor.copy(alpha = 0.7f)
                    )
                }
            }

            if (isLoading) {
                // Pulsing loading placeholder
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(1f - index * 0.1f)
                                .height(14.dp)
                                .background(
                                    primaryColor.copy(alpha = loadingAlpha * (0.2f - index * 0.05f)),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
            } else {
                // Level & XP Section
                userLevelData?.let { level ->
                    LevelXpSection(
                        level = level,
                        primaryColor = primaryColor
                    )
                }

                // Goals List
                if (goals.isNullOrEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(primaryColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No goals yet. Goals will appear here once created.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KlikBlack.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Active Goals
                    if (!activeGoals.isNullOrEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            activeGoals.forEach { goal ->
                                GoalItemCard(
                                    goal = goal,
                                    isExpanded = expandedGoalId == goal.id,
                                    onExpandToggle = {
                                        expandedGoalId = if (expandedGoalId == goal.id) null else goal.id
                                    },
                                    primaryColor = primaryColor
                                )
                            }
                        }
                    }

                    // Completed Goals (collapsed by default, show count)
                    if (!completedGoals.isNullOrEmpty()) {
                        var showCompleted by remember { mutableStateOf(false) }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCompleted = !showCompleted }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = StriveColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "${completedGoals.size} completed",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = KlikBlack.copy(alpha = 0.6f)
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    imageVector = if (showCompleted) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = if (showCompleted) "Collapse" else "Expand",
                                    tint = KlikBlack.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (showCompleted) {
                                completedGoals.forEach { goal ->
                                    GoalItemCard(
                                        goal = goal,
                                        isExpanded = expandedGoalId == goal.id,
                                        onExpandToggle = {
                                            expandedGoalId = if (expandedGoalId == goal.id) null else goal.id
                                        },
                                        primaryColor = StriveColor.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Locked overlay for non-pro users
        if (isLocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFFFFCC00),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Pro Feature",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = KlikBlack
                    )
                    Text(
                        text = "Upgrade to unlock Goals & Progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = KlikBlack.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelXpSection(
    level: io.github.fletchmckee.liquid.samples.app.data.source.remote.UserLevelData,
    primaryColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Level ${level.level}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KlikBlack
                )
                if (level.levelTitle.isNotBlank()) {
                    Text(
                        text = level.levelTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = KlikBlack.copy(alpha = 0.7f)
                    )
                }
            }

            // Streak badge
            if (level.streakDays > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(LiveColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Streak",
                        tint = LiveColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${level.streakDays} day streak",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = LiveColor
                    )
                }
            }
        }

        // XP Progress bar
        val xpProgress = level.currentXp.toFloat() / (level.currentXp + level.xpToNextLevel).toFloat()
        LinearProgressIndicator(
            progress = { xpProgress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = primaryColor,
            trackColor = Color.White.copy(alpha = 0.5f),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${level.currentXp} XP",
                style = MaterialTheme.typography.labelSmall,
                color = KlikBlack.copy(alpha = 0.7f)
            )
            Text(
                text = "${level.xpToNextLevel} XP to next level",
                style = MaterialTheme.typography.labelSmall,
                color = KlikBlack.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun GoalItemCard(
    goal: io.github.fletchmckee.liquid.samples.app.data.source.remote.GoalDto,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    primaryColor: Color
) {
    val completedMilestones = goal.milestones.count { it.status == "completed" }
    val totalMilestones = goal.milestones.size

    val statusColor = when (goal.status) {
        "completed" -> StriveColor
        "active", "in_progress" -> LiveColor
        "paused" -> KlikBlack.copy(alpha = 0.5f)
        else -> primaryColor
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onExpandToggle() }
            .animateContentSize()
    ) {
        // Goal Header
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.goal,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = KlikBlack,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category chip
                        Box(
                            modifier = Modifier
                                .background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = goal.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = primaryColor
                            )
                        }
                        // Timeline
                        Text(
                            text = "${goal.timelineMonths}mo",
                            style = MaterialTheme.typography.labelSmall,
                            color = KlikBlack.copy(alpha = 0.5f)
                        )
                    }
                }

                // Expand/collapse icon
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = KlikBlack.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { goal.currentProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = Color.White.copy(alpha = 0.5f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(goal.currentProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
                Text(
                    text = "$completedMilestones/$totalMilestones milestones",
                    style = MaterialTheme.typography.labelSmall,
                    color = KlikBlack.copy(alpha = 0.6f)
                )
            }
        }

        // Expanded: Milestones
        if (isExpanded && goal.milestones.isNotEmpty()) {
            HorizontalDivider(
                color = primaryColor.copy(alpha = 0.1f),
                thickness = 1.dp
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Milestones",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = KlikBlack.copy(alpha = 0.7f)
                )

                goal.milestones.sortedBy { it.sequenceOrder }.forEach { milestone ->
                    MilestoneItem(
                        milestone = milestone,
                        primaryColor = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestoneItem(
    milestone: io.github.fletchmckee.liquid.samples.app.data.source.remote.GoalMilestoneDto,
    primaryColor: Color
) {
    val isCompleted = milestone.status == "completed"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isCompleted) StriveColor.copy(alpha = 0.08f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Completed",
                tint = StriveColor,
                modifier = Modifier.size(18.dp)
            )
        } else {
            // Empty circle for pending milestones
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .border(1.5.dp, KlikBlack.copy(alpha = 0.3f), CircleShape)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Medium,
                color = if (isCompleted) KlikBlack.copy(alpha = 0.6f) else KlikBlack,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
            if (milestone.successCriteria.isNotBlank()) {
                Text(
                    text = milestone.successCriteria,
                    style = MaterialTheme.typography.labelSmall,
                    color = KlikBlack.copy(alpha = 0.5f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Target date
        Text(
            text = milestone.targetDate.take(10), // Just date part
            style = MaterialTheme.typography.labelSmall,
            color = KlikBlack.copy(alpha = 0.4f)
        )
    }
}

