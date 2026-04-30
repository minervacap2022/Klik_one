// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.model

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import io.github.fletchmckee.liquid.samples.app.data.storage.ArchivePinStorageManager
import io.github.fletchmckee.liquid.samples.app.data.storage.SecureStorage
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.domain.entity.ReauthInfo
import io.github.fletchmckee.liquid.samples.app.domain.entity.Scenario
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskStatus
import io.github.fletchmckee.liquid.samples.app.domain.entity.TodoItem
import io.github.fletchmckee.liquid.samples.app.domain.entity.TodoType
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Configure lenient JSON loader
val jsonLoader = Json {
  ignoreUnknownKeys = true
  isLenient = true
  coerceInputValues = true
}

/**
 * Extension to add color property to Domain Entity Scenario for UI usage.
 * Parses colorHex (e.g. "FF2EC4B6") to Compose Color.
 */
val Scenario.color: Color
  get() = try {
    // Remove # if present, though toLong(16) handles pure hex
    val hex = colorHex.removePrefix("#")
    // If length is 6, add FF alpha? Or assume ARGB
    // Logic in previous version was: Color(colorHex.toLong(16))
    // Assuming hex string is ARGB like "FFxxxxxx" or RRGGBB?
    // Step 199 GrowthDto writes "FF2EC4B6". That is ARGB (8 chars).
    // Long parsing:
    Color(hex.toLong(16))
  } catch (e: Exception) {
    io.github.fletchmckee.liquid.samples.app.logging.KlikLogger.w("Models", "Failed to parse color hex '$colorHex': ${e.message}", e)
    Color.Gray
  }

// Global Mutable State for Data (Populated by AppModule from Backend)
val peopleState = mutableStateOf<List<Person>>(emptyList())
val allPeople: List<Person> get() = peopleState.value

// History item for task version tracking
@Serializable
data class TaskHistoryItem(
  val subtitle: String,
  val version: String = "",
)

/**
 * UI model for execution steps in todo cards.
 * Represents one tool call in the execution sequence.
 */
@Serializable
data class ExecutionStepUi(
  val stepNumber: Int,
  val toolName: String,
  val success: Boolean = true,
  val errorMessage: String? = null,
  val durationMs: Int? = null,
  val output: String? = null, // Stringified output, for task_complete this is the result
)

// Task metadata for richer card display - Kept as UI Model/DTO for task json files
@Serializable
data class TaskMetadata(
  val id: String,
  val title: String,
  val subtitle: String,
  val context: String,
  val relatedProject: String,
  val relatedPeople: List<String>,
  val dueInfo: String,
  val priority: String = "Normal", // "High", "Normal", "Low"
  val isPinned: Boolean = false,
  val isRecurring: Boolean = false, // Daily recurring tasks - can have logs
  // Additional fields for UI display
  val history: List<TaskHistoryItem> = emptyList(), // Version history for expanded stack
  val isNew: Boolean = false, // Show "New" badge for suggested items
  val suggestionText: String? = null, // AI suggestion description
  val completedInfo: String? = null, // Completion timestamp display (e.g., "Completed 14:00, Dec 8")
  val description: String? = null, // Full task description
  // Task status from backend
  val status: TaskStatus = TaskStatus.PENDING,
  // Backend-provided confirmation flag (true when IN_REVIEW and needs user action)
  val needsConfirmation: Boolean = false,
  // KK_exec integration fields
  val kkExecCategory: String? = null, // a_simple, b_apis, c_complex_level1, d_complex_level2, e_complex_level3, f_cannotdo
  val kkExecCanExecute: Boolean? = null, // Whether this task can be executed
  val kkExecIsSensitive: Boolean? = null, // Whether this requires user confirmation (e_complex_level3)
  val kkExecSensitivityReasons: List<String> = emptyList(), // Why this task is sensitive
  val kkExecStatus: String? = null, // PENDING, IN_REVIEW, RUNNING, EVALUATING, COMPLETED, FAILED, CANNOT_EXECUTE, APPROVED, REJECTED, ARCHIVED
  val kkExecSessionId: String? = null, // Session ID this todo belongs to
  val kkExecTodoId: Int? = null, // Todo ID in KK_exec database
  // Related entities from backend
  val relatedMeetingId: String? = null, // session_id from backend
  val relatedSegments: List<Int> = emptyList(), // segment_ids from backend
  val relatedOrganizations: List<String> = emptyList(), // organizations from backend
  val relatedProjects: List<String> = emptyList(), // projects list from backend (in addition to relatedProject)
  // Tool category fields for grouping in Daily section
  val toolCategoriesNeeded: List<String> = emptyList(), // Tool categories (e.g., ["Google Calendar", "Create Event"])
  val toolCategoryGroupId: String? = null, // SHA256[:16] hash for category grouping
  // Execution step fields for real-time progress display
  val executionSteps: List<ExecutionStepUi> = emptyList(), // Steps from todo_execution_steps table
  val taskResult: String? = null, // Result from task_complete tool output
  val executionOutcome: String? = null, // Structured execution outcome from agent_outputs
  val executionLinks: List<String> = emptyList(), // Links extracted from agent_outputs (sources)
  val plannedTools: List<String> = emptyList(), // Tools/providers needed for execution (from exec_spec.providers)
  val currentExecutingStep: Int? = null, // Current step being executed (for animation)
  // OAuth reconnect payload — non-null when status is REQUIRES_REAUTH
  val reauthInfo: ReauthInfo? = null,
  // ISO 8601 timestamp when the todo was created — used to sort Moves newest-first.
  val createdAt: String? = null,
) {
  /**
   * Check if this task is from KK_exec (has KK_exec integration fields).
   */
  val isFromKKExec: Boolean
    get() = kkExecTodoId != null

  /**
   * Check if this is a sensitive task requiring user confirmation.
   */
  val requiresConfirmation: Boolean
    get() = kkExecIsSensitive == true || kkExecCategory == "E_COMPLEX_LEVEL3"

  /**
   * Check if this task can be auto-executed.
   */
  val isAutoExecutable: Boolean
    get() = kkExecCanExecute == true && kkExecIsSensitive != true

  /**
   * Check if this task cannot be executed (f_cannotdo category).
   */
  val cannotExecute: Boolean
    get() = kkExecCanExecute == false || kkExecCategory == "F_CANNOTDO"
}

val reviewMetadataState = mutableStateOf<List<TaskMetadata>>(emptyList())
val reviewMetadata: List<TaskMetadata> get() = reviewMetadataState.value

// Review items - mapped from metadata to Domain TodoItem
val reviewItems: List<TodoItem> get() = reviewMetadata.map {
  TodoItem(it.id, it.title, type = TodoType.ACTION_REQUIRED)
}

val pendingMetadataState = mutableStateOf<List<TaskMetadata>>(emptyList())
val pendingMetadata: List<TaskMetadata> get() = pendingMetadataState.value

// Pending items - mapped from metadata
val pendingItems: List<TodoItem> get() = pendingMetadata.map {
  TodoItem(it.id, it.title, type = TodoType.TODO)
}

val completedMetadataState = mutableStateOf<List<TaskMetadata>>(emptyList())
val completedMetadata: List<TaskMetadata> get() = completedMetadataState.value

// Completed items - mapped from metadata
val completedItems: List<TodoItem> get() = completedMetadata.map {
  TodoItem(it.id, it.title, isCompleted = true, type = TodoType.COMPLETED)
}

// ==================== KK_exec Todos State ====================
// These are populated from KK_exec API and filtered by category

/**
 * KK_exec Sensitive todos (requires user confirmation) - e_complex_level3 category
 * Displayed in the "Sensitive" section of the Function screen.
 */
val kkExecSensitiveTodosState = mutableStateOf<List<TaskMetadata>>(emptyList())
val kkExecSensitiveTodos: List<TaskMetadata> get() = kkExecSensitiveTodosState.value

/**
 * KK_exec Daily todos (auto-executable and cannot execute)
 * Displayed in the "Daily" section of the Function screen.
 * Includes categories: a_simple, b_apis, c_complex_level1, d_complex_level2, f_cannotdo
 */
val kkExecDailyTodosState = mutableStateOf<List<TaskMetadata>>(emptyList())
val kkExecDailyTodos: List<TaskMetadata> get() = kkExecDailyTodosState.value

/**
 * KK_exec Daily todos grouped by tool_category_group_id for category-based stacking.
 * Key is the group_id (SHA256[:16] hash), value is the list of tasks in that category.
 * "uncategorized" key contains tasks with null/empty group_id.
 */
val kkExecDailyTodosGroupedState = mutableStateOf<Map<String, List<TaskMetadata>>>(emptyMap())
val kkExecDailyTodosGrouped: Map<String, List<TaskMetadata>> get() = kkExecDailyTodosGroupedState.value

/**
 * All KK_exec todos (combined from all sessions)
 */
val allKKExecTodosState = mutableStateOf<List<TaskMetadata>>(emptyList())
val allKKExecTodos: List<TaskMetadata> get() = allKKExecTodosState.value

/**
 * Todo IDs currently executing (after approve/retry, before completion).
 * Used to show progress indicator on the card.
 */
val executingTodoIdsState = mutableStateOf<Set<String>>(emptySet())

/**
 * Category group IDs that have recently completed execution.
 * Used to show a red dot on the category header and sort it to top.
 */
val recentlyCompletedCategoriesState = mutableStateOf<Set<String>>(emptySet())

// ==================== UNIFIED ARCHIVE/PIN STATE ====================
// These hold the IDs of archived and pinned items, persisting across data refreshes.
// This is the single source of truth for archive/pin state across all screens.

/**
 * Archived task IDs - persists across refreshes
 */
val archivedTaskIdsState = mutableStateOf<Set<String>>(emptySet())

/**
 * Archived meeting IDs - persists across refreshes
 */
val archivedMeetingIdsState = mutableStateOf<Set<String>>(emptySet())

/**
 * Archived entity IDs (projects, people, organizations) - persists across refreshes
 */
val archivedProjectIdsState = mutableStateOf<Set<String>>(emptySet())
val archivedPersonIdsState = mutableStateOf<Set<String>>(emptySet())
val archivedOrganizationIdsState = mutableStateOf<Set<String>>(emptySet())

/**
 * Pinned task IDs with timestamp - persists across refreshes
 */
val pinnedTaskIdsState = mutableStateOf<Map<String, Long>>(emptyMap())

/**
 * Pinned meeting IDs with timestamp - persists across refreshes
 */
val pinnedMeetingIdsState = mutableStateOf<Map<String, Long>>(emptyMap())

/**
 * Pinned entity IDs with timestamp - persists across refreshes
 */
val pinnedProjectIdsState = mutableStateOf<Map<String, Long>>(emptyMap())
val pinnedPersonIdsState = mutableStateOf<Map<String, Long>>(emptyMap())
val pinnedOrganizationIdsState = mutableStateOf<Map<String, Long>>(emptyMap())

/**
 * Task IDs the user has opened/seen this session. Drives the unread dot on Moves.
 * Session-scoped — cleared on app restart by design.
 */
val seenTaskIdsState = mutableStateOf<Set<String>>(emptySet())

fun markTaskSeen(id: String) {
  if (id.isBlank() || id in seenTaskIdsState.value) return
  seenTaskIdsState.value = seenTaskIdsState.value + id
}

/**
 * Permanently deleted IDs - items that have been permanently removed from archive.
 * These IDs are kept in their respective archived ID sets to prevent reappearing in active lists,
 * but filtered out of ArchivedScreen display.
 */
val permanentlyDeletedIdsState = mutableStateOf<Set<String>>(emptySet())

/**
 * Archived tasks metadata - for display in ArchivedScreen
 */
val archivedTasksState = mutableStateOf<List<TaskMetadata>>(emptyList())

/**
 * Archived meetings metadata - for display in ArchivedScreen
 */
val archivedMeetingsState = mutableStateOf<List<Meeting>>(emptyList())

// ==================== PERSISTENT STORAGE ====================

/**
 * Storage manager for archive/pin persistence
 */
private val archivePinStorage: ArchivePinStorageManager by lazy {
  ArchivePinStorageManager(SecureStorage())
}

/**
 * Flag to track if persistence has been initialized
 */
private var persistenceInitialized = false

/**
 * Initialize archive/pin state from persistent storage.
 * Call this on app startup.
 */
fun initializeArchivePinState() {
  if (persistenceInitialized) return
  persistenceInitialized = true

  KlikLogger.i("Models", "Loading archive/pin state from storage")

  // Load archived IDs
  archivedTaskIdsState.value = archivePinStorage.loadArchivedTaskIds()
  archivedMeetingIdsState.value = archivePinStorage.loadArchivedMeetingIds()
  archivedProjectIdsState.value = archivePinStorage.loadArchivedProjectIds()
  archivedPersonIdsState.value = archivePinStorage.loadArchivedPersonIds()
  archivedOrganizationIdsState.value = archivePinStorage.loadArchivedOrganizationIds()

  // Load pinned IDs
  pinnedTaskIdsState.value = archivePinStorage.loadPinnedTaskIds()
  pinnedMeetingIdsState.value = archivePinStorage.loadPinnedMeetingIds()
  pinnedProjectIdsState.value = archivePinStorage.loadPinnedProjectIds()
  pinnedPersonIdsState.value = archivePinStorage.loadPinnedPersonIds()
  pinnedOrganizationIdsState.value = archivePinStorage.loadPinnedOrganizationIds()

  KlikLogger.i("Models", "Archive/pin state loaded from storage")
}

/**
 * Save current archive/pin state to persistent storage
 */
private fun saveArchivePinState() {
  archivePinStorage.saveArchivedTaskIds(archivedTaskIdsState.value)
  archivePinStorage.saveArchivedMeetingIds(archivedMeetingIdsState.value)
  archivePinStorage.saveArchivedProjectIds(archivedProjectIdsState.value)
  archivePinStorage.saveArchivedPersonIds(archivedPersonIdsState.value)
  archivePinStorage.saveArchivedOrganizationIds(archivedOrganizationIdsState.value)
  archivePinStorage.savePinnedTaskIds(pinnedTaskIdsState.value)
  archivePinStorage.savePinnedMeetingIds(pinnedMeetingIdsState.value)
  archivePinStorage.savePinnedProjectIds(pinnedProjectIdsState.value)
  archivePinStorage.savePinnedPersonIds(pinnedPersonIdsState.value)
  archivePinStorage.savePinnedOrganizationIds(pinnedOrganizationIdsState.value)
}

/**
 * Clear all archive/pin state (in-memory and persistent storage).
 * Call this on logout to prevent state leaking across users.
 */
fun clearArchivePinState() {
  archivedTaskIdsState.value = emptySet()
  archivedMeetingIdsState.value = emptySet()
  archivedProjectIdsState.value = emptySet()
  archivedPersonIdsState.value = emptySet()
  archivedOrganizationIdsState.value = emptySet()
  pinnedTaskIdsState.value = emptyMap()
  pinnedMeetingIdsState.value = emptyMap()
  pinnedProjectIdsState.value = emptyMap()
  pinnedPersonIdsState.value = emptyMap()
  pinnedOrganizationIdsState.value = emptyMap()
  permanentlyDeletedIdsState.value = emptySet()
  archivePinStorage.clearAll()
  persistenceInitialized = false
  KlikLogger.i("Models", "Cleared all archive/pin state for logout")
}

// ==================== ARCHIVE/PIN HELPER FUNCTIONS ====================

/**
 * Archive a task - adds to archived set and removes from display
 */
fun archiveTask(taskId: String, task: TaskMetadata?) {
  archivedTaskIdsState.value = archivedTaskIdsState.value + taskId
  // Remove from pinned if it was pinned
  pinnedTaskIdsState.value = pinnedTaskIdsState.value - taskId
  // Store task metadata for ArchivedScreen
  if (task != null) {
    archivedTasksState.value = archivedTasksState.value + task
  }
  saveArchivePinState()
  KlikLogger.i("Models", "Task archived: $taskId")
}

/**
 * Unarchive a task - removes from archived set
 */
fun unarchiveTask(taskId: String) {
  archivedTaskIdsState.value = archivedTaskIdsState.value - taskId
  archivedTasksState.value = archivedTasksState.value.filter { it.id != taskId }
  saveArchivePinState()
  KlikLogger.i("Models", "Task unarchived: $taskId")
}

/**
 * Toggle pin state for a task
 */
fun toggleTaskPin(taskId: String) {
  val current = pinnedTaskIdsState.value
  pinnedTaskIdsState.value = if (current.containsKey(taskId)) {
    current - taskId
  } else {
    current + (taskId to kotlinx.datetime.Clock.System.now().toEpochMilliseconds())
  }
  saveArchivePinState()
  KlikLogger.d("Models", "Task pin toggled: $taskId, isPinned=${pinnedTaskIdsState.value.containsKey(taskId)}")
}

/**
 * Archive a meeting
 */
fun archiveMeeting(meetingId: String, meeting: Meeting?) {
  archivedMeetingIdsState.value = archivedMeetingIdsState.value + meetingId
  pinnedMeetingIdsState.value = pinnedMeetingIdsState.value - meetingId
  if (meeting != null) {
    archivedMeetingsState.value = archivedMeetingsState.value + meeting
  }
  saveArchivePinState()
  KlikLogger.i("Models", "Meeting archived: $meetingId")
}

/**
 * Unarchive a meeting
 */
fun unarchiveMeeting(meetingId: String) {
  archivedMeetingIdsState.value = archivedMeetingIdsState.value - meetingId
  archivedMeetingsState.value = archivedMeetingsState.value.filter { it.id != meetingId }
  saveArchivePinState()
  KlikLogger.i("Models", "Meeting unarchived: $meetingId")
}

/**
 * Toggle pin state for a meeting
 */
fun toggleMeetingPin(meetingId: String) {
  val current = pinnedMeetingIdsState.value
  pinnedMeetingIdsState.value = if (current.containsKey(meetingId)) {
    current - meetingId
  } else {
    current + (meetingId to kotlinx.datetime.Clock.System.now().toEpochMilliseconds())
  }
  saveArchivePinState()
  KlikLogger.d("Models", "Meeting pin toggled: $meetingId")
}

/**
 * Archive a project
 */
fun archiveProject(projectId: String) {
  archivedProjectIdsState.value = archivedProjectIdsState.value + projectId
  pinnedProjectIdsState.value = pinnedProjectIdsState.value - projectId
  saveArchivePinState()
  KlikLogger.i("Models", "Project archived: $projectId")
}

/**
 * Unarchive a project
 */
fun unarchiveProject(projectId: String) {
  archivedProjectIdsState.value = archivedProjectIdsState.value - projectId
  saveArchivePinState()
  KlikLogger.i("Models", "Project unarchived: $projectId")
}

/**
 * Archive a person
 */
fun archivePerson(personId: String) {
  archivedPersonIdsState.value = archivedPersonIdsState.value + personId
  pinnedPersonIdsState.value = pinnedPersonIdsState.value - personId
  saveArchivePinState()
  KlikLogger.i("Models", "Person archived: $personId")
}

/**
 * Unarchive a person
 */
fun unarchivePerson(personId: String) {
  archivedPersonIdsState.value = archivedPersonIdsState.value - personId
  saveArchivePinState()
  KlikLogger.i("Models", "Person unarchived: $personId")
}

/**
 * Archive an organization
 */
fun archiveOrganization(organizationId: String) {
  archivedOrganizationIdsState.value = archivedOrganizationIdsState.value + organizationId
  pinnedOrganizationIdsState.value = pinnedOrganizationIdsState.value - organizationId
  saveArchivePinState()
  KlikLogger.i("Models", "Organization archived: $organizationId")
}

/**
 * Unarchive an organization
 */
fun unarchiveOrganization(organizationId: String) {
  archivedOrganizationIdsState.value = archivedOrganizationIdsState.value - organizationId
  saveArchivePinState()
  KlikLogger.i("Models", "Organization unarchived: $organizationId")
}

// ==================== PERMANENT DELETE FUNCTIONS ====================

/**
 * Permanently delete a task - removes from archived display, keeps ID archived so it won't reappear
 */
fun permanentlyDeleteTask(taskId: String) {
  archivedTasksState.value = archivedTasksState.value.filter { it.id != taskId }
  permanentlyDeletedIdsState.value = permanentlyDeletedIdsState.value + taskId
  saveArchivePinState()
  KlikLogger.i("Models", "Task permanently deleted: $taskId")
}

/**
 * Permanently delete a meeting
 */
fun permanentlyDeleteMeeting(meetingId: String) {
  archivedMeetingsState.value = archivedMeetingsState.value.filter { it.id != meetingId }
  permanentlyDeletedIdsState.value = permanentlyDeletedIdsState.value + meetingId
  saveArchivePinState()
  KlikLogger.i("Models", "Meeting permanently deleted: $meetingId")
}

/**
 * Permanently delete a project
 */
fun permanentlyDeleteProject(projectId: String) {
  permanentlyDeletedIdsState.value = permanentlyDeletedIdsState.value + projectId
  saveArchivePinState()
  KlikLogger.i("Models", "Project permanently deleted: $projectId")
}

/**
 * Permanently delete a person
 */
fun permanentlyDeletePerson(personId: String) {
  permanentlyDeletedIdsState.value = permanentlyDeletedIdsState.value + personId
  saveArchivePinState()
  KlikLogger.i("Models", "Person permanently deleted: $personId")
}

/**
 * Permanently delete an organization
 */
fun permanentlyDeleteOrganization(organizationId: String) {
  permanentlyDeletedIdsState.value = permanentlyDeletedIdsState.value + organizationId
  saveArchivePinState()
  KlikLogger.i("Models", "Organization permanently deleted: $organizationId")
}

/**
 * Set exclusive pin for project (only one can be pinned at a time)
 */
fun pinProject(projectId: String) {
  pinnedProjectIdsState.value = mapOf(projectId to kotlinx.datetime.Clock.System.now().toEpochMilliseconds())
  saveArchivePinState()
  KlikLogger.d("Models", "Project pinned: $projectId")
}

/**
 * Set exclusive pin for person (only one can be pinned at a time)
 */
fun pinPerson(personId: String) {
  pinnedPersonIdsState.value = mapOf(personId to kotlinx.datetime.Clock.System.now().toEpochMilliseconds())
  saveArchivePinState()
  KlikLogger.d("Models", "Person pinned: $personId")
}

/**
 * Set exclusive pin for organization (only one can be pinned at a time)
 */
fun pinOrganization(organizationId: String) {
  pinnedOrganizationIdsState.value = mapOf(organizationId to kotlinx.datetime.Clock.System.now().toEpochMilliseconds())
  saveArchivePinState()
  KlikLogger.d("Models", "Organization pinned: $organizationId")
}

/**
 * Get non-archived tasks from a list
 */
fun filterArchivedTasks(tasks: List<TaskMetadata>): List<TaskMetadata> = tasks.filter { it.id !in archivedTaskIdsState.value }

/**
 * Get non-archived meetings from a list
 */
fun filterArchivedMeetings(meetings: List<Meeting>): List<Meeting> = meetings.filter { it.id !in archivedMeetingIdsState.value }

/**
 * Sort tasks with pinned items at top
 */
fun sortTasksByPinned(tasks: List<TaskMetadata>): List<TaskMetadata> {
  val pinned = pinnedTaskIdsState.value
  return tasks.sortedWith(
    compareByDescending<TaskMetadata> { pinned.containsKey(it.id) }
      .thenByDescending { pinned[it.id] ?: 0L },
  )
}

/**
 * Sort meetings with pinned items at top
 */
fun sortMeetingsByPinned(meetings: List<Meeting>): List<Meeting> {
  val pinned = pinnedMeetingIdsState.value
  return meetings.sortedWith(
    compareByDescending<Meeting> { pinned.containsKey(it.id) }
      .thenByDescending { pinned[it.id] ?: 0L },
  )
}

// Meetings state - loaded from backend API
val meetingsState = mutableStateOf<List<Meeting>>(emptyList())
val allMeetings: List<Meeting> get() = meetingsState.value

val scenariosState = mutableStateOf<List<Scenario>>(emptyList())
val allScenarios: List<Scenario> get() = scenariosState.value

val projectsState = mutableStateOf<List<Project>>(emptyList())
val allProjects: List<Project> get() = projectsState.value

val organizationsState = mutableStateOf<List<Organization>>(emptyList())
val allOrganizations: List<Organization> get() = organizationsState.value

// Goal and Level state (from KK_goal API)
val goalsState = mutableStateOf<io.github.fletchmckee.liquid.samples.app.data.source.remote.GoalListResponse?>(null)
val goalsData: io.github.fletchmckee.liquid.samples.app.data.source.remote.GoalListResponse? get() = goalsState.value

val userLevelState = mutableStateOf<io.github.fletchmckee.liquid.samples.app.data.source.remote.UserLevelData?>(null)
val userLevelData: io.github.fletchmckee.liquid.samples.app.data.source.remote.UserLevelData? get() = userLevelState.value

// Calendar helper functions (using Domain entities)
fun getMeetingsForDate(meetings: List<Meeting>, date: LocalDate): List<Meeting> = meetings.filter { it.date == date }

fun getMeetingsCountForMonth(
  meetings: List<Meeting>,
  year: Int,
  month: Int,
): Map<Int, Int> = meetings
  .filter { it.date.year == year && it.date.monthNumber == month }
  .groupBy { it.date.dayOfMonth }
  .mapValues { it.value.size }

fun getDaysInMonth(year: Int, month: Int): Int = when (month) {
  1, 3, 5, 7, 8, 10, 12 -> 31
  4, 6, 9, 11 -> 30
  2 -> if (isLeapYear(year)) 29 else 28
  else -> 30
}

fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

fun getFirstDayOfWeek(year: Int, month: Int): Int {
  // Zeller's formula to get day of week (0 = Sunday)
  val y = if (month < 3) year - 1 else year
  val m = if (month < 3) month + 12 else month
  val q = 1 // First day
  val k = y % 100
  val j = y / 100
  val h = (q + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 - 2 * j) % 7
  return ((h + 6) % 7) // Convert to 0 = Sunday
}

fun getDaysInPrevMonth(year: Int, month: Int): Int {
  val prevMonth = if (month == 1) 12 else month - 1
  val prevYear = if (month == 1) year - 1 else year
  return getDaysInMonth(prevYear, prevMonth)
}

fun getDayOfWeek(date: LocalDate): Int = getFirstDayOfWeek(date.year, date.monthNumber) + date.dayOfMonth - 1

fun formatDateForDisplay(date: LocalDate): String {
  val monthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
  )
  val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
  val dayOfWeek = (getFirstDayOfWeek(date.year, date.monthNumber) + date.dayOfMonth - 1) % 7
  return "${monthNames[date.monthNumber - 1]} ${date.dayOfMonth}, ${dayNames[dayOfWeek]}"
}
