// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.window.Popup
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalDensity
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings
import io.github.fletchmckee.liquid.samples.app.theme.KlikBlack
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.liquid
import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.samples.app.theme.BackgroundOption
import io.github.fletchmckee.liquid.samples.app.theme.BackgroundOptions
import io.github.fletchmckee.liquid.samples.app.theme.LiquidGlassSettings
import io.github.fletchmckee.liquid.samples.app.theme.LocalSnackbarHostState
import io.github.fletchmckee.liquid.samples.app.theme.LiquidTheme
import io.github.fletchmckee.liquid.samples.app.theme.LocalLiquidGlassSettings
import io.github.fletchmckee.liquid.samples.app.theme.getKlikAccentGradient
import io.github.fletchmckee.liquid.samples.app.theme.getKlikPrimaryColor
import androidx.compose.ui.graphics.Brush
import org.jetbrains.compose.resources.painterResource
import liquid_root.samples.composeapp.generated.resources.Res
import liquid_root.samples.composeapp.generated.resources.gradient_peach
import liquid_root.samples.composeapp.generated.resources.gradient_lavender
import liquid_root.samples.composeapp.generated.resources.*
import androidx.compose.ui.geometry.Size
import liquid_root.samples.composeapp.generated.resources.moon_and_stars
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.Image
import io.github.fletchmckee.liquid.samples.app.ui.icons.Bluetooth
import io.github.fletchmckee.liquid.samples.app.ui.icons.CustomIcons
import io.github.fletchmckee.liquid.samples.app.ui.icons.Mic
import io.github.fletchmckee.liquid.samples.app.ui.components.MiniCalendar
import io.github.fletchmckee.liquid.samples.app.ui.screens.ArchivedScreen
import io.github.fletchmckee.liquid.samples.app.ui.klikone.AskKlikSheet
import io.github.fletchmckee.liquid.samples.app.ui.klikone.NetworkScreen
import io.github.fletchmckee.liquid.samples.app.ui.klikone.TodayScreen
import io.github.fletchmckee.liquid.samples.app.ui.klikone.MovesScreen
import io.github.fletchmckee.liquid.samples.app.ui.klikone.YouScreen
import io.github.fletchmckee.liquid.samples.app.ui.klikone.SessionDetailScreen
import io.github.fletchmckee.liquid.samples.app.ui.klikone.OnboardingScreen
import io.github.fletchmckee.liquid.samples.app.ui.klikone.LiveRecordingScreen
import io.github.fletchmckee.liquid.samples.app.data.storage.KlikOneOnboardingKeys
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatSource
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatSourceType
import io.github.fletchmckee.liquid.samples.app.ui.screens.GrowthTreeScreen
import io.github.fletchmckee.liquid.samples.app.ui.screens.NotificationsScreen
import io.github.fletchmckee.liquid.samples.app.ui.screens.PricingScreen
import io.github.fletchmckee.liquid.samples.app.ui.screens.PrivacySettingsScreen
import io.github.fletchmckee.liquid.samples.app.ui.screens.RecordingConsentScreen
import io.github.fletchmckee.liquid.samples.app.ui.screens.AccountSecurityScreen
import io.github.fletchmckee.liquid.samples.app.ui.screens.BiometricConsentScreen
import io.github.fletchmckee.liquid.samples.app.ui.screens.NotificationSettingsScreen
import io.github.fletchmckee.liquid.samples.app.ui.components.UpgradeRequiredDialog
import io.github.fletchmckee.liquid.samples.app.presentation.auth.AuthViewModel
import io.github.fletchmckee.liquid.samples.app.data.repository.IntegrationRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.data.storage.AppleIntegrationStorageKeys
import io.github.fletchmckee.liquid.samples.app.data.storage.IntegrationStorageKeys
import io.github.fletchmckee.liquid.samples.app.data.storage.SecureStorage
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationInfo
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationProviders
import io.github.fletchmckee.liquid.samples.app.platform.AppleIntegrationService
import io.github.fletchmckee.liquid.samples.app.platform.ApplePermissionStatus
import io.github.fletchmckee.liquid.samples.app.platform.AppleSyncManager
import io.github.fletchmckee.liquid.samples.app.platform.DeepLinkHandler
import io.github.fletchmckee.liquid.samples.app.platform.NetworkMonitor
import io.github.fletchmckee.liquid.samples.app.platform.parseDeepLink
import io.github.fletchmckee.liquid.samples.app.platform.OAUTH_CALLBACK_SCHEME
import io.github.fletchmckee.liquid.samples.app.platform.OAuthBrowser
import io.github.fletchmckee.liquid.samples.app.platform.OAuthSessionResult
import io.github.fletchmckee.liquid.samples.app.ui.components.IntegrationPromptDialog
import io.github.fletchmckee.liquid.samples.app.ui.components.ErrorPopup
import io.github.fletchmckee.liquid.samples.app.reporting.CrashReporter
import io.github.fletchmckee.liquid.samples.app.reporting.GitHubIssueReporter
import io.github.fletchmckee.liquid.samples.app.core.rememberViewModel
import io.github.fletchmckee.liquid.samples.app.model.formatDateForDisplay
import io.github.fletchmckee.liquid.samples.app.model.getDaysInMonth
import io.github.fletchmckee.liquid.samples.app.model.reviewMetadata
import io.github.fletchmckee.liquid.samples.app.model.pendingMetadata
import io.github.fletchmckee.liquid.samples.app.model.completedMetadata
import io.github.fletchmckee.liquid.samples.app.model.kkExecSensitiveTodosState
import io.github.fletchmckee.liquid.samples.app.model.kkExecDailyTodosState
import io.github.fletchmckee.liquid.samples.app.model.kkExecDailyTodosGroupedState
import io.github.fletchmckee.liquid.samples.app.model.executingTodoIdsState
import io.github.fletchmckee.liquid.samples.app.model.recentlyCompletedCategoriesState
import io.github.fletchmckee.liquid.samples.app.model.allKKExecTodosState
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.model.projectsState
import io.github.fletchmckee.liquid.samples.app.model.peopleState
import io.github.fletchmckee.liquid.samples.app.model.projectsState
import io.github.fletchmckee.liquid.samples.app.model.organizationsState
import io.github.fletchmckee.liquid.samples.app.model.organizationsState
import io.github.fletchmckee.liquid.samples.app.model.meetingsState
import io.github.fletchmckee.liquid.samples.app.model.goalsData
import io.github.fletchmckee.liquid.samples.app.model.userLevelData
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData
import io.github.fletchmckee.liquid.samples.app.ui.components.EntityType
import io.github.fletchmckee.liquid.samples.app.ui.components.TracedSegmentNavigation
import io.github.fletchmckee.liquid.samples.app.model.archivedTasksState
import io.github.fletchmckee.liquid.samples.app.model.archivedMeetingsState
import io.github.fletchmckee.liquid.samples.app.model.archivedProjectIdsState
import io.github.fletchmckee.liquid.samples.app.model.archivedPersonIdsState
import io.github.fletchmckee.liquid.samples.app.model.archivedOrganizationIdsState
import io.github.fletchmckee.liquid.samples.app.model.unarchiveTask
import io.github.fletchmckee.liquid.samples.app.model.unarchiveMeeting
import io.github.fletchmckee.liquid.samples.app.model.archiveProject as modelArchiveProject
import io.github.fletchmckee.liquid.samples.app.model.unarchiveProject as modelUnarchiveProject
import io.github.fletchmckee.liquid.samples.app.model.archivePerson as modelArchivePerson
import io.github.fletchmckee.liquid.samples.app.model.unarchivePerson as modelUnarchivePerson
import io.github.fletchmckee.liquid.samples.app.model.archiveOrganization as modelArchiveOrganization
import io.github.fletchmckee.liquid.samples.app.model.unarchiveOrganization as modelUnarchiveOrganization
import io.github.fletchmckee.liquid.samples.app.model.initializeArchivePinState
import io.github.fletchmckee.liquid.samples.app.domain.entity.Insights
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.data.source.remote.NotificationDto
import io.github.fletchmckee.liquid.samples.app.data.source.remote.KKExecRetryLimitException
import io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher
import io.github.fletchmckee.liquid.samples.app.data.source.remote.EncourageData
import io.github.fletchmckee.liquid.samples.app.data.source.remote.WorklifeData
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.di.AppModule
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.ui.screens.LoadingScreen
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import io.github.fletchmckee.liquid.samples.app.platform.AppLifecycleObserver
import io.github.fletchmckee.liquid.samples.app.platform.FixedSessionAudioStreamer
import io.github.fletchmckee.liquid.samples.app.resources.klikLogoPainter

/**
 * Helper function to count meetings per day for a given month.
 * Uses domain Meeting entities.
 */
private fun getMeetingsCountForMonth(
    meetings: List<Meeting>,
    year: Int,
    month: Int
): Map<Int, Int> {
    return meetings
        .filter { it.date.year == year && it.date.monthNumber == month }
        .groupBy { it.date.dayOfMonth }
        .mapValues { it.value.size }
}

/**
 * Helper function to find a TracedSegment across all data sources.
 * Searches in insights, encourage, and worklife traced segments.
 *
 * @param sessionId The session/meeting ID
 * @param segmentId The segment ID
 * @param insights Insights data with traced segments
 * @param encourageData Encourage data with traced segments
 * @param worklifeData Worklife data with traced segments
 * @return The matching TracedSegment, or null if not found
 */
private fun findTracedSegment(
    sessionId: String,
    segmentId: String,
    insights: Insights?,
    encourageData: EncourageData?,
    worklifeData: WorklifeData?
): io.github.fletchmckee.liquid.samples.app.domain.entity.TracedSegment? {
    // Search in insights traced segments
    insights?.tracedSegments?.find {
        it.sessionId == sessionId && it.segmentId == segmentId
    }?.let { return it }

    // Search in encourage traced segments
    encourageData?.tracedSegments?.find {
        it.sessionId == sessionId && it.segmentId == segmentId
    }?.let { return it }

    // Search in worklife traced segments
    worklifeData?.tracedSegments?.find {
        it.sessionId == sessionId && it.segmentId == segmentId
    }?.let { return it }

    return null
}

/**
 * Resolve KK_exec entity_refs name strings to canonical entity IDs from the loaded API data.
 * Each relatedPeople/relatedProjects/relatedOrganizations entry is matched against
 * the entity name (exact, case-insensitive). Matched entries are replaced with the entity ID.
 */
private fun resolveEntityRefs(tasks: List<io.github.fletchmckee.liquid.samples.app.model.TaskMetadata>): List<io.github.fletchmckee.liquid.samples.app.model.TaskMetadata> {
    val personNameToId = mutableMapOf<String, String>()
    for (person in peopleState.value) {
        personNameToId[person.name.lowercase()] = person.id
        if (person.canonicalName.isNotEmpty()) {
            personNameToId[person.canonicalName.lowercase()] = person.id
        }
    }
    val projectNameToId = mutableMapOf<String, String>()
    for (project in projectsState.value) {
        projectNameToId[project.name.lowercase()] = project.id
    }
    val orgNameToId = mutableMapOf<String, String>()
    for (org in organizationsState.value) {
        orgNameToId[org.name.lowercase()] = org.id
    }
    return tasks.map { task ->
        task.copy(
            relatedPeople = task.relatedPeople.map { name -> personNameToId[name.lowercase()] ?: name },
            relatedProject = projectNameToId[task.relatedProject.lowercase()] ?: task.relatedProject,
            relatedProjects = task.relatedProjects.map { name -> projectNameToId[name.lowercase()] ?: name },
            relatedOrganizations = task.relatedOrganizations.map { name -> orgNameToId[name.lowercase()] ?: name }
        )
    }
}

// Helper to get route index for determining slide direction (main 3 screens only)
private fun getRouteIndex(route: String): Int = when (route) {
    "today" -> 0
    "function" -> 1
    "growth" -> 2
    else -> 0
}

/**
 * For each Apple Calendar meeting, look up a temporally-overlapping Klik recording
 * meeting on the same date and copy its [io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting.id]
 * (which is the backend session_id) into [io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting.sessionId].
 *
 * The session detail screen filters todos by `meeting.sessionId ?: meeting.id`, so
 * once an Apple Calendar event carries a sessionId, KK_exec todos created from the
 * recording show up under the calendar event's per-session To-dos tab.
 *
 * Match policy: same date, and the Klik meeting's start time falls within the
 * Apple event's [start, end] window (or vice versa for safety). First match wins.
 */
private fun linkAppleToKlikSessions(
    apple: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting>,
    klik: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting>,
): List<io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting> {
    if (apple.isEmpty() || klik.isEmpty()) return apple
    val klikByDate = klik.groupBy { it.date }
    return apple.map { ev ->
        if (ev.sessionId != null) return@map ev
        val candidates = klikByDate[ev.date] ?: return@map ev
        val (evStart, evEnd) = parseTimeRangeMinutes(ev.time) ?: return@map ev
        val match = candidates.firstOrNull { kl ->
            val klStart = kl.startTime.hour * 60 + kl.startTime.minute
            klStart in evStart..evEnd
        } ?: return@map ev
        ev.copy(sessionId = match.id)
    }
}

/**
 * Parse a time range string like "1:48 PM - 1:50 PM" into ([startMinutes], [endMinutes])
 * since midnight. Returns null on parse failure.
 */
private fun parseTimeRangeMinutes(time: String): Pair<Int, Int>? {
    val parts = time.split("-").map { it.trim() }
    if (parts.size != 2) return null
    val start = parseSingleTimeMinutes(parts[0]) ?: return null
    val end = parseSingleTimeMinutes(parts[1]) ?: return null
    return start to end
}

private fun parseSingleTimeMinutes(s: String): Int? = try {
    val clean = s.uppercase().trim()
    val isPM = clean.contains("PM")
    val timeOnly = clean.replace("AM", "").replace("PM", "").trim()
    val pieces = timeOnly.split(":")
    var hour = pieces[0].toInt()
    val minute = if (pieces.size > 1) pieces[1].toInt() else 0
    if (isPM && hour != 12) hour += 12
    if (!isPM && hour == 12) hour = 0
    hour * 60 + minute
} catch (e: Exception) {
    KlikLogger.w("MainApp", "Failed to parse time '$s': ${e.message}", e)
    null
}

// Module-level init guard: prevents duplicate API calls when composition is disposed and recreated.
// Tracks when each init block STARTED — skips re-entry within the debounce window.
// Uses start time (not completion) because cancelled inits never complete but still did work.
private object InitGuard {
    var mainInitStartedAt = 0L
    var todosInitStartedAt = 0L
    var integrationsCheckedAt = 0L
    const val DEBOUNCE_MS = 30_000L // 30 seconds

    fun tryStartMainInit(): Boolean {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        if (mainInitStartedAt > 0 && (now - mainInitStartedAt) < DEBOUNCE_MS) return false
        mainInitStartedAt = now
        return true
    }

    fun tryStartTodosInit(): Boolean {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        if (todosInitStartedAt > 0 && (now - todosInitStartedAt) < DEBOUNCE_MS) return false
        todosInitStartedAt = now
        return true
    }

    fun tryStartIntegrationsCheck(): Boolean {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        if (integrationsCheckedAt > 0 && (now - integrationsCheckedAt) < DEBOUNCE_MS) return false
        integrationsCheckedAt = now
        return true
    }
}

// Static cache for slow LLM endpoint data (insights, encourage, worklife).
// Survives composition disposal/recreation so data fetched before disposal
// is immediately available when the composable re-enters the tree.
private object LlmDataCache {
    var insights: Insights? = null
    var encourageData: EncourageData? = null
    var worklifeData: WorklifeData? = null
}

@Composable
fun MainApp() {
  val liquidState = rememberLiquidState()
  var currentRoute by remember { mutableStateOf("today") }
  var lastMainRoute by remember { mutableStateOf("today") } // Track last main screen for returning from explore
  var showAskKlik by remember { mutableStateOf(false) }
  var isTopBarOverlapped by remember { mutableStateOf(false) }  // Track when content overlaps top bar

  // Fixed Session recording state — derived from the streamer's own StateFlow so it
  // survives Compose composition teardown/rebuild (e.g. after an auth reset on cold start).
  val isFixedSessionRecording by FixedSessionAudioStreamer.isStreaming.collectAsState()
  val isFixedSessionPaused by FixedSessionAudioStreamer.isPaused.collectAsState()
  // Feeds the K1 post-session processing banner with REAL orchestrator state.
  // Polls KK_orchestrator's /api/v1/pipeline/session/{sid}/status (exposed
  // via nginx as /api/orchestrator/...). Cleared when is_terminal=true.
  var processingStartedAtMillis by remember { mutableStateOf<Long?>(null) }
  var processingSessionId       by remember { mutableStateOf<String?>(null) }
  var processingStage           by remember { mutableStateOf<String?>(null) }
  var processingStatus          by remember { mutableStateOf<String?>(null) }
  var processingMessage         by remember { mutableStateOf<String?>(null) }
  var processingProgressPct     by remember { mutableStateOf<Double?>(null) }
  var processingError           by remember { mutableStateOf<String?>(null) }
  val dismissProcessing: () -> Unit = {
      processingStartedAtMillis = null
      processingSessionId = null
      processingStage = null
      processingStatus = null
      processingMessage = null
      processingProgressPct = null
      processingError = null
  }

  // Increments on each successful pipeline completion. Observed below (after
  // meetingsRefreshKey/tasksRefreshKey are declared) to trigger a list reload
  // so the newly processed session shows up without a manual pull-to-refresh.
  var pipelineCompletedTrigger by remember { mutableStateOf(0) }

  // Poll KK_orchestrator every 2s while there's an active session. Stops when
  // is_terminal=true, holds the terminal state for 1.5s so the user sees it,
  // 10-minute hard cap on pathological runs.
  LaunchedEffect(processingStartedAtMillis, processingSessionId) {
      val startedAt = processingStartedAtMillis ?: return@LaunchedEffect
      val sid = processingSessionId
      val deadline = startedAt + 10 * 60 * 1000L
      while (kotlinx.datetime.Clock.System.now().toEpochMilliseconds() < deadline) {
          if (sid != null) {
              try {
                  val snap = RemoteDataFetcher.fetchOrchestratorSessionStatus(sid)
                  if (snap != null) {
                      processingStatus      = snap.status.lowercase()
                      processingStage       = snap.stage
                      processingMessage     = snap.message
                      processingProgressPct = snap.progressPct
                      processingError       = snap.error
                      if (snap.isTerminal) {
                          if (snap.error == null) {
                              pipelineCompletedTrigger++
                          }
                          kotlinx.coroutines.delay(1_500)
                          dismissProcessing()
                          return@LaunchedEffect
                      }
                  }
              } catch (e: Exception) {
                  KlikLogger.w("MainApp", "orchestrator status poll failed: ${e.message}")
              }
          }
          kotlinx.coroutines.delay(2_000)
      }
      dismissProcessing()
  }
  var fixedSessionId by remember { mutableStateOf<String?>(null) }
  // Prominent in-app recording-started banner (replaces Material snackbar)
  var showRecordingStartedBanner by remember { mutableStateOf(false) }
  // Wall-clock capture start so LiveRecordingScreen can render elapsed until
  // the backend live-transcript WS (see BACKEND-REQUIREMENTS.md §12) lands.
  var recordingStartedAtMillis by remember { mutableStateOf<Long?>(null) }
  // Meeting selected for the SessionDetailScreen route (tapped from TodayScreen).
  var sessionDetailMeeting by remember { mutableStateOf<Meeting?>(null) }
  var taskDetailId by remember { mutableStateOf<String?>(null) }
  // When set, MovesScreen renders the matching task with a highlight band
  // and scrolls it into view. Cleared on any non-Moves navigation.
  var highlightedMoveId by remember { mutableStateOf<String?>(null) }
  var personDetailId by remember { mutableStateOf<String?>(null) }
  var projectDetailId by remember { mutableStateOf<String?>(null) }
  var orgDetailId by remember { mutableStateOf<String?>(null) }
  // Klik One first-run onboarding — null until SecureStorage is read per user_id.
  var hasCompletedKlikOnboarding by remember { mutableStateOf<Boolean?>(null) }
  val onboardingStorage = remember { io.github.fletchmckee.liquid.samples.app.data.storage.SecureStorage() }

  // Recording consent state
  var hasRecordingConsent by remember { mutableStateOf<Boolean?>(null) }
  // Biometric voiceprint consent state (BIPA compliance) — required for fixed-session recording
  var hasBiometricConsent by remember { mutableStateOf<Boolean?>(null) }
  // Pending action to run once all required consents are granted (chained consent flow)
  var pendingPostConsentAction by remember { mutableStateOf<(() -> Unit)?>(null) }
  // True while the app is forcing the user through the mandatory consent-onboarding gate
  // (shown right after login when any required consent has not yet been granted).
  var isOnboardingConsent by remember { mutableStateOf(false) }

  // Network connectivity state
  var isNetworkConnected by remember { mutableStateOf(true) }

  // Track meeting to expand when navigating from AskKlik sources or traced segments
  var expandMeetingSessionId by remember { mutableStateOf<String?>(null) }
  var expandSegmentId by remember { mutableStateOf<String?>(null) }  // Unique segment identifier
  var expandSegmentText by remember { mutableStateOf<String?>(null) }  // Segment text content

  // Track entity to highlight/scroll-to when navigating to growth screen
  var highlightProjectId by remember { mutableStateOf<String?>(null) }
  var highlightPersonId by remember { mutableStateOf<String?>(null) }
  var highlightOrganizationId by remember { mutableStateOf<String?>(null) }

  // Typography settings
  var fontSizeScale by remember { mutableStateOf(1f) }
  var letterSpacingScale by remember { mutableStateOf(1f) }
  var lineHeightScale by remember { mutableStateOf(1f) }

  // Auth state
  val authViewModel = rememberViewModel { AuthViewModel() }
  val authState by authViewModel.state.collectAsState()
  var minLoadTimeCompleted by remember { mutableStateOf(false) }

  // Single stable flag for auth readiness - only transitions false→true ONCE
  // This eliminates the cancelled first HTTP request caused by multiple LaunchedEffect key changes
  var isAuthReady by remember { mutableStateOf(false) }

  // Apple Calendar events fetched from iOS Calendar (two-way sync)
  var appleCalendarMeetings by remember { mutableStateOf<List<Meeting>>(emptyList()) }

  // Check auth state on startup and sync CurrentUser
  LaunchedEffect(Unit) {
    // Initialize archive/pin state from persistent storage FIRST
    initializeArchivePinState()

    // Start network connectivity monitoring
    NetworkMonitor.startMonitoring()

    // Start lifecycle observer for foreground/background detection
    AppLifecycleObserver.startObserving()

    val isLoggedIn = authViewModel.checkAuthState()
    // Sync CurrentUser singleton with auth state for API calls
    AppModule.syncCurrentUser()
    // Set auth ready flag ONLY if user is logged in - this is the single trigger point
    if (isLoggedIn) {
      isAuthReady = true
    }
  }

  // Poll network connectivity and check for deep links every 3 seconds
  LaunchedEffect(Unit) {
    while (true) {
      isNetworkConnected = NetworkMonitor.isConnected()
      // Check for deep links that arrived while app is running
      if (isAuthReady) {
        val pendingUrl = DeepLinkHandler.consumePendingDeepLink()
        if (pendingUrl != null) {
          val action = parseDeepLink(pendingUrl)
          if (action != null) {
            KlikLogger.i("MainApp", "Processing runtime deep link: ${action.route} entity=${action.entityType}/${action.entityId}")
            when (action.entityType) {
              "meeting" -> {
                expandMeetingSessionId = action.entityId
                currentRoute = "today"
              }
              "person" -> {
                action.entityId?.let { highlightPersonId = it }
                currentRoute = "growth"
              }
              "project" -> {
                action.entityId?.let { highlightProjectId = it }
                currentRoute = "growth"
              }
              "organization" -> {
                action.entityId?.let { highlightOrganizationId = it }
                currentRoute = "growth"
              }
              else -> {
                currentRoute = action.route
              }
            }
          }
        }
      }
      delay(3000L)
    }
  }

  // Clean up network monitor on dispose
  DisposableEffect(Unit) {
    onDispose {
      NetworkMonitor.stopMonitoring()
    }
  }

  // Auto-stop recording when the app exits while a FixedSession is active.
  // Uses a scope independent of the Compose lifecycle so the stop request
  // reaches the backend even if the composition is being torn down.
  DisposableEffect(Unit) {
    onDispose {
      if (isFixedSessionRecording) {
        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
          try {
            FixedSessionAudioStreamer.stopStreaming()
            RemoteDataFetcher.stopFixedSession()
            KlikLogger.i("MainApp", "Recording auto-stopped on app exit")
          } catch (e: Exception) {
            KlikLogger.e("MainApp", "Failed to auto-stop recording on exit: ${e.message}", e)
          }
        }
      }
    }
  }

  // Process pending deep links once auth is ready
  LaunchedEffect(isAuthReady) {
    if (!isAuthReady) return@LaunchedEffect
    val pendingUrl = DeepLinkHandler.consumePendingDeepLink() ?: return@LaunchedEffect
    val action = parseDeepLink(pendingUrl) ?: return@LaunchedEffect
    KlikLogger.i("MainApp", "Processing deep link: ${action.route} entity=${action.entityType}/${action.entityId}")
    // Navigate to the target entity
    when (action.entityType) {
      "meeting" -> {
        expandMeetingSessionId = action.entityId
        currentRoute = "today"
      }
      "person" -> {
        action.entityId?.let { highlightPersonId = it }
        currentRoute = "growth"
      }
      "project" -> {
        action.entityId?.let { highlightProjectId = it }
        currentRoute = "growth"
      }
      "organization" -> {
        action.entityId?.let { highlightOrganizationId = it }
        currentRoute = "growth"
      }
      else -> {
        currentRoute = action.route
      }
    }
  }

  // React to auth state changes:
  // - Login: set isAuthReady to start data loading
  // - Logout (or forced token expiry): reset isAuthReady to stop all data loading
  LaunchedEffect(authState.isLoggedIn) {
    if (authState.isLoggedIn && !isAuthReady) {
      AppModule.syncCurrentUser()
      isAuthReady = true
    } else if (!authState.isLoggedIn && isAuthReady) {
      KlikLogger.i("MainApp", "Auth state cleared — resetting isAuthReady")
      isAuthReady = false
      // Clear Apple sync state and calendar events on logout
      AppleSyncManager.clearSyncState()
      appleCalendarMeetings = emptyList()
      // Clear cached LLM data to prevent cross-user data leak
      LlmDataCache.insights = null
      LlmDataCache.encourageData = null
      LlmDataCache.worklifeData = null
      // Clear per-user consent state so the next login re-fetches it
      hasCompletedKlikOnboarding = null
      hasRecordingConsent = null
      hasBiometricConsent = null
      pendingPostConsentAction = null
      isOnboardingConsent = false
      // Reset init guards so re-login triggers fresh data loading
      InitGuard.mainInitStartedAt = 0L
      InitGuard.todosInitStartedAt = 0L
      InitGuard.integrationsCheckedAt = 0L
      // Reset AppModule so it re-initializes on next login
      AppModule.resetForLogout()
    }
  }

  // Check recording consent status separately so composition disposal of the init
  // LaunchedEffect (which sets isAuthReady=true before this runs) can't skip it
  LaunchedEffect(isAuthReady) {
    if (isAuthReady && hasRecordingConsent == null) {
      try {
          val consentStatus = RemoteDataFetcher.getRecordingConsentStatus()
          hasRecordingConsent = consentStatus.isGranted
          KlikLogger.i("MainApp", "Recording consent status: ${consentStatus.isGranted}")
      } catch (e: kotlin.coroutines.cancellation.CancellationException) {
          throw e
      } catch (e: Exception) {
          KlikLogger.w("MainApp", "Could not check recording consent status: ${e.message}")
          hasRecordingConsent = false
      }
    }
  }

  // Check biometric voiceprint consent status (BIPA compliance — required for meeting recording)
  LaunchedEffect(isAuthReady) {
    if (isAuthReady && hasBiometricConsent == null) {
      try {
          val consentStatus = RemoteDataFetcher.getBiometricConsentStatus()
          hasBiometricConsent = consentStatus.isGranted
          KlikLogger.i("MainApp", "Biometric consent status: ${consentStatus.isGranted}")
      } catch (e: kotlin.coroutines.cancellation.CancellationException) {
          throw e
      } catch (e: Exception) {
          KlikLogger.w("MainApp", "Could not check biometric consent status: ${e.message}")
          hasBiometricConsent = false
      }
    }
  }

  // Resolve Klik One first-run completion. Backend (KK_auth) is the source of
  // truth — survives reinstalls, follows the user across devices. SecureStorage
  // is a write-through cache for offline-first launches and a one-shot
  // backfill bridge for installs that completed onboarding before this
  // change shipped (legacy local-only flag → push to server on first run).
  LaunchedEffect(isAuthReady, authState.userId) {
    if (isAuthReady && hasCompletedKlikOnboarding == null) {
      val uid = authState.userId
      if (uid.isNullOrBlank()) return@LaunchedEffect

      val cacheKey = KlikOneOnboardingKeys.completedKey(uid)
      val localCached = onboardingStorage.getString(cacheKey) == "true"

      val serverCompleted = try {
        io.github.fletchmckee.liquid.samples.app.data.network.OnboardingStateClient
          .fetch().onboardingComplete
      } catch (ce: kotlin.coroutines.cancellation.CancellationException) {
        // Composition disposed mid-fetch — normal Compose lifecycle, not an error.
        // Re-throw so the LaunchedEffect coroutine cleans up correctly.
        throw ce
      } catch (t: Throwable) {
        KlikLogger.e("MainApp", "Onboarding state fetch failed: ${t.message}", t)
        // No fallback fabrication — fall back to local cache only when server
        // is unreachable, never to "false".
        null
      }

      hasCompletedKlikOnboarding = when {
        serverCompleted == true -> {
          // Mirror server → cache so next cold-start is instant.
          if (!localCached) onboardingStorage.saveString(cacheKey, "true")
          true
        }
        serverCompleted == false && localCached -> {
          // Pre-migration install: backfill the server with a single PATCH so
          // future devices/reinstalls see it.
          try {
            io.github.fletchmckee.liquid.samples.app.data.network.OnboardingStateClient
              .patch(onboardingCompleted = true)
            KlikLogger.i("MainApp", "Backfilled onboarding completion to server for $uid")
          } catch (ce: kotlin.coroutines.cancellation.CancellationException) {
            // Composition disposed mid-PATCH — normal Compose lifecycle, rethrow.
            throw ce
          } catch (t: Throwable) {
            KlikLogger.e("MainApp", "Onboarding backfill failed: ${t.message}", t)
          }
          true
        }
        serverCompleted == false -> false
        // serverCompleted == null → server unreachable. Trust local cache.
        else -> localCached
      }
    }
  }

  // Mandatory onboarding gate. Order: Klik One first-run → recording consent →
  // biometric consent → main app. Runs whenever any flag resolves so chained
  // steps flow without intermediate flicker.
  LaunchedEffect(isAuthReady, hasCompletedKlikOnboarding, hasRecordingConsent, hasBiometricConsent) {
    if (!isAuthReady) return@LaunchedEffect
    if (hasCompletedKlikOnboarding == null) return@LaunchedEffect
    if (hasRecordingConsent == null || hasBiometricConsent == null) return@LaunchedEffect

    when {
      hasCompletedKlikOnboarding == false -> {
        isOnboardingConsent = true
        lastMainRoute = "today"
        currentRoute = "klikone_onboarding"
      }
      hasRecordingConsent == false -> {
        isOnboardingConsent = true
        lastMainRoute = "today"
        currentRoute = "recording_consent"
      }
      hasBiometricConsent == false -> {
        isOnboardingConsent = true
        lastMainRoute = "today"
        currentRoute = "biometric_consent"
      }
      isOnboardingConsent -> {
        isOnboardingConsent = false
        currentRoute = "today"
      }
    }
  }

  // Calendar State - selected date and expansion state (defaults to actual today)
  val actualToday = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
  var selectedDate by remember { mutableStateOf(actualToday) }
  var isCalendarExpanded by remember { mutableStateOf(false) }

  // Badge count for review items - loaded from task summary
  var reviewBadgeCount by remember { mutableStateOf(0) }

  // Meetings loaded from repository
  var meetings by remember { mutableStateOf<List<Meeting>>(emptyList()) }

  // Insights loaded from KLIK Insights API (user-specific)
  // Initialized from LlmDataCache so data survives composition recreation
  var insights by remember { mutableStateOf(LlmDataCache.insights) }

  // Encourage data loaded from Encourage API (port 8335, user-specific)
  var encourageData by remember { mutableStateOf(LlmDataCache.encourageData) }

  // Worklife data loaded from Worklife API (port 8337, user-specific)
  var worklifeData by remember { mutableStateOf(LlmDataCache.worklifeData) }

  // Notifications state
  var notifications by remember { mutableStateOf<List<NotificationDto>>(emptyList()) }
  var unreadNotifCount by remember { mutableStateOf(0) }
  var isNotificationsLoading by remember { mutableStateOf(false) }
  var isNotificationsRefreshing by remember { mutableStateOf(false) }
  var notificationsRefreshKey by remember { mutableStateOf(0) }

  // Subscription state (from KK_subscription API - port 8416)
  var subscriptionData by remember { mutableStateOf<io.github.fletchmckee.liquid.samples.app.domain.entity.Subscription?>(null) }
  var subscriptionFeatures by remember { mutableStateOf<io.github.fletchmckee.liquid.samples.app.domain.entity.SubscriptionFeatures?>(null) }
  var subscriptionPlans by remember { mutableStateOf<List<io.github.fletchmckee.liquid.samples.app.domain.entity.SubscriptionPlan>>(emptyList()) }
  var subscriptionRefreshKey by remember { mutableStateOf(0) }
  var showUpgradeModal by remember { mutableStateOf(false) }
  var upgradeModalFeatureName by remember { mutableStateOf("") }

  // Speaker map for mapping VP IDs to names in transcripts
  var speakerMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

  // Initialize crash reporter at app startup
  LaunchedEffect(Unit) {
    CrashReporter.initialize()
  }

  // Start remote log shipper so all KlikLogger entries are streamed to KK_logs
  // in 10s batches. Safe to start before auth — it buffers until a token exists.
  LaunchedEffect(Unit) {
    io.github.fletchmckee.liquid.samples.app.logging.RemoteLogShipper.start(
      platform = io.github.fletchmckee.liquid.samples.app.data.network.DeviceInfo.getDeviceType(),
      deviceId = io.github.fletchmckee.liquid.samples.app.data.network.DeviceInfo.getDeviceId()
    )
  }

  // Track current route for crash context
  LaunchedEffect(currentRoute) {
    CrashReporter.setCurrentRoute(currentRoute)
  }

  // Error state for initialization failures
  var initError by remember { mutableStateOf<String?>(null) }

  // Retry trigger - incrementing this re-triggers the init LaunchedEffect
  var initRetryTrigger by remember { mutableStateOf(0) }

  // Loading state for Functions/Events screen
  var isEventsLoading by remember { mutableStateOf(true) }

  // Loading state for Growth/WorkLife screen (encourage, worklife, entities)
  var isGrowthLoading by remember { mutableStateOf(true) }

  // Loading state for LLM-generated content (insights, encourage, worklife)
  var isLlmDataLoading by remember { mutableStateOf(true) }

  // Refresh key to trigger re-fetching of meetings data
  var meetingsRefreshKey by remember { mutableStateOf(0) }

  // Refresh state for CalendarScreen pull-to-refresh indicator
  var isMeetingsRefreshing by remember { mutableStateOf(false) }

  // Function to refresh meetings data - use stable callback pattern
  val onRefreshMeetings: () -> Unit = {
    KlikLogger.d("MainApp", "refreshMeetings lambda called, meetingsRefreshKey BEFORE increment: $meetingsRefreshKey")
    isMeetingsRefreshing = true
    meetingsRefreshKey++
    KlikLogger.d("MainApp", "meetingsRefreshKey AFTER increment: $meetingsRefreshKey")
  }

  // Coroutine scope for archive operations
  val archiveScope = androidx.compose.runtime.rememberCoroutineScope()

  // Function to archive a meeting and refresh the list
  val onArchiveMeeting: (String) -> Unit = { meetingId ->
    archiveScope.launch {
      KlikLogger.d("MainApp", "Archiving meeting: $meetingId")
      val result = AppModule.archiveMeeting(meetingId)
      if (result is Result.Success) {
        KlikLogger.i("MainApp", "Meeting archived successfully, refreshing list")
        meetingsRefreshKey++
      } else {
        KlikLogger.e("MainApp", "Failed to archive meeting: $result")
      }
    }
  }

  // Refresh key for tasks/events screen data
  var tasksRefreshKey by remember { mutableStateOf(0) }
  var isTasksRefreshing by remember { mutableStateOf(false) }
  // Timestamp (epoch ms) of the last failed tasks refresh — used to prevent infinite retry loops.
  // When the KK_exec API is returning errors, rapid state changes can re-trigger refresh
  // (e.g., foreground events, recomposition). This cooldown blocks automatic re-triggering
  // for 10 seconds after a failure, while still allowing explicit user pull-to-refresh.
  var lastTasksRefreshFailedAt by remember { mutableStateOf(0L) }

  // Auto-refresh meetings + tasks as soon as the orchestrator reports a
  // successful pipeline completion. Triggered from the polling LaunchedEffect
  // above so the newly processed session appears on Today without the user
  // pulling to refresh.
  LaunchedEffect(pipelineCompletedTrigger) {
    if (pipelineCompletedTrigger > 0) {
      KlikLogger.i("MainApp", "pipeline_completed → refreshing meetings + tasks (trigger=$pipelineCompletedTrigger)")
      meetingsRefreshKey++
      val now = Clock.System.now().toEpochMilliseconds()
      val timeSinceTasksFailure = now - lastTasksRefreshFailedAt
      if (lastTasksRefreshFailedAt > 0 && timeSinceTasksFailure < 10_000) {
        KlikLogger.d("MainApp", "pipeline_completed refresh skipping tasks — last refresh failed ${timeSinceTasksFailure}ms ago")
      } else {
        tasksRefreshKey++
      }
    }
  }

  // Function to refresh tasks data (pull-to-refresh on EventsScreen)
  val onRefreshTasks: () -> Unit = {
    val now = Clock.System.now().toEpochMilliseconds()
    val timeSinceLastFailure = now - lastTasksRefreshFailedAt
    if (isTasksRefreshing) {
      KlikLogger.d("MainApp", "refreshTasks skipped — already refreshing")
    } else if (lastTasksRefreshFailedAt > 0 && timeSinceLastFailure < 10_000) {
      KlikLogger.d("MainApp", "refreshTasks skipped — last refresh failed ${timeSinceLastFailure}ms ago (cooldown 10s)")
    } else {
      KlikLogger.d("MainApp", "refreshTasks called, tasksRefreshKey: $tasksRefreshKey")
      isTasksRefreshing = true
      tasksRefreshKey++
    }
  }

  // Refresh key for growth screen entities (projects, people, organizations)
  var growthRefreshKey by remember { mutableStateOf(0) }

  // Function to refresh growth data (pull-to-refresh on WorkLifeScreen)
  val onRefreshGrowth: () -> Unit = {
    KlikLogger.d("MainApp", "refreshGrowth called, growthRefreshKey: $growthRefreshKey")
    growthRefreshKey++
  }

  // Function to archive a project
  val onArchiveProject: (String) -> Unit = { projectId ->
    KlikLogger.d("MainApp", "Archiving project: $projectId")
    val success = AppModule.archiveProject(projectId)
    if (success) {
      KlikLogger.i("MainApp", "Project archived successfully")
      growthRefreshKey++
    } else {
      KlikLogger.e("MainApp", "Failed to archive project: $projectId")
    }
  }

  // Function to archive a person
  val onArchivePerson: (String) -> Unit = { personId ->
    KlikLogger.d("MainApp", "Archiving person: $personId")
    val success = AppModule.archivePerson(personId)
    if (success) {
      KlikLogger.i("MainApp", "Person archived successfully")
      growthRefreshKey++
    } else {
      KlikLogger.e("MainApp", "Failed to archive person: $personId")
    }
  }

  // Function to archive an organization
  val onArchiveOrganization: (String) -> Unit = { organizationId ->
    KlikLogger.d("MainApp", "Archiving organization: $organizationId")
    val success = AppModule.archiveOrganization(organizationId)
    if (success) {
      KlikLogger.i("MainApp", "Organization archived successfully")
      growthRefreshKey++
    } else {
      KlikLogger.e("MainApp", "Failed to archive organization: $organizationId")
    }
  }

  // ============== ARCHIVED ITEMS STATE ==============
  // Using global state from Models for archive/pin tracking
  // This ensures state persists across data refreshes
  val archivedTasks by archivedTasksState
  val archivedMeetings by archivedMeetingsState
  val archivedProjectIds by archivedProjectIdsState
  val archivedPersonIds by archivedPersonIdsState
  val archivedOrganizationIds by archivedOrganizationIdsState

  // Function to unarchive a task (uses global function)
  val onUnarchiveTask: (String) -> Unit = { taskId ->
    unarchiveTask(taskId)
  }

  // Function to unarchive a meeting (uses global function)
  val onUnarchiveMeeting: (String) -> Unit = { meetingId ->
    unarchiveMeeting(meetingId)
    meetingsRefreshKey++
  }

  // Function to unarchive a project (uses global function + AppModule)
  val onUnarchiveProject: (String) -> Unit = { projectId ->
    modelUnarchiveProject(projectId)
    AppModule.unarchiveProject(projectId)
    growthRefreshKey++
  }

  // Function to unarchive a person (uses global function + AppModule)
  val onUnarchivePerson: (String) -> Unit = { personId ->
    modelUnarchivePerson(personId)
    AppModule.unarchivePerson(personId)
    growthRefreshKey++
  }

  // Function to unarchive an organization (uses global function + AppModule)
  val onUnarchiveOrganization: (String) -> Unit = { organizationId ->
    modelUnarchiveOrganization(organizationId)
    AppModule.unarchiveOrganization(organizationId)
    growthRefreshKey++
  }

  // ============== INTEGRATION PROMPT STATE ==============
  // Integration repository for OAuth operations
  val integrationRepository = remember { IntegrationRepositoryImpl() }

  // Secure storage instance for integration preferences
  val integrationStorage = remember { SecureStorage() }

  // State for integration prompt dialog
  var showIntegrationPrompt by remember { mutableStateOf(false) }
  var unconnectedIntegrations by remember { mutableStateOf<List<IntegrationInfo>>(emptyList()) }

  // Coroutine scope for integration operations
  val integrationScope = androidx.compose.runtime.rememberCoroutineScope()

  // Application-level scope for critical operations that must survive composition changes.
  // Fixed session start/stop MUST complete even if the composable tree recomposes.
  val appScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }

  // Function to authorize an integration
  // Handles both OAuth (browser-based) and Apple native (system permission dialog) integrations
  // USER-SPECIFIC: All operations include user context in logging
  val onAuthorizeIntegration: (String) -> Unit = { providerId ->
    val userId = io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser.userId
    
    // Check if this is an Apple native integration
    if (IntegrationProviders.isAppleNativeProvider(providerId)) {
      // Apple native integration - request system permission
      KlikLogger.d("MainApp", "Requesting Apple native permission: provider=$providerId, user=$userId")
      integrationRepository.requestAppleNativePermission(providerId) { granted ->
        KlikLogger.i("MainApp", "Apple permission result: provider=$providerId, user=$userId, granted=$granted")
        if (granted) {
          // Sync to backend with proper error handling
          integrationScope.launch {
            val syncResult = integrationRepository.syncAppleNativeCredential(providerId, true)
            syncResult.fold(
              onSuccess = {
                KlikLogger.i("MainApp", "Apple credential synced to backend: provider=$providerId, user=$userId")
              },
              onFailure = { error ->
                KlikLogger.e("MainApp", "Failed to sync Apple credential to backend: provider=$providerId, user=$userId, error=${error.message}", error)
              }
            )
          }
          // Update local storage with user context
          when (providerId) {
            IntegrationProviders.APPLE_CALENDAR -> {
              integrationStorage.saveString(AppleIntegrationStorageKeys.CALENDAR_PERMISSION_GRANTED, "true")
              KlikLogger.i("MainApp", "Saved calendar permission to local storage: user=$userId")
            }
            IntegrationProviders.APPLE_REMINDERS -> {
              integrationStorage.saveString(AppleIntegrationStorageKeys.REMINDERS_PERMISSION_GRANTED, "true")
              KlikLogger.i("MainApp", "Saved reminders permission to local storage: user=$userId")
            }
          }
          // Remove from unconnected list
          unconnectedIntegrations = unconnectedIntegrations.filter { it.providerId != providerId }
          // Hide dialog if all integrations are now connected
          if (unconnectedIntegrations.isEmpty()) {
            showIntegrationPrompt = false
          }
        } else {
          KlikLogger.w("MainApp", "Apple permission denied by user: provider=$providerId, user=$userId")
        }
      }
    } else {
      // OAuth integration — in-app session (ASWebAuthenticationSession on iOS,
      // browser + klik:// deep-link return on Android). User stays in Klik;
      // the post-OAuth redirect is captured by the system and the row flips
      // to Connected immediately, no foreground-refresh delay.
      integrationScope.launch {
        KlikLogger.d("MainApp", "Starting OAuth: provider=$providerId, user=$userId")
        val result = integrationRepository.getAuthorizationUrl(providerId, callbackScheme = OAUTH_CALLBACK_SCHEME)
        result.fold(
          onSuccess = { response ->
            showIntegrationPrompt = false
            when (val outcome = OAuthBrowser.openOAuthSession(response.authorizationUrl, OAUTH_CALLBACK_SCHEME)) {
              is OAuthSessionResult.Completed -> {
                if (outcome.isSuccess) {
                  KlikLogger.i("MainApp", "OAuth completed: provider=$providerId, user=$userId")
                  unconnectedIntegrations = unconnectedIntegrations.filter { it.providerId != providerId }
                } else {
                  KlikLogger.w("MainApp", "OAuth callback returned error: code=${outcome.errorCode}, provider=${outcome.provider}")
                }
              }
              is OAuthSessionResult.Cancelled -> {
                KlikLogger.i("MainApp", "OAuth cancelled by user: provider=$providerId")
              }
              is OAuthSessionResult.Error -> {
                KlikLogger.e("MainApp", "OAuth session error: provider=$providerId, msg=${outcome.message}")
              }
            }
          },
          onFailure = { error ->
            KlikLogger.e("MainApp", "Failed to get authorization URL: provider=$providerId, user=$userId, error=${error.message}", error)
          }
        )
      }
    }
  }

  // Function to handle "never show again" preference
  val onNeverShowIntegrationPrompt: () -> Unit = {
    integrationStorage.saveString(IntegrationStorageKeys.NEVER_PROMPT_INTEGRATIONS, "true")
    showIntegrationPrompt = false
    KlikLogger.i("MainApp", "User opted out of integration prompts")
  }

  // Debug: Log callback hash to verify it's the same instance passed through chain
  LaunchedEffect(Unit) {
    KlikLogger.d("MainApp", "onRefreshMeetings callback created (hash: ${onRefreshMeetings.hashCode()})")
  }

  // Foreground refresh: When app returns from background, refresh all data
  // This collects lifecycle events from AppLifecycleObserver and triggers data refresh
  LaunchedEffect(isAuthReady) {
    if (!isAuthReady) return@LaunchedEffect

    AppLifecycleObserver.foregroundEvents.collectLatest { isForeground ->
      if (isForeground) {
        KlikLogger.i("MainApp", "App returned to foreground — triggering data refresh")
        // Increment all refresh keys to trigger data reload
        meetingsRefreshKey++
        // Guard tasks refresh against recent failure to prevent infinite retry loop
        val now = Clock.System.now().toEpochMilliseconds()
        val timeSinceTasksFailure = now - lastTasksRefreshFailedAt
        if (lastTasksRefreshFailedAt > 0 && timeSinceTasksFailure < 10_000) {
          KlikLogger.d("MainApp", "Foreground refresh skipping tasks — last refresh failed ${timeSinceTasksFailure}ms ago")
        } else {
          tasksRefreshKey++
        }
        growthRefreshKey++
        notificationsRefreshKey++
      }
    }
  }

  // Initialize app and load data when user is logged in
  // Wait for isAuthReady flag - only triggers ONCE after auth check completes with logged-in user
  // IMPORTANT: This is keyed ONLY on isAuthReady so it runs once and is NOT cancelled by meetingsRefreshKey changes.
  // Meetings refresh is handled by a separate LaunchedEffect below.
  LaunchedEffect(isAuthReady, initRetryTrigger) {
    KlikLogger.d("MainApp", "LaunchedEffect[init] triggered: isAuthReady=$isAuthReady, retryTrigger=$initRetryTrigger")

    // Don't start initialization until auth is ready (user is logged in)
    if (!isAuthReady) return@LaunchedEffect

    // Guard against duplicate init from composition disposal/recreation
    if (!InitGuard.tryStartMainInit()) {
      KlikLogger.d("MainApp", "Skipping duplicate init (started ${kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - InitGuard.mainInitStartedAt}ms ago)")
      // Main init already ran or is running, but slow LLM coroutines may have been
      // cancelled when composition was disposed. Re-fetch any missing LLM data.
      val shouldFetchMissingLlmData = insights == null || encourageData == null || worklifeData == null
      if (shouldFetchMissingLlmData) {
        isLlmDataLoading = true
        launch {
          try {
            kotlinx.coroutines.coroutineScope {
              if (insights == null) {
                launch {
                  try {
                    val fetched = RemoteDataFetcher.fetchInsights()
                    LlmDataCache.insights = fetched
                    insights = fetched
                  } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                    throw e
                  } catch (e: Exception) {
                    KlikLogger.e("MainApp", "Failed to fetch insights (retry): ${e.message}")
                  }
                }
              }
              if (encourageData == null) {
                launch {
                  try {
                    val fetched = RemoteDataFetcher.fetchEncourage()
                    LlmDataCache.encourageData = fetched
                    encourageData = fetched
                  } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                    throw e
                  } catch (e: Exception) {
                    KlikLogger.e("MainApp", "Failed to fetch encourage (retry): ${e.message}")
                  }
                }
              }
              if (worklifeData == null) {
                launch {
                  try {
                    val fetched = RemoteDataFetcher.fetchWorklife()
                    LlmDataCache.worklifeData = fetched
                    worklifeData = fetched
                  } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                    throw e
                  } catch (e: Exception) {
                    KlikLogger.e("MainApp", "Failed to fetch worklife (retry): ${e.message}")
                  }
                }
              }
            }
          } finally {
            isLlmDataLoading = false
          }
        }
      } else {
        isLlmDataLoading = false
      }
      return@LaunchedEffect
    }

    // Clear any previous error before starting initialization
    initError = null

    try {
      // Initial load - full initialization
      AppModule.initialize()

      // Populate speakerMap from people data (for Transcript section name mapping)
      val peopleResult = AppModule.getPeopleUseCase()
      if (peopleResult is Result.Success) {
        speakerMap = peopleResult.data.filter { it.canonicalName.isNotBlank() }.associate { it.id to it.canonicalName }
        KlikLogger.i("MainApp", "speakerMap initialized with ${speakerMap.size} entries")
      }

      // CRITICAL: Mark loading flags done NOW, before slow LLM calls.
      // Insights/encourage/worklife are slow POST endpoints (LLM, >60s).
      // If composition is disposed during these calls, the loading flags would never
      // be set to false, causing the UI to show loaders forever.
      isGrowthLoading = false
      isEventsLoading = false

      // Launch slow LLM calls in parallel, non-blocking.
      // These update both the composable state AND the static LlmDataCache
      // so data survives composition disposal/recreation.
      isLlmDataLoading = true
      launch {
        try {
          kotlinx.coroutines.coroutineScope {
            launch {
              try {
                val fetchedInsights = RemoteDataFetcher.fetchInsights()
                LlmDataCache.insights = fetchedInsights
                insights = fetchedInsights
              } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
              } catch (e: Exception) {
                KlikLogger.e("MainApp", "Failed to fetch insights: ${e.message}")
              }
            }
            launch {
              try {
                val fetchedEncourage = RemoteDataFetcher.fetchEncourage()
                LlmDataCache.encourageData = fetchedEncourage
                encourageData = fetchedEncourage
                KlikLogger.i("MainApp", "Loaded encourage data: ${fetchedEncourage.message.take(50)}")
              } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
              } catch (e: Exception) {
                KlikLogger.e("MainApp", "Failed to fetch encourage: ${e.message}")
              }
            }
            launch {
              try {
                val fetchedWorklife = RemoteDataFetcher.fetchWorklife()
                LlmDataCache.worklifeData = fetchedWorklife
                worklifeData = fetchedWorklife
                KlikLogger.i("MainApp", "Loaded worklife data")
              } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
              } catch (e: Exception) {
                KlikLogger.e("MainApp", "Failed to fetch worklife: ${e.message}")
              }
            }
          }
        } finally {
          isLlmDataLoading = false
        }
      }

      // Load meetings from repository
      // Use collectLatest to avoid hanging on long-lived flows and ensure latest values are used
      try {
        AppModule.observeMeetingsUseCase().collectLatest { result ->
          if (result is Result.Success) {
            meetings = result.data  // Assign to state - this triggers recomposition
            KlikLogger.i("MainApp", "Meetings state updated: ${result.data.size} meetings now available in UI")
            // Log date distribution for debugging
            val dateDistribution = result.data.groupBy { it.date }.mapValues { it.value.size }
            KlikLogger.d("MainApp", "Meeting date distribution: $dateDistribution")
            KlikLogger.d("MainApp", "selectedDate for filtering: $selectedDate")
            val todayMeetings = result.data.filter { it.date == selectedDate }
            KlikLogger.d("MainApp", "Meetings matching selectedDate ($selectedDate): ${todayMeetings.size}")
            // Auto-sync meetings to Apple Calendar (if permission granted)
            AppleSyncManager.syncMeetingsToCalendar(result.data)
            // Fetch Apple Calendar events (two-way sync, deduplicated)
            AppleSyncManager.fetchAppleCalendarEvents { appleEvents ->
              appleCalendarMeetings = appleEvents
              KlikLogger.i("MainApp", "Apple Calendar events loaded: ${appleEvents.size}")
            }
          }
        }
      } catch (e: kotlin.coroutines.cancellation.CancellationException) {
        // Expected when composition is disposed - just rethrow
        throw e
      }
    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
      // Reset guard so the retry after composition recreation actually loads data
      InitGuard.mainInitStartedAt = 0L
      KlikLogger.w("MainApp", "Initialization cancelled (composition disposed), will retry")
      throw e
    } catch (e: Exception) {
      // Surface the actual error for debugging (real errors only)
      val errorMessage = "Initialization failed: ${e::class.simpleName}: ${e.message}"
      KlikLogger.e("MainApp", errorMessage, e)
      CrashReporter.reportError("MainApp", "Initialization failed", e)
      initError = errorMessage
      isEventsLoading = false // Stop loading on error too
      isGrowthLoading = false // Stop growth loading on error too
      isLlmDataLoading = false // isLlmDataLoading starts true; must clear on init failure or it hangs forever
    }
  }

  // Meetings refresh - isolated from initialization so changing meetingsRefreshKey
  // does NOT cancel token refresh, integration checks, KK_exec todo loading, or other init work.
  LaunchedEffect(meetingsRefreshKey) {
    if (meetingsRefreshKey == 0) return@LaunchedEffect  // Skip initial value; init handles first load
    if (!isAuthReady) return@LaunchedEffect

    KlikLogger.d("MainApp", "LaunchedEffect[refresh] triggered: refreshKey=$meetingsRefreshKey")

    try {
      // TARGETED REFRESH: If expandMeetingSessionId is set,
      // load meetings for the specific month containing that session
      if (expandMeetingSessionId != null) {
        KlikLogger.d("MainApp", "TARGETED REFRESH for session: $expandMeetingSessionId")

        // Extract date from session ID
        val sessionDate = io.github.fletchmckee.liquid.samples.app.utils.SessionIdUtils.extractDateFromSessionId(expandMeetingSessionId!!)

        if (sessionDate != null) {
          // Get month range for the meeting
          val (monthStart, monthEnd) = io.github.fletchmckee.liquid.samples.app.utils.SessionIdUtils.extractDateRangeForMonth(sessionDate)
          KlikLogger.d("MainApp", "Loading meetings for date range: $monthStart to $monthEnd")

          // Fetch meetings for that month
          val monthMeetings = RemoteDataFetcher.fetchMeetings(
            limit = 500,
            startDate = monthStart,
            endDate = monthEnd
          )

          // Merge with existing meetings (avoid duplicates)
          val existingIds = meetings.map { it.id }.toSet()
          val newMeetings = monthMeetings.filter { it.id !in existingIds }
          meetings = meetings + newMeetings

          KlikLogger.i("MainApp", "Added ${newMeetings.size} meetings from historical range, total now: ${meetings.size}")

          // Also reload people data for updated speakerMap (in case names changed)
          val peopleResult = AppModule.getPeopleUseCase()
          if (peopleResult is Result.Success) {
            speakerMap = peopleResult.data.filter { it.canonicalName.isNotBlank() }.associate { it.id to it.canonicalName }
            KlikLogger.i("MainApp", "speakerMap refreshed with ${speakerMap.size} entries")
          }
        } else {
          KlikLogger.e("MainApp", "Could not extract date from session ID: $expandMeetingSessionId")
        }

        return@LaunchedEffect
      }

      // STANDARD REFRESH: Reload meetings AND people data
      // (People reload is needed for speakerMap to get updated canonical names after name corrections)
      KlikLogger.d("MainApp", "STANDARD REFRESH (key=$meetingsRefreshKey)")
      AppModule.reloadPeople()  // Reload people first (for updated canonical names)

      // Update speakerMap with fresh people data (critical for Transcript section)
      val peopleResult = AppModule.getPeopleUseCase()
      if (peopleResult is Result.Success) {
        speakerMap = peopleResult.data.filter { it.canonicalName.isNotBlank() }.associate { it.id to it.canonicalName }
        KlikLogger.i("MainApp", "speakerMap refreshed with ${speakerMap.size} entries")
      }

      AppModule.reloadMeetings()
      // Get the updated meetings directly from global state (reloadMeetings already updated it)
      // Use toList() to force a NEW list reference so Compose detects the change
      val updatedMeetings = meetingsState.value.toList()
      meetings = updatedMeetings
      KlikLogger.i("MainApp", "UI updated with ${updatedMeetings.size} meetings")
      // Auto-sync meetings to Apple Calendar on refresh (runs in background)
      AppleSyncManager.syncMeetingsToCalendar(updatedMeetings)
      // Re-fetch Apple Calendar events on refresh (callback-based, non-blocking)
      AppleSyncManager.fetchAppleCalendarEvents { appleEvents ->
        appleCalendarMeetings = appleEvents
      }
      // Refresh insights (shown on CalendarScreen) in parallel
      launch {
        try {
          val fetchedInsights = RemoteDataFetcher.fetchInsights()
          LlmDataCache.insights = fetchedInsights
          insights = fetchedInsights
          KlikLogger.i("MainApp", "Insights refreshed")
        } catch (e: kotlin.coroutines.cancellation.CancellationException) { throw e }
        catch (e: Exception) { KlikLogger.e("MainApp", "Failed to refresh insights: ${e.message}") }
      }
      // Clear refresh state - refresh complete
      isMeetingsRefreshing = false
      KlikLogger.i("MainApp", "Meetings refresh complete")
    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
      KlikLogger.w("MainApp", "Meetings refresh cancelled (key=$meetingsRefreshKey), will retry on next trigger")
      isMeetingsRefreshing = false
      throw e
    } catch (e: Exception) {
      KlikLogger.e("MainApp", "Meetings refresh failed: ${e::class.simpleName}: ${e.message}", e)
      isMeetingsRefreshing = false
    }
  }

  // Tasks refresh - reload KK_exec todos and task data when pull-to-refresh on EventsScreen
  LaunchedEffect(tasksRefreshKey) {
    if (tasksRefreshKey == 0) return@LaunchedEffect  // Skip initial value; init handles first load
    if (!isAuthReady) {
      isTasksRefreshing = false
      return@LaunchedEffect
    }

    KlikLogger.d("MainApp", "LaunchedEffect[tasksRefresh] triggered: refreshKey=$tasksRefreshKey")

    try {
      // Reload KK_exec todos
      val allTodos = RemoteDataFetcher.fetchAllKKExecTodos()
      if (allTodos.isNotEmpty()) {
        val taskMetadataList = resolveEntityRefs(allTodos.map { it.toTaskMetadata() })
        allKKExecTodosState.value = taskMetadataList
        kkExecSensitiveTodosState.value = taskMetadataList.filter { it.kkExecStatus?.equals("in_review", ignoreCase = true) == true }
        val dailyTodos = taskMetadataList.filter { it.kkExecStatus?.equals("in_review", ignoreCase = true) != true }
        kkExecDailyTodosState.value = dailyTodos
        kkExecDailyTodosGroupedState.value = dailyTodos.groupBy { task ->
          task.toolCategoriesNeeded.sorted().joinToString(", ").takeIf { it.isNotBlank() } ?: "uncategorized"
        }
        KlikLogger.i("MainApp", "Tasks refreshed: ${allTodos.size} todos")
        // Auto-sync todos to Apple Reminders on refresh
        AppleSyncManager.syncTodosToReminders(taskMetadataList)
      }

      // Reload task summary for badge count
      val summaryResult = AppModule.getTaskSummaryUseCase()
      if (summaryResult is Result.Success) {
        reviewBadgeCount = summaryResult.data.reviewCount
      }
      // Clear failure cooldown on success
      lastTasksRefreshFailedAt = 0L
    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
      KlikLogger.w("MainApp", "Tasks refresh cancelled (key=$tasksRefreshKey)")
      throw e
    } catch (e: Exception) {
      KlikLogger.e("MainApp", "Tasks refresh failed: ${e::class.simpleName}: ${e.message}", e)
      // Record failure timestamp to prevent infinite retry loops.
      // Automated refresh triggers (foreground events, recomposition) will be blocked
      // for 10 seconds. User-triggered pull-to-refresh still respects this cooldown
      // to avoid hammering a broken API.
      lastTasksRefreshFailedAt = Clock.System.now().toEpochMilliseconds()
    } finally {
      isTasksRefreshing = false
    }
  }

  // Auto-polling for real-time execution updates
  // When any todo is RUNNING/EVALUATING or we have locally-tracked executing IDs, poll every 10 seconds
  val hasExecutingTodo = allKKExecTodosState.value.any {
    it.kkExecStatus?.equals("RUNNING", ignoreCase = true) == true ||
    it.kkExecStatus?.equals("EVALUATING", ignoreCase = true) == true
  }
  val hasLocallyExecuting = executingTodoIdsState.value.isNotEmpty()

  LaunchedEffect(hasExecutingTodo, hasLocallyExecuting) {
    if ((!hasExecutingTodo && !hasLocallyExecuting) || !isAuthReady) return@LaunchedEffect

    KlikLogger.d("MainApp", "Starting execution polling - RUNNING/EVALUATING or locally executing todos")

    var pollCount = 0
    val maxPolls = 60 // Stop after 10 minutes (60 * 10s)

    while (pollCount < maxPolls) {
      kotlinx.coroutines.delay(10000) // Poll every 10 seconds
      pollCount++

      try {
        val allTodos = RemoteDataFetcher.fetchAllKKExecTodos()
        if (allTodos.isNotEmpty()) {
          val taskMetadataList = allTodos.map { it.toTaskMetadata() }
          allKKExecTodosState.value = taskMetadataList
          kkExecSensitiveTodosState.value = taskMetadataList.filter { it.kkExecStatus?.equals("in_review", ignoreCase = true) == true }
          val dailyTodos = taskMetadataList.filter { it.kkExecStatus?.equals("in_review", ignoreCase = true) != true }
          kkExecDailyTodosState.value = dailyTodos
          kkExecDailyTodosGroupedState.value = dailyTodos.groupBy { task ->
            task.toolCategoriesNeeded.sorted().joinToString(", ").takeIf { it.isNotBlank() } ?: "uncategorized"
          }

          // Detect todos that finished executing — remove from executingTodoIds and mark their categories
          val currentExecuting = executingTodoIdsState.value
          if (currentExecuting.isNotEmpty()) {
            val finishedIds = currentExecuting.filter { execId ->
              val todo = taskMetadataList.find { it.id == execId }
              // Finished if status is no longer RUNNING/EVALUATING/APPROVED/PENDING/IN_REVIEW
              todo != null && todo.kkExecStatus?.uppercase() in listOf("COMPLETED", "FAILED", "REJECTED", "CANNOT_EXECUTE", "ARCHIVED")
            }
            if (finishedIds.isNotEmpty()) {
              executingTodoIdsState.value = currentExecuting - finishedIds.toSet()
              // Mark their categories for red dot
              val finishedCategories = finishedIds.mapNotNull { fid ->
                taskMetadataList.find { it.id == fid }?.let { task ->
                  task.toolCategoriesNeeded.sorted().joinToString(", ").takeIf { it.isNotBlank() } ?: "uncategorized"
                }
              }.toSet()
              recentlyCompletedCategoriesState.value = recentlyCompletedCategoriesState.value + finishedCategories
              KlikLogger.i("MainApp", "Execution completed for ${finishedIds.size} todos, categories: $finishedCategories")
            }
          }

          // Check if still executing
          val stillExecuting = taskMetadataList.any {
            it.kkExecStatus?.equals("RUNNING", ignoreCase = true) == true ||
            it.kkExecStatus?.equals("EVALUATING", ignoreCase = true) == true
          }
          if (!stillExecuting && executingTodoIdsState.value.isEmpty()) {
            KlikLogger.i("MainApp", "Execution polling stopped - no more executing todos")
            break
          }
        }
      } catch (e: kotlin.coroutines.cancellation.CancellationException) {
        KlikLogger.d("MainApp", "Execution polling cancelled")
        throw e
      } catch (e: Exception) {
        KlikLogger.w("MainApp", "Execution poll failed: ${e.message}")
        // Continue polling despite errors
      }
    }
    if (pollCount >= maxPolls) {
      KlikLogger.i("MainApp", "Execution polling stopped - max polls reached ($maxPolls)")
    }
  }

  // Growth refresh - reload people, projects, organizations, encourage, worklife when pull-to-refresh on WorkLifeScreen
  LaunchedEffect(growthRefreshKey) {
    if (growthRefreshKey == 0) return@LaunchedEffect  // Skip initial value; init handles first load
    if (!isAuthReady) return@LaunchedEffect

    KlikLogger.d("MainApp", "LaunchedEffect[growthRefresh] triggered: refreshKey=$growthRefreshKey")

    try {
      // Reload entities
      AppModule.reloadPeople()
      val peopleResult = AppModule.getPeopleUseCase()
      if (peopleResult is Result.Success) {
        speakerMap = peopleResult.data.filter { it.canonicalName.isNotBlank() }.associate { it.id to it.canonicalName }
      }

      // Reload projects and organizations via full re-init of entity data
      val fetcher = RemoteDataFetcher
      val projects = fetcher.fetchProjects()
      io.github.fletchmckee.liquid.samples.app.model.projectsState.value = projects
      val orgs = fetcher.fetchOrganizations()
      io.github.fletchmckee.liquid.samples.app.model.organizationsState.value = orgs

      // Reload insights, encourage, and worklife data — fetchers throw on failure,
      // so the assignments are unconditional.
      val fetchedInsights = fetcher.fetchInsights()
      LlmDataCache.insights = fetchedInsights
      insights = fetchedInsights
      val fetchedEncourage = fetcher.fetchEncourage()
      LlmDataCache.encourageData = fetchedEncourage
      encourageData = fetchedEncourage
      val fetchedWorklife = fetcher.fetchWorklife()
      LlmDataCache.worklifeData = fetchedWorklife
      worklifeData = fetchedWorklife

      // Reload goals — fetch the full set (see AppModule.kt: pending_approval
      // goals would otherwise be invisible until the user approves them).
      val goals = fetcher.fetchGoals(status = null, limit = 50)
      io.github.fletchmckee.liquid.samples.app.model.goalsState.value = goals

      KlikLogger.i("MainApp", "Growth data refreshed: ${projects.size} projects, ${orgs.size} orgs")
    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
      KlikLogger.w("MainApp", "Growth refresh cancelled (key=$growthRefreshKey)")
      throw e
    } catch (e: Exception) {
      KlikLogger.e("MainApp", "Growth refresh failed: ${e::class.simpleName}: ${e.message}", e)
    }
  }

  // Load task summary for badge count when user is logged in
  LaunchedEffect(isAuthReady) {
    if (!isAuthReady) return@LaunchedEffect

    try {
      val result = AppModule.getTaskSummaryUseCase()
      if (result is Result.Success) {
        reviewBadgeCount = result.data.reviewCount
      }
    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
      throw e
    } catch (e: Exception) {
      KlikLogger.e("MainApp", "Failed to load task summary: ${e.message}", e)
    }
  }

  // Load KK_exec todos when user is logged in (user-specific and session-specific)
  LaunchedEffect(isAuthReady) {
    if (!isAuthReady) return@LaunchedEffect

    // Guard against duplicate loading from composition disposal/recreation
    if (!InitGuard.tryStartTodosInit()) {
      KlikLogger.d("MainApp", "Skipping duplicate todos init (started ${kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - InitGuard.todosInitStartedAt}ms ago)")
      return@LaunchedEffect
    }

    try {
      KlikLogger.d("MainApp", "Loading KK_exec todos")
      // Fetch all todos from all sessions for the current user
      val allTodos = RemoteDataFetcher.fetchAllKKExecTodos()

      if (allTodos.isNotEmpty()) {
        KlikLogger.i("MainApp", "Loaded ${allTodos.size} KK_exec todos")

        // Convert to TaskMetadata and store in global state
        val taskMetadataList = resolveEntityRefs(allTodos.map { it.toTaskMetadata() })
        allKKExecTodosState.value = taskMetadataList

        // Filter into categories:
        // Sensitive = status is in_review (needs user approval before execution)
        val sensitiveTodos = taskMetadataList.filter { it.kkExecStatus?.equals("in_review", ignoreCase = true) == true }
        kkExecSensitiveTodosState.value = sensitiveTodos
        KlikLogger.i("MainApp", "Sensitive todos: ${sensitiveTodos.size}")

        // Daily = everything else
        val dailyTodos = taskMetadataList.filter { it.kkExecStatus?.equals("in_review", ignoreCase = true) != true }
        kkExecDailyTodosState.value = dailyTodos
        KlikLogger.i("MainApp", "Daily todos: ${dailyTodos.size}")

        // Group daily todos by selected_sub_categories for category-based stacking
        val groupedDaily = dailyTodos.groupBy { task ->
            task.toolCategoriesNeeded.sorted().joinToString(", ").takeIf { it.isNotBlank() } ?: "uncategorized"
        }
        kkExecDailyTodosGroupedState.value = groupedDaily
        KlikLogger.i("MainApp", "Daily todos grouped into ${groupedDaily.size} categories")

        // Auto-sync all todos to Apple Reminders (if permission granted)
        AppleSyncManager.syncTodosToReminders(taskMetadataList)
      } else {
        KlikLogger.i("MainApp", "No KK_exec todos found")
      }
    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
      // Reset guard so the retry after composition recreation actually loads data
      InitGuard.todosInitStartedAt = 0L
      KlikLogger.w("MainApp", "KK_exec todos loading cancelled (composition disposed)")
      throw e
    } catch (e: Exception) {
      KlikLogger.e("MainApp", "Failed to load KK_exec todos: ${e.message}", e)
      // Set failure cooldown to prevent foreground/refresh events from immediately retrying
      lastTasksRefreshFailedAt = Clock.System.now().toEpochMilliseconds()
    }
  }

  // Load notifications from KK_notifications API (port 8341)
  LaunchedEffect(isAuthReady, notificationsRefreshKey) {
    if (!isAuthReady) return@LaunchedEffect

    isNotificationsLoading = true
    try {
      val response = RemoteDataFetcher.fetchNotifications()
      notifications = response.items
      unreadNotifCount = response.unreadCount
      KlikLogger.i("MainApp", "Loaded ${response.items.size} notifications (${response.unreadCount} unread)")
    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
      throw e
    } catch (e: Exception) {
      KlikLogger.e("MainApp", "Failed to load notifications: ${e.message}", e)
    } finally {
      isNotificationsLoading = false
      isNotificationsRefreshing = false
    }
  }

  // Load subscription data from KK_subscription API (port 8416)
  LaunchedEffect(isAuthReady, subscriptionRefreshKey) {
    if (!isAuthReady) return@LaunchedEffect

    try {
      val fetchedSubscription = RemoteDataFetcher.fetchMySubscription()
      subscriptionData = fetchedSubscription
      KlikLogger.i("MainApp", "Loaded subscription: ${fetchedSubscription.planCode} (${fetchedSubscription.status})")

      val fetchedFeatures = RemoteDataFetcher.fetchSubscriptionFeatures()
      subscriptionFeatures = fetchedFeatures
      KlikLogger.i("MainApp", "Loaded subscription features: tier=${fetchedFeatures.tier}, goals=${fetchedFeatures.goalsEnabled}")

      val fetchedPlans = RemoteDataFetcher.fetchSubscriptionPlans()
      subscriptionPlans = fetchedPlans
      KlikLogger.i("MainApp", "Loaded ${fetchedPlans.size} subscription plans")
    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
      throw e
    } catch (e: Exception) {
      KlikLogger.e("MainApp", "Failed to load subscription data: ${e.message}", e)
    }
  }

  // Check for unconnected integrations on login and show prompt if needed
  // This includes both OAuth integrations and Apple native integrations (Calendar, Reminders)
  // USER-SPECIFIC: All checks and logging include user context
  LaunchedEffect(isAuthReady) {
    if (!isAuthReady) return@LaunchedEffect
    if (!InitGuard.tryStartIntegrationsCheck()) {
      KlikLogger.d("MainApp", "Skipping duplicate integration check")
      return@LaunchedEffect
    }

    val userId = io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser.userId

    try {
      KlikLogger.d("MainApp", "Checking integration status for user=$userId")

      // Check if user opted out of prompts
      val neverPrompt = integrationStorage.getString(IntegrationStorageKeys.NEVER_PROMPT_INTEGRATIONS) == "true"
      if (neverPrompt) {
        KlikLogger.i("MainApp", "User=$userId opted out of integration prompts, skipping check")
        return@LaunchedEffect
      }

      // Collect all unconnected integrations from both OAuth and Apple native sources
      val allUnconnected = mutableListOf<IntegrationInfo>()

      // 1. Check Apple native integrations first (iOS only)
      if (AppleIntegrationService.isSupported()) {
        KlikLogger.d("MainApp", "Checking Apple native integration permissions for user=$userId")
        
        // Check if user has already declined Apple integrations prompt
        val neverPromptApple = integrationStorage.getString(AppleIntegrationStorageKeys.NEVER_PROMPT_APPLE) == "true"
        
        if (!neverPromptApple) {
          val appleIntegrations = integrationRepository.getAppleNativeIntegrations()
          val unconnectedApple = appleIntegrations.filter { !it.connected }
          
          if (unconnectedApple.isNotEmpty()) {
            KlikLogger.i("MainApp", "Found ${unconnectedApple.size} unconnected Apple native integrations for user=$userId: ${unconnectedApple.map { it.providerId }}")
            allUnconnected.addAll(unconnectedApple)
          } else {
            KlikLogger.i("MainApp", "All Apple native integrations already connected for user=$userId")
          }
        } else {
          KlikLogger.i("MainApp", "User=$userId opted out of Apple integration prompts")
        }
      }

      // 2. Fetch OAuth integration info to see which are connected
      val result = integrationRepository.getIntegrationInfoList()
      result.fold(
        onSuccess = { integrations ->
          // Filter out Apple native providers (they're handled separately above)
          val oauthIntegrations = integrations.filter { !IntegrationProviders.isAppleNativeProvider(it.providerId) }
          val unconnectedOAuth = oauthIntegrations.filter { !it.connected && it.configured }
          KlikLogger.i("MainApp", "Found ${unconnectedOAuth.size} unconnected OAuth integrations for user=$userId: ${unconnectedOAuth.map { it.providerId }}")
          allUnconnected.addAll(unconnectedOAuth)
        },
        onFailure = { error ->
          KlikLogger.e("MainApp", "Failed to check OAuth integrations for user=$userId: ${error.message}", error)
        }
      )

      // Show prompt if there are any unconnected integrations
      if (allUnconnected.isNotEmpty()) {
        unconnectedIntegrations = allUnconnected
        showIntegrationPrompt = true
        KlikLogger.i("MainApp", "Showing integration prompt dialog for user=$userId with ${allUnconnected.size} unconnected integrations: ${allUnconnected.map { it.providerId }}")
      } else {
        KlikLogger.i("MainApp", "All integrations connected for user=$userId, no prompt needed")
      }
    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
      throw e
    } catch (e: Exception) {
      KlikLogger.e("MainApp", "Failed checking integrations for user=$userId: ${e.message}", e)
    }
  }

  // Hydrate K1 prefs (dark mode, font index, haptic) from the server on every
  // auth-ready transition. PreferencesScreen also updates these on each save,
  // but the cold-start path needs this so the toggle state survives relaunches.
  LaunchedEffect(isAuthReady) {
    if (!isAuthReady) return@LaunchedEffect
    try {
      val remotePrefs = RemoteDataFetcher.fetchUserPreferences()
      io.github.fletchmckee.liquid.samples.app.model.darkModeEnabledState.value = remotePrefs.darkModeEnabled
      io.github.fletchmckee.liquid.samples.app.model.selectedFontIndexState.value = remotePrefs.selectedFontIndex
      io.github.fletchmckee.liquid.samples.app.model.hapticEnabledState.value = remotePrefs.hapticFeedbackEnabled
      KlikLogger.i(
        "MainApp",
        "User preferences hydrated: dark=${remotePrefs.darkModeEnabled}, " +
          "font=${remotePrefs.selectedFontIndex}, haptic=${remotePrefs.hapticFeedbackEnabled}",
      )
    } catch (ce: kotlin.coroutines.cancellation.CancellationException) {
      throw ce
    } catch (t: Throwable) {
      KlikLogger.e("MainApp", "User preferences hydration failed: ${t.message}", t)
    }
  }

  // Load initial preferences from local storage (for persisted defaults)
  val initialPrefs = remember { AppModule.getInitialPreferences() }
  val initialBackgroundIndex = initialPrefs?.defaultBackgroundIndex ?: 1
  val initialFontIndex = initialPrefs?.defaultFontIndex ?: 2

  // Background State - track both option and index for accent color (use saved defaults)
  var backgroundIndex by remember { mutableStateOf(initialBackgroundIndex) }
  var backgroundOption by remember { mutableStateOf(BackgroundOptions.getOrElse(initialBackgroundIndex) { BackgroundOptions[1] }) }

  // Font State - track selected font index (use saved default)
  var fontIndex by remember { mutableStateOf(initialFontIndex) }

  // Debug log to verify preferences are loaded correctly
  LaunchedEffect(Unit) {
    KlikLogger.i("MainApp", "Loaded initial preferences: bg=$initialBackgroundIndex, font=$initialFontIndex")
  }

  // Liquid Glass Settings State
  var liquidGlassSettings by remember { mutableStateOf(LiquidGlassSettings()) }

  // Get artistic accent gradient and primary color for the K logo based on current background
  val klikAccentGradient = getKlikAccentGradient(backgroundIndex)
  val klikPrimaryColor = getKlikPrimaryColor(backgroundIndex)

  val darkModeFromPrefs by io.github.fletchmckee.liquid.samples.app.model.darkModeEnabledState
  val fontIndexFromPrefs by io.github.fletchmckee.liquid.samples.app.model.selectedFontIndexState
  LiquidTheme(
    darkMode = darkModeFromPrefs,
    setBackgroundColor = { newOption ->
      backgroundOption = newOption
      backgroundIndex = BackgroundOptions.indexOf(newOption).coerceAtLeast(0)
    },
    liquidGlassSettings = liquidGlassSettings,
    setLiquidGlassSettings = { newSettings -> liquidGlassSettings = newSettings },
    backgroundIndex = backgroundIndex,
    fontIndex = fontIndexFromPrefs,
    setFontIndex = { newFontIndex ->
      fontIndex = newFontIndex
      io.github.fletchmckee.liquid.samples.app.model.selectedFontIndexState.value = newFontIndex
    },
    fontSizeScale = fontSizeScale,
    setFontSizeScale = { fontSizeScale = it },
    letterSpacingScale = letterSpacingScale,
    setLetterSpacingScale = { letterSpacingScale = it },
    lineHeightScale = lineHeightScale,
    setLineHeightScale = { lineHeightScale = it }
  ) {
    val snackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
    Scaffold(
      containerColor = Color.Transparent,
      snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
      BoxWithConstraints(
        Modifier.fillMaxSize()
    ) {
      // Determine app state: loading → auth or main
      // Note: Auth check completes within the 2.5s min load time, so authState.isLoggedIn is stable by then
      val appState = when {
          !minLoadTimeCompleted -> "loading"
          !authState.isLoggedIn -> "auth"
          else -> "main"
      }

      // Splash → app transition. The logo stays centered and simply fades out
      // (no drift, no morph) — the previous animation traveled the logo across
      // the screen and shrank it into a corner pill, which read as ugly.
      val heroTransition = updateTransition(appState, label = "HeroLogoTransition")

      val logoAlpha by heroTransition.animateFloat(
          transitionSpec = { tween(260, easing = androidx.compose.animation.core.FastOutSlowInEasing) },
          label = "LogoAlpha"
      ) { state -> if (state == "loading") 1f else 0f }

      // Record-session start/stop actions invoked by the K1 TodayScreen pill.
      val startFixedSessionAction: () -> Unit = {
          val startRecordingNow: () -> Unit = {
              appScope.launch {
                  try {
                      val response = RemoteDataFetcher.startFixedSession()
                      fixedSessionId = response.sessionId
                      recordingStartedAtMillis = Clock.System.now().toEpochMilliseconds()
                      KlikLogger.i("MainApp", "Fixed session started: ${response.sessionId}")
                      showRecordingStartedBanner = true
                      val userId = io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser.userId
                          ?: throw IllegalStateException("No user logged in for audio streaming")
                      val streamStarted = FixedSessionAudioStreamer.startStreaming(userId)
                      if (!streamStarted) {
                          KlikLogger.e("MainApp", "Failed to start audio streaming, stopping session")
                          RemoteDataFetcher.stopFixedSession()
                          fixedSessionId = null
                      }
                  } catch (e: Exception) {
                      KlikLogger.e("MainApp", "Failed to start fixed session: ${e.message}", e)
                      fixedSessionId = null
                  }
              }
          }
          when {
              hasRecordingConsent != true -> {
                  pendingPostConsentAction = startRecordingNow
                  lastMainRoute = currentRoute
                  currentRoute = "recording_consent"
              }
              hasBiometricConsent != true -> {
                  pendingPostConsentAction = startRecordingNow
                  lastMainRoute = currentRoute
                  currentRoute = "biometric_consent"
              }
              else -> startRecordingNow()
          }
      }
      val stopFixedSessionAction: () -> Unit = {
          fixedSessionId = null
          recordingStartedAtMillis = null
          // Surface the post-session processing pipeline on Today. Stage /
          // runId get populated by the poll loop once the backend responds.
          processingStartedAtMillis = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
          processingStage = null
          processingStatus = "queued"
          processingMessage = "Uploading your session…"
          processingProgressPct = 0.0
          processingError = null
          if (currentRoute == "live_recording") currentRoute = lastMainRoute
          CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
              try {
                  FixedSessionAudioStreamer.stopStreaming()
                  val response = RemoteDataFetcher.stopFixedSession()
                  KlikLogger.i("MainApp", "Fixed session stopped: ${response.message}, session: ${response.sessionId ?: "none"}, duration: ${response.durationSeconds ?: 0.0}s")
                  if (response.errorType == "no_audio_frames") {
                      // No audio was captured — dismiss processing banner immediately
                      dismissProcessing()
                  } else {
                      response.sessionId?.let { processingSessionId = it }
                  }
              } catch (e: Exception) {
                  KlikLogger.e("MainApp", "Failed to stop fixed session: ${e.message}", e)
              }
          }
      }

      // Single source of truth for entity renames. Every long-press / detail-
      // screen rename goes through these three actions so the optimistic state
      // update (and the wire shape we send to KK_entity_feedback) can never
      // drift between call sites.
      val renamePersonAction: (String, String) -> Unit = { id, newName ->
          appScope.launch {
              val r = io.github.fletchmckee.liquid.samples.app.data.network.EntityFeedbackClient
                  .updatePersonEntity(personId = id, name = newName)
              if (r.isSuccess) {
                  peopleState.value = io.github.fletchmckee.liquid.samples.app.data
                      .renamePersonInList(peopleState.value, id, newName)
                  // Keep speakerMap consistent so transcript / summary renders
                  // pick up the rename immediately, the way TaskDetail used to
                  // do inline.
                  speakerMap = speakerMap + (id to newName)
              } else {
                  KlikLogger.e("MainApp", "Person rename failed: ${r.exceptionOrNull()?.message}")
              }
          }
      }
      val renameProjectAction: (String, String) -> Unit = { id, newName ->
          appScope.launch {
              val r = io.github.fletchmckee.liquid.samples.app.data.network.EntityFeedbackClient
                  .updateProjectEntity(projectId = id, name = newName)
              if (r.isSuccess) {
                  projectsState.value = io.github.fletchmckee.liquid.samples.app.data
                      .renameProjectInList(projectsState.value, id, newName)
              } else {
                  KlikLogger.e("MainApp", "Project rename failed: ${r.exceptionOrNull()?.message}")
              }
          }
      }
      val renameOrganizationAction: (String, String) -> Unit = { id, newName ->
          appScope.launch {
              val r = io.github.fletchmckee.liquid.samples.app.data.network.EntityFeedbackClient
                  .updateOrganizationEntity(orgId = id, name = newName)
              if (r.isSuccess) {
                  organizationsState.value = io.github.fletchmckee.liquid.samples.app.data
                      .renameOrganizationInList(organizationsState.value, id, newName)
              } else {
                  KlikLogger.e("MainApp", "Org rename failed: ${r.exceptionOrNull()?.message}")
              }
          }
      }
      val renameRelatedEntityAction: (EntityType, String, String) -> Unit = { type, id, newName ->
          when (type) {
              EntityType.PERSON -> renamePersonAction(id, newName)
              EntityType.PROJECT -> renameProjectAction(id, newName)
              EntityType.ORGANIZATION -> renameOrganizationAction(id, newName)
              else -> Unit
          }
      }

      AnimatedContent(
          targetState = appState,
          transitionSpec = {
              if (initialState == "loading") {
                  // Logo -> Auth or Main: Smooth Fade Out + Scale/Fade In
                  (fadeIn(animationSpec = tween(800)) + androidx.compose.animation.scaleIn(initialScale = 0.95f, animationSpec = tween(800))) togetherWith
                  fadeOut(animationSpec = tween(800))
              } else if (initialState == "auth" && targetState == "main") {
                   // Auth -> Main (Login success): Slide Up or Fade
                   (fadeIn(animationSpec = tween(600)) + slideInVertically { it / 10 }) togetherWith 
                   fadeOut(animationSpec = tween(600))
              } else {
                  // Default fade
                  fadeIn(tween(600)) togetherWith fadeOut(tween(600))
              }
          },
          label = "AppLaunchTransition"
      ) { state ->
          when (state) {
              "loading" -> {
                   LoadingScreen(
                       minDuration = 2500, // 2.5s to be safe and beautiful
                       onLoadingFinished = { minLoadTimeCompleted = true }
                   )
              }
              "auth" -> {
                 // Show auth screen with background
                 Box(Modifier.fillMaxSize()) {
                     key(backgroundOption) {
                       when (val option = backgroundOption) {
                         is BackgroundOption.ColorBackground -> {
                           Box(
                             Modifier
                               .fillMaxSize()
                               .background(option.color)
                               .liquefiable(liquidState)
                           )
                         }
                         is BackgroundOption.GradientBackground -> {
                           Box(
                             Modifier
                               .fillMaxSize()
                               .background(option.brush)
                               .liquefiable(liquidState)
                           )
                         }
                         is BackgroundOption.ImageBackground -> {
                           val painter = when (option.resourceName) {
                             "gradient_peach.png" -> painterResource(Res.drawable.gradient_peach)
                             "gradient_lavender.png" -> painterResource(Res.drawable.gradient_lavender)
                             else -> painterResource(Res.drawable.moon_and_stars)
                           }
                           Image(
                             painter = painter,
                             contentDescription = null,
                             modifier = Modifier
                               .fillMaxSize()
                               .liquefiable(liquidState),
                             contentScale = ContentScale.Crop
                           )
                         }
                       }
                     }
                     io.github.fletchmckee.liquid.samples.app.ui.klikone.K1AuthScreen(
                       viewModel = authViewModel,
                       onAuthSuccess = {
                         // Auth success handled via state
                       }
                     )
                 }
              }
              "main" -> {
                  // Shared entity navigation: routes any EntityNavigationData to the correct detail screen
                  val navigateToEntity: (io.github.fletchmckee.liquid.samples.app.ui.components.EntityNavigationData) -> Unit = { nav ->
                      // Resolve a reference (which may be an id, canonicalName, name, or
                      // alias) into a concrete entity id. Returns null only when the
                      // reference can't be resolved against any loaded entity of that
                      // type — caller can then short-circuit to avoid dead navigation.
                      fun resolvePersonId(ref: String): String? = peopleState.value.firstOrNull {
                          it.id == ref ||
                              it.canonicalName.equals(ref, true) ||
                              it.name.equals(ref, true) ||
                              it.aliases.any { a -> a.equals(ref, true) }
                      }?.id
                      fun resolveProjectId(ref: String): String? = projectsState.value.firstOrNull {
                          it.id == ref ||
                              it.canonicalName.equals(ref, true) ||
                              it.name.equals(ref, true) ||
                              it.aliases.any { a -> a.equals(ref, true) }
                      }?.id
                      fun resolveOrgId(ref: String): String? = organizationsState.value.firstOrNull {
                          it.id == ref ||
                              it.canonicalName.equals(ref, true) ||
                              it.name.equals(ref, true) ||
                              it.aliases.any { a -> a.equals(ref, true) }
                      }?.id
                      when (nav.entityType) {
                          EntityType.TASK -> { taskDetailId = nav.entityId; currentRoute = "task_detail" }
                          EntityType.PERSON -> {
                              val resolved = resolvePersonId(nav.entityId) ?: nav.entityId
                              personDetailId = resolved
                              currentRoute = "person_detail"
                          }
                          EntityType.PROJECT -> {
                              val resolved = resolveProjectId(nav.entityId) ?: nav.entityId
                              projectDetailId = resolved
                              currentRoute = "project_detail"
                          }
                          EntityType.ORGANIZATION -> {
                              val resolved = resolveOrgId(nav.entityId) ?: nav.entityId
                              orgDetailId = resolved
                              currentRoute = "org_detail"
                          }
                          EntityType.MEETING -> {
                              val target = meetings.find { it.id == nav.entityId }
                              if (target != null) {
                                  sessionDetailMeeting = target
                                  currentRoute = "session_detail"
                              }
                          }
                      }
                  }
                  // Main App Content - User is logged in
                  // Background Layer - use key to force recomposition when background changes
                  Box(Modifier.fillMaxSize()) {
                      key(backgroundOption) {
                        when (val option = backgroundOption) {
                          is BackgroundOption.ColorBackground -> {
                            Box(
                              Modifier
                                .fillMaxSize()
                                .background(option.color)
                                .liquefiable(liquidState)
                            )
                          }
                          is BackgroundOption.GradientBackground -> {
                            Box(
                              Modifier
                                .fillMaxSize()
                                .background(option.brush)
                                .liquefiable(liquidState)
                            )
                          }
                          is BackgroundOption.ImageBackground -> {
                            val painter = when (option.resourceName) {
                              "gradient_peach.png" -> painterResource(Res.drawable.gradient_peach)
                              "gradient_lavender.png" -> painterResource(Res.drawable.gradient_lavender)
                              else -> painterResource(Res.drawable.moon_and_stars)
                            }
                            Image(
                              painter = painter,
                              contentDescription = null,
                              modifier = Modifier
                                .fillMaxSize()
                                .liquefiable(liquidState),
                              contentScale = ContentScale.Crop
                            )
                          }
                        }
                      }
                      // Main Content - Animated screen switching with slide transitions
                      // Touch interceptor: collapse calendar on touch but let event pass through for scrolling
                      Box(
                        Modifier
                          .liquefiable(liquidState)
                          .fillMaxSize()
                          .pointerInput(isCalendarExpanded) {
                            if (isCalendarExpanded) {
                              awaitPointerEventScope {
                                // Final pass: only fire if a child did NOT consume (i.e. tap
                                // landed outside chip/mini-calendar). Initial pass would steal
                                // the chip's own toggle click and lock the calendar open.
                                val event = awaitPointerEvent(PointerEventPass.Final)
                                if (event.changes.any { it.pressed && !it.isConsumed }) {
                                  isCalendarExpanded = false
                                }
                              }
                            }
                          }
                      ) {
                        AnimatedContent(
                          targetState = currentRoute,
                          transitionSpec = {
                            // Growth Tree / Notifications screens slide up from bottom (full-screen modal)
                            if (targetState == "growth_tree" || targetState == "notifications" || targetState == "pricing") {
                              (slideInVertically(
                                initialOffsetY = { fullHeight -> fullHeight },
                                animationSpec = tween(400)
                              ) + fadeIn(animationSpec = tween(400))) togetherWith
                              (fadeOut(animationSpec = tween(200)))
                            } else if (initialState == "growth_tree" || initialState == "notifications" || initialState == "pricing") {
                              // Coming back from growth tree / notifications
                              (fadeIn(animationSpec = tween(200))) togetherWith
                              (slideOutVertically(
                                targetOffsetY = { fullHeight -> fullHeight },
                                animationSpec = tween(400)
                              ) + fadeOut(animationSpec = tween(400)))
                            }
                            // Explore screen slides from top-right
                            else if (targetState == "explore") {
                              (slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(350)
                              ) + slideInVertically(
                                initialOffsetY = { fullHeight -> -fullHeight / 3 },
                                animationSpec = tween(350)
                              ) + fadeIn(animationSpec = tween(350))) togetherWith
                              (fadeOut(animationSpec = tween(200)))
                            } else if (initialState == "explore") {
                              // Coming back from explore
                              (fadeIn(animationSpec = tween(200))) togetherWith
                              (slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(350)
                              ) + slideOutVertically(
                                targetOffsetY = { fullHeight -> -fullHeight / 3 },
                                animationSpec = tween(350)
                              ) + fadeOut(animationSpec = tween(350)))
                            } else {
                              // Main 3 screens - horizontal slide
                              val currentIndex = getRouteIndex(initialState)
                              val targetIndex = getRouteIndex(targetState)
                              val slideDirection = if (targetIndex > currentIndex) 1 else -1
                
                              (slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth * slideDirection },
                                animationSpec = tween(300)
                              ) + fadeIn(animationSpec = tween(300))) togetherWith
                              (slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth * slideDirection },
                                animationSpec = tween(300)
                              ) + fadeOut(animationSpec = tween(300)))
                            }
                          },
                          label = "ScreenTransition"
                        ) { route ->
                          when (route) {
                            "today" -> TodayScreen(
                                selectedDate = selectedDate,
                                isCalendarExpanded = isCalendarExpanded,
                                isLoading = isEventsLoading,
                                isRefreshing = isMeetingsRefreshing,
                                isLlmDataLoading = isLlmDataLoading,
                                meetings = meetings + linkAppleToKlikSessions(appleCalendarMeetings, meetings),
                                insights = insights,
                                speakerMap = speakerMap,
                                onRefreshMeetings = onRefreshMeetings,
                                onArchiveMeeting = onArchiveMeeting,
                                expandSessionId = expandMeetingSessionId,
                                expandSegmentId = expandSegmentId,
                                expandSegmentText = expandSegmentText,
                                onSessionExpanded = {
                                    expandMeetingSessionId = null
                                    expandSegmentId = null
                                    expandSegmentText = null
                                },
                                onDateChange = { newDate ->
                                    selectedDate = newDate
                                },
                                onContentOverlap = { isOverlapping ->
                                    isTopBarOverlapped = isOverlapping
                                },
                                topDockHeightPx = 0,
                                // Entity highlighting parameters
                                tasks = reviewMetadata + pendingMetadata + completedMetadata,
                                projects = projectsState.value,
                                people = peopleState.value,
                                organizations = organizationsState.value,
                                onEntityClick = { entityNav ->
                                    // Underlined entities in Today's Insights / brief should land on
                                    // the same detail screens as the rest of the app — not on the
                                    // parent list. Delegate to the shared router.
                                    lastMainRoute = currentRoute
                                    navigateToEntity(entityNav)
                                },
                                onSegmentClick = { segmentNav ->
                                    KlikLogger.d("CalendarScreen", "Segment clicked: sessionId=${segmentNav.sessionId}, segmentId=${segmentNav.segmentId}")

                                    // Look up the traced segment to get its text
                                    val tracedSegment = findTracedSegment(
                                        sessionId = segmentNav.sessionId,
                                        segmentId = segmentNav.segmentId,
                                        insights = insights,
                                        encourageData = encourageData,
                                        worklifeData = worklifeData
                                    )

                                    // Extract date from session ID to check if meeting is already loaded
                                    val sessionDate = io.github.fletchmckee.liquid.samples.app.utils.SessionIdUtils.extractDateFromSessionId(segmentNav.sessionId)

                                    if (sessionDate != null) {
                                        // Check if meeting is already in loaded meetings
                                        val meetingExists = meetings.any { it.id == segmentNav.sessionId }

                                        if (!meetingExists) {
                                            // Meeting not loaded - trigger reload for that month
                                            KlikLogger.d("CalendarScreen", "Meeting not in current list, loading month: ${sessionDate.month} ${sessionDate.year}")

                                            // Set all navigation parameters
                                            expandMeetingSessionId = segmentNav.sessionId
                                            expandSegmentId = segmentNav.segmentId
                                            expandSegmentText = tracedSegment?.text
                                            selectedDate = sessionDate  // Change selected date to meeting's date
                                            meetingsRefreshKey++  // Trigger targeted reload
                                        } else {
                                            // Meeting already loaded - just navigate
                                            KlikLogger.d("CalendarScreen", "Meeting already loaded, navigating directly")
                                            expandMeetingSessionId = segmentNav.sessionId
                                            expandSegmentId = segmentNav.segmentId
                                            expandSegmentText = tracedSegment?.text
                                        }
                                    } else {
                                        // Session ID doesn't match expected format - try anyway
                                        KlikLogger.w("CalendarScreen", "Could not parse date from sessionId: ${segmentNav.sessionId}")
                                        expandMeetingSessionId = segmentNav.sessionId
                                        expandSegmentId = segmentNav.segmentId
                                        expandSegmentText = tracedSegment?.text
                                    }

                                    currentRoute = "today"

                                    KlikLogger.d("CalendarScreen", "Navigating to segment: sessionId=${segmentNav.sessionId}, segmentId=${segmentNav.segmentId}, text=${tracedSegment?.text?.take(30)}")
                                },
                                onMeetingClick = { m ->
                                    sessionDetailMeeting = m
                                    lastMainRoute = "today"
                                    currentRoute = "session_detail"
                                },
                                isRecording = isFixedSessionRecording,
                                processingStartedAtMillis = processingStartedAtMillis,
                                processingStage = processingStage,
                                processingStatus = processingStatus,
                                processingMessage = processingMessage,
                                processingProgressPct = processingProgressPct,
                                processingError = processingError,
                                onDismissProcessing = dismissProcessing,
                                onStartRecording = startFixedSessionAction,
                                onStopRecording = stopFixedSessionAction,
                                onOpenLiveRecording = {
                                    lastMainRoute = currentRoute
                                    currentRoute = "live_recording"
                                },
                                onCalendarToggle = {
                                    isCalendarExpanded = !isCalendarExpanded
                                    // Mutual exclusivity: closing the Ask sheet if calendar opens.
                                    if (isCalendarExpanded) {
                                        showAskKlik = false
                                        expandMeetingSessionId = null
                                    }
                                },
                            )
                            "function" -> MovesScreen(
                                isLoading = isEventsLoading,
                                isRefreshing = isTasksRefreshing,
                                highlightedTaskId = highlightedMoveId,
                                onHighlightConsumed = { highlightedMoveId = null },
                                // Featured: AI suggestions from backend
                                featuredTasks = if (isEventsLoading) emptyList() else reviewMetadata.take(3),
                                // Sensitive: KK_exec todos that require confirmation (e_complex_level3)
                                sensitiveTasks = if (isEventsLoading) emptyList() else kkExecSensitiveTodosState.value,
                                // Daily: KK_exec todos that are auto-executable or cannot execute
                                dailyTasks = if (isEventsLoading) emptyList() else kkExecDailyTodosState.value,
                                // Daily grouped by tool_category_group_id for category-based stacking
                                dailyTasksGrouped = if (isEventsLoading) emptyMap() else kkExecDailyTodosGroupedState.value,
                                onRefresh = onRefreshTasks,
                                onApproveTask = { taskId ->
                                    integrationScope.launch {
                                        try {
                                            val task = kkExecSensitiveTodosState.value.first { it.id == taskId }
                                            // Mark as executing immediately for UI feedback
                                            executingTodoIdsState.value = executingTodoIdsState.value + taskId
                                            RemoteDataFetcher.approveKKExecTodo(task.kkExecTodoId!!)
                                            KlikLogger.i("MainApp", "KK_exec todo ${task.kkExecTodoId} approved")
                                            onRefreshTasks()
                                            // After approve + refresh, task moves from sensitive to daily.
                                            // Mark its category as recently completed so the red dot shows.
                                            executingTodoIdsState.value = executingTodoIdsState.value - taskId
                                            val refreshedTask = allKKExecTodosState.value.find { it.id == taskId }
                                            val category = refreshedTask?.toolCategoriesNeeded?.sorted()?.joinToString(", ")?.takeIf { it.isNotBlank() } ?: "uncategorized"
                                            recentlyCompletedCategoriesState.value = recentlyCompletedCategoriesState.value + category
                                            KlikLogger.i("MainApp", "Approved task $taskId moved to category: $category")
                                        } catch (e: Exception) {
                                            executingTodoIdsState.value = executingTodoIdsState.value - taskId
                                            KlikLogger.e("MainApp", "Failed to approve task $taskId: ${e.message}", e)
                                        }
                                    }
                                },
                                onRejectTask = { taskId ->
                                    integrationScope.launch {
                                        try {
                                            val task = kkExecSensitiveTodosState.value.first { it.id == taskId }
                                            RemoteDataFetcher.rejectKKExecTodo(task.kkExecTodoId!!, reason = "Rejected by user")
                                            KlikLogger.i("MainApp", "KK_exec todo ${task.kkExecTodoId} rejected")
                                            onRefreshTasks()
                                        } catch (e: Exception) {
                                            KlikLogger.e("MainApp", "Failed to reject task $taskId: ${e.message}", e)
                                        }
                                    }
                                },
                                onRejectTaskWithReason = { taskId, reason ->
                                    integrationScope.launch {
                                        try {
                                            val task = kkExecSensitiveTodosState.value.first { it.id == taskId }
                                            RemoteDataFetcher.rejectKKExecTodo(task.kkExecTodoId!!, reason = reason)
                                            KlikLogger.i("MainApp", "KK_exec todo ${task.kkExecTodoId} rejected with reason")
                                            onRefreshTasks()
                                        } catch (e: Exception) {
                                            KlikLogger.e("MainApp", "Failed to reject task $taskId: ${e.message}", e)
                                        }
                                    }
                                },
                                onArchiveTaskOnBackend = { taskId ->
                                    integrationScope.launch {
                                        try {
                                            RemoteDataFetcher.updateTaskStatus(taskId, "archived")
                                            KlikLogger.i("MainApp", "Task $taskId archived on backend")
                                        } catch (e: Exception) {
                                            KlikLogger.e("MainApp", "Failed to archive task $taskId on backend: ${e.message}", e)
                                        }
                                    }
                                },
                                onRetryTask = { taskId ->
                                    integrationScope.launch {
                                        try {
                                            // Look up in both sensitive and daily todos
                                            val task = (kkExecSensitiveTodosState.value + kkExecDailyTodosState.value).first { it.id == taskId }
                                            // Mark as executing immediately for UI feedback
                                            executingTodoIdsState.value = executingTodoIdsState.value + taskId
                                            RemoteDataFetcher.retryKKExecTodo(task.kkExecTodoId!!)
                                            KlikLogger.i("MainApp", "KK_exec todo ${task.kkExecTodoId} retry started")
                                            onRefreshTasks()
                                        } catch (e: KKExecRetryLimitException) {
                                            executingTodoIdsState.value = executingTodoIdsState.value - taskId
                                            KlikLogger.w("MainApp", "Retry limit exceeded for task $taskId — upgrade required")
                                            currentRoute = "pricing"
                                        } catch (e: Exception) {
                                            executingTodoIdsState.value = executingTodoIdsState.value - taskId
                                            KlikLogger.e("MainApp", "Failed to retry task $taskId: ${e.message}", e)
                                        }
                                    }
                                },
                                onEntityClick = { entityNav ->
                                    lastMainRoute = currentRoute
                                    when (entityNav.entityType) {
                                        EntityType.TASK -> {
                                            taskDetailId = entityNav.entityId
                                            currentRoute = "task_detail"
                                        }
                                        EntityType.PERSON -> {
                                            personDetailId = entityNav.entityId
                                            currentRoute = "person_detail"
                                        }
                                        EntityType.ORGANIZATION -> {
                                            orgDetailId = entityNav.entityId
                                            currentRoute = "org_detail"
                                        }
                                        EntityType.PROJECT -> {
                                            projectDetailId = entityNav.entityId
                                            currentRoute = "project_detail"
                                        }
                                        EntityType.MEETING -> {
                                            expandMeetingSessionId = entityNav.entityId
                                            currentRoute = "today"
                                        }
                                    }
                                },
                                onSegmentClick = { segmentNav ->
                                    KlikLogger.d("MainApp", "Segment clicked from EventsScreen: sessionId=${segmentNav.sessionId}, segmentId=${segmentNav.segmentId}")
                                    val tracedSegment = findTracedSegment(
                                        sessionId = segmentNav.sessionId,
                                        segmentId = segmentNav.segmentId,
                                        insights = insights,
                                        encourageData = encourageData,
                                        worklifeData = worklifeData
                                    )
                                    val sessionDate = io.github.fletchmckee.liquid.samples.app.utils.SessionIdUtils.extractDateFromSessionId(segmentNav.sessionId)
                                    if (sessionDate != null) {
                                        val meetingExists = meetings.any { it.id == segmentNav.sessionId }
                                        if (!meetingExists) {
                                            expandMeetingSessionId = segmentNav.sessionId
                                            expandSegmentId = segmentNav.segmentId
                                            expandSegmentText = tracedSegment?.text
                                            selectedDate = sessionDate
                                            meetingsRefreshKey++
                                        } else {
                                            expandMeetingSessionId = segmentNav.sessionId
                                            expandSegmentId = segmentNav.segmentId
                                            expandSegmentText = tracedSegment?.text
                                        }
                                    } else {
                                        expandMeetingSessionId = segmentNav.sessionId
                                        expandSegmentId = segmentNav.segmentId
                                        expandSegmentText = tracedSegment?.text
                                    }
                                    currentRoute = "today"
                                }
                            )
                            "growth" -> NetworkScreen(
                                isLoading = isGrowthLoading,
                                isLlmDataLoading = isLlmDataLoading,
                                encourageData = encourageData,
                                worklifeData = worklifeData,
                                goalsData = goalsData,
                                userLevelData = userLevelData,
                                xpHistoryItems = io.github.fletchmckee.liquid.samples.app.model.xpHistoryItems,
                                onArchiveProject = onArchiveProject,
                                onArchivePerson = onArchivePerson,
                                onArchiveOrganization = onArchiveOrganization,
                                tasks = reviewMetadata + pendingMetadata + completedMetadata,
                                meetings = meetings,
                                onEntityClick = { entityNav ->
                                    lastMainRoute = currentRoute
                                    when (entityNav.entityType) {
                                        EntityType.TASK -> {
                                            taskDetailId = entityNav.entityId
                                            currentRoute = "task_detail"
                                        }
                                        EntityType.MEETING -> {
                                            expandMeetingSessionId = entityNav.entityId
                                            currentRoute = "today"
                                        }
                                        EntityType.PROJECT -> {
                                            projectDetailId = entityNav.entityId
                                            currentRoute = "project_detail"
                                        }
                                        EntityType.PERSON -> {
                                            personDetailId = entityNav.entityId
                                            currentRoute = "person_detail"
                                        }
                                        EntityType.ORGANIZATION -> {
                                            orgDetailId = entityNav.entityId
                                            currentRoute = "org_detail"
                                        }
                                    }
                                },
                                onSegmentClick = { segmentNav ->
                                    val tracedSegment = findTracedSegment(
                                        sessionId = segmentNav.sessionId,
                                        segmentId = segmentNav.segmentId,
                                        insights = insights,
                                        encourageData = encourageData,
                                        worklifeData = worklifeData,
                                    )
                                    val sessionDate = io.github.fletchmckee.liquid.samples.app.utils.SessionIdUtils.extractDateFromSessionId(segmentNav.sessionId)
                                    if (sessionDate != null) {
                                        val meetingExists = meetings.any { it.id == segmentNav.sessionId }
                                        if (!meetingExists) {
                                            expandMeetingSessionId = segmentNav.sessionId
                                            expandSegmentId = segmentNav.segmentId
                                            expandSegmentText = tracedSegment?.text
                                            selectedDate = sessionDate
                                            meetingsRefreshKey++
                                        } else {
                                            expandMeetingSessionId = segmentNav.sessionId
                                            expandSegmentId = segmentNav.segmentId
                                            expandSegmentText = tracedSegment?.text
                                        }
                                    } else {
                                        expandMeetingSessionId = segmentNav.sessionId
                                        expandSegmentId = segmentNav.segmentId
                                        expandSegmentText = tracedSegment?.text
                                    }
                                    currentRoute = "today"
                                },
                                highlightProjectId = highlightProjectId,
                                highlightPersonId = highlightPersonId,
                                highlightOrganizationId = highlightOrganizationId,
                                onEntityHighlighted = {
                                    highlightProjectId = null
                                    highlightPersonId = null
                                    highlightOrganizationId = null
                                },
                                onRefresh = onRefreshGrowth,
                                onGrowthTreeClick = {
                                    lastMainRoute = currentRoute
                                    currentRoute = "growth_tree"
                                },
                                subscriptionFeatures = subscriptionFeatures,
                                onUpgradeRequired = { featureName ->
                                    upgradeModalFeatureName = featureName
                                    showUpgradeModal = true
                                },
                                onRenamePerson = renamePersonAction,
                                onRenameProject = renameProjectAction,
                                onRenameOrganization = renameOrganizationAction,
                            )
                            "growth_tree" -> GrowthTreeScreen(
                                onBack = {
                                    currentRoute = lastMainRoute
                                }
                            )
                            "explore" -> YouScreen(
                                onNavigateToArchived = {
                                    lastMainRoute = currentRoute
                                    currentRoute = "archived"
                                },
                                subscription = subscriptionData,
                                onNavigateToPricing = {
                                    lastMainRoute = currentRoute
                                    currentRoute = "pricing"
                                },
                                onNavigateToPrivacy = {
                                    lastMainRoute = currentRoute
                                    currentRoute = "privacy_settings"
                                },
                                onNavigateToAccountSecurity = {
                                    lastMainRoute = currentRoute
                                    currentRoute = "account_security"
                                },
                                onNavigateToNotificationSettings = {
                                    lastMainRoute = currentRoute
                                    currentRoute = "notification_settings"
                                },
                                onNavigateToXpLogs = {
                                    lastMainRoute = currentRoute
                                    currentRoute = "xp_logs"
                                },
                                onNavigateToPreferences = {
                                    lastMainRoute = currentRoute
                                    currentRoute = "preferences"
                                },
                                onNavigateToAchievements = {
                                    lastMainRoute = currentRoute
                                    currentRoute = "achievements"
                                },
                            )
                            "preferences" -> io.github.fletchmckee.liquid.samples.app.ui.klikone.PreferencesScreen(
                                onBack = { currentRoute = lastMainRoute },
                                onOpenNotifications = {
                                    lastMainRoute = currentRoute
                                    currentRoute = "notification_settings"
                                },
                            )
                            "achievements" -> io.github.fletchmckee.liquid.samples.app.ui.klikone.AchievementsScreen(
                                onBack = { currentRoute = lastMainRoute },
                            )
                            "xp_logs" -> io.github.fletchmckee.liquid.samples.app.ui.klikone.XpLogsScreen(
                                items = io.github.fletchmckee.liquid.samples.app.model.xpHistoryItems,
                                tasks = reviewMetadata + pendingMetadata + completedMetadata +
                                    kkExecSensitiveTodosState.value + kkExecDailyTodosState.value,
                                meetings = meetings,
                                onBack = { currentRoute = lastMainRoute },
                            )
                            "archived" -> ArchivedScreen(
                                onBack = {
                                    currentRoute = "explore"
                                }
                            )
                            "notifications" -> NotificationsScreen(
                                notifications = notifications,
                                isLoading = isNotificationsLoading,
                                isRefreshing = isNotificationsRefreshing,
                                onRefresh = {
                                    isNotificationsRefreshing = true
                                    notificationsRefreshKey++
                                },
                                onMarkRead = { notificationId ->
                                    archiveScope.launch {
                                        try {
                                            RemoteDataFetcher.markNotificationRead(notificationId.toString())
                                            // Update local state immediately
                                            notifications = notifications.map {
                                                if (it.id == notificationId) it.copy(isRead = true) else it
                                            }
                                            unreadNotifCount = notifications.count { !it.isRead }
                                        } catch (e: Exception) {
                                            KlikLogger.e("MainApp", "Failed to mark notification read: ${e.message}", e)
                                        }
                                    }
                                },
                                onBack = {
                                    currentRoute = lastMainRoute
                                }
                            )
                            "pricing" -> PricingScreen(
                                plans = subscriptionPlans,
                                currentSubscription = subscriptionData,
                                onUpgrade = { planCode, billingCycle ->
                                    archiveScope.launch {
                                        try {
                                            RemoteDataFetcher.upgradeSubscription(planCode, billingCycle)
                                            KlikLogger.i("MainApp", "Subscription upgraded to $planCode")
                                            subscriptionRefreshKey++
                                            currentRoute = lastMainRoute
                                        } catch (e: Exception) {
                                            KlikLogger.e("MainApp", "Failed to upgrade subscription: ${e.message}", e)
                                        }
                                    }
                                },
                                onDowngrade = { planCode, billingCycle ->
                                    archiveScope.launch {
                                        try {
                                            RemoteDataFetcher.downgradeSubscription(planCode, billingCycle)
                                            KlikLogger.i("MainApp", "Subscription downgraded to $planCode")
                                            subscriptionRefreshKey++
                                            currentRoute = lastMainRoute
                                        } catch (e: Exception) {
                                            KlikLogger.e("MainApp", "Failed to downgrade subscription: ${e.message}", e)
                                        }
                                    }
                                },
                                onBack = {
                                    currentRoute = lastMainRoute
                                }
                            )
                            "privacy_settings" -> PrivacySettingsScreen(
                                onBack = {
                                    currentRoute = "explore"
                                },
                                onNavigateToRecordingConsent = {
                                    lastMainRoute = currentRoute
                                    currentRoute = "recording_consent"
                                },
                                onNavigateToBiometricConsent = {
                                    lastMainRoute = currentRoute
                                    currentRoute = "biometric_consent"
                                }
                            )
                            "recording_consent" -> RecordingConsentScreen(
                                onAccept = {
                                    archiveScope.launch {
                                        try {
                                            RemoteDataFetcher.submitRecordingConsent()
                                            KlikLogger.i("MainApp", "Recording consent accepted")
                                            hasRecordingConsent = true
                                            // In onboarding, the LaunchedEffect will re-route to the next
                                            // missing consent or to the main app — do nothing here.
                                            if (!isOnboardingConsent) {
                                                // Chain to biometric consent if still needed, otherwise run
                                                // the pending action (e.g. start recording) and return.
                                                if (pendingPostConsentAction != null && hasBiometricConsent != true) {
                                                    currentRoute = "biometric_consent"
                                                } else {
                                                    val action = pendingPostConsentAction
                                                    pendingPostConsentAction = null
                                                    currentRoute = lastMainRoute
                                                    action?.invoke()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            KlikLogger.e("MainApp", "Failed to submit recording consent: ${e.message}", e)
                                        }
                                    }
                                },
                                onBack = {
                                    // User cancelled — drop any pending post-consent action
                                    pendingPostConsentAction = null
                                    currentRoute = lastMainRoute
                                },
                                isOnboarding = isOnboardingConsent,
                                onSignOut = {
                                    KlikLogger.i("MainApp", "Sign out from onboarding consent")
                                    authViewModel.logout()
                                }
                            )
                            "account_security" -> AccountSecurityScreen(
                                onChangePassword = { currentPassword, newPassword ->
                                    archiveScope.launch {
                                        try {
                                            RemoteDataFetcher.changePassword(currentPassword, newPassword)
                                            KlikLogger.i("MainApp", "Password changed successfully")
                                            currentRoute = "explore"
                                        } catch (e: Exception) {
                                            KlikLogger.e("MainApp", "Failed to change password: ${e.message}", e)
                                        }
                                    }
                                },
                                onBack = {
                                    currentRoute = "explore"
                                }
                            )
                            "notification_settings" -> NotificationSettingsScreen(
                                onSavePreferences = { meetingReminders, taskUpdates, insightsDigest, pushEnabled ->
                                    archiveScope.launch {
                                        try {
                                            RemoteDataFetcher.updateNotificationPreferences(
                                                meetingReminders = meetingReminders,
                                                taskUpdates = taskUpdates,
                                                insightsDigest = insightsDigest,
                                                pushEnabled = pushEnabled
                                            )
                                            KlikLogger.i("MainApp", "Notification preferences saved")
                                        } catch (e: Exception) {
                                            KlikLogger.e("MainApp", "Failed to save notification preferences: ${e.message}", e)
                                        }
                                    }
                                },
                                onBack = {
                                    currentRoute = "explore"
                                }
                            )
                            "biometric_consent" -> BiometricConsentScreen(
                                onEnable = {
                                    archiveScope.launch {
                                        try {
                                            RemoteDataFetcher.submitBiometricConsent()
                                            KlikLogger.i("MainApp", "Biometric consent accepted")
                                            hasBiometricConsent = true
                                            // In onboarding, the LaunchedEffect will route to the main app.
                                            if (!isOnboardingConsent) {
                                                val action = pendingPostConsentAction
                                                pendingPostConsentAction = null
                                                currentRoute = lastMainRoute
                                                action?.invoke()
                                            }
                                        } catch (e: Exception) {
                                            KlikLogger.e("MainApp", "Failed to submit biometric consent: ${e.message}", e)
                                        }
                                    }
                                },
                                onDecline = {
                                    // User explicitly declined — drop pending action; recording will
                                    // stay blocked until they grant consent from Privacy Settings.
                                    pendingPostConsentAction = null
                                    currentRoute = lastMainRoute
                                },
                                onBack = {
                                    pendingPostConsentAction = null
                                    currentRoute = lastMainRoute
                                },
                                isOnboarding = isOnboardingConsent,
                                onSignOut = {
                                    KlikLogger.i("MainApp", "Sign out from onboarding consent")
                                    authViewModel.logout()
                                }
                            )
                            "klikone_onboarding" -> OnboardingScreen(
                                onComplete = { fullName, pickedRole, avatar ->
                                    val uid = authState.userId
                                    if (!uid.isNullOrBlank()) {
                                        onboardingStorage.saveString(
                                            KlikOneOnboardingKeys.completedKey(uid), "true"
                                        )
                                        pickedRole?.id?.let {
                                            onboardingStorage.saveString(
                                                KlikOneOnboardingKeys.roleKey(uid), it
                                            )
                                        }
                                        // Persist to KK_auth so the flag survives
                                        // reinstall + follows the user across devices.
                                        appScope.launch {
                                            try {
                                                io.github.fletchmckee.liquid.samples.app.data.network
                                                    .OnboardingStateClient
                                                    .patch(onboardingCompleted = true)
                                            } catch (t: Throwable) {
                                                KlikLogger.e("MainApp", "Onboarding PATCH failed: ${t.message}", t)
                                            }
                                        }
                                    } else {
                                        KlikLogger.e("MainApp", "Klik One onboarding complete but authState.userId is null — completion not persisted")
                                    }
                                    authViewModel.submitOnboardingProfile(
                                        name = fullName,
                                        occupation = pickedRole?.title,
                                        avatar = avatar,
                                    )
                                    KlikLogger.i("MainApp", "Klik One onboarding complete (role=${pickedRole?.id}, hasAvatar=${avatar != null})")
                                    hasCompletedKlikOnboarding = true
                                }
                            )
                            "session_detail" -> {
                                val m = sessionDetailMeeting
                                if (m != null) {
                                    SessionDetailScreen(
                                        meeting = m,
                                        tasks = reviewMetadata + pendingMetadata + completedMetadata +
                                            kkExecSensitiveTodosState.value + kkExecDailyTodosState.value,
                                        allMeetings = meetings,
                                        projects = projectsState.value,
                                        people = peopleState.value,
                                        organizations = organizationsState.value,
                                        speakerMap = speakerMap,
                                        expandSegmentId = expandSegmentText,
                                        onBack = {
                                            sessionDetailMeeting = null
                                            expandSegmentId = null
                                            expandSegmentText = null
                                            currentRoute = "today"
                                        },
                                        onEntityClick = { nav -> navigateToEntity(nav) },
                                        onRenameParticipant = renamePersonAction,
                                    )
                                } else {
                                    LaunchedEffect(Unit) {
                                        if (currentRoute == "session_detail") currentRoute = "today"
                                    }
                                }
                            }
                            "task_detail" -> {
                                val allTasks = reviewMetadata + pendingMetadata + completedMetadata +
                                    kkExecSensitiveTodosState.value + kkExecDailyTodosState.value
                                val t = allTasks.find { it.id == taskDetailId }
                                if (t != null) {
                                    io.github.fletchmckee.liquid.samples.app.ui.klikone.TaskDetailScreen(
                                        task = t,
                                        meetings = meetings,
                                        projects = projectsState.value,
                                        people = peopleState.value,
                                        organizations = organizationsState.value,
                                        onBack = {
                                            taskDetailId = null
                                            currentRoute = lastMainRoute
                                        },
                                        onEntityClick = { nav -> navigateToEntity(nav) },
                                        onApprove = if (t.needsConfirmation) {
                                            {
                                                integrationScope.launch {
                                                    try {
                                                        val todo = kkExecSensitiveTodosState.value
                                                            .firstOrNull { it.id == t.id }
                                                        if (todo?.kkExecTodoId != null) {
                                                            executingTodoIdsState.value =
                                                                executingTodoIdsState.value + t.id
                                                            RemoteDataFetcher.approveKKExecTodo(todo.kkExecTodoId!!)
                                                            onRefreshTasks()
                                                            executingTodoIdsState.value =
                                                                executingTodoIdsState.value - t.id
                                                        }
                                                    } catch (e: Exception) {
                                                        KlikLogger.e("MainApp", "Approve from detail failed: ${e.message}", e)
                                                    }
                                                }
                                                taskDetailId = null
                                                currentRoute = lastMainRoute
                                            }
                                        } else null,
                                        onReject = if (t.needsConfirmation) {
                                            {
                                                integrationScope.launch {
                                                    try {
                                                        RemoteDataFetcher.updateTaskStatus(t.id, "archived")
                                                    } catch (e: Exception) {
                                                        KlikLogger.e("MainApp", "Archive from detail failed: ${e.message}", e)
                                                    }
                                                }
                                                taskDetailId = null
                                                currentRoute = lastMainRoute
                                            }
                                        } else null,
                                        onRejectWithReason = if (t.needsConfirmation) {
                                            { reason: String ->
                                                integrationScope.launch {
                                                    try {
                                                        val todo = kkExecSensitiveTodosState.value
                                                            .firstOrNull { it.id == t.id }
                                                        if (todo?.kkExecTodoId != null) {
                                                            RemoteDataFetcher.rejectKKExecTodo(
                                                                todo.kkExecTodoId!!,
                                                                reason = reason,
                                                            )
                                                            onRefreshTasks()
                                                        } else {
                                                            RemoteDataFetcher.updateTaskStatus(t.id, "archived")
                                                        }
                                                    } catch (e: Exception) {
                                                        KlikLogger.e("MainApp", "Reject-with-reason failed: ${e.message}", e)
                                                    }
                                                }
                                                taskDetailId = null
                                                currentRoute = lastMainRoute
                                            }
                                        } else null,
                                        onRetry = run {
                                            val isFailed = t.kkExecStatus?.uppercase() == "FAILED" ||
                                                t.kkExecStatus?.uppercase() == "ERROR"
                                            if (isFailed) {
                                                {
                                                    integrationScope.launch {
                                                        try {
                                                            val todo = (kkExecSensitiveTodosState.value +
                                                                kkExecDailyTodosState.value)
                                                                .firstOrNull { it.id == t.id }
                                                            if (todo?.kkExecTodoId != null) {
                                                                executingTodoIdsState.value =
                                                                    executingTodoIdsState.value + t.id
                                                                RemoteDataFetcher.retryKKExecTodo(todo.kkExecTodoId!!)
                                                                onRefreshTasks()
                                                            }
                                                        } catch (e: Exception) {
                                                            executingTodoIdsState.value =
                                                                executingTodoIdsState.value - t.id
                                                            KlikLogger.e("MainApp", "Retry failed: ${e.message}", e)
                                                        }
                                                    }
                                                }
                                            } else null
                                        },
                                        onJumpToTranscript = { sessionId, anchor ->
                                            // Open the source session and pre-scroll its Transcript
                                            // tab to the line matching `anchor`. We pass the anchor
                                            // through the existing expandSegmentText slot — Session
                                            // Detail's TranscriptPanel does a tolerant substring
                                            // match against transcript bodies and tints the row.
                                            val target = meetings.find { it.id == sessionId }
                                            if (target != null) {
                                                lastMainRoute = currentRoute
                                                sessionDetailMeeting = target
                                                expandSegmentText = anchor
                                                currentRoute = "session_detail"
                                            }
                                        },
                                        onRenameRelatedEntity = renameRelatedEntityAction,
                                    )
                                } else {
                                    LaunchedEffect(Unit) { currentRoute = lastMainRoute }
                                }
                            }
                            "person_detail" -> {
                                val p = peopleState.value.find { it.id == personDetailId }
                                if (p != null) {
                                    io.github.fletchmckee.liquid.samples.app.ui.klikone.PersonDetailScreen(
                                        person = p,
                                        meetings = meetings,
                                        tasks = reviewMetadata + pendingMetadata + completedMetadata,
                                        projects = projectsState.value,
                                        organizations = organizationsState.value,
                                        allPeople = peopleState.value,
                                        onBack = {
                                            personDetailId = null
                                            currentRoute = lastMainRoute
                                        },
                                        onEntityClick = { nav -> navigateToEntity(nav) },
                                        onRename = { newName -> renamePersonAction(p.id, newName) },
                                    )
                                } else {
                                    LaunchedEffect(Unit) { currentRoute = lastMainRoute }
                                }
                            }
                            "project_detail" -> {
                                val pr = projectsState.value.find { it.id == projectDetailId }
                                if (pr != null) {
                                    io.github.fletchmckee.liquid.samples.app.ui.klikone.ProjectDetailScreen(
                                        project = pr,
                                        meetings = meetings,
                                        tasks = reviewMetadata + pendingMetadata + completedMetadata,
                                        people = peopleState.value,
                                        allProjects = projectsState.value,
                                        organizations = organizationsState.value,
                                        onBack = {
                                            projectDetailId = null
                                            currentRoute = lastMainRoute
                                        },
                                        onEntityClick = { nav -> navigateToEntity(nav) },
                                        onRename = { newName -> renameProjectAction(pr.id, newName) },
                                    )
                                } else {
                                    LaunchedEffect(Unit) { currentRoute = lastMainRoute }
                                }
                            }
                            "org_detail" -> {
                                val o = organizationsState.value.find { it.id == orgDetailId }
                                if (o != null) {
                                    io.github.fletchmckee.liquid.samples.app.ui.klikone.OrgDetailScreen(
                                        org = o,
                                        people = peopleState.value,
                                        projects = projectsState.value,
                                        meetings = meetings,
                                        onBack = {
                                            orgDetailId = null
                                            currentRoute = lastMainRoute
                                        },
                                        onEntityClick = { nav -> navigateToEntity(nav) },
                                        onRename = { newName -> renameOrganizationAction(o.id, newName) },
                                    )
                                } else {
                                    LaunchedEffect(Unit) { currentRoute = lastMainRoute }
                                }
                            }
                            "live_recording" -> {
                                val startedAt = recordingStartedAtMillis
                                var tickMs by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
                                LaunchedEffect(Unit) {
                                    while (true) {
                                        kotlinx.coroutines.delay(1000)
                                        tickMs = Clock.System.now().toEpochMilliseconds()
                                    }
                                }
                                val elapsedSec = if (startedAt != null) ((tickMs - startedAt) / 1000L).coerceAtLeast(0) else 0L
                                val mm = elapsedSec / 60
                                val ss = elapsedSec % 60
                                val elapsedLabel = "${mm.toString().padStart(2, '0')}:${ss.toString().padStart(2, '0')}"
                                LiveRecordingScreen(
                                    title = "In conversation",
                                    startedAgo = if (startedAt != null) "Recording for $mm min" else "Just started",
                                    speakerCount = 1,
                                    elapsed = elapsedLabel,
                                    recentTurns = emptyList(),   // wired once §12 live-transcript WS lands
                                    detections = emptyList(),
                                    isPaused = isFixedSessionPaused,
                                    onMinimize = { currentRoute = lastMainRoute },
                                    onPauseResume = {
                                        if (isFixedSessionPaused) FixedSessionAudioStreamer.resumeStreaming()
                                        else FixedSessionAudioStreamer.pauseStreaming()
                                    },
                                    onStop = stopFixedSessionAction,
                                    onAddContext = {},
                                )
                            }
                          }
                        }
                      }

      // Upgrade Required Modal
      if (showUpgradeModal) {
          UpgradeRequiredDialog(
              featureName = upgradeModalFeatureName,
              currentTier = subscriptionData?.planCode ?: "starter",
              onUpgrade = {
                  showUpgradeModal = false
                  lastMainRoute = currentRoute
                  currentRoute = "pricing"
              },
              onDismiss = { showUpgradeModal = false }
          )
      }

      // Ask Klik Overlay (Popup)
      AnimatedVisibility(
        visible = showAskKlik,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)),
        modifier = Modifier.fillMaxSize()
      ) {
         AskKlikSheet(
           hasRecordingConsent = hasRecordingConsent == true,
           onRequestRecordingConsent = {
             // Dismiss the AskKlik modal and route to the consent screen. No pending action
             // is set — user re-opens AskKlik and taps mic again once consent is granted.
             showAskKlik = false
             lastMainRoute = currentRoute
             currentRoute = "recording_consent"
           },
           onDismiss = { showAskKlik = false },
           onNavigateToSource = { source ->
             // Navigate to the meeting/session based on source type
             KlikLogger.d("Navigation", "Source clicked: type=${source.type}, sessionId=${source.sessionId}, segmentId=${source.id}, content=${source.content?.take(50)}")
             when (source.type) {
               ChatSourceType.MEETING_SEGMENT -> {
                 source.sessionId?.let { sessionId ->
                   KlikLogger.d("Navigation", "Setting expandMeetingSessionId=$sessionId, segmentId=${source.id}")

                   // Extract date from session ID to check if meeting is already loaded
                   val sessionDate = io.github.fletchmckee.liquid.samples.app.utils.SessionIdUtils.extractDateFromSessionId(sessionId)

                   if (sessionDate != null) {
                     // Check if meeting is already in loaded meetings
                     val meetingExists = meetings.any { it.id == sessionId }

                     if (!meetingExists) {
                       // Meeting not loaded - trigger reload for that month
                       KlikLogger.d("Navigation", "Meeting not in current list, loading month: ${sessionDate.month} ${sessionDate.year}")

                       // Set all navigation parameters
                       expandMeetingSessionId = sessionId
                       expandSegmentId = source.id
                       expandSegmentText = source.content
                       selectedDate = sessionDate  // Change selected date to meeting's date
                       meetingsRefreshKey++  // Trigger targeted reload (handled in LaunchedEffect)
                     } else {
                       // Meeting already loaded - just navigate
                       KlikLogger.d("Navigation", "Meeting already loaded, navigating directly")
                       expandMeetingSessionId = sessionId
                       expandSegmentId = source.id
                       expandSegmentText = source.content
                     }
                   } else {
                     // Session ID doesn't match expected format - try anyway
                     KlikLogger.w("Navigation", "Could not parse date from sessionId: $sessionId")
                     expandMeetingSessionId = sessionId
                     expandSegmentId = source.id
                     expandSegmentText = source.content
                   }

                   currentRoute = "today"
                   showAskKlik = false
                 }
               }
               ChatSourceType.SESSION_SUMMARY -> {
                 // For session summaries, just expand the meeting without scrolling to specific segment
                 source.sessionId?.let { sessionId ->
                   KlikLogger.d("Navigation", "Setting expandMeetingSessionId=$sessionId (summary - no segment scroll)")
                   expandMeetingSessionId = sessionId
                   // Don't set expandSegmentId or expandSegmentText - just expand the meeting
                   expandSegmentId = null
                   expandSegmentText = null
                   currentRoute = "today"
                   showAskKlik = false
                 }
               }
               ChatSourceType.ENTITY -> {
                 // Navigate to person/project/organization profile based on entity ID prefix
                 val entityId = source.id
                 KlikLogger.d("Navigation", "ENTITY source clicked: entityId=$entityId")

                 when {
                   entityId.startsWith("VP_") -> {
                     // Person entity
                     KlikLogger.d("Navigation", "Navigating to Person: $entityId")
                     highlightPersonId = entityId
                     currentRoute = "growth"
                   }
                   entityId.startsWith("VO_") -> {
                     // Organization entity
                     KlikLogger.d("Navigation", "Navigating to Organization: $entityId")
                     highlightOrganizationId = entityId
                     currentRoute = "growth"
                   }
                   entityId.startsWith("VPR_") -> {
                     // Project entity
                     KlikLogger.d("Navigation", "Navigating to Project: $entityId")
                     highlightProjectId = entityId
                     currentRoute = "growth"
                   }
                   else -> {
                     // Unknown entity type - log warning
                     KlikLogger.w("Navigation", "Unknown entity type for ID: $entityId")
                   }
                 }
                 showAskKlik = false
               }
               else -> {
                 showAskKlik = false
               }
             }
           }
         )
      }

       // Error overlay - shows initialization errors with scroll, copy, and retry buttons
       ErrorPopup(
           isVisible = initError != null,
           title = "Initialization Error",
           message = initError ?: "",
           onDismiss = { initError = null },
           onRetry = {
               // Reset InitGuard and increment retry trigger to re-run the init LaunchedEffect
               InitGuard.mainInitStartedAt = 0L
               InitGuard.todosInitStartedAt = 0L
               initError = null
               isEventsLoading = true
               isGrowthLoading = true
               initRetryTrigger++
           },
           onReport = initError?.let { error ->
               { GitHubIssueReporter.reportError("Initialization Error", error) }
           }
       )

      // Integration Prompt Dialog - shows unconnected integrations on login
      IntegrationPromptDialog(
        isVisible = showIntegrationPrompt,
        unconnectedIntegrations = unconnectedIntegrations,
        autoDismissSeconds = 30,
        onIntegrationClick = onAuthorizeIntegration,
        onDismiss = { showIntegrationPrompt = false },
        onNeverShowAgain = onNeverShowIntegrationPrompt
      )

      // K1 tab routes render their own header + bottom nav. Every other route
      // is a drill-down (settings, detail, consent, live capture) that owns
      // its own back navigation — no app-level chrome.
      val isKlikOneTab = currentRoute in setOf("today", "function", "growth", "explore")

      // Klik One 4-tab bottom nav on main K1 tab routes. Hidden while the
      // AskKlik sheet is open so it can render edge-to-edge.
      if (isKlikOneTab && !showAskKlik) {
        // Moves badge counts unseen tasks across all three Move buckets
        // (review, sensitive, daily). Tapping a card in MovesScreen calls
        // markTaskSeen, which decrements this until the user has touched
        // every outstanding item.
        val seenIds by io.github.fletchmckee.liquid.samples.app.model.seenTaskIdsState
        val unreadMovesCount = (
          reviewMetadata +
            kkExecSensitiveTodosState.value +
            kkExecDailyTodosState.value
          ).distinctBy { it.id }.count { it.id !in seenIds }
        io.github.fletchmckee.liquid.samples.app.ui.klikone.K1BottomNav(
          items = listOf(
            io.github.fletchmckee.liquid.samples.app.ui.klikone.K1NavItem(
              route = "today",
              label = "Today",
              iconPath = { io.github.fletchmckee.liquid.samples.app.ui.klikone.K1IconToday(active = currentRoute == "today") },
            ),
            io.github.fletchmckee.liquid.samples.app.ui.klikone.K1NavItem(
              route = "function",
              label = "Moves",
              iconPath = { io.github.fletchmckee.liquid.samples.app.ui.klikone.K1IconMoves(active = currentRoute == "function") },
              badgeCount = unreadMovesCount,
            ),
            io.github.fletchmckee.liquid.samples.app.ui.klikone.K1NavItem(
              route = "growth",
              label = "Network",
              iconPath = { io.github.fletchmckee.liquid.samples.app.ui.klikone.K1IconNetwork(active = currentRoute == "growth") },
            ),
            io.github.fletchmckee.liquid.samples.app.ui.klikone.K1NavItem(
              route = "explore",
              label = "You",
              iconPath = { io.github.fletchmckee.liquid.samples.app.ui.klikone.K1IconYou(active = currentRoute == "explore") },
            ),
          ),
          currentRoute = currentRoute,
          onSelect = { r ->
            showAskKlik = false
            currentRoute = r
          },
          modifier = Modifier.align(Alignment.BottomCenter),
        )
        // Floating Ask Klik button above the nav — toggles the sheet. Hidden
        // while the sheet is open so it doesn't hover over the chat.
        if (!showAskKlik) {
          io.github.fletchmckee.liquid.samples.app.ui.klikone.K1AskFab(
            onClick = {
              showAskKlik = true
              isCalendarExpanded = false
            },
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(end = 20.dp, bottom = 96.dp),
          )
        }
      }

                      // Offline banner overlay
                      AnimatedVisibility(
                        visible = !isNetworkConnected,
                        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                        modifier = Modifier.align(Alignment.TopCenter)
                      ) {
                        Box(
                          modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE53935))
                            .padding(top = 54.dp, bottom = 8.dp),
                          contentAlignment = Alignment.Center
                        ) {
                          Text(
                            text = stringResource(Res.string.no_internet_connection),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                          )
                        }
                      }

                      // Recording-started banner — K1 editorial: paper card, hairline border,
                      // pulsing alert dot, no Material chrome.
                      Box(
                        modifier = Modifier
                          .align(Alignment.TopCenter)
                          .padding(top = 54.dp, start = 20.dp, end = 20.dp)
                      ) {
                        io.github.fletchmckee.liquid.samples.app.ui.klikone.K1RecordingBanner(
                          visible = showRecordingStartedBanner,
                          title = stringResource(Res.string.recording_started),
                          subtitle = stringResource(Res.string.recording_started_subtitle),
                          durationMillis = 4000L,
                          onDismiss = { showRecordingStartedBanner = false }
                        )
                      }
                  }
              }
          }
      }

      // Splash logo overlay. Stays centered, fades out as we reach "main"/"auth".
      if (logoAlpha > 0f) {
          Box(
              modifier = Modifier
                  .align(Alignment.Center)
                  .size(144.dp)
                  .alpha(logoAlpha),
              contentAlignment = Alignment.Center
          ) {
              Icon(
                  painter = klikLogoPainter(),
                  contentDescription = null,
                  modifier = Modifier.size(144.dp),
                  tint = Color.Black
              )
          }
      }

  } // End of BoxWithConstraints content
  } // End of Scaffold content
  } // End of CompositionLocalProvider (snackbar)
  } // End of CompositionLocalProvider (theme)
}

/**
 * Map an orchestrator `stage` code to a one-second-readable verb for the
 * K1 processing banner. Orchestrator stage values as emitted by KK_orchestrator:
 *   denoise, asr, diarization, speaker_names, knowledge_graph,
 *   meeting_minutes, tasks, dropbox
 */
fun humaniseOrchestratorStage(stage: String?): String? {
    val s = stage?.lowercase()?.trim().orEmpty()
    return when (s) {
        "", "queued"         -> null // let caller fall back to `message`
        "denoise"            -> "Cleaning up the audio…"
        "asr", "transcribe"  -> "Transcribing what was said…"
        "diarization"        -> "Separating who spoke when…"
        "speaker_names"      -> "Identifying speakers…"
        "knowledge_graph",
        "entity_extraction"  -> "Mapping people and projects…"
        "meeting_minutes"    -> "Drafting meeting minutes…"
        "tasks",
        "task_extraction"    -> "Pulling out tasks and commitments…"
        "dropbox",
        "cloud_backup"       -> "Backing up to cloud storage…"
        else -> s.replace('_', ' ').replaceFirstChar { it.uppercase() } + "…"
    }
}
