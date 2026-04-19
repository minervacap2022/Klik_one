package io.github.fletchmckee.liquid.samples.app.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import io.github.fletchmckee.liquid.samples.app.ui.icons.CustomIcons
import io.github.fletchmckee.liquid.samples.app.ui.icons.PushPin
import io.github.fletchmckee.liquid.samples.app.ui.icons.Archive
import io.github.fletchmckee.liquid.samples.app.ui.icons.FolderOpen
import io.github.fletchmckee.liquid.samples.app.platform.OAuthBrowser
import io.github.fletchmckee.liquid.samples.app.ui.components.AiGeneratedBadge
import io.github.fletchmckee.liquid.samples.app.ui.components.AiContentDisclaimer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.samples.app.theme.EngageColor
import io.github.fletchmckee.liquid.samples.app.theme.LiveColor
import io.github.fletchmckee.liquid.samples.app.platform.HapticService
import io.github.fletchmckee.liquid.samples.app.theme.StriveColor
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.domain.entity.DailyBriefing
import io.github.fletchmckee.liquid.samples.app.domain.entity.Insights
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.model.formatDateForDisplay
import io.github.fletchmckee.liquid.samples.app.model.archivedMeetingIdsState
import io.github.fletchmckee.liquid.samples.app.model.pinnedMeetingIdsState
import io.github.fletchmckee.liquid.samples.app.model.archiveMeeting
import io.github.fletchmckee.liquid.samples.app.model.unarchiveMeeting
import io.github.fletchmckee.liquid.samples.app.model.toggleMeetingPin
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.delay
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings
import io.github.fletchmckee.liquid.samples.app.theme.LocalInsightCardColors
import io.github.fletchmckee.liquid.samples.app.ui.components.FeedbackPopup
import io.github.fletchmckee.liquid.samples.app.ui.components.FeedbackData
import io.github.fletchmckee.liquid.samples.app.utils.onLongPress
import io.github.fletchmckee.liquid.samples.app.data.network.HttpClient
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import io.github.fletchmckee.liquid.samples.app.theme.LocalSnackbarHostState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import io.github.fletchmckee.liquid.samples.app.ui.components.LiquidPullToRefreshIndicator
import androidx.compose.ui.platform.LocalDensity
import io.github.fletchmckee.liquid.samples.app.ui.utils.SegmentNavigationUtils
import io.github.fletchmckee.liquid.samples.app.ui.components.ShimmerTextBlock
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import liquid_root.samples.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import io.github.fletchmckee.liquid.samples.app.domain.entity.MeetingSource

/**
 * Extract date from session ID format: SESSION_YYYYMMDD_HHMMSS_hash
 */
private fun extractDateFromSessionId(sessionId: String): LocalDate {
    require(sessionId.startsWith("SESSION_")) { "Invalid session ID format: must start with SESSION_" }
    val datePart = sessionId.removePrefix("SESSION_").take(8) // YYYYMMDD
    require(datePart.length == 8) { "Invalid session ID format: date portion must be 8 digits, got '${datePart}'" }
    return LocalDate.parse("${datePart.substring(0, 4)}-${datePart.substring(4, 6)}-${datePart.substring(6, 8)}")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    isCalendarExpanded: Boolean = false,
    isLoading: Boolean = false,
    isRefreshing: Boolean = false,  // Pull-to-refresh state from MainApp
    isLlmDataLoading: Boolean = false,
    meetings: List<Meeting> = emptyList(),
    dailyBriefing: DailyBriefing? = null,
    insights: Insights? = null,
    speakerMap: Map<String, String> = emptyMap(),
    onRefreshMeetings: () -> Unit = {},
    onArchiveMeeting: (String) -> Unit = {},
    expandSessionId: String? = null,
    expandSegmentId: String? = null,  // Renamed from expandSegmentText for clarity
    expandSegmentText: String? = null,  // Keep for backward compatibility
    onSessionExpanded: () -> Unit = {},
    onDateChange: (LocalDate) -> Unit = {},
    onContentOverlap: (Boolean) -> Unit = {},
    topDockHeightPx: Int = 0,
    // Entity highlighting parameters
    tasks: List<io.github.fletchmckee.liquid.samples.app.model.TaskMetadata> = emptyList(),
    projects: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Project> = emptyList(),
    people: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Person> = emptyList(),
    organizations: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Organization> = emptyList(),
    onEntityClick: (io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData) -> Unit = {},
    onSegmentClick: (io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation) -> Unit = {}
) {
    val liquidState = rememberLiquidState()
    val snackbarScope = rememberCoroutineScope()

    // Liquid-glass action banner state for archive undo
    var undoBannerVisible by remember { mutableStateOf(false) }
    var undoBannerMessage by remember { mutableStateOf("") }
    var undoBannerAction by remember { mutableStateOf<() -> Unit>({}) }

    // Pull-to-refresh state - use the passed-in isRefreshing prop from MainApp
    val pullRefreshState = rememberPullToRefreshState()

    // Debug log for refresh state changes
    LaunchedEffect(isRefreshing) {
        KlikLogger.d("CalendarScreen", "isRefreshing changed to: $isRefreshing")
    }

    // Debug: Confirm callback is passed
    LaunchedEffect(Unit) {
        KlikLogger.i("CalendarScreen", "Initialized with onRefreshMeetings callback (hash: ${onRefreshMeetings.hashCode()})")
    }

    // Track which meeting should be expanded (from AskKlik source navigation or traced segments)
    var expandedMeetingId by remember { mutableStateOf<String?>(null) }
    var highlightSegmentId by remember { mutableStateOf<String?>(null) }
    var highlightSegmentText by remember { mutableStateOf<String?>(null) }

    // When expandSessionId changes, find matching meeting and expand it
    LaunchedEffect(expandSessionId, expandSegmentId, expandSegmentText) {
        if (expandSessionId != null) {
            KlikLogger.d("CalendarScreen", "Received expandSessionId=$expandSessionId, segmentId=$expandSegmentId, segmentText=${expandSegmentText?.take(30)}")
            KlikLogger.d("CalendarScreen", "Total meetings: ${meetings.size}")
            KlikLogger.d("CalendarScreen", "Meeting IDs: ${meetings.map { it.id }.take(5)}")

            // Use utility function to find meeting by session ID
            val matchingMeeting = SegmentNavigationUtils.findMeetingBySessionId(meetings, expandSessionId)

            if (matchingMeeting != null) {
                KlikLogger.d("CalendarScreen", "Found matching meeting: ${matchingMeeting.title} on date ${matchingMeeting.date}")

                // Change to the meeting's date if different
                if (matchingMeeting.date != selectedDate) {
                    KlikLogger.i("CalendarScreen", "Changing date from $selectedDate to ${matchingMeeting.date}")
                    onDateChange(matchingMeeting.date)
                }

                expandedMeetingId = matchingMeeting.id
                // Store both segmentId and text for precise matching
                highlightSegmentId = expandSegmentId
                highlightSegmentText = expandSegmentText
                onSessionExpanded()

                // Clear external expansion state after the highlight animation has completed.
                // MeetingCard promotes isExternallyExpanded to internalExpanded immediately,
                // so the card stays open. The delay gives TranscriptView time to consume
                // highlightSegmentId/Text (gated on isExternallyExpanded) and run its animation
                // (~4s total: scroll + 3 pulses + fade). After that, clear the external state
                // so isExternallyExpanded stops evaluating true on every recomposition.
                delay(5000)
                expandedMeetingId = null
                highlightSegmentId = null
                highlightSegmentText = null
            } else {
                // Meeting not found - extract date from session ID (format: SESSION_YYYYMMDD_HHMMSS_hash)
                val dateFromId = extractDateFromSessionId(expandSessionId)
                if (dateFromId != selectedDate) {
                    KlikLogger.i("CalendarScreen", "Meeting not in list, navigating to extracted date: $dateFromId")
                    onDateChange(dateFromId)
                } else {
                    KlikLogger.w("CalendarScreen", "No matching meeting found for sessionId=$expandSessionId on date $dateFromId")
                }
                onSessionExpanded()
            }
        }
    }

    // Use global archive/pin state from Models
    val archivedMeetingIds by archivedMeetingIdsState
    val pinnedMeetings by pinnedMeetingIdsState

    // Initialize global pinned state with meetings that have isPinned = true (once)
    LaunchedEffect(meetings) {
        val now = Clock.System.now().toEpochMilliseconds()
        val currentPinned = pinnedMeetingIdsState.value.toMutableMap()
        meetings.filter { it.isPinned && !currentPinned.containsKey(it.id) }.forEachIndexed { idx, meeting ->
            currentPinned[meeting.id] = meeting.pinnedAt ?: (now - idx * 1000)
        }
        if (currentPinned != pinnedMeetingIdsState.value) {
            pinnedMeetingIdsState.value = currentPinned
        }
    }

    // Debug: Log filtering parameters
    LaunchedEffect(selectedDate, meetings, archivedMeetingIds) {
        KlikLogger.d("CalendarScreen", "selectedDate=$selectedDate")
        KlikLogger.d("CalendarScreen", "Total meetings received: ${meetings.size}")
        KlikLogger.d("CalendarScreen", "Archived meeting IDs: ${archivedMeetingIds.size}")
        if (meetings.isNotEmpty()) {
            val uniqueDates = meetings.map { it.date }.distinct().sorted()
            KlikLogger.d("CalendarScreen", "Meeting dates in list: $uniqueDates")
            val matchingCount = meetings.count { it.date == selectedDate }
            KlikLogger.d("CalendarScreen", "Meetings matching selectedDate ($selectedDate): $matchingCount")
            if (matchingCount == 0 && meetings.isNotEmpty()) {
                KlikLogger.w("CalendarScreen", "NO MATCHES - first 3 meeting dates: ${meetings.take(3).map { "${it.title} -> ${it.date}" }}")
            }
        }
    }

    // Filter meetings by selected date AND exclude archived
    val filteredMeetings = remember(selectedDate, meetings, archivedMeetingIds) {
        val result = meetings.filter { it.date == selectedDate && it.id !in archivedMeetingIds }
        KlikLogger.d("CalendarScreen", "filteredMeetings: ${result.size} (from ${meetings.size} total)")
        result
    }

    // Sort meetings: pinned first (latest pinned at top), then non-pinned by start time descending (latest first)
    val sortedMeetings = remember(filteredMeetings, pinnedMeetings) {
        filteredMeetings
            .sortedWith(
                compareByDescending<Meeting> { pinnedMeetings[it.id] != null }
                    .thenByDescending { pinnedMeetings[it.id] ?: 0L }
                    .thenByDescending { it.startTime } // Latest meeting times first
            )
    }

    val dateDisplay = formatDateForDisplay(selectedDate)

    // topDockHeightPx is now the bottom edge position of TopStatusDock (positionInRoot.y + height)
    // This gives us the exact Y coordinate where the dock ends, so we just add a small gap
    val density = LocalDensity.current
    val topDockBottomDp = with(density) { topDockHeightPx.toDp() }
    val contentGap = 16.dp  // Visual gap between TopStatusDock and content

    // Dynamic top padding: bottom edge of TopStatusDock + small visual gap
    val topPadding by animateDpAsState(
        targetValue = if (topDockHeightPx > 0) {
            topDockBottomDp + contentGap
        } else {
            // Initial values before view is measured (will update immediately after layout)
            if (isCalendarExpanded) 460.dp else 130.dp
        },
        animationSpec = tween(durationMillis = 300)
    )

    // Track scroll state for overlap detection
    val listState = rememberLazyListState()

    // When calendar expands, scroll content to the very top so the FULL mini-calendar is visible
    // with NO overlap at all. Scroll to item 0 with offset 0 to reset position.
    LaunchedEffect(isCalendarExpanded) {
        if (isCalendarExpanded) {
            // Scroll to the absolute top - first item with no offset
            // This ensures content starts cleanly below the expanded calendar
            listState.animateScrollToItem(index = 0, scrollOffset = 0)
        }
    }

    // Detect when content overlaps the top bar
    // The top bar header ends at ~90dp. Content starts at topPadding (106dp collapsed).
    // Overlap occurs when content has scrolled up past its original position
    // Use derivedStateOf so this only triggers when the boolean VALUE changes,
    // not on every scroll pixel (which would recompose the entire app tree at 60fps)
    val isContentOverlapping by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 16
        }
    }
    LaunchedEffect(isContentOverlapping) {
        onContentOverlap(isContentOverlapping)
    }

    // Scroll main screen to show expanded meeting card
    LaunchedEffect(expandedMeetingId) {
        if (expandedMeetingId != null && !isLoading) {
            // Wait for recomposition
            delay(600)

            // Find meeting index in sorted list
            val meetingIndexInList = sortedMeetings.indexOfFirst { it.id == expandedMeetingId }

            if (meetingIndexInList >= 0) {
                // LazyColumn structure: item(MorningBriefing), item("Meetings" header), items(meetings)
                // So index is: 2 + meetingIndexInList
                val lazyColumnIndex = 2 + meetingIndexInList

                KlikLogger.i("CalendarScreen", "Scrolling main screen to LazyColumn index $lazyColumnIndex (meeting index: $meetingIndexInList)")

                try {
                    listState.animateScrollToItem(
                        index = lazyColumnIndex,
                        scrollOffset = -100
                    )
                    delay(200)
                    KlikLogger.i("CalendarScreen", "Main screen scrolled to expanded meeting")
                } catch (e: Exception) {
                    KlikLogger.e("CalendarScreen", "Main screen scroll failed: ${e.message}", e)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        state = pullRefreshState,
        onRefresh = {
            // MainApp handles setting isRefreshing = true in onRefreshMeetings
            onRefreshMeetings()
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
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = topPadding, bottom = 100.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = !isCalendarExpanded
    ) {
        item {
            MorningBriefingCard(
                liquidState = liquidState,
                dailyBriefing = dailyBriefing,
                insights = insights,
                isLoading = isLoading,
                isLlmDataLoading = isLlmDataLoading,
                tasks = tasks,
                meetings = meetings,
                projects = projects,
                people = people,
                organizations = organizations,
                onEntityClick = onEntityClick,
                onSegmentClick = onSegmentClick
            )
        }

        item {
            Text(
                if (sortedMeetings.isNotEmpty() || isLoading) stringResource(Res.string.calendar_meetings) else stringResource(Res.string.calendar_no_meetings),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
        }

        if (sortedMeetings.isEmpty()) {
            if (isLoading) {
                // Show loading placeholders for meetings
                items(2) { index ->
                    MeetingCardLoadingPlaceholder(liquidState)
                }
            } else {
                item {
                    Text(
                        "No meetings for $dateDisplay yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        } else {
            items(sortedMeetings, key = { it.id }) { meeting ->
                val isExternallyExpanded = expandedMeetingId == meeting.id
                if (isExternallyExpanded) {
                    KlikLogger.d("MeetingCard", "Meeting ${meeting.id} is externally expanded, segmentId=$highlightSegmentId, text=${highlightSegmentText?.take(30)}")
                }

                val archivedText = stringResource(Res.string.calendar_meeting_archived)
                MeetingCard(
                    liquidState = liquidState,
                    meeting = meeting,
                    isPinned = pinnedMeetings[meeting.id] != null,
                    onPinToggle = { meetingId ->
                        toggleMeetingPin(meetingId)
                    },
                    onArchive = { meetingId ->
                        KlikLogger.i("CalendarScreen", "Archive meeting: $meetingId")
                        archiveMeeting(meetingId, meeting)
                        undoBannerMessage = archivedText
                        undoBannerAction = { unarchiveMeeting(meetingId) }
                        undoBannerVisible = true
                    },
                    speakerMap = speakerMap,
                    onRefreshMeetings = onRefreshMeetings,
                    isExternallyExpanded = isExternallyExpanded,
                    onExpandedChange = { expanded ->
                        if (!expanded) {
                            expandedMeetingId = null
                            highlightSegmentId = null
                            highlightSegmentText = null
                        }
                    },
                    initialTab = if (isExternallyExpanded) 2 else 0,  // 2 = Transcript tab
                    highlightSegmentId = if (isExternallyExpanded) highlightSegmentId else null,
                    highlightSegmentText = if (isExternallyExpanded) highlightSegmentText else null,
                    onParticipantClick = { personId ->
                        onEntityClick(io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData(
                            entityType = io.github.fletchmckee.liquid.samples.app.ui.components.EntityType.PERSON,
                            entityId = personId
                        ))
                    }
                )
            }
        }
    }
    } // PullToRefreshBox

    // Liquid-glass undo banner overlay (replaces Material snackbar)
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
}

@Composable
fun MorningBriefingCard(
    liquidState: LiquidState,
    dailyBriefing: DailyBriefing? = null,
    insights: Insights? = null,
    isLoading: Boolean = false,
    isLlmDataLoading: Boolean = false,
    tasks: List<io.github.fletchmckee.liquid.samples.app.model.TaskMetadata> = emptyList(),
    meetings: List<Meeting> = emptyList(),
    projects: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Project> = emptyList(),
    people: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Person> = emptyList(),
    organizations: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Organization> = emptyList(),
    onEntityClick: (io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData) -> Unit = {},
    onSegmentClick: (io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var showTracedSegments by remember { mutableStateOf(false) }
    val glassSettings = LocalLiquidGlassSettings.current
    val insightColors = LocalInsightCardColors.current
    val cardShape = RoundedCornerShape(24.dp)

    // Insight card: same as normal cards, just with a subtle tint to the base color
    val baseColor = if (glassSettings.applyToCards) {
        insightColors.calendarTint.copy(alpha = 0.12f).compositeOver(Color.White)
    } else {
        Color.White
    }

    // Use ONLY insights from KLIK Insights API - do not fall back to dailyBriefing (which has task counts)
    val briefingText: String? = insights?.summary
    val tracedSegments: List<io.github.fletchmckee.liquid.samples.app.domain.entity.TracedSegment>? = insights?.tracedSegments

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(baseColor.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f), cardShape)
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
            .clickable { expanded = !expanded }
            .onLongPress {
                if (tracedSegments?.isNotEmpty() == true) {
                    showTracedSegments = !showTracedSegments
                }
            }
            .padding(20.dp)
            .animateContentSize()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, stringResource(Res.string.calendar_insight), tint = StriveColor)
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(Res.string.calendar_insights),
                style = MaterialTheme.typography.titleSmall,
                color = StriveColor
            )
            Spacer(Modifier.width(8.dp))
            AiGeneratedBadge("AI-Generated Insight")
            Spacer(Modifier.weight(1f))
            if (isLoading || (briefingText == null && isLlmDataLoading)) {
                Text(
                    stringResource(Res.string.loading),
                    style = MaterialTheme.typography.labelSmall,
                    color = StriveColor.copy(alpha = 0.7f)
                )
            }
            if (tracedSegments?.isNotEmpty() == true && !isLoading && !isLlmDataLoading) {
                Text(
                    "${tracedSegments.size} sources",
                    style = MaterialTheme.typography.labelSmall,
                    color = StriveColor.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (isLoading) {
            // Loading placeholder
            ShimmerTextBlock(lines = 3, color = StriveColor)
        } else if (briefingText != null) {
            // Entity-highlighted text
            io.github.fletchmckee.liquid.samples.app.ui.components.EntityHighlightedText(
                text = briefingText,
                tasks = tasks,
                meetings = meetings,
                projects = projects,
                people = people,
                organizations = organizations,
                onEntityClick = onEntityClick,
                modifier = Modifier.graphicsLayer {
                    alpha = if (expanded) 1f else 0.7f
                },
                style = MaterialTheme.typography.bodyMedium,
                color = KlikBlack,
                maxLines = if (expanded) Int.MAX_VALUE else Int.MAX_VALUE
            )

            // Traced segments section (shown on long press)
            if (showTracedSegments && tracedSegments?.isNotEmpty() == true) {
                Spacer(Modifier.height(12.dp))
                io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentsSection(
                    liquidState = liquidState,
                    segments = tracedSegments,
                    onSegmentClick = onSegmentClick
                )
            }

            AiContentDisclaimer()
        } else if (isLlmDataLoading) {
            // LLM insights still loading after general data loaded
            ShimmerTextBlock(lines = 3, color = StriveColor)
        }
    }
}

@Composable
fun MeetingCardLoadingPlaceholder(liquidState: LiquidState) {
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(20.dp)

    // Pulsing animation for loading state
    val infiniteTransition = rememberInfiniteTransition(label = "meeting_loading")
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
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f),
                cardShape
            )
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
        // Time placeholder
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(12.dp)
                .background(Color.Gray.copy(alpha = loadingAlpha * 0.3f), RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.height(12.dp))
        // Title placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(16.dp)
                .background(Color.Gray.copy(alpha = loadingAlpha * 0.4f), RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.height(8.dp))
        // Subtitle placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(12.dp)
                .background(Color.Gray.copy(alpha = loadingAlpha * 0.25f), RoundedCornerShape(4.dp))
        )
    }
}

// Pin indicator color
private val PinColor = Color(0xFFFFCC00)
private val ArchiveColor = Color(0xFFFF3B30)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingCard(
    liquidState: LiquidState,
    meeting: Meeting,
    isPinned: Boolean = false,
    onPinToggle: (String) -> Unit = {},
    onArchive: (String) -> Unit = {},
    speakerMap: Map<String, String> = emptyMap(),
    onRefreshMeetings: () -> Unit = {},
    isExternallyExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    initialTab: Int = 0,
    highlightSegmentId: String? = null,  // Unique segment identifier
    highlightSegmentText: String? = null,  // Segment text content (for text-based matching)
    onParticipantClick: (String) -> Unit = {}
) {
    // Debug: Confirm callback is passed to MeetingCard
    LaunchedEffect(Unit) {
        KlikLogger.d("MeetingCard", "${meeting.id}: onRefreshMeetings callback (hash: ${onRefreshMeetings.hashCode()})")
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    HapticService.lightImpact()
                    onPinToggle(meeting.id)
                    false // Reset swipe, just toggle pin
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    HapticService.mediumImpact()
                    onArchive(meeting.id)
                    false // Reset swipe position, item removed by filtering
                }
                else -> false
            }
        }
    )
    // Use external state if provided, otherwise internal
    var internalExpanded by remember { mutableStateOf(false) }
    val expanded = isExternallyExpanded || internalExpanded
    var selectedTab by remember(isExternallyExpanded, initialTab) {
        mutableStateOf(if (isExternallyExpanded) initialTab else 0)
    } // 0: Minutes, 1: Todos, 2: Transcript

    // When externally expanded, promote to internal state so the card stays open
    // after the external expansion is cleared (prevents recomposition storm from
    // expandedMeetingId staying set indefinitely)
    LaunchedEffect(isExternallyExpanded) {
        if (isExternallyExpanded) {
            internalExpanded = true
            onExpandedChange(true)
        }
    }
    val glassSettings = LocalLiquidGlassSettings.current
    val cardShape = RoundedCornerShape(20.dp)
    val coroutineScope = rememberCoroutineScope()

    // Card-level feedback popup state
    var showFeedbackPopup by remember { mutableStateOf(false) }
    var feedbackTargetText by remember { mutableStateOf("") }
    var feedbackTargetVoiceprintId by remember { mutableStateOf<String?>(null) }

    // Loading state for name refresh animation
    var isRefreshingNames by remember { mutableStateOf(false) }

    // Create a signature of participant names to detect when data actually changes
    val participantsSignature = meeting.participants.map { it.name }.sorted().joinToString(",")

    // Clear loading state when participant data actually changes (dynamic)
    LaunchedEffect(participantsSignature) {
        if (isRefreshingNames) {
            // Small delay to ensure smooth transition
            delay(300)
            isRefreshingNames = false
        }
    }

    // Safety timeout: Reset loading state after 10 seconds to prevent infinite spinner
    LaunchedEffect(isRefreshingNames) {
        if (isRefreshingNames) {
            delay(10000)
            isRefreshingNames = false
            KlikLogger.w("CalendarScreen", "Refresh timeout reached - loading state reset")
        }
    }

    val onShowFeedback: (String, String?) -> Unit = { text, vpId ->
        feedbackTargetText = text
        feedbackTargetVoiceprintId = vpId
        showFeedbackPopup = true
    }

    // Card background color - tint based on source and pin state
    val cardBackgroundColor = when {
        isPinned -> PinColor.copy(alpha = 0.08f).compositeOver(Color.White)
        meeting.source == MeetingSource.APPLE_CALENDAR && meeting.sourceColor != null ->
            Color(meeting.sourceColor).copy(alpha = 0.12f).compositeOver(Color.White)
        meeting.source == MeetingSource.APPLE_CALENDAR ->
            Color(0xFF007AFF).copy(alpha = 0.12f).compositeOver(Color.White)
        else -> Color.White
    }

    Box {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val color = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> PinColor
                    SwipeToDismissBoxValue.EndToStart -> ArchiveColor
                    else -> Color.Transparent
                }

                val icon = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> CustomIcons.PushPin
                    SwipeToDismissBoxValue.EndToStart -> CustomIcons.Archive
                    else -> CustomIcons.Archive
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
                           tint = color.copy(alpha = 0.2f)
                        }
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBackgroundColor.copy(alpha = if (glassSettings.applyToCards) glassSettings.transparency else 0.95f), cardShape)
                    .border(BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.12f)), cardShape)
                    .liquid(liquidState) {
                        edge = if (glassSettings.applyToCards) glassSettings.edge else 0.01f
                        shape = cardShape
                        if (glassSettings.applyToCards) {
                            frost = glassSettings.frost
                            curve = glassSettings.curve
                            refraction = glassSettings.refraction
                        }
                        tint = if (isPinned) PinColor.copy(alpha = 0.05f) else Color.Transparent
                    }
                    .clip(cardShape)
                    .clickable {
                        internalExpanded = !internalExpanded
                        onExpandedChange(internalExpanded)
                    }
                    .padding(16.dp)
                    .animateContentSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        meeting.time,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    // Right side: Dropbox link + Pin indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dropbox link button - only show if URL exists
                        if (!meeting.dropboxUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        OAuthBrowser.openUrl(meeting.dropboxUrl)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    CustomIcons.FolderOpen,
                                    contentDescription = stringResource(Res.string.calendar_open_in_dropbox),
                                    tint = Color(0xFF0061FF), // Dropbox blue
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        // Pin indicator
                        if (isPinned) {
                            Icon(
                                CustomIcons.PushPin,
                                contentDescription = stringResource(Res.string.calendar_pinned),
                                tint = PinColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    meeting.title,
                    style = MaterialTheme.typography.titleSmall
                )

                if (expanded) {
                    Spacer(Modifier.height(16.dp))

                    // Tabs - Custom rounded pill style
                    val tabs = listOf(stringResource(Res.string.calendar_minutes_tab), stringResource(Res.string.calendar_todos_tab), stringResource(Res.string.calendar_transcript_tab))
                    val tabColors = listOf(LiveColor, EngageColor, StriveColor)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            val tabColor = tabColors[index]

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) tabColor.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedTab = index }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) tabColor else Color.Gray
                                )
                            }
                        }

                        // Share button
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    // Format content based on selected tab
                                    val (subject, content) = when (selectedTab) {
                                        0 -> {
                                            // Meeting Minutes
                                            val minutesText = meeting.minutes.joinToString("\n\n") { minute ->
                                                "${minute.category}:\n" + minute.items.joinToString("\n") { "• $it" }
                                            }
                                            "Meeting Minutes: ${meeting.title}" to minutesText.ifEmpty { "No minutes available" }
                                        }
                                        1 -> {
                                            // Meeting Todos
                                            val todosText = meeting.todos.mapIndexed { index, todo ->
                                                "${index + 1}. $todo"
                                            }.joinToString("\n")
                                            "Meeting Todos: ${meeting.title}" to todosText.ifEmpty { "No todos available" }
                                        }
                                        else -> {
                                            // Meeting Transcript
                                            val transcriptText = meeting.transcriptLines.joinToString("\n") { (speaker, text) ->
                                                "$speaker: $text"
                                            }
                                            "Meeting Transcript: ${meeting.title}" to transcriptText.ifEmpty { "No transcript available" }
                                        }
                                    }
                                    // Call native share service
                                    io.github.fletchmckee.liquid.samples.app.platform.ShareService.share(
                                        text = "$subject\n\n$content",
                                        subject = subject
                                    )
                                }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = stringResource(Res.string.calendar_share),
                                tint = KlikPrimary.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // AI content badge per tab
                    when (selectedTab) {
                        0 -> AiGeneratedBadge("AI-Generated Summary")
                        1 -> AiGeneratedBadge("AI-Suggested Action Items")
                        2 -> AiGeneratedBadge(stringResource(Res.string.calendar_ai_transcription))
                    }
                    Spacer(Modifier.height(8.dp))

                    // Content
                    when (selectedTab) {
                        0 -> MinutesList(
                            minutes = meeting.minutes,
                            participants = meeting.participants,
                            onShowFeedback = onShowFeedback,
                            isRefreshingNames = isRefreshingNames,
                            onParticipantClick = onParticipantClick
                        )
                        1 -> TodoList(meeting.todos)
                        2 -> TranscriptView(
                            lines = meeting.transcriptLines,
                            speakerMap = speakerMap,
                            onShowFeedback = onShowFeedback,
                            highlightSegmentId = highlightSegmentId,
                            highlightSegmentText = highlightSegmentText,
                            isRefreshingNames = isRefreshingNames
                        )
                    }
                    AiContentDisclaimer()
                }
            }
        }

        // Card-level FeedbackPopup - covers entire meeting card with rounded corners
        FeedbackPopup(
            isVisible = showFeedbackPopup,
            originalText = feedbackTargetText,
            voiceprintId = feedbackTargetVoiceprintId,
            onDismiss = { showFeedbackPopup = false },
            onSubmitFeedback = { feedback ->
                KlikLogger.d("FeedbackPopup", "Feedback received: ${feedback.originalText}, correction: ${feedback.correction}, isWrong: ${feedback.isMarkedWrong}")
                showFeedbackPopup = false

                // Submit feedback to backend
                val hasCorrection = !feedback.correction.isNullOrBlank() && feedback.correction != feedback.originalText
                val isMarkedWrong = feedback.isMarkedWrong

                if (hasCorrection || isMarkedWrong) {
                    // Start loading animation immediately
                    isRefreshingNames = true

                    coroutineScope.launch {
                        try {
                            val correctedName = if (hasCorrection) feedback.correction else feedback.originalText
                            val requestBody = buildJsonObject {
                                put("original_name", feedback.originalText)
                                put("corrected_name", correctedName)
                                put("is_marked_wrong", isMarkedWrong)
                                if (feedback.voiceprintId != null) {
                                    put("voiceprint_id", feedback.voiceprintId)
                                }
                            }.toString()
                            val response = HttpClient.post("/feedback/name-correction", requestBody)
                            if (response != null && response.contains("success\":true")) {
                                KlikLogger.i("FeedbackPopup", "Feedback submission success")
                                KlikLogger.d("FeedbackPopup", "Response: $response")
                                KlikLogger.d("FeedbackPopup", "About to call onRefreshMeetings (hash: ${onRefreshMeetings.hashCode()})")
                                try {
                                    onRefreshMeetings()
                                    KlikLogger.i("FeedbackPopup", "onRefreshMeetings() call completed successfully")
                                } catch (e: Exception) {
                                    KlikLogger.e("FeedbackPopup", "Error calling onRefreshMeetings: ${e.message}", e)
                                }
                            } else {
                                KlikLogger.e("FeedbackPopup", "Feedback submission failed: $response")
                                isRefreshingNames = false // Stop animation on failure
                            }
                        } catch (e: Exception) {
                            KlikLogger.e("FeedbackPopup", "Feedback submission error: ${e.message}", e)
                            isRefreshingNames = false // Stop animation on error
                        }
                    }
                }
            },
            overlayShape = cardShape,
            modifier = Modifier.matchParentSize()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MinutesList(
    minutes: List<io.github.fletchmckee.liquid.samples.app.domain.entity.MeetingMinute>,
    participants: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Person>,
    onShowFeedback: (String, String?) -> Unit = { _, _ -> },
    isRefreshingNames: Boolean = false,
    onParticipantClick: (String) -> Unit = {}
) {
    // Pulsing animation for loading state
    val infiniteTransition = rememberInfiniteTransition(label = "namesPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column {
        // Participants section with FlowRow for proper wrapping
        if (participants.isNotEmpty()) {
            Text(stringResource(Res.string.calendar_participants), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LiveColor)
            Spacer(Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                participants.forEachIndexed { index, person ->
                    Row {
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = KlikBlack.copy(alpha = if (isRefreshingNames) pulseAlpha else 1f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isRefreshingNames) KlikPrimary.copy(alpha = 0.1f * (1f - pulseAlpha))
                                    else Color.Transparent
                                )
                                .pointerInput(person.id) {
                                    detectTapGestures(
                                        onTap = { onParticipantClick(person.id) },
                                        onLongPress = { onShowFeedback(person.name, person.id) }
                                    )
                                }
                                .padding(vertical = 2.dp)
                        )
                        if (index < participants.size - 1) {
                            Text(", ", style = MaterialTheme.typography.bodySmall, color = KlikBlack)
                        }
                    }
                }
            }
        }

        // Minutes sections
        minutes.forEach { section ->
            Spacer(Modifier.height(8.dp))
            Text(section.category, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LiveColor)
            Spacer(Modifier.height(4.dp))
            section.items.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                     // Bullet with alignment fix
                    Text("\u2022", modifier = Modifier.padding(end = 8.dp).padding(top = 1.dp), color = LiveColor, style = MaterialTheme.typography.bodySmall)
                    Text(item, style = MaterialTheme.typography.bodySmall, color = KlikBlack)
                }
            }
        }
    }
}

@Composable
fun TodoList(todos: List<String>) {
    Column {
        todos.forEach {
             Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun TranscriptView(
    lines: List<Pair<String, String>>,
    speakerMap: Map<String, String> = emptyMap(),
    onShowFeedback: (String, String?) -> Unit = { _, _ -> },
    highlightSegmentId: String? = null,     // Unique segment identifier for precise matching
    highlightSegmentText: String? = null,    // Segment text content for text-based matching
    isRefreshingNames: Boolean = false
) {
    val listState = rememberLazyListState()

    // Pulse animation: starts at 0, pulses 2-3 times, then fades
    var highlightAlpha by remember { mutableStateOf(0f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = highlightAlpha,
        animationSpec = tween(300)
    )

    // Track which line index is highlighted (for rendering)
    var highlightedLineIndex by remember { mutableStateOf(-1) }

    // Pulsing animation for loading state (speaker names)
    val infiniteTransition = rememberInfiniteTransition(label = "speakerPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speakerPulseAlpha"
    )

    // Scroll to highlighted segment using precise matching logic
    LaunchedEffect(highlightSegmentId, highlightSegmentText) {
        // Only proceed if we have data to match
        if ((highlightSegmentId != null || highlightSegmentText != null) && lines.isNotEmpty()) {
            // CRITICAL: Wait for meeting card to fully expand and transcript view to render
            // Without this delay, the scroll won't work because the view isn't laid out yet
            delay(400)  // Give time for card expansion animation and layout

            // Use utility function for precise matching
            val segmentId = highlightSegmentId ?: ""
            val segmentText = highlightSegmentText ?: ""

            KlikLogger.d("TranscriptView", "Starting segment match for segmentId=$segmentId")

            val matchedIndex = SegmentNavigationUtils.findSegmentIndex(
                lines = lines,
                segmentId = segmentId,
                segmentText = segmentText
            )

            if (matchedIndex >= 0) {
                KlikLogger.d("TranscriptView", "Found segment at index $matchedIndex out of ${lines.size} total lines")

                // Validate the match is reasonable
                if (SegmentNavigationUtils.validateSegmentMatch(lines, matchedIndex, segmentText)) {
                    highlightedLineIndex = matchedIndex

                    // CRITICAL FIX: The transcript LazyColumn has heightIn(max = 300.dp)
                    // This shows only ~6-10 items at once. To ensure the highlighted item
                    // is VISIBLE and CENTERED, we need to scroll MUCH further back.
                    //
                    // Strategy: Scroll to 3-5 items before the target to show it in the middle/top
                    val itemsToShowBefore = 3  // Show 3 items before, then target, then remaining
                    val scrollToIndex = maxOf(0, matchedIndex - itemsToShowBefore)

                    KlikLogger.d("TranscriptView", "Target index: $matchedIndex, scrolling to: $scrollToIndex (showing $itemsToShowBefore items before)")

                    try {
                        // Use animateScrollToItem for smooth scrolling
                        listState.animateScrollToItem(
                            index = scrollToIndex,
                            scrollOffset = 0  // No additional offset needed
                        )

                        // Wait for scroll animation to complete
                        delay(350)

                        KlikLogger.d("TranscriptView", "Scroll animation completed")

                        // Verify the item is actually visible now
                        val firstVisible = listState.firstVisibleItemIndex
                        val visibleCount = listState.layoutInfo.visibleItemsInfo.size
                        val lastVisible = firstVisible + visibleCount - 1

                        if (matchedIndex in firstVisible..lastVisible) {
                            KlikLogger.d("TranscriptView", "Target item $matchedIndex IS NOW VISIBLE (range: $firstVisible-$lastVisible, total visible: $visibleCount)")
                        } else {
                            KlikLogger.w("TranscriptView", "Target $matchedIndex NOT visible! Visible range: $firstVisible-$lastVisible")
                            KlikLogger.d("TranscriptView", "Retrying scroll with negative offset...")
                            listState.animateScrollToItem(matchedIndex, scrollOffset = -150)
                            delay(250)
                        }
                    } catch (e: Exception) {
                        KlikLogger.e("TranscriptView", "Scroll failed: ${e.message}", e)
                    }

                    // Pulse animation: 3 pulses over ~2 seconds
                    // Keep highlight visible during entire animation
                    repeat(3) {
                        highlightAlpha = 0.4f
                        delay(300)
                        highlightAlpha = 0.15f
                        delay(300)
                    }

                    // Keep highlight visible for an extra second so user can see it
                    highlightAlpha = 0.2f
                    delay(1000)

                    // Fade out
                    highlightAlpha = 0f
                    highlightedLineIndex = -1  // Clear highlight

                    KlikLogger.d("TranscriptView", "Highlight animation complete")
                } else {
                    KlikLogger.w("TranscriptView", "Match validation failed for index $matchedIndex")
                }
            } else {
                KlikLogger.w("TranscriptView", "Could not find matching segment (segmentId=$segmentId, text=${segmentText.take(30)})")
                KlikLogger.d("TranscriptView", "Available lines preview: ${lines.take(3).map { it.second.take(30) }}")
            }
        }
    }

    // Use heightIn to constrain the LazyColumn inside expandable card (avoids infinite height crash)
    LazyColumn(
        state = listState,
        modifier = Modifier.heightIn(max = 300.dp)
    ) {
        itemsIndexed(lines) { index, (speaker, text) ->
            // Map speaker name if ID is found in the map, and track which voiceprint_id matched
            var displaySpeaker = speaker
            var matchedVoiceprintId: String? = null
            speakerMap.forEach { (id, name) ->
                if (displaySpeaker.contains(id)) {
                    displaySpeaker = displaySpeaker.replace(id, name)
                    matchedVoiceprintId = id
                }
            }
            // Use index-based highlighting (much more precise than text matching)
            val isHighlighted = index == highlightedLineIndex

            // Extract just the speaker name (remove timestamp prefix like "[0:00:09 - 0:00:12]")
            val speakerNameOnly = displaySpeaker.substringAfter("] ").trim().ifEmpty { displaySpeaker }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(
                        if (isHighlighted) Color(0xFF4CAF50).copy(alpha = animatedAlpha)
                        else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(if (isHighlighted) 8.dp else 0.dp)
            ) {
                Text(
                    text = displaySpeaker,
                    style = MaterialTheme.typography.labelSmall,
                    color = KlikPrimary.copy(alpha = if (isRefreshingNames) pulseAlpha else 1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isRefreshingNames) KlikPrimary.copy(alpha = 0.08f * (1f - pulseAlpha))
                            else Color.Transparent
                        )
                        .onLongPress { onShowFeedback(speakerNameOnly, matchedVoiceprintId) }
                        .padding(vertical = 2.dp)
                )
                Text(text, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
