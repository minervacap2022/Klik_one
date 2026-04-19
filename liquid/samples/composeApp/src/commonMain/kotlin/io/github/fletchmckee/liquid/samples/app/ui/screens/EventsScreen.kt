package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.theme.EngageColor
import io.github.fletchmckee.liquid.samples.app.platform.HapticService
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings
import io.github.fletchmckee.liquid.samples.app.theme.LocalInsightCardColors
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.model.archivedTaskIdsState
import io.github.fletchmckee.liquid.samples.app.model.pinnedTaskIdsState
import io.github.fletchmckee.liquid.samples.app.model.archiveTask
import io.github.fletchmckee.liquid.samples.app.model.unarchiveTask
import io.github.fletchmckee.liquid.samples.app.model.toggleTaskPin
import io.github.fletchmckee.liquid.samples.app.model.executingTodoIdsState
import io.github.fletchmckee.liquid.samples.app.model.peopleState
import io.github.fletchmckee.liquid.samples.app.model.projectsState
import io.github.fletchmckee.liquid.samples.app.model.organizationsState
import io.github.fletchmckee.liquid.samples.app.model.recentlyCompletedCategoriesState
import androidx.compose.material3.LinearProgressIndicator
import io.github.fletchmckee.liquid.samples.app.ui.components.FeedbackPopup
import io.github.fletchmckee.liquid.samples.app.ui.icons.CustomIcons
import io.github.fletchmckee.liquid.samples.app.ui.icons.PushPin
import io.github.fletchmckee.liquid.samples.app.ui.icons.Archive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import kotlinx.datetime.Clock
import io.github.fletchmckee.liquid.samples.app.utils.onLongPress
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import liquid_root.samples.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskStatus
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import io.github.fletchmckee.liquid.samples.app.ui.components.LiquidPullToRefreshIndicator
import androidx.compose.runtime.rememberCoroutineScope
import io.github.fletchmckee.liquid.samples.app.theme.LocalSnackbarHostState
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// Colors
private val FeaturedColor = Color(0xFFFFB020) // Amber
private val SensitiveColor = Color(0xFFFF3B30) // Red/Pink
private val DailyColor = Color(0xFF4CAF50)    // Green

// KK_exec Status Colors
private val StatusPendingColor = Color(0xFFFFB020)   // Amber - waiting
private val StatusExecutingColor = Color(0xFF2196F3) // Blue - in progress
private val StatusSuccessColor = Color(0xFF4CAF50)   // Green - completed
private val StatusFailedColor = Color(0xFFFF3B30)    // Red - failed
private val StatusCannotExecuteColor = Color(0xFF9E9E9E) // Gray - cannot do

// Task Status Colors (for main TaskStatus display)
private val StatusApprovedColor = Color(0xFF4CAF50)   // Green
private val StatusRejectedColor = Color(0xFFFF3B30)   // Red
private val StatusInReviewColor = Color(0xFFFFB020)   // Amber/Orange
private val StatusInProgressColor = Color(0xFF2196F3) // Blue
private val StatusCompletedColor = Color(0xFF4CAF50)  // Green

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    isLoading: Boolean = false,
    isRefreshing: Boolean = false,
    featuredTasks: List<TaskMetadata> = emptyList(),
    sensitiveTasks: List<TaskMetadata> = emptyList(),
    dailyTasks: List<TaskMetadata> = emptyList(),
    dailyTasksGrouped: Map<String, List<TaskMetadata>> = emptyMap(),
    onRefresh: () -> Unit = {},
    onApproveTask: (String) -> Unit = {},
    onRejectTask: (String) -> Unit = {},
    onRejectTaskWithReason: (String, String) -> Unit = { _, _ -> },
    onRetryTask: (String) -> Unit = {},
    onArchiveTaskOnBackend: (String) -> Unit = {},
    onEntityClick: (io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData) -> Unit = {},
    onSegmentClick: (io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation) -> Unit = {}
) {
    val liquidState = rememberLiquidState()
    val scope = rememberCoroutineScope()

    // Sensitive task confirmation dialog state
    var taskToReview by remember { mutableStateOf<TaskMetadata?>(null) }

    // Liquid-glass undo banner state (replaces Material snackbar)
    var undoBannerVisible by remember { mutableStateOf(false) }
    var undoBannerMessage by remember { mutableStateOf("") }
    var undoBannerAction by remember { mutableStateOf<() -> Unit>({}) }

    // Pull-to-refresh state - controlled by parent via isRefreshing param
    val pullRefreshState = rememberPullToRefreshState()

    // Use global pin/archive state from Models
    val archivedTaskIds by archivedTaskIdsState
    val pinnedTaskIds by pinnedTaskIdsState

    // Entity lookups are defined inside TaskCardContent where entity refs are displayed

    // All tasks combined (for finding task to archive)
    val allTasks = remember(featuredTasks, sensitiveTasks, dailyTasks) {
        featuredTasks + sensitiveTasks + dailyTasks
    }

    // Archive handler using global function with liquid-glass undo banner
    val archivedText = stringResource(Res.string.events_task_archived)
    val onArchiveTask: (String) -> Unit = { taskId ->
        val task = allTasks.find { it.id == taskId }
        archiveTask(taskId, task)
        undoBannerMessage = archivedText
        undoBannerAction = { unarchiveTask(taskId) }
        undoBannerVisible = true
    }

    // Initialize with pre-pinned items when data is loaded (merge with global state)
    LaunchedEffect(featuredTasks, sensitiveTasks, dailyTasks) {
        val now = Clock.System.now().toEpochMilliseconds()
        val newPinned = mutableMapOf<String, Long>()
        (featuredTasks + sensitiveTasks + dailyTasks).filter { it.isPinned }.forEachIndexed { idx, item ->
            if (!pinnedTaskIdsState.value.containsKey(item.id)) {
                newPinned[item.id] = now - idx * 1000
            }
        }
        if (newPinned.isNotEmpty()) {
            pinnedTaskIdsState.value = pinnedTaskIdsState.value + newPinned
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        state = pullRefreshState,
        onRefresh = {
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 68.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Featured Section
        item {
            TaskSection(
                liquidState = liquidState,
                title = stringResource(Res.string.events_featured),
                color = FeaturedColor,
                tasks = featuredTasks,
                sectionType = "Featured",
                isLoading = isLoading,
                onArchive = onArchiveTask,
                onRefresh = onRefresh,
                onEntityClick = onEntityClick,
                onSegmentClick = onSegmentClick
            )
        }

        // Sensitive Section — tapping approve/reject opens confirmation dialog
        item {
            TaskSection(
                liquidState = liquidState,
                title = stringResource(Res.string.events_sensitive),
                color = SensitiveColor,
                tasks = sensitiveTasks,
                sectionType = "Sensitive",
                isLoading = isLoading,
                onArchive = onArchiveTask,
                onApproveTask = { taskId ->
                    taskToReview = sensitiveTasks.find { it.id == taskId }
                },
                onRejectTask = { taskId ->
                    taskToReview = sensitiveTasks.find { it.id == taskId }
                },
                onRefresh = onRefresh,
                onEntityClick = onEntityClick,
                onSegmentClick = onSegmentClick
            )
        }

        // Daily Section - uses grouped tasks for category-based stacking
        item {
            TaskSection(
                liquidState = liquidState,
                title = stringResource(Res.string.events_daily),
                color = DailyColor,
                tasks = dailyTasks,
                groupedTasks = dailyTasksGrouped,
                sectionType = "Daily",
                isLoading = isLoading,
                onArchive = onArchiveTask,
                onRetryTask = onRetryTask,
                onRefresh = onRefresh,
                onEntityClick = onEntityClick,
                onSegmentClick = onSegmentClick
            )
        }
    }
    } // PullToRefreshBox

    // Liquid-glass undo action banner overlay (replaces Material snackbar)
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 100.dp)
    ) {
        io.github.fletchmckee.liquid.samples.app.ui.components.LiquidGlassActionBanner(
            visible = undoBannerVisible,
            message = undoBannerMessage,
            actionLabel = stringResource(Res.string.undo),
            onAction = undoBannerAction,
            onDismiss = { undoBannerVisible = false }
        )
    }
    } // Outer Box

    // Sensitive task confirmation dialog
    taskToReview?.let { task ->
        SensitiveTaskConfirmationDialog(
            task = task,
            onApprove = {
                onApproveTask(task.id)
                taskToReview = null
            },
            onReject = { reason ->
                onRejectTaskWithReason(task.id, reason)
                taskToReview = null
            },
            onDismiss = { taskToReview = null }
        )
    }
}

@Composable
fun TaskSection(
    liquidState: LiquidState,
    title: String,
    color: Color,
    tasks: List<TaskMetadata> = emptyList(),
    groupedTasks: Map<String, List<TaskMetadata>> = emptyMap(),
    sectionType: String,
    isLoading: Boolean = false,
    onArchive: (String) -> Unit = {},
    onApproveTask: (String) -> Unit = {},
    onRejectTask: (String) -> Unit = {},
    onRetryTask: (String) -> Unit = {},
    onRefresh: (() -> Unit)? = null,
    onEntityClick: (io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData) -> Unit = {},
    onSegmentClick: (io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation) -> Unit = {}
) {
    // Use global archive/pin state from Models
    val archivedIds by archivedTaskIdsState
    val pinnedIds by pinnedTaskIdsState

    // Filter out archived items and sort with pinned items at top
    val displayTasks = tasks
        .filter { it.id !in archivedIds }
        .sortedWith(compareByDescending<TaskMetadata> { pinnedIds.containsKey(it.id) }
            .thenByDescending { pinnedIds[it.id] ?: 0L })

    // Determine how many loading cards to show per section
    val loadingCardCount = when (sectionType) {
        "Featured" -> 2
        "Sensitive" -> 1
        "Daily" -> 2
        else -> 2
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title, color, isLoading)
        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> {
                // Show loading skeleton cards
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(loadingCardCount) { index ->
                        LoadingCard(
                            liquidState = liquidState,
                            color = color,
                            delayMillis = index * 150 // Stagger the animations
                        )
                    }
                }
            }
            displayTasks.isEmpty() && groupedTasks.isEmpty() -> {
                EmptyStateCard(
                    liquidState = liquidState,
                    sectionType = sectionType,
                    color = color,
                    onRefresh = onRefresh
                )
            }
            // Daily section with grouped tasks - render category stacks
            sectionType == "Daily" && groupedTasks.isNotEmpty() -> {
                val recentlyCompleted by recentlyCompletedCategoriesState
                val executingIds by executingTodoIdsState
                // Sort: categories with recently completed todos first, then categories with executing todos
                val sortedGroups = groupedTasks.entries.sortedWith(
                    compareByDescending<Map.Entry<String, List<TaskMetadata>>> { it.key in recentlyCompleted }
                        .thenByDescending { entry -> entry.value.any { it.id in executingIds } }
                )
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Render each category group
                    sortedGroups.forEach { (groupId, categoryTasks) ->
                        key(groupId) {
                            // Filter out archived tasks and sort by pinned
                            val filteredCategoryTasks = categoryTasks
                                .filter { it.id !in archivedIds }
                                .sortedWith(compareByDescending<TaskMetadata> { pinnedIds.containsKey(it.id) }
                                    .thenByDescending { pinnedIds[it.id] ?: 0L })

                            if (filteredCategoryTasks.isNotEmpty()) {
                                CategoryTaskStack(
                                    liquidState = liquidState,
                                    categoryGroupId = groupId,
                                    tasks = filteredCategoryTasks,
                                    sectionType = sectionType,
                                    color = color,
                                    pinnedIds = pinnedIds,
                                    onPinToggle = { id -> toggleTaskPin(id) },
                                    onArchive = onArchive,
                                    onApproveTask = onApproveTask,
                                    onRejectTask = onRejectTask,
                                    onRetryTask = onRetryTask,
                                    onEntityClick = onEntityClick,
                                    onSegmentClick = onSegmentClick
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    displayTasks.forEachIndexed { index, task ->
                        // Use key to ensure proper identity tracking when items are removed
                        key(task.id) {
                            // === LOGIC MAPPING ===
                            // isSuggested now uses metadata.isNew from JSON instead of hardcoded index
                            val isSuggested = sectionType == "Featured" && task.isNew

                            // isRecurring now uses metadata.isRecurring from JSON
                            val isRecurring = sectionType == "Daily" && task.isRecurring

                            // hasHistory now uses metadata.history from JSON instead of hardcoded index
                            val hasHistory = task.history.isNotEmpty()

                            TaskStack(
                                liquidState = liquidState,
                                metadata = task,
                                sectionType = sectionType,
                                isPinned = pinnedIds.containsKey(task.id),
                                isRecurring = isRecurring,
                                isSuggested = isSuggested,
                                hasHistory = hasHistory,
                                onPinToggle = { id -> toggleTaskPin(id) },
                                onArchive = onArchive,
                                onApprove = { id ->
                                    KlikLogger.i("EventsScreen", "Approve: $id")
                                    onApproveTask(id)
                                },
                                onReject = { id ->
                                    KlikLogger.i("EventsScreen", "Reject: $id")
                                    onRejectTask(id)
                                },
                                onRetry = onRetryTask,
                                onAdd = { id -> KlikLogger.i("EventsScreen", "Add Function: $id") },
                                onEntityClick = onEntityClick,
                                onSegmentClick = onSegmentClick
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Loading skeleton card with pulse animation
 */
@Composable
fun LoadingCard(
    liquidState: LiquidState,
    color: Color,
    delayMillis: Int = 0
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    // Create infinite pulsing animation
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(80.dp)
            .background(
                Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.7f),
                cardShape
            )
            .border(
                BorderStroke(0.5.dp, color.copy(alpha = 0.15f)),
                cardShape
            )
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                if (glassSettings.applyToCards) {
                    frost = glassSettings.frost
                    curve = glassSettings.curve
                    refraction = glassSettings.refraction
                }
                tint = color.copy(alpha = 0.03f)
            }
            .clip(cardShape)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title placeholder
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(14.dp)
                    .background(
                        Color.Gray.copy(alpha = alpha),
                        RoundedCornerShape(50)
                    )
            )
            // Subtitle placeholder
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(10.dp)
                    .background(
                        Color.Gray.copy(alpha = alpha * 0.6f),
                        RoundedCornerShape(50)
                    )
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    liquidState: LiquidState,
    sectionType: String,
    color: Color,
    onRefresh: (() -> Unit)? = null
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    val emptyMessage = when (sectionType) {
        "Featured" -> stringResource(Res.string.events_no_featured)
        "Sensitive" -> stringResource(Res.string.events_no_sensitive)
        "Daily" -> stringResource(Res.string.events_no_daily)
        else -> stringResource(Res.string.events_no_items)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.6f),
                cardShape
            )
            .border(
                BorderStroke(1.dp, color.copy(alpha = 0.2f)),
                cardShape
            )
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                if (glassSettings.applyToCards) {
                    frost = glassSettings.frost
                    curve = glassSettings.curve
                    refraction = glassSettings.refraction
                }
                tint = color.copy(alpha = 0.05f)
            }
            .clip(cardShape)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = emptyMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray.copy(alpha = 0.7f)
        )
        if (onRefresh != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Refresh",
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onRefresh() }
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Format a session ID like SESSION_20260326_075413_b9ec8ca1 into a readable date/time.
 */
private fun formatSessionIdForDisplay(sessionId: String): String {
    if (!sessionId.startsWith("SESSION_")) return sessionId
    val parts = sessionId.removePrefix("SESSION_").split("_")
    if (parts.size < 2) return sessionId
    val datePart = parts[0] // YYYYMMDD
    val timePart = parts[1] // HHMMSS
    if (datePart.length != 8 || timePart.length < 4) return sessionId
    val year = datePart.substring(0, 4)
    val month = datePart.substring(4, 6)
    val day = datePart.substring(6, 8)
    val hour = timePart.substring(0, 2)
    val minute = timePart.substring(2, 4)
    return "$year-$month-$day $hour:$minute"
}

@Composable
private fun SectionHeader(title: String, color: Color, isLoading: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(end = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .size(width = 4.dp, height = 24.dp)
                .liquid(rememberLiquidState()) {
                    edge = 0.01f
                    shape = RoundedCornerShape(50)
                    tint = color
                }
        ) {}
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal
        )
        Spacer(Modifier.weight(1f))
        if (isLoading) {
            Text(
                stringResource(Res.string.loading),
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Format tool category names from snake_case to Title Case for display.
 * e.g., "code_repository" -> "Code Repository", "web_search" -> "Web Search"
 */
private fun formatToolCategoryName(category: String): String {
    return category.split("_").joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercaseChar() }
    }
}

/**
 * CategoryTaskStack - Displays a group of tasks stacked by tool_category_group_id.
 * When collapsed, shows a single card with the category name and task count.
 * When expanded, shows all tasks in the category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTaskStack(
    liquidState: LiquidState,
    categoryGroupId: String,
    tasks: List<TaskMetadata>,
    sectionType: String,
    color: Color,
    pinnedIds: Map<String, Long>,
    onPinToggle: (String) -> Unit = {},
    onArchive: (String) -> Unit = {},
    onApproveTask: (String) -> Unit = {},
    onRejectTask: (String) -> Unit = {},
    onRetryTask: (String) -> Unit = {},
    onEntityClick: (io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData) -> Unit = {},
    onSegmentClick: (io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(24.dp)
    val glassSettings = LocalLiquidGlassSettings.current

    // Get display name from categoryGroupId (which is the sorted selected_sub_categories joined by ", ")
    val displayName = if (categoryGroupId == "uncategorized") {
        "Uncategorized"
    } else {
        categoryGroupId.split(", ").joinToString(" + ") { formatToolCategoryName(it) }
    }

    val taskCount = tasks.size

    // Track executing and recently completed states
    val executingIds by executingTodoIdsState
    val recentlyCompleted by recentlyCompletedCategoriesState
    val hasRecentlyCompleted = categoryGroupId in recentlyCompleted
    val executingCount = tasks.count { it.id in executingIds }
    val hasFailedTask = tasks.any { it.kkExecStatus?.equals("FAILED", ignoreCase = true) == true }

    // Animation values for deck visual
    // Unified Animation Spec
    val animationSpec = tween<Float>(durationMillis = 300)
    val animationSpecDp = tween<Dp>(durationMillis = 300)

    val contentAlpha by animateFloatAsState(targetValue = if (expanded) 0f else 1f, animationSpec = animationSpec)
    val dummyScaleBottom by animateFloatAsState(targetValue = if (expanded) 1f else 0.92f, animationSpec = animationSpec)
    val dummyOffsetBottom by animateDpAsState(targetValue = if (expanded) 0.dp else 16.dp, animationSpec = animationSpecDp)
    val dummyScaleMiddle by animateFloatAsState(targetValue = if (expanded) 1f else 0.96f, animationSpec = animationSpec)
    val dummyOffsetMiddle by animateDpAsState(targetValue = if (expanded) 0.dp else 8.dp, animationSpec = animationSpecDp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (!expanded) dummyOffsetBottom else 0.dp)
    ) {

        // Card background color — red tint if category has failed tasks
        val categoryCardBg = if (hasFailedTask) {
            StatusFailedColor.copy(alpha = 0.12f).compositeOver(Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f))
        } else {
            Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f)
        }
        val categoryBorder = if (hasFailedTask) {
            BorderStroke(1.dp, StatusFailedColor.copy(alpha = 0.4f))
        } else {
            BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f))
        }

        // --- Dummy Deck Cards (only if more than 1 task) ---
        if (taskCount > 1) {
            // Bottom dummy card
            if (!expanded || contentAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            scaleX = dummyScaleBottom
                            translationY = dummyOffsetBottom.toPx()
                            alpha = contentAlpha * 0.9f
                        }
                        .background(categoryCardBg, cardShape)
                        .border(categoryBorder, cardShape)
                        .liquid(liquidState) {
                            edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                            shape = cardShape
                            if (glassSettings.applyToCards) {
                                frost = glassSettings.frost
                                curve = glassSettings.curve
                                refraction = glassSettings.refraction
                            }
                            tint = color.copy(alpha = 0.03f)
                        }
                )
            }

            // Middle dummy card (only if more than 2 tasks)
            if (taskCount > 2) {
                if (!expanded || contentAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(horizontal = 16.dp)
                            .graphicsLayer {
                                scaleX = dummyScaleMiddle
                                translationY = dummyOffsetMiddle.toPx()
                                alpha = contentAlpha * 0.95f
                            }
                            .background(categoryCardBg, cardShape)
                            .border(categoryBorder, cardShape)
                            .liquid(liquidState) {
                                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                                shape = cardShape
                                if (glassSettings.applyToCards) {
                                    frost = glassSettings.frost
                                    curve = glassSettings.curve
                                    refraction = glassSettings.refraction
                                }
                                tint = color.copy(alpha = 0.04f)
                            }
                    )
                }
            }
        }

        // --- Main Content Column ---
        Column(
            modifier = Modifier.fillMaxWidth().animateContentSize(tween(300))
        ) {
            // Main category card (Header)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(80.dp) // Enforce consistent height matching dummy cards
                    .background(categoryCardBg, cardShape)
                    .border(categoryBorder, cardShape)
                    .liquid(liquidState) {
                        edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                        shape = cardShape
                        if (glassSettings.applyToCards) {
                            frost = glassSettings.frost
                            curve = glassSettings.curve
                            refraction = glassSettings.refraction
                        }
                        tint = color.copy(alpha = 0.05f)
                    }
                    .clip(cardShape)
                    .clickable {
                        expanded = !expanded
                        // Clear the red dot when user interacts with this category
                        if (hasRecentlyCompleted) {
                            recentlyCompletedCategoriesState.value = recentlyCompletedCategoriesState.value - categoryGroupId
                        }
                    }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Normal,
                                color = KlikBlack
                            )
                            // Red dot for recently completed execution
                            if (hasRecentlyCompleted) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(StatusFailedColor, CircleShape)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        if (executingCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Executing $executingCount/$taskCount",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusExecutingColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Text(
                                text = "$taskCount tasks",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) stringResource(Res.string.events_collapse) else stringResource(Res.string.events_expand),
                        tint = Color.Gray
                    )
                }
                // Executing count shown in header text; progress bar is on each individual todo card
            }

            // Expanded List
            if (expanded) {
                Spacer(Modifier.height(12.dp))

                // Individual tasks
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    tasks.forEach { task ->
                        key(task.id) {
                            TaskStack(
                                liquidState = liquidState,
                                metadata = task,
                                sectionType = sectionType,
                                isPinned = pinnedIds.containsKey(task.id),
                                isRecurring = task.isRecurring,
                                isSuggested = false,
                                hasHistory = task.history.isNotEmpty(),
                                onPinToggle = onPinToggle,
                                onArchive = onArchive,
                                onApprove = { id ->
                                    KlikLogger.i("EventsScreen", "Approve: $id")
                                    onApproveTask(id)
                                },
                                onReject = { id ->
                                    KlikLogger.i("EventsScreen", "Reject: $id")
                                    onRejectTask(id)
                                },
                                onRetry = onRetryTask,
                                onAdd = { id -> KlikLogger.i("EventsScreen", "Add Function: $id") },
                                onEntityClick = onEntityClick,
                                onSegmentClick = onSegmentClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskStack(
    liquidState: LiquidState,
    metadata: TaskMetadata,
    sectionType: String,
    isPinned: Boolean = false,
    isRecurring: Boolean = false,
    isSuggested: Boolean = false,
    hasHistory: Boolean = false,
    onPinToggle: (String) -> Unit = {},
    onArchive: (String) -> Unit = {},
    onApprove: (String) -> Unit = {},
    onReject: (String) -> Unit = {},
    onRetry: (String) -> Unit = {},
    onAdd: (String) -> Unit = {},
    onEntityClick: (io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData) -> Unit = {},
    onSegmentClick: (io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(24.dp)
    val glassSettings = LocalLiquidGlassSettings.current

    // Check if task has any related entities that should be shown when expanded
    val hasRelatedEntities = metadata.relatedMeetingId != null ||
            metadata.relatedSegments.isNotEmpty() ||
            metadata.relatedPeople.isNotEmpty() ||
            metadata.relatedProjects.isNotEmpty() ||
            metadata.relatedOrganizations.isNotEmpty() ||
            metadata.description?.isNotBlank() == true

    // Stackable logic: expandable if has history OR related entities
    val isStackable = hasHistory || hasRelatedEntities
    
    // Swipeable logic:
    val isSwipeable = sectionType != "Sensitive"

    // Animation Values (Deck visual) - Only animate if stackable
    val animationSpec = tween<Float>(durationMillis = 300)
    val animationSpecDp = tween<Dp>(durationMillis = 300)

    val dummyAlpha by animateFloatAsState(targetValue = if (expanded) 0f else 1f, animationSpec = animationSpec)
    val dummyScaleBottom by animateFloatAsState(targetValue = if (expanded) 1f else 0.92f, animationSpec = animationSpec)
    val dummyOffsetBottom by animateDpAsState(targetValue = if (expanded) 0.dp else 16.dp, animationSpec = animationSpecDp)
    val dummyScaleMiddle by animateFloatAsState(targetValue = if (expanded) 1f else 0.96f, animationSpec = animationSpec)
    val dummyOffsetMiddle by animateDpAsState(targetValue = if (expanded) 0.dp else 8.dp, animationSpec = animationSpecDp)

    // Outer Swipe Wrapper: Handles "Entire Stack" swipe when collapsed
    SwipableCardWrapper(
        isSwipeable = isStackable && !expanded && sectionType != "Sensitive",
        sectionType = sectionType,
        metadata = metadata,
        liquidState = liquidState,
        onApprove = onApprove,
        onReject = onReject,
        onPinToggle = onPinToggle,
        onArchive = onArchive
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isStackable) dummyOffsetBottom else 0.dp)
        ) {

            // --- Dummy Deck Cards (only if history exists) ---
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
            // --- Main Content Column ---
            Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                
                // 1. The Main Card
                // Inner Swipe Wrapper: Handles individual swipe when expanded (or if not stackable)
                SwipableCardWrapper(
                    isSwipeable = (!isStackable || expanded) && isSwipeable,
                    sectionType = sectionType,
                    metadata = metadata,
                    liquidState = liquidState,
                    onApprove = onApprove,
                    onReject = onReject,
                    onPinToggle = onPinToggle,
                    onArchive = onArchive
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        TaskCardContent(
                            liquidState = liquidState,
                            metadata = metadata,
                            sectionType = sectionType,
                            isPinned = isPinned,
                            isRecurring = isRecurring,
                            isSuggested = isSuggested,
                            onClick = if (isStackable) { { expanded = !expanded } } else { {} },
                            onApprove = onApprove,
                            onReject = onReject,
                            onRetry = onRetry,
                            onAdd = onAdd
                        )
                    }
                }

                // 2. Related Entities Section (when expanded)
                if (expanded) {
                    TaskRelatedEntitiesSection(
                        liquidState = liquidState,
                        metadata = metadata,
                        sectionType = sectionType,
                        onEntityClick = onEntityClick,
                        onSegmentClick = onSegmentClick
                    )
                }

                // 3. The Expanded Stack (History) - loaded from JSON
                if (expanded && isStackable && metadata.history.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        metadata.history.forEach { historyItem ->
                            val historyMetadata = metadata.copy(subtitle = historyItem.subtitle)
                            SwipableCardWrapper(
                                isSwipeable = true,
                                sectionType = sectionType,
                                metadata = historyMetadata,
                                liquidState = liquidState,
                                onApprove = { onApprove(metadata.id) },
                                onReject = { onReject(metadata.id) },
                                onPinToggle = { }, onArchive = { }
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    TaskCardContent(
                                        liquidState = liquidState,
                                        metadata = historyMetadata,
                                        sectionType = sectionType,
                                        isPinned = false,
                                        onClick = { }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Related Entities Section - Shows sessions, segments, people, projects, organizations
 * Consistent design with WorkLifeScreen entity cards
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskRelatedEntitiesSection(
    liquidState: LiquidState,
    metadata: TaskMetadata,
    sectionType: String,
    onEntityClick: (io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData) -> Unit = {},
    onSegmentClick: (io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation) -> Unit = {}
) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(24.dp)

    // Entity ID → display name lookups (relatedPeople/Projects/Orgs store resolved IDs)
    val people by peopleState
    val projects by projectsState
    val organizations by organizationsState
    val personIdToName = remember(people) { people.associate { it.id to it.name } }
    val projectIdToName = remember(projects) { projects.associate { it.id to it.name } }
    val orgIdToName = remember(organizations) { organizations.associate { it.id to it.name } }

    val hasAnyRelatedEntities = metadata.relatedMeetingId != null ||
            metadata.relatedSegments.isNotEmpty() ||
            metadata.relatedPeople.isNotEmpty() ||
            metadata.relatedProjects.isNotEmpty() ||
            metadata.relatedOrganizations.isNotEmpty() ||
            metadata.description?.isNotBlank() == true

    if (!hasAnyRelatedEntities) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f), cardShape)
            .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.08f)), cardShape)
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                if (glassSettings.applyToCards) {
                    frost = glassSettings.frost
                    curve = glassSettings.curve
                    refraction = glassSettings.refraction
                }
                tint = Color.Black.copy(alpha = 0.02f)
            }
            .clip(cardShape)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (metadata.description?.isNotBlank() == true) {
                Text(stringResource(Res.string.events_description), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Text(
                    metadata.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF555555)
                )
            }

            if (metadata.relatedMeetingId != null) {
                Text(stringResource(Res.string.events_related_session), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Text(
                    formatSessionIdForDisplay(metadata.relatedMeetingId),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2196F3),
                    modifier = Modifier
                        .clickable {
                            onSegmentClick(io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation(
                                sessionId = metadata.relatedMeetingId,
                                segmentId = "0"
                            ))
                        }
                        .background(Color(0xFF2196F3).copy(alpha = 0.1f), RoundedCornerShape(50))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            if (metadata.relatedSegments.isNotEmpty()) {
                Text(stringResource(Res.string.events_related_sentences), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    metadata.relatedSegments.take(10).forEach { segmentId ->
                        Text(
                            "Sentence $segmentId",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2196F3),
                            modifier = Modifier
                                .clickable {
                                    val sessionId = metadata.relatedMeetingId
                                    if (sessionId != null) {
                                        onSegmentClick(io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation(
                                            sessionId = sessionId,
                                            segmentId = segmentId.toString()
                                        ))
                                    }
                                }
                                .background(Color(0xFF2196F3).copy(alpha = 0.1f), RoundedCornerShape(50))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (metadata.relatedSegments.size > 10) {
                        Text(
                            "+${metadata.relatedSegments.size - 10} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            modifier = Modifier
                                .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(50))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (metadata.relatedPeople.isNotEmpty()) {
                Text(stringResource(Res.string.events_related_people), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    metadata.relatedPeople.take(5).forEach { personId ->
                        val displayName = personIdToName[personId] ?: personId
                        Text(
                            displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2196F3),
                            modifier = Modifier
                                .clickable {
                                    onEntityClick(io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData(
                                        entityType = io.github.fletchmckee.liquid.samples.app.ui.components.EntityType.PERSON,
                                        entityId = personId
                                    ))
                                }
                                .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(50))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (metadata.relatedPeople.size > 5) {
                        Text(
                            "+${metadata.relatedPeople.size - 5} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            modifier = Modifier
                                .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(50))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (metadata.relatedProjects.isNotEmpty()) {
                Text(stringResource(Res.string.events_related_projects), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    metadata.relatedProjects.take(3).forEach { projectId ->
                        val displayName = projectIdToName[projectId] ?: projectId
                        Text(
                            displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF555555),
                            modifier = Modifier
                                .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(50))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (metadata.relatedProjects.size > 3) {
                        Text(
                            "+${metadata.relatedProjects.size - 3} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            modifier = Modifier
                                .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(50))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (metadata.relatedOrganizations.isNotEmpty()) {
                Text(stringResource(Res.string.events_related_organizations), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    metadata.relatedOrganizations.take(3).forEach { orgId ->
                        val displayName = orgIdToName[orgId] ?: orgId
                        Text(
                            displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2196F3),
                            modifier = Modifier
                                .clickable {
                                    onEntityClick(io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData(
                                        entityType = io.github.fletchmckee.liquid.samples.app.ui.components.EntityType.ORGANIZATION,
                                        entityId = orgId
                                    ))
                                }
                                .background(Color(0xFF555555).copy(alpha = 0.1f), RoundedCornerShape(50))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (metadata.relatedOrganizations.size > 3) {
                        Text(
                            "+${metadata.relatedOrganizations.size - 3} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            modifier = Modifier
                                .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(50))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipableCardWrapper(
    isSwipeable: Boolean,
    sectionType: String,
    metadata: TaskMetadata,
    liquidState: LiquidState,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
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

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Pin toggle - return false to reset swipe position
                    HapticService.lightImpact()
                    onPinToggle(metadata.id)
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Archive - call handler and return false to reset swipe position
                    // The item will be removed by filtering in TaskSection
                    HapticService.mediumImpact()
                    onArchive(metadata.id)
                    false
                }
                else -> false
            }
        }
    )

    val (startColor, startIcon) = when {
        sectionType == "Featured" -> Pair(Color(0xFFFFB020), CustomIcons.PushPin) // Use Pin for Featured
        else -> Pair(Color(0xFFFFCC00), CustomIcons.PushPin)
    }
    val (endColor, endIcon) = when {
        sectionType == "Featured" -> Pair(Color(0xFFFF3B30), CustomIcons.Archive) // Use Archive for Featured
        else -> Pair(Color(0xFFFF3B30), CustomIcons.Archive)
    }

    SwipeToDismissBox(
        modifier = Modifier.fillMaxWidth(),
        state = dismissState,
        backgroundContent = {
            val (color, icon) = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Pair(startColor, startIcon)
                SwipeToDismissBoxValue.EndToStart -> Pair(endColor, endIcon)
                else -> Pair(Color.Transparent, startIcon)
            }
            val glassSettings = LocalLiquidGlassSettings.current
            val cardShape = RoundedCornerShape(24.dp)
            Box(
                modifier = Modifier.fillMaxSize()
                    .liquid(liquidState) {
                       edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                       shape = cardShape
                       if (glassSettings.applyToCards) {
                           frost = glassSettings.frost
                           curve = glassSettings.curve
                           refraction = glassSettings.refraction
                       }
                       tint = color.copy(alpha = 0.2f)
                    },
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
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
 * Reusable Card Content
 */
@Composable
fun TaskCardContent(
    liquidState: LiquidState,
    metadata: TaskMetadata,
    sectionType: String,
    isPinned: Boolean = false,
    isRecurring: Boolean = false,
    isSuggested: Boolean = false,
    backgroundAlphaOverride: Float? = null,
    onClick: () -> Unit = {},
    onApprove: ((String) -> Unit)? = null,
    onReject: ((String) -> Unit)? = null,
    onRetry: ((String) -> Unit)? = null,
    onAdd: ((String) -> Unit)? = null
) {
    val glassSettings = LocalLiquidGlassSettings.current

    // Entity ID → display name lookups (relatedPeople/Projects/Orgs store resolved IDs)
    val people by peopleState
    val projects by projectsState
    val organizations by organizationsState
    val personIdToName = remember(people) { people.associate { it.id to it.name } }
    val projectIdToName = remember(projects) { projects.associate { it.id to it.name } }
    val orgIdToName = remember(organizations) { organizations.associate { it.id to it.name } }
    val cardShape = RoundedCornerShape(24.dp)
    
    val isFailed = metadata.kkExecStatus?.equals("FAILED", ignoreCase = true) == true
    val isExecuting = metadata.id in executingTodoIdsState.value
    val cardBackgroundColor = when {
        isFailed -> StatusFailedColor.copy(alpha = 0.12f).compositeOver(Color.White)
        isExecuting -> StatusExecutingColor.copy(alpha = 0.06f).compositeOver(Color.White)
        isPinned && sectionType != "Featured" -> Color(0xFFFFCC00).copy(alpha = 0.08f).compositeOver(Color.White)
        else -> Color.White
    }

    val border = when {
        isFailed -> BorderStroke(1.dp, StatusFailedColor.copy(alpha = 0.4f))
        isExecuting -> BorderStroke(1.dp, StatusExecutingColor.copy(alpha = 0.4f))
        isSuggested -> BorderStroke(1.dp, FeaturedColor.copy(alpha=0.5f))
        else -> null
    }
    val baseAlpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f
    val cardAlpha = backgroundAlphaOverride ?: baseAlpha

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBackgroundColor.copy(alpha = cardAlpha), cardShape)
            // Apply subtle border to ALL cards, unless a specific border is meant to override (like 'Suggested')
            .border(border ?: BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape) 
            .liquid(liquidState) {
                edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                shape = cardShape
                if (glassSettings.applyToCards) {
                    frost = glassSettings.frost
                    curve = glassSettings.curve
                    refraction = glassSettings.refraction
                }
                tint = if (isSuggested) FeaturedColor.copy(alpha=0.05f) else Color.Transparent 
            }
            .clip(cardShape)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = if (isSuggested) Alignment.Top else Alignment.CenterVertically
        ) {
            // LEFT CONTENT
            Column(modifier = Modifier.weight(1f)) {
                
                if (isSuggested) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            metadata.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = KlikBlack,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (metadata.isNew) {
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(Res.string.new_label), style = MaterialTheme.typography.labelSmall, color = FeaturedColor)
                        }
                    }
                     Spacer(Modifier.height(8.dp))
                    metadata.suggestionText?.let { suggestion ->
                        Text(
                            suggestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 3,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                     Spacer(Modifier.height(8.dp))
                    // Text removed (Estimated Time) and Duplication Fixed
                } 
                else {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            metadata.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = KlikBlack,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (isPinned && sectionType != "Featured" && sectionType != "Sensitive") {
                            Spacer(Modifier.width(6.dp))
                            Icon(CustomIcons.PushPin, "Pinned", tint = Color(0xFFFFCC00), modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        metadata.completedInfo ?: metadata.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    // Tools execution row (for KK_exec tasks with tools or execution steps)
                    if (metadata.isFromKKExec && (metadata.plannedTools.isNotEmpty() || metadata.executionSteps.isNotEmpty())) {
                        Spacer(Modifier.height(8.dp))
                        val executionState = when (metadata.kkExecStatus?.uppercase()) {
                            "RUNNING", "EVALUATING", "APPROVED" -> ToolExecutionState.EXECUTING
                            "COMPLETED" -> ToolExecutionState.COMPLETED
                            "FAILED" -> ToolExecutionState.FAILED
                            else -> ToolExecutionState.PENDING
                        }
                        ToolsExecutionRow(
                            plannedTools = metadata.plannedTools,
                            executionSteps = metadata.executionSteps,
                            executionState = executionState,
                            currentStep = metadata.currentExecutingStep
                        )

                        // Show execution outcome if available
                        if (metadata.executionOutcome != null && executionState in listOf(ToolExecutionState.COMPLETED, ToolExecutionState.FAILED)) {
                            ExecutionOutcomeDisplay(
                                outcome = metadata.executionOutcome,
                                links = metadata.executionLinks,
                                success = executionState == ToolExecutionState.COMPLETED
                            )
                        }
                    }
                }
            }
            
            // RIGHT CONTENT
            Row(verticalAlignment = Alignment.CenterVertically) {

                // KK_exec status indicator (for tasks from backend)
                if (metadata.isFromKKExec) {
                    // Show execution status if task is from KK_exec
                    if (metadata.cannotExecute) {
                        CannotExecuteTag()
                        Spacer(Modifier.width(8.dp))
                    } else if (metadata.kkExecStatus != null) {
                        KKExecStatusIndicator(
                            status = metadata.kkExecStatus,
                            cannotExecute = false
                        )
                        Spacer(Modifier.width(8.dp))
                    } else if (metadata.isAutoExecutable && sectionType == "Daily") {
                        // Show auto-executable indicator for daily tasks
                        AutoExecuteTag()
                        Spacer(Modifier.width(8.dp))
                    }
                } else {
                    // Show main task status for non-KK_exec tasks
                    TaskStatusIndicator(status = metadata.status)
                    Spacer(Modifier.width(8.dp))
                }

                if (isRecurring) {
                    RecurringTag()
                    Spacer(Modifier.width(8.dp))
                }

                // Action buttons — driven by kkExecStatus state machine:
                // PENDING → can execute (no user action needed here)
                // IN_REVIEW → Approve / Reject
                // APPROVED → auto-transitions to RUNNING
                // RUNNING / EVALUATING → in progress (no action)
                // COMPLETED / FAILED → Retry
                // REJECTED / CANNOT_EXECUTE / ARCHIVED → terminal
                val status = metadata.kkExecStatus?.uppercase()

                // Action icon buttons — stacked vertically, icon-only
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Retry: only for COMPLETED or FAILED
                    if (metadata.isFromKKExec && onRetry != null && status in listOf("COMPLETED", "FAILED")) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(StatusExecutingColor.copy(alpha = 0.1f), RoundedCornerShape(50))
                                .clickable { onRetry(metadata.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Refresh, "Retry", tint = StatusExecutingColor, modifier = Modifier.size(16.dp))
                        }
                    }

                    if (isSuggested && onAdd != null) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(FeaturedColor.copy(alpha = 0.1f), RoundedCornerShape(50))
                                .clickable { onAdd(metadata.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Add, null, tint = FeaturedColor, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Approve/Reject: for IN_REVIEW status (sensitive todos enter this status at creation)
                    if (metadata.isFromKKExec && status == "IN_REVIEW" && onApprove != null && onReject != null) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF4CAF50).copy(alpha = 0.1f), RoundedCornerShape(50))
                                .clickable { onApprove(metadata.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Check, "Approve", tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(SensitiveColor.copy(alpha = 0.1f), RoundedCornerShape(50))
                                .clickable { onReject(metadata.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Close, "Reject", tint = SensitiveColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
        // Progress bar for executing tasks
        if (isExecuting) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Executing...",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusExecutingColor,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = StatusExecutingColor,
                trackColor = StatusExecutingColor.copy(alpha = 0.1f)
            )
        }
        } // Column
    }
}

@Composable
fun RecurringTag() {
    Row(
        modifier = Modifier
            .background(Color(0xFFE8F5E9), RoundedCornerShape(50))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Refresh, null, tint = DailyColor, modifier = Modifier.size(10.dp))
        Spacer(Modifier.width(2.dp))
        Text("Recurring", style = MaterialTheme.typography.labelSmall, color = DailyColor, fontSize = androidx.compose.ui.unit.TextUnit.Unspecified)
    }
}

/**
 * KK_exec Status Indicator - shows execution status for tasks from KK_exec backend
 * Status values: PENDING, IN_REVIEW, RUNNING, EVALUATING, COMPLETED, FAILED, CANNOT_EXECUTE, APPROVED, REJECTED, ARCHIVED
 * Also shows CANNOT_EXECUTE for f_cannotdo category tasks
 */
@Composable
fun KKExecStatusIndicator(
    status: String?,
    cannotExecute: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Determine color and icon based on status
    val (color, icon, label) = when {
        cannotExecute -> Triple(StatusCannotExecuteColor, Icons.Filled.Close, "Cannot Do")
        status == null -> return // No status to show
        status.equals("PENDING", ignoreCase = true) -> Triple(StatusPendingColor, Icons.Filled.PlayArrow, "Pending")
        status.equals("RUNNING", ignoreCase = true) -> Triple(StatusExecutingColor, Icons.Filled.Refresh, "Running")
        status.equals("EVALUATING", ignoreCase = true) -> Triple(StatusExecutingColor, Icons.Filled.Refresh, "Processing")
        status.equals("APPROVED", ignoreCase = true) -> Triple(StatusExecutingColor, Icons.Filled.Refresh, "Running")
        status.equals("CANNOT_EXECUTE", ignoreCase = true) -> Triple(StatusCannotExecuteColor, Icons.Filled.Close, "Cannot Execute")
        status.equals("IN_REVIEW", ignoreCase = true) -> return // No badge for in_review
        status.equals("COMPLETED", ignoreCase = true) -> return // No badge for completed
        status.equals("APPROVED", ignoreCase = true) -> Triple(StatusApprovedColor, Icons.Filled.Check, "Approved")
        status.equals("REJECTED", ignoreCase = true) -> Triple(StatusRejectedColor, Icons.Filled.Close, "Rejected")
        status.equals("ARCHIVED", ignoreCase = true) -> Triple(StatusCannotExecuteColor, Icons.Filled.Close, "Archived")
        status.equals("FAILED", ignoreCase = true) -> return // No badge — card background turns red
        else -> return // Unknown status
    }

    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Animated icon for RUNNING/EVALUATING status
        if (status?.equals("RUNNING", ignoreCase = true) == true || status?.equals("EVALUATING", ignoreCase = true) == true) {
            val infiniteTransition = rememberInfiniteTransition(label = "status_spin")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier
                    .size(12.dp)
                    .graphicsLayer { rotationZ = rotation }
            )
        } else {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

/**
 * Cannot Execute Tag - for f_cannotdo category tasks
 * Shows a gray "Cannot Do" indicator
 */
@Composable
fun CannotExecuteTag() {
    Row(
        modifier = Modifier
            .background(StatusCannotExecuteColor.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            Icons.Filled.Close,
            contentDescription = "Cannot execute",
            tint = StatusCannotExecuteColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            "Cannot Do",
            style = MaterialTheme.typography.labelSmall,
            color = StatusCannotExecuteColor
        )
    }
}

/**
 * Auto Execute Tag - for auto-executable tasks (categories a-d)
 */
@Composable
fun AutoExecuteTag() {
    Row(
        modifier = Modifier
            .background(DailyColor.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            Icons.Filled.PlayArrow,
            contentDescription = "Auto executable",
            tint = DailyColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            "Auto",
            style = MaterialTheme.typography.labelSmall,
            color = DailyColor
        )
    }
}

/**
 * Task Status Indicator - shows the main TaskStatus as a small badge
 * Only displayed for statuses that are meaningful to show: IN_REVIEW, IN_PROGRESS, APPROVED, REJECTED, COMPLETED
 */
@Composable
fun TaskStatusIndicator(
    status: TaskStatus,
    modifier: Modifier = Modifier
) {
    val (color, icon) = when (status) {
        TaskStatus.IN_REVIEW -> Pair(StatusInReviewColor, Icons.Filled.Star)
        TaskStatus.IN_PROGRESS -> Pair(StatusInProgressColor, Icons.Filled.Refresh)
        TaskStatus.APPROVED -> Pair(StatusApprovedColor, Icons.Filled.Check)
        TaskStatus.REJECTED -> Pair(StatusRejectedColor, Icons.Filled.Close)
        TaskStatus.COMPLETED -> Pair(StatusCompletedColor, Icons.Filled.Check)
        else -> return // Don't show indicator for PENDING or ARCHIVED
    }

    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            icon,
            contentDescription = status.displayName,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Text(
            status.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

// ==================== Tool Execution Display ====================

/**
 * Maps tool names to icons for display.
 * Returns a letter-based icon for unknown tools.
 */
private fun getToolIcon(toolName: String): ImageVector {
    return when {
        toolName.contains("search", ignoreCase = true) -> Icons.Filled.Share
        toolName.contains("calendar", ignoreCase = true) -> Icons.Filled.List
        toolName.contains("email", ignoreCase = true) -> Icons.Filled.Share
        toolName.contains("code", ignoreCase = true) -> Icons.Filled.Build
        toolName.contains("github", ignoreCase = true) -> Icons.Filled.Build
        toolName.contains("file", ignoreCase = true) -> Icons.Filled.List
        toolName.contains("task_complete", ignoreCase = true) -> Icons.Filled.CheckCircle
        toolName.contains("read", ignoreCase = true) -> Icons.Filled.List
        toolName.contains("write", ignoreCase = true) -> Icons.Filled.Build
        toolName.contains("api", ignoreCase = true) -> Icons.Filled.Share
        toolName.contains("person", ignoreCase = true) -> Icons.Filled.Person
        else -> Icons.Filled.Star
    }
}

/**
 * Get short display name for a tool (max 2 chars).
 */
private fun getToolShortName(toolName: String): String {
    // Extract meaningful short name from tool name
    val parts = toolName.replace("_", " ").split(" ")
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        toolName.length >= 2 -> toolName.take(2).uppercase()
        else -> toolName.uppercase()
    }
}

/**
 * Execution state for tools display.
 */
enum class ToolExecutionState {
    PENDING,    // Not started yet - static display
    EXECUTING,  // Currently running - animated flow
    COMPLETED,  // Finished - show success/fail status
    FAILED      // Finished with error
}

/**
 * ToolsExecutionRow - Displays execution tools with real-time progress.
 *
 * States:
 * - PENDING: Static row of tool icons (from plannedTools)
 * - EXECUTING: Animated flow highlighting current step
 * - COMPLETED/FAILED: Each tool colored by success status
 *
 * @param plannedTools List of tools needed for execution
 * @param executionSteps Completed execution steps with status
 * @param executionState Current overall state
 * @param currentStep Current step being executed (1-indexed)
 */
@Composable
fun ToolsExecutionRow(
    plannedTools: List<String>,
    executionSteps: List<io.github.fletchmckee.liquid.samples.app.model.ExecutionStepUi>,
    executionState: ToolExecutionState,
    currentStep: Int? = null,
    modifier: Modifier = Modifier
) {
    if (plannedTools.isEmpty() && executionSteps.isEmpty()) return

    // Combine planned tools with executed steps for display
    val displayTools = if (executionSteps.isNotEmpty()) {
        executionSteps.map { it.toolName }
    } else {
        plannedTools
    }

    if (displayTools.isEmpty()) return

    // Animation for executing state
    val infiniteTransition = rememberInfiniteTransition(label = "tools_flow")
    val flowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "flow_progress"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(
            space = if (executionState == ToolExecutionState.EXECUTING) 4.dp else 8.dp,
            alignment = Alignment.Start
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayTools.forEachIndexed { index, toolName ->
            val stepData = executionSteps.getOrNull(index)
            val isCurrentStep = executionState == ToolExecutionState.EXECUTING &&
                               currentStep != null && index == currentStep - 1

            // Determine icon color based on state
            val iconColor = when {
                executionState == ToolExecutionState.PENDING -> Color.Gray.copy(alpha = 0.5f)
                executionState == ToolExecutionState.EXECUTING -> {
                    when {
                        stepData != null && stepData.success -> StatusSuccessColor
                        stepData != null && !stepData.success -> StatusFailedColor
                        isCurrentStep -> StatusExecutingColor
                        index < (currentStep ?: 0) - 1 -> StatusSuccessColor.copy(alpha = 0.7f)
                        else -> Color.Gray.copy(alpha = 0.4f)
                    }
                }
                stepData != null && stepData.success -> StatusSuccessColor
                stepData != null && !stepData.success -> StatusFailedColor
                else -> Color.Gray.copy(alpha = 0.5f)
            }

            // Show connector arrow for executing state
            if (executionState == ToolExecutionState.EXECUTING && index > 0) {
                val connectorAlpha = when {
                    index < (currentStep ?: 0) -> 1f
                    index == currentStep -> flowProgress
                    else -> 0.3f
                }
                Text(
                    "→",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusExecutingColor.copy(alpha = connectorAlpha),
                    modifier = Modifier.graphicsLayer {
                        if (index == currentStep) {
                            scaleX = 1f + flowProgress * 0.3f
                            scaleY = 1f + flowProgress * 0.3f
                        }
                    }
                )
            }

            // Tool icon/badge
            ToolBadge(
                toolName = toolName,
                color = iconColor,
                isExecuting = isCurrentStep,
                isCompleted = stepData != null
            )
        }
    }
}

/**
 * Individual tool badge with icon or letter.
 */
@Composable
private fun ToolBadge(
    toolName: String,
    color: Color,
    isExecuting: Boolean = false,
    isCompleted: Boolean = false
) {
    val shape = RoundedCornerShape(50)

    // Pulse animation for executing state
    val infiniteTransition = rememberInfiniteTransition(label = "tool_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .size(24.dp)
            .background(
                color = color.copy(alpha = if (isExecuting) pulseAlpha * 0.2f else 0.15f),
                shape = shape
            )
            .border(
                width = if (isExecuting) 1.5.dp else 0.5.dp,
                color = color.copy(alpha = if (isExecuting) pulseAlpha else 0.5f),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Use short name text instead of icon for consistency
        Text(
            text = getToolShortName(toolName),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = androidx.compose.ui.unit.TextUnit.Unspecified,
            maxLines = 1
        )
    }
}

/**
 * Execution outcome display - shows the actual result of task execution.
 * Displays structured outcomes from agent_outputs (e.g., "Sent email to X",
 * "Posted to #channel", scraped data summary) with clickable source links.
 * Falls back to task_complete output if no structured outcome is available.
 */
@Composable
fun ExecutionOutcomeDisplay(
    outcome: String,
    links: List<String> = emptyList(),
    success: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (success) Icons.Filled.CheckCircle else Icons.Filled.Close,
                contentDescription = if (success) "Success" else "Failed",
                tint = if (success) StatusSuccessColor else StatusFailedColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = outcome,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray.copy(alpha = 0.8f)
            )
        }
        if (links.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            links.forEach { link ->
                Text(
                    text = link,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2196F3),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 20.dp)
                )
            }
        }
    }
}

// ==================== Sensitive Task Confirmation Dialog ====================

@Composable
fun SensitiveTaskConfirmationDialog(
    task: TaskMetadata,
    onApprove: () -> Unit,
    onReject: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showRejectInput by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    io.github.fletchmckee.liquid.samples.app.ui.components.LiquidGlassDialogScaffold(
        onDismissRequest = onDismiss,
        title = "Review Sensitive Task",
        icon = {
            Icon(
                Icons.Filled.Warning,
                contentDescription = "Sensitive",
                tint = SensitiveColor,
                modifier = Modifier.size(32.dp)
            )
        },
        confirmText = if (showRejectInput) "Confirm Reject" else "Approve",
        confirmColor = if (showRejectInput) SensitiveColor else Color(0xFF4CAF50),
        onConfirm = {
            if (showRejectInput) {
                onReject(rejectReason.ifBlank { "Rejected by user" })
            } else {
                onApprove()
            }
        },
        dismissText = if (showRejectInput) "Back" else "Reject",
        onDismissAction = {
            if (showRejectInput) {
                showRejectInput = false
                rejectReason = ""
            } else {
                showRejectInput = true
            }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            // Task title
            Text(
                task.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = KlikBlack
            )

            // Task description
            if (!task.description.isNullOrBlank()) {
                Text(
                    task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Sensitivity reasons
            if (task.kkExecSensitivityReasons.isNotEmpty()) {
                Column {
                    Text(
                        "Why this needs approval:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SensitiveColor
                    )
                    Spacer(Modifier.height(4.dp))
                    task.kkExecSensitivityReasons.forEach { reason ->
                        Row(
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Meeting context
            if (task.kkExecSessionId != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "From session: ${task.kkExecSessionId.take(8)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Reject reason input (optional)
            if (showRejectInput) {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = { Text(stringResource(Res.string.events_reason_label)) },
                    placeholder = { Text(stringResource(Res.string.events_reason_placeholder), color = Color.Gray.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        }
    }
}
