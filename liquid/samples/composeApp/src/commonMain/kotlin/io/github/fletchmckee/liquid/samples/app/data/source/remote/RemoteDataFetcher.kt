// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.source.remote

import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser
import io.github.fletchmckee.liquid.samples.app.data.network.HttpClient
import io.github.fletchmckee.liquid.samples.app.data.network.dto.AchievementsListDto
import io.github.fletchmckee.liquid.samples.app.data.network.dto.RemoteUserPreferencesDto
import io.github.fletchmckee.liquid.samples.app.domain.entity.ChatMessage
import io.github.fletchmckee.liquid.samples.app.domain.entity.DimensionScore
import io.github.fletchmckee.liquid.samples.app.domain.entity.Insights
import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import io.github.fletchmckee.liquid.samples.app.domain.entity.MeetingMinute
import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.PlanType
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.domain.entity.ProjectStatus
import io.github.fletchmckee.liquid.samples.app.domain.entity.ProjectTrend
import io.github.fletchmckee.liquid.samples.app.domain.entity.ReauthInfo
import io.github.fletchmckee.liquid.samples.app.domain.entity.Scenario
import io.github.fletchmckee.liquid.samples.app.domain.entity.Subscription
import io.github.fletchmckee.liquid.samples.app.domain.entity.SubscriptionFeatures
import io.github.fletchmckee.liquid.samples.app.domain.entity.SubscriptionPlan
import io.github.fletchmckee.liquid.samples.app.domain.entity.SubscriptionUsage
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskPriority
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskStatus
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskSummary
import io.github.fletchmckee.liquid.samples.app.domain.entity.TodoItem
import io.github.fletchmckee.liquid.samples.app.domain.entity.TodoType
import io.github.fletchmckee.liquid.samples.app.domain.entity.TracedSegment
import io.github.fletchmckee.liquid.samples.app.domain.entity.UsageDetails
import io.github.fletchmckee.liquid.samples.app.domain.entity.User
import io.github.fletchmckee.liquid.samples.app.domain.entity.UserPreferences
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata as UITaskMetadata
import io.github.fletchmckee.liquid.samples.app.reporting.CrashReporter
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * Fetches data from the real backend API.
 * PRODUCTION: JWT-only authentication via Authorization Bearer header.
 * Backend extracts user_id from JWT claims (sub field).
 */
object RemoteDataFetcher {

  /** Current language preference for LLM API requests. Updated from UserPreferences. */
  var currentLanguage: String = "en"

  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
  }

  /**
   * Decode JSON response with error context.
   * If the API returns non-JSON (e.g. Cloudflare 502/524 HTML error page),
   * throws with a truncated summary — not the entire HTML page.
   */
  private inline fun <reified T> decodeJsonResponse(response: String, endpoint: String): T {
    val trimmed = response.trimStart()
    if (!trimmed.startsWith("{") && !trimmed.startsWith("[") && !trimmed.startsWith("\"")) {
      // Extract useful info from HTML error pages (e.g. "524: A timeout occurred")
      val titleMatch = Regex("<title>([^<]+)</title>").find(response)
      val hint = titleMatch?.groupValues?.get(1) ?: response.take(100)
      val error = IllegalStateException("API ($endpoint) returned non-JSON: $hint")
      CrashReporter.reportError("RemoteDataFetcher", "Non-JSON response from $endpoint: $hint", error)
      throw error
    }
    // Detect backend error responses (e.g. 500 with {"detail":"..."})
    // before attempting deserialization into the expected type
    if (trimmed.startsWith("{") && trimmed.contains("\"detail\"")) {
      try {
        val errorObj = json.parseToJsonElement(trimmed).jsonObject
        val detail = errorObj["detail"]?.jsonPrimitive?.content
        if (detail != null && errorObj.size <= 2) {
          val error = IllegalStateException("API ($endpoint) returned error: $detail")
          CrashReporter.reportError("RemoteDataFetcher", "API error from $endpoint: $detail", error)
          throw error
        }
      } catch (_: kotlinx.serialization.SerializationException) {
        // Not a simple error object, proceed with normal deserialization
      } catch (_: IllegalArgumentException) {
        // jsonPrimitive cast failed, proceed with normal deserialization
      }
    }
    return json.decodeFromString<T>(response)
  }

  // ==================== Meetings ====================

  /**
   * Fetch meetings from backend API.
   * USER-SPECIFIC: Returns meetings for the authenticated user.
   *
   * @param limit Maximum number of meetings to return
   * @param startDate Optional start date for filtering (inclusive)
   * @param endDate Optional end date for filtering (inclusive)
   * @return List of meetings within the specified date range (if provided)
   *
   * PRODUCTION: When startDate and endDate are provided, backend will return
   * meetings within that date range. Used for loading historical meetings.
   */
  suspend fun fetchMeetings(
    limit: Int = 500,
    startDate: kotlinx.datetime.LocalDate? = null,
    endDate: kotlinx.datetime.LocalDate? = null,
  ): List<Meeting> {
    var url = "${ApiConfig.Endpoints.MEETINGS}?limit=$limit"

    // Add date range parameters if provided
    if (startDate != null && endDate != null) {
      url += "&start_date=$startDate&end_date=$endDate"
      KlikLogger.d("RemoteDataFetcher", "Fetching meetings for range: $startDate to $endDate (limit=$limit)")
    } else {
      KlikLogger.d("RemoteDataFetcher", "Fetching meetings (default range, limit=$limit)")
    }

    val response = HttpClient.get(url)
      ?: throw IllegalStateException("Failed to fetch meetings from backend. URL: $url")

    val wrapper = decodeJsonResponse<MeetingsResponse>(response, "meetings")
    KlikLogger.i("RemoteDataFetcher", "API returned ${wrapper.items.size} raw meetings")
    val meetings = wrapper.items.map { it.toDomain() }

    // Log date distribution summary
    val dateDistribution = meetings.groupBy { it.date }.mapValues { it.value.size }
    KlikLogger.i("RemoteDataFetcher", "Loaded ${meetings.size} meetings")
    val distributionSummary = dateDistribution.entries.sortedBy { it.key }.joinToString(", ") { (date, count) -> "$date: $count" }
    KlikLogger.d("RemoteDataFetcher", "Date distribution: $distributionSummary")

    return meetings
  }

  /**
   * Fetch insights from KLIK Insights API (port 8336).
   * Returns user-specific insights based on user_id in request body.
   */
  suspend fun fetchInsights(): Insights {
    val userId = CurrentUser.userId
      ?: throw IllegalStateException("Cannot fetch insights: no user logged in")

    // Build request body for insights API
    val requestBody = buildJsonObject {
      put("user_id", userId)
      put("days", 30)
      put("language", currentLanguage)
    }.toString()
    val url = "${ApiConfig.INSIGHTS_BASE_URL}${ApiConfig.Endpoints.INSIGHTS}"

    KlikLogger.d("RemoteDataFetcher", "Fetching insights from: $url for user: $userId")

    val response = HttpClient.postUrl(url, requestBody)
      ?: throw IllegalStateException("Failed to fetch insights from backend. URL: $url")

    KlikLogger.d("RemoteDataFetcher", "Insights response: ${response.take(200)}...")
    val dto = decodeJsonResponse<InsightsApiResponseDto>(response, "insights")
    return dto.toDomain()
  }

  /**
   * Fetch encouraging message from Encourage API (port 8335).
   * Returns user-specific encouraging summary of recent activities.
   */
  suspend fun fetchEncourage(days: Int = 7): EncourageData {
    val userId = CurrentUser.userId
      ?: throw IllegalStateException("Cannot fetch encourage: no user logged in")

    val requestBody = buildJsonObject {
      put("user_id", userId)
      put("days", days)
      put("language", currentLanguage)
    }.toString()
    val url = "${ApiConfig.ENCOURAGE_BASE_URL}${ApiConfig.Endpoints.ENCOURAGE}"

    KlikLogger.d("RemoteDataFetcher", "Fetching encourage from: $url for user: $userId")

    val response = HttpClient.postUrl(url, requestBody)
      ?: throw IllegalStateException("Failed to fetch encourage from backend. URL: $url")

    KlikLogger.d("RemoteDataFetcher", "Encourage response: ${response.take(200)}...")
    val dto = decodeJsonResponse<EncourageApiResponseDto>(response, "encourage")
    return dto.toDomain()
  }

  /**
   * Fetch worklife insights from Worklife API (port 8337).
   * Returns user-specific knowledge graph based insights.
   */
  suspend fun fetchWorklife(days: Int = 30): WorklifeData {
    val userId = CurrentUser.userId
      ?: throw IllegalStateException("Cannot fetch worklife: no user logged in")

    val requestBody = buildJsonObject {
      put("user_id", userId)
      put("days", days)
      put("language", currentLanguage)
    }.toString()
    val url = "${ApiConfig.WORKLIFE_BASE_URL}${ApiConfig.Endpoints.WORKLIFE}"

    KlikLogger.d("RemoteDataFetcher", "Fetching worklife from: $url for user: $userId")

    val response = HttpClient.postUrl(url, requestBody)
      ?: throw IllegalStateException("Failed to fetch worklife from backend. URL: $url")

    KlikLogger.d("RemoteDataFetcher", "Worklife response: ${response.take(200)}...")
    val dto = decodeJsonResponse<WorklifeApiResponseDto>(response, "worklife")
    return dto.toDomain()
  }

  // ==================== Tasks ====================

  suspend fun fetchTasks(): List<TaskMetadata> {
    val url = ApiConfig.Endpoints.TASKS
    val response = HttpClient.get(url)
      ?: throw IllegalStateException("Failed to fetch tasks from backend. URL: $url")

    val wrapper = decodeJsonResponse<TasksResponse>(response, "tasks")
    return wrapper.items.map { it.toDomain() }
  }

  /**
   * Update task status via backend PUT /api/v1/tasks/{id}.
   * Used for approve, reject, archive, and other status transitions.
   */
  suspend fun updateTaskStatus(taskId: String, status: String): TaskMetadata {
    val url = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.TASKS}/$taskId"
    val requestBody = buildJsonObject {
      put("status", status)
    }.toString()

    KlikLogger.d("RemoteDataFetcher", "Updating task $taskId status to $status at: $url")

    val response = HttpClient.putUrl(url, requestBody)
      ?: throw IllegalStateException("Failed to update task status. URL: $url, taskId: $taskId, status: $status")

    val dto = decodeJsonResponse<TaskDto>(response, "task-update")
    return dto.toDomain()
  }

  // ==================== KK_exec Todos ====================

  /**
   * Fetch todos from KK_exec API (port 8338) for a specific session.
   * Returns todos that were extracted by KK_todo_extract from meeting transcripts.
   * User-specific via JWT token (backend extracts user_id from claims).
   */
  suspend fun fetchKKExecTodos(sessionId: String): List<KKExecTodoItem> {
    val userId = CurrentUser.userId
      ?: throw IllegalStateException("Cannot fetch KK_exec todos: no user logged in")

    val url = "${ApiConfig.EXEC_BASE_URL}${ApiConfig.Endpoints.TODOS}/$sessionId"
    KlikLogger.d("RemoteDataFetcher", "Fetching KK_exec todos from: $url for user: $userId")

    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to fetch KK_exec todos from backend. URL: $url")

    val todos = decodeJsonResponse<List<KKExecTodoItemDto>>(response, "exec-todos")
    KlikLogger.i("RemoteDataFetcher", "Fetched ${todos.size} KK_exec todos")
    // For session-specific fetch, items should have user_id from backend
    return todos.map { it.toDomain(wrapperUserId = userId) }
  }

  /**
   * Fetch todos from KK_exec API for all user sessions.
   * Returns combined todos across all sessions for the current user.
   */
  suspend fun fetchAllKKExecTodos(): List<KKExecTodoItem> {
    val userId = CurrentUser.userId
      ?: throw IllegalStateException("Cannot fetch KK_exec todos: no user logged in")

    // Fetch todos for user (all sessions) - backend extracts user_id from JWT
    val url = "${ApiConfig.EXEC_BASE_URL}${ApiConfig.Endpoints.USER_TODOS}"
    KlikLogger.d("RemoteDataFetcher", "Fetching all KK_exec todos from: $url")

    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to fetch all KK_exec todos from backend. URL: $url")

    val wrapper = decodeJsonResponse<KKExecTodosResponse>(response, "exec-all-todos")
    KlikLogger.i("RemoteDataFetcher", "Fetched ${wrapper.items.size} KK_exec todos total for user ${wrapper.user_id}")
    // Pass wrapper's user_id to each item since it may not be in the item itself
    return wrapper.items.map { it.toDomain(wrapperUserId = wrapper.user_id) }
  }

  /**
   * Execute a todo via KK_exec API.
   * POST /v1/todos/{session_id}/execute
   */
  suspend fun executeKKExecTodo(sessionId: String, todoId: Int): KKExecExecutionResult {
    val url = "${ApiConfig.EXEC_BASE_URL}${ApiConfig.Endpoints.EXECUTE_TODOS}/$sessionId/execute"
    // todoId is Int — safe from injection, but using buildJsonObject for consistency
    val requestBody = buildJsonObject {
      putJsonArray("todo_ids") { add(todoId) }
    }.toString()

    KlikLogger.d("RemoteDataFetcher", "Executing KK_exec todo $todoId in session $sessionId")

    val response = HttpClient.postUrl(url, requestBody)
      ?: throw IllegalStateException("Failed to execute KK_exec todo. URL: $url")

    return decodeJsonResponse<KKExecExecutionResult>(response, "exec-execute")
  }

  /**
   * Approve a sensitive todo via KK_exec API.
   * POST /v1/todos/todo/{todo_id}/approve
   */
  suspend fun approveKKExecTodo(todoId: Int, reason: String? = null): KKExecApprovalResult {
    val url = "${ApiConfig.EXEC_BASE_URL}${ApiConfig.Endpoints.TODO_REVIEW}/todo/$todoId/approve"
    val requestBody = json.encodeToString(KKExecApproveRequest.serializer(), KKExecApproveRequest(reason = reason))

    KlikLogger.d("RemoteDataFetcher", "Approving KK_exec todo $todoId")

    val response = HttpClient.postUrl(url, requestBody)
      ?: throw IllegalStateException("Failed to approve KK_exec todo. URL: $url")

    return decodeJsonResponse<KKExecApprovalResult>(response, "exec-approve")
  }

  /**
   * Reject a sensitive todo via KK_exec API.
   * POST /v1/todos/todo/{todo_id}/reject
   */
  suspend fun rejectKKExecTodo(todoId: Int, reason: String? = null): KKExecApprovalResult {
    val url = "${ApiConfig.EXEC_BASE_URL}${ApiConfig.Endpoints.TODO_REVIEW}/todo/$todoId/reject"
    val requestBody = json.encodeToString(KKExecRejectRequest.serializer(), KKExecRejectRequest(reason = reason))

    KlikLogger.d("RemoteDataFetcher", "Rejecting KK_exec todo $todoId")

    val response = HttpClient.postUrl(url, requestBody)
      ?: throw IllegalStateException("Failed to reject KK_exec todo. URL: $url")

    return decodeJsonResponse<KKExecApprovalResult>(response, "exec-reject")
  }

  /**
   * Bulk approve/reject sensitive todos via KK_exec API.
   * POST /v1/todos/todos/bulk-review
   */
  suspend fun bulkReviewKKExecTodos(items: List<KKExecBulkReviewItem>): KKExecBulkReviewResult {
    val url = "${ApiConfig.EXEC_BASE_URL}${ApiConfig.Endpoints.TODO_BULK_REVIEW}/todos/bulk-review"
    val requestBody = json.encodeToString(KKExecBulkReviewRequest.serializer(), KKExecBulkReviewRequest(items = items))

    KlikLogger.d("RemoteDataFetcher", "Bulk reviewing ${items.size} KK_exec todos")

    val response = HttpClient.postUrl(url, requestBody)
      ?: throw IllegalStateException("Failed to bulk review KK_exec todos. URL: $url")

    return decodeJsonResponse<KKExecBulkReviewResult>(response, "exec-bulk-review")
  }

  /**
   * Fetch pending-review todos from KK_exec API.
   * GET /v1/todos/pending-review
   * Response shape: user_id, total, items (list of TodoResponse).
   */
  suspend fun fetchPendingReviewTodos(): List<KKExecTodoItem> {
    val userId = CurrentUser.userId
      ?: throw IllegalStateException("Cannot fetch pending review todos: no user logged in")

    val url = "${ApiConfig.EXEC_BASE_URL}${ApiConfig.Endpoints.TODO_PENDING_REVIEW}"
    KlikLogger.d("RemoteDataFetcher", "Fetching pending review todos from: $url")

    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to fetch pending review todos. URL: $url")

    val wrapper = decodeJsonResponse<KKExecTodosResponse>(response, "exec-pending-review")
    return wrapper.items.map { it.toDomain(wrapperUserId = wrapper.user_id) }
  }

  /**
   * Retry a completed/failed todo via KK_exec API.
   * POST /v1/todos/todo/{todo_id}/retry
   */
  suspend fun retryKKExecTodo(todoId: Int): KKExecRetryResult {
    val url = "${ApiConfig.EXEC_BASE_URL}${ApiConfig.Endpoints.TODO_RETRY}/todo/$todoId/retry"

    KlikLogger.d("RemoteDataFetcher", "Retrying KK_exec todo $todoId")

    val response = HttpClient.postUrl(url, "{}")
      ?: throw IllegalStateException("Failed to retry KK_exec todo. URL: $url")

    return decodeJsonResponse<KKExecRetryResult>(response, "exec-retry")
  }

  suspend fun fetchTaskSummary(): TaskSummary {
    val response = HttpClient.get(ApiConfig.Endpoints.TASK_SUMMARY)
      ?: throw IllegalStateException("Failed to fetch task summary from backend.")

    return decodeJsonResponse<TaskSummary>(response, "task-summary")
  }

  // ==================== Fixed Session Audio ====================

  /**
   * Start a fixed recording session.
   * POST /api/v1/audio/start_fixed_session
   */
  suspend fun startFixedSession(): FixedSessionStartResponse {
    val userId = CurrentUser.userId
      ?: throw IllegalStateException("Cannot start fixed session: no user logged in")

    val url = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.AUDIO_START_FIXED}"
    KlikLogger.d("RemoteDataFetcher", "Starting fixed session at: $url for user: $userId")

    val response = HttpClient.postUrl(url, "{}", mapOf(ApiConfig.Headers.USER_ID to userId))
      ?: throw IllegalStateException("Failed to start fixed session. URL: $url")

    return decodeJsonResponse<FixedSessionStartResponse>(response, "fixed-session-start")
  }

  /**
   * Stop a fixed recording session.
   * POST /api/v1/audio/stop/{user_id}/fixed
   */
  suspend fun stopFixedSession(): FixedSessionStopResponse {
    val userId = CurrentUser.userId
      ?: throw IllegalStateException("Cannot stop fixed session: no user logged in")

    val url = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.AUDIO_STOP_FIXED}/$userId/fixed"
    KlikLogger.d("RemoteDataFetcher", "Stopping fixed session at: $url")

    val response = HttpClient.postUrl(url, "{}")
      ?: throw IllegalStateException("Failed to stop fixed session. URL: $url")

    return decodeJsonResponse<FixedSessionStopResponse>(response, "fixed-session-stop")
  }

  // ==================== Post-recording pipeline status ====================

  /**
   * Real post-recording pipeline state for the KK_orchestrator job that
   * took over when the user stopped a recording. One poll per ~2s is enough
   * since the orchestrator emits 0–100 % progress and the current stage
   * name (denoise → asr → diarization → knowledge_graph → meeting_minutes
   * → tasks → dropbox).
   *
   * GET /api/orchestrator/api/v1/pipeline/session/{session_id}/status
   * Returns null on transport / parse failure.
   */
  suspend fun fetchOrchestratorSessionStatus(sessionId: String): OrchestratorStatusDto? {
    // Must use getUrl(), not get(): the orchestrator route sits under
    // /api/orchestrator/, not /api/v1/, so we need to pass the full
    // absolute URL and bypass the base-url auto-prefix that get() applies.
    val base = ApiConfig.BASE_URL.substringBefore("/api/v1")
    val url = "$base/api/orchestrator/api/v1/pipeline/session/$sessionId/status"
    val response = HttpClient.getUrl(url) ?: return null
    return try {
      decodeJsonResponse<OrchestratorStatusDto>(response, "orchestrator-status")
    } catch (e: Exception) {
      KlikLogger.w("RemoteDataFetcher", "fetchOrchestratorSessionStatus parse failed: ${e.message}")
      null
    }
  }

  // ==================== People ====================

  suspend fun fetchPeople(): List<Person> {
    val url = ApiConfig.Endpoints.PEOPLE
    val response = HttpClient.get(url)
      ?: throw IllegalStateException("Failed to fetch people from backend. URL: $url")

    val userId = CurrentUser.userId
      ?: throw IllegalStateException("Cannot fetch people: no user logged in")

    val wrapper = decodeJsonResponse<PeopleResponse>(response, "people")
    return wrapper.items.map { it.toDomain(userId) }
  }

  // ==================== Organizations ====================

  suspend fun fetchOrganizations(): List<Organization> {
    val url = ApiConfig.Endpoints.ORGANIZATIONS
    val response = HttpClient.get(url)
      ?: throw IllegalStateException("Failed to fetch organizations from backend. URL: $url")

    val userId = CurrentUser.userId
      ?: throw IllegalStateException("Cannot fetch organizations: no user logged in")

    val wrapper = decodeJsonResponse<OrganizationsResponse>(response, "organizations")
    return wrapper.items.map { it.toDomain(userId) }
  }

  // ==================== Projects ====================

  suspend fun fetchProjects(): List<Project> {
    val url = ApiConfig.Endpoints.PROJECTS
    val response = HttpClient.get(url)
      ?: throw IllegalStateException("Failed to fetch projects from backend. URL: $url")

    val userId = CurrentUser.userId
      ?: throw IllegalStateException("Cannot fetch projects: no user logged in")

    val wrapper = decodeJsonResponse<ProjectsResponse>(response, "projects")
    return wrapper.items.map { it.toDomain(userId) }
  }

  // ==================== Chat ====================

  suspend fun fetchChatMessages(limit: Int = 50): List<ChatMessage> {
    val url = "${ApiConfig.Endpoints.CHAT_MESSAGES}?limit=$limit"
    val response = HttpClient.get(url)
      ?: throw IllegalStateException("Failed to fetch chat messages from backend. URL: $url")

    val wrapper = decodeJsonResponse<ChatMessagesResponse>(response, "chat-messages")
    return wrapper.items.map { it.toDomain() }
  }

  suspend fun fetchSuggestedQuestions(): List<String> {
    val response = HttpClient.get(ApiConfig.Endpoints.SUGGESTED_QUESTIONS)
      ?: throw IllegalStateException("Failed to fetch suggested questions from backend.")

    val wrapper = decodeJsonResponse<SuggestionsResponse>(response, "suggestions")
    return wrapper.suggestions.map { it.text }
  }

  // ==================== User ====================

  suspend fun fetchCurrentUser(): User {
    val response = HttpClient.get(ApiConfig.Endpoints.USER)
      ?: throw IllegalStateException("Failed to fetch current user from backend.")

    val dto = decodeJsonResponse<UserDto>(response, "user")
    return dto.toDomain()
  }

  // ==================== Growth ====================

  suspend fun fetchScenarios(): List<Scenario> {
    val response = HttpClient.get(ApiConfig.Endpoints.SCENARIOS)
      ?: throw IllegalStateException("Failed to fetch scenarios from backend.")

    val wrapper = decodeJsonResponse<ScenariosResponseWrapper>(response, "scenarios")
    return wrapper.scenarios.map { it.toDomain() }
  }

  /**
   * Fetch user's goals from KK_goal API
   */
  suspend fun fetchGoals(
    status: String? = null,
    limit: Int = 10,
  ): GoalListResponse {
    val userId = CurrentUser.userId
      ?: throw IllegalStateException("Cannot fetch goals: no user logged in")

    val statusParam = if (status != null) "&status=$status" else ""
    val url = "${ApiConfig.GOAL_BASE_URL}${ApiConfig.Endpoints.GOALS_LIST}/$userId/goals?limit=$limit$statusParam"

    KlikLogger.d("RemoteDataFetcher", "Fetching goals from: $url")

    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to fetch goals from backend. URL: $url")

    return decodeJsonResponse<GoalListResponse>(response, "goals")
  }

  /**
   * Fetch growth tree data from KK_tree API (port 8415)
   */
  suspend fun fetchGrowthTree(
    fromDate: String? = null,
    toDate: String? = null,
    includeFuture: Boolean = true,
  ): GrowthTreeResponse {
    val params = buildString {
      append("?include_future=$includeFuture")
      if (fromDate != null) append("&from_date=$fromDate")
      if (toDate != null) append("&to_date=$toDate")
    }
    val url = "${ApiConfig.TREE_BASE_URL}${ApiConfig.Endpoints.GROWTH_TREE}$params"

    KlikLogger.d("RemoteDataFetcher", "Fetching growth tree from: $url")

    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to fetch growth tree from backend. URL: $url")

    return decodeJsonResponse<GrowthTreeResponse>(response, "growth-tree")
  }

  /**
   * Fetch user's level and XP information
   */
  suspend fun fetchUserLevel(): UserLevelData {
    val userId = CurrentUser.userId
      ?: throw IllegalStateException("Cannot fetch user level: no user logged in")

    val url = "${ApiConfig.GOAL_BASE_URL}${ApiConfig.Endpoints.USER_LEVEL}/$userId/level"

    KlikLogger.d("RemoteDataFetcher", "Fetching user level from: $url")

    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to fetch user level from backend. URL: $url")

    return decodeJsonResponse<UserLevelData>(response, "user-level")
  }

  /**
   * Fetch the recent XP transaction history for the authenticated user.
   * Backs the "recent XP" timeline rendered under the level dial on the
   * Network/Growth screen — newest first, paginated.
   */
  suspend fun fetchXpHistory(limit: Int = 20, offset: Int = 0): XpHistoryResponse {
    val userId = CurrentUser.userId
      ?: throw IllegalStateException("Cannot fetch XP history: no user logged in")

    val url = "${ApiConfig.GOAL_BASE_URL}${ApiConfig.Endpoints.XP_HISTORY}/$userId/xp/history?limit=$limit&offset=$offset"

    KlikLogger.d("RemoteDataFetcher", "Fetching XP history from: $url")

    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to fetch XP history from backend. URL: $url")

    return decodeJsonResponse<XpHistoryResponse>(response, "xp-history")
  }

  // ==================== Notifications ====================

  /**
   * Fetch notifications from KK_notifications API (port 8341).
   * Returns paginated list of notifications for the authenticated user.
   * USER-SPECIFIC: Backend extracts user_id from JWT token.
   */
  suspend fun fetchNotifications(limit: Int = 50, offset: Int = 0): NotificationsResponseDto {
    val url = "${ApiConfig.NOTIFICATIONS_BASE_URL}${ApiConfig.Endpoints.NOTIFICATIONS}?limit=$limit&offset=$offset"

    KlikLogger.d("RemoteDataFetcher", "Fetching notifications from: $url")

    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to fetch notifications from backend. URL: $url")

    return decodeJsonResponse<NotificationsResponseDto>(response, "notifications")
  }

  /**
   * Mark a notification as read.
   * POST /v1/notifications/{id}/read
   */
  suspend fun markNotificationRead(notificationId: String) {
    val url = "${ApiConfig.NOTIFICATIONS_BASE_URL}${ApiConfig.Endpoints.NOTIFICATIONS}/$notificationId/read"

    KlikLogger.d("RemoteDataFetcher", "Marking notification as read: $url")

    HttpClient.postUrl(url, "{}")
      ?: throw IllegalStateException("Failed to mark notification as read. URL: $url")
  }

  /**
   * Fetch unread notification count for badge display.
   * GET /v1/notifications/unread-count
   */
  suspend fun fetchUnreadCount(): Int {
    val url = "${ApiConfig.NOTIFICATIONS_BASE_URL}${ApiConfig.Endpoints.UNREAD_COUNT}"

    KlikLogger.d("RemoteDataFetcher", "Fetching unread count from: $url")

    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to fetch unread count from backend. URL: $url")

    val dto = decodeJsonResponse<UnreadCountDto>(response, "unread-count")
    return dto.count
  }

  // ==================== Subscription ====================

  /**
   * Fetch available subscription plans (no auth required).
   * GET /plans
   */
  suspend fun fetchSubscriptionPlans(): List<SubscriptionPlan> {
    val url = "${ApiConfig.SUBSCRIPTION_BASE_URL}${ApiConfig.Endpoints.SUBSCRIPTION_PLANS}"
    KlikLogger.d("RemoteDataFetcher", "Fetching subscription plans from: $url")

    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to fetch subscription plans. URL: $url")

    val dtos = decodeJsonResponse<List<SubscriptionPlanDto>>(response, "subscription-plans")
    return dtos.map { it.toDomain() }
  }

  /**
   * Fetch current user's subscription.
   * GET /me (auth required)
   */
  suspend fun fetchMySubscription(): Subscription {
    val url = "${ApiConfig.SUBSCRIPTION_BASE_URL}${ApiConfig.Endpoints.SUBSCRIPTION_ME}"
    KlikLogger.d("RemoteDataFetcher", "Fetching my subscription from: $url")

    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to fetch subscription. URL: $url")

    val dto = decodeJsonResponse<SubscriptionMeDto>(response, "subscription-me")
    return dto.toDomain()
  }

  /**
   * Fetch feature flags for current user's subscription tier.
   * GET /features (auth required)
   */
  suspend fun fetchSubscriptionFeatures(): SubscriptionFeatures {
    val url = "${ApiConfig.SUBSCRIPTION_BASE_URL}${ApiConfig.Endpoints.SUBSCRIPTION_FEATURES}"
    KlikLogger.d("RemoteDataFetcher", "Fetching subscription features from: $url")

    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to fetch subscription features. URL: $url")

    val dto = decodeJsonResponse<SubscriptionFeaturesDto>(response, "subscription-features")
    return dto.toDomain()
  }

  /**
   * Fetch detailed usage for current billing period.
   * GET /usage (auth required)
   */
  suspend fun fetchSubscriptionUsage(): UsageDetails {
    val url = "${ApiConfig.SUBSCRIPTION_BASE_URL}${ApiConfig.Endpoints.SUBSCRIPTION_USAGE}"
    KlikLogger.d("RemoteDataFetcher", "Fetching subscription usage from: $url")

    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to fetch subscription usage. URL: $url")

    val dto = decodeJsonResponse<UsageDetailsDto>(response, "subscription-usage")
    return dto.toDomain()
  }

  /**
   * Upgrade subscription to a higher tier.
   * POST /upgrade (auth required)
   */
  suspend fun upgradeSubscription(planCode: String, billingCycle: String): SubscriptionChangeResponseDto {
    val url = "${ApiConfig.SUBSCRIPTION_BASE_URL}${ApiConfig.Endpoints.SUBSCRIPTION_UPGRADE}"
    val body = json.encodeToString(
      SubscriptionChangeRequestDto.serializer(),
      SubscriptionChangeRequestDto(planCode, billingCycle),
    )
    KlikLogger.d("RemoteDataFetcher", "Upgrading subscription to $planCode ($billingCycle)")

    val response = HttpClient.postUrl(url, body)
      ?: throw IllegalStateException("Failed to upgrade subscription. URL: $url")

    return decodeJsonResponse<SubscriptionChangeResponseDto>(response, "subscription-upgrade")
  }

  /**
   * Downgrade subscription to a lower tier.
   * POST /downgrade (auth required)
   */
  suspend fun downgradeSubscription(planCode: String, billingCycle: String): SubscriptionChangeResponseDto {
    val url = "${ApiConfig.SUBSCRIPTION_BASE_URL}${ApiConfig.Endpoints.SUBSCRIPTION_DOWNGRADE}"
    val body = json.encodeToString(
      SubscriptionChangeRequestDto.serializer(),
      SubscriptionChangeRequestDto(planCode, billingCycle),
    )
    KlikLogger.d("RemoteDataFetcher", "Downgrading subscription to $planCode ($billingCycle)")

    val response = HttpClient.postUrl(url, body)
      ?: throw IllegalStateException("Failed to downgrade subscription. URL: $url")

    return decodeJsonResponse<SubscriptionChangeResponseDto>(response, "subscription-downgrade")
  }

  // ==================== Consent API (COMPLIANCE_BASE_URL → /api/compliance/*) ====================

  suspend fun getConsentStatus(consentType: String): ConsentStatusResponse {
    val url = "${ApiConfig.COMPLIANCE_BASE_URL}${ApiConfig.Endpoints.CONSENT_STATUS}/$consentType"
    KlikLogger.d("RemoteDataFetcher", "Checking consent status for: $consentType")
    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to get consent status. URL: $url")
    return decodeJsonResponse<ConsentStatusResponse>(response, "consent-status-$consentType")
  }

  suspend fun getRecordingConsentStatus(): ConsentStatusResponse = getConsentStatus(ApiConfig.Endpoints.CONSENT_TYPE_RECORDING)

  suspend fun submitRecordingConsent(): ConsentResponse {
    val url = "${ApiConfig.COMPLIANCE_BASE_URL}${ApiConfig.Endpoints.CONSENT_SUBMIT}"
    val body = buildJsonObject {
      put("consent_type", ApiConfig.Endpoints.CONSENT_TYPE_RECORDING)
      put("consent_version", "1.0")
    }.toString()
    KlikLogger.d("RemoteDataFetcher", "Submitting recording consent")
    val response = HttpClient.postUrl(url, body)
      ?: throw IllegalStateException("Failed to submit recording consent. URL: $url")
    return decodeJsonResponse<ConsentResponse>(response, "consent-recording-submit")
  }

  suspend fun revokeRecordingConsent(): ConsentResponse {
    val url = "${ApiConfig.COMPLIANCE_BASE_URL}${ApiConfig.Endpoints.CONSENT_REVOKE}/${ApiConfig.Endpoints.CONSENT_TYPE_RECORDING}"
    KlikLogger.d("RemoteDataFetcher", "Revoking recording consent")
    val response = HttpClient.deleteUrl(url)
      ?: throw IllegalStateException("Failed to revoke recording consent. URL: $url")
    return decodeJsonResponse<ConsentResponse>(response, "consent-recording-revoke")
  }

  suspend fun getBiometricConsentStatus(): ConsentStatusResponse = getConsentStatus(ApiConfig.Endpoints.CONSENT_TYPE_BIOMETRIC)

  suspend fun submitBiometricConsent(): ConsentResponse {
    val url = "${ApiConfig.COMPLIANCE_BASE_URL}${ApiConfig.Endpoints.CONSENT_SUBMIT}"
    val body = buildJsonObject {
      put("consent_type", ApiConfig.Endpoints.CONSENT_TYPE_BIOMETRIC)
      put("consent_version", "1.0")
      put(
        "metadata",
        buildJsonObject {
          put("written_notice_acknowledged", true)
        },
      )
    }.toString()
    KlikLogger.d("RemoteDataFetcher", "Submitting biometric consent")
    val response = HttpClient.postUrl(url, body)
      ?: throw IllegalStateException("Failed to submit biometric consent. URL: $url")
    return decodeJsonResponse<ConsentResponse>(response, "consent-biometric-submit")
  }

  suspend fun revokeBiometricConsent(): ConsentResponse {
    val url = "${ApiConfig.COMPLIANCE_BASE_URL}${ApiConfig.Endpoints.CONSENT_REVOKE}/${ApiConfig.Endpoints.CONSENT_TYPE_BIOMETRIC}"
    KlikLogger.d("RemoteDataFetcher", "Revoking biometric consent")
    val response = HttpClient.deleteUrl(url)
      ?: throw IllegalStateException("Failed to revoke biometric consent. URL: $url")
    return decodeJsonResponse<ConsentResponse>(response, "consent-biometric-revoke")
  }

  // ==================== Voiceprint API ====================

  suspend fun getVoiceprints(userId: String): List<VoiceprintDto> {
    val url = "${ApiConfig.COMPLIANCE_BASE_URL}${ApiConfig.Endpoints.VOICEPRINTS}/$userId"
    KlikLogger.d("RemoteDataFetcher", "Fetching voiceprints for user")
    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to get voiceprints. URL: $url")
    return decodeJsonResponse<List<VoiceprintDto>>(response, "voiceprints")
  }

  suspend fun deleteVoiceprint(voiceprintId: String): ConsentResponse {
    val url = "${ApiConfig.COMPLIANCE_BASE_URL}${ApiConfig.Endpoints.VOICEPRINT_DELETE}/$voiceprintId"
    KlikLogger.d("RemoteDataFetcher", "Deleting voiceprint: $voiceprintId")
    val response = HttpClient.deleteUrl(url)
      ?: throw IllegalStateException("Failed to delete voiceprint. URL: $url")
    return decodeJsonResponse<ConsentResponse>(response, "voiceprint-delete")
  }

  // ==================== Privacy Settings API (COMPLIANCE_BASE_URL) ====================

  suspend fun getPrivacySettings(): PrivacySettingsDto {
    val url = "${ApiConfig.COMPLIANCE_BASE_URL}${ApiConfig.Endpoints.PRIVACY_SETTINGS}"
    KlikLogger.d("RemoteDataFetcher", "Fetching privacy settings")
    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to get privacy settings. URL: $url")
    return decodeJsonResponse<PrivacySettingsDto>(response, "privacy-settings")
  }

  suspend fun updatePrivacySettings(settings: PrivacySettingsDto): ConsentResponse {
    val url = "${ApiConfig.COMPLIANCE_BASE_URL}${ApiConfig.Endpoints.PRIVACY_SETTINGS}"
    val body = json.encodeToString(PrivacySettingsDto.serializer(), settings)
    KlikLogger.d("RemoteDataFetcher", "Updating privacy settings")
    val response = HttpClient.putUrl(url, body)
      ?: throw IllegalStateException("Failed to update privacy settings. URL: $url")
    return decodeJsonResponse<ConsentResponse>(response, "privacy-settings-update")
  }

  suspend fun requestDataExport(format: String): ConsentResponse {
    val url = "${ApiConfig.COMPLIANCE_BASE_URL}${ApiConfig.Endpoints.PRIVACY_DATA_EXPORT}"
    val body = buildJsonObject {
      put("format", format)
    }.toString()
    KlikLogger.d("RemoteDataFetcher", "Requesting data export in $format format")
    val response = HttpClient.postUrl(url, body)
      ?: throw IllegalStateException("Failed to request data export. URL: $url")
    return decodeJsonResponse<ConsentResponse>(response, "privacy-export-request")
  }

  suspend fun submitPrivacyRequest(requestType: String): ConsentResponse {
    val url = "${ApiConfig.COMPLIANCE_BASE_URL}${ApiConfig.Endpoints.PRIVACY_REQUEST}"
    val body = buildJsonObject {
      put("request_type", requestType)
    }.toString()
    KlikLogger.d("RemoteDataFetcher", "Submitting privacy request: $requestType")
    val response = HttpClient.postUrl(url, body)
      ?: throw IllegalStateException("Failed to submit privacy request. URL: $url")
    return decodeJsonResponse<ConsentResponse>(response, "privacy-request-$requestType")
  }

  suspend fun getPrivacyRequests(): List<PrivacyRequestDto> {
    val url = "${ApiConfig.COMPLIANCE_BASE_URL}${ApiConfig.Endpoints.PRIVACY_REQUESTS}"
    KlikLogger.d("RemoteDataFetcher", "Fetching privacy requests")
    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to get privacy requests. URL: $url")
    return decodeJsonResponse<List<PrivacyRequestDto>>(response, "privacy-requests")
  }

  // ==================== Account Security API ====================

  suspend fun changePassword(currentPassword: String, newPassword: String): ConsentResponse {
    val url = "${ApiConfig.AUTH_BASE_URL}${ApiConfig.Endpoints.CHANGE_PASSWORD}"
    val body = buildJsonObject {
      put("old_password", currentPassword)
      put("new_password", newPassword)
    }.toString()
    KlikLogger.d("RemoteDataFetcher", "Changing password")
    val response = HttpClient.postUrl(url, body)
      ?: throw IllegalStateException("Failed to change password. URL: $url")
    return decodeJsonResponse<ConsentResponse>(response, "change-password")
  }

  // ==================== Notification Preferences API ====================

  suspend fun updateNotificationPreferences(
    meetingReminders: Boolean,
    taskUpdates: Boolean,
    insightsDigest: Boolean,
    pushEnabled: Boolean,
  ): ConsentResponse {
    val url = "${ApiConfig.AUTH_BASE_URL}${ApiConfig.Endpoints.NOTIFICATION_PREFERENCES}"
    val body = buildJsonObject {
      put("meeting_reminders", meetingReminders)
      put("task_updates", taskUpdates)
      put("insights_digest", insightsDigest)
      put("push_enabled", pushEnabled)
    }.toString()
    KlikLogger.d("RemoteDataFetcher", "Updating notification preferences")
    val response = HttpClient.putUrl(url, body)
      ?: throw IllegalStateException("Failed to update notification preferences. URL: $url")
    return decodeJsonResponse<ConsentResponse>(response, "notification-preferences")
  }

  suspend fun getNotificationPreferences(): NotificationPreferencesDto {
    val url = "${ApiConfig.AUTH_BASE_URL}${ApiConfig.Endpoints.NOTIFICATION_PREFERENCES}"
    KlikLogger.d("RemoteDataFetcher", "Fetching notification preferences")
    val response = HttpClient.getUrl(url)
      ?: throw IllegalStateException("Failed to get notification preferences. URL: $url")
    return decodeJsonResponse<NotificationPreferencesDto>(response, "notification-preferences")
  }

  // ==================== User Preferences (cross-device sync) ====================

  /** Read the server-side preferences row. Backend merges defaults if no row yet. */
  suspend fun fetchUserPreferences(): RemoteUserPreferencesDto {
    val response = HttpClient.get(ApiConfig.Endpoints.USER_PREFERENCES)
      ?: throw IllegalStateException("Failed to fetch user preferences from backend.")
    return decodeJsonResponse<RemoteUserPreferencesDto>(response, "user_preferences")
  }

  /** Upsert the full preferences row. Returns the server's view after persist. */
  suspend fun updateUserPreferences(prefs: RemoteUserPreferencesDto): RemoteUserPreferencesDto {
    val url = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.USER_PREFERENCES}"
    val body = json.encodeToString(RemoteUserPreferencesDto.serializer(), prefs)
    val response = HttpClient.putUrl(url, body)
      ?: throw IllegalStateException("Failed to update user preferences.")
    return decodeJsonResponse<RemoteUserPreferencesDto>(response, "user_preferences_updated")
  }

  // ==================== Achievements (computed server-side) ====================

  /** Fetch the achievements catalog with locked/unlocked + progress for current user. */
  suspend fun fetchAchievementsList(): AchievementsListDto {
    val response = HttpClient.get(ApiConfig.Endpoints.ACHIEVEMENTS)
      ?: throw IllegalStateException("Failed to fetch achievements from backend.")
    return decodeJsonResponse<AchievementsListDto>(response, "achievements")
  }
}

// ==================== Response DTOs ====================

@Serializable
data class MeetingsResponse(
  val items: List<MeetingDto> = emptyList(),
  val total: Int = 0,
)

@Serializable
data class TasksResponse(
  val items: List<TaskDto> = emptyList(),
  val total: Int = 0,
)

@Serializable
data class PeopleResponse(
  val items: List<PersonDto> = emptyList(),
  val total: Int = 0,
)

@Serializable
data class OrganizationsResponse(
  val items: List<OrganizationDto> = emptyList(),
  val total: Int = 0,
)

@Serializable
data class ProjectsResponse(
  val items: List<ProjectDto> = emptyList(),
  val total: Int = 0,
)

@Serializable
data class ChatMessagesResponse(
  val items: List<ChatMessageDto> = emptyList(),
  val total: Int = 0,
)

@Serializable
data class SuggestionsResponse(
  val suggestions: List<SuggestionDto> = emptyList(),
)

@Serializable
data class SuggestionDto(
  val text: String = "",
  val category: String = "",
)

@Serializable
data class ScenariosResponseWrapper(
  val scenarios: List<ScenarioDto> = emptyList(),
)

@Serializable
data class ScenarioDto(
  val id: String = "",
  val name: String = "",
  val description: String = "",
  val category: String = "",
) {
  fun toDomain(): Scenario {
    // Map category to a color (you can customize this mapping)
    val colorHex = when (category.lowercase()) {
      "productivity", "work" -> "#4A90E2"
      "collaboration", "meeting" -> "#7B68EE"
      "learning", "growth" -> "#50C878"
      "communication" -> "#FFB347"
      else -> "#9B9B9B"
    }
    return Scenario(
      title = name,
      count = 0, // Backend doesn't provide count, defaulting to 0
      colorHex = colorHex,
    )
  }
}

// ==================== API DTOs ====================

@Serializable
data class ActionItemDto(
  val id: String = "",
  val title: String = "",
  val description: String = "",
  val completed: Boolean = false,
)

@Serializable
data class MeetingItemDto(
  val id: String = "",
  val content: String = "",
  val speaker: String? = null,
  val timestamp: String? = null,
)

@Serializable
data class MeetingDto(
  val id: String,
  val title: String = "", // summary_title from database
  val date: String = "",
  val time: String = "",
  val participants: List<PersonDto> = emptyList(),
  val summary: String? = null, // summary_brief from database
  val summaryDetailed: String? = null, // summary_detailed from database
  val decisions: List<MeetingItemDto> = emptyList(), // from meeting_decisions
  val discussions: List<MeetingItemDto> = emptyList(), // from meeting_discussions
  val milestones: List<MeetingItemDto> = emptyList(), // from meeting_milestones
  val actionItems: List<ActionItemDto> = emptyList(),
  val transcript: String? = null, // from optimized_transcripts
  val isPinned: Boolean = false,
  val isArchived: Boolean = false,
  val dropboxUrl: String? = null,
  // session_id from the recording pipeline — needed to link KK_exec todos which
  // are keyed by SESSION_… ids, not by the meetings table primary key.
  @SerialName("session_id") val sessionId: String? = null,
) {
  fun toDomain(): Meeting {
    val localDate = try {
      val parsed = kotlinx.datetime.LocalDate.parse(date)
      KlikLogger.d("MeetingDto", "Parsed date '$date' -> $parsed for meeting '$title' (id=$id)")
      parsed
    } catch (e: Exception) {
      throw IllegalArgumentException(
        "Invalid date format '$date' for meeting '$title' (id=$id). " +
          "Backend must provide dates in ISO-8601 format (YYYY-MM-DD). Error: ${e.message}",
      )
    }
    return Meeting(
      id = id,
      title = title,
      date = localDate,
      time = time,
      participants = participants.map { it.toDomain() },
      summary = summary ?: "",
      actionItems = actionItems.mapIndexed { index, item ->
        TodoItem(
          id = item.id.ifEmpty { "$id-action-$index" },
          text = item.title,
          isCompleted = item.completed,
          type = TodoType.TODO,
        )
      },
      minutes = buildMinutes(),
      transcript = transcript,
      isPinned = isPinned,
      isArchived = isArchived,
      dropboxUrl = dropboxUrl,
      sessionId = sessionId,
    )
  }

  private fun buildMinutes(): List<MeetingMinute> {
    val minutes = mutableListOf<MeetingMinute>()

    // Add detailed summary as first item if available
    if (!summaryDetailed.isNullOrBlank()) {
      minutes.add(
        MeetingMinute(
          category = "Summary",
          items = listOf(summaryDetailed),
        ),
      )
    }

    // Add decisions
    if (decisions.isNotEmpty()) {
      minutes.add(
        MeetingMinute(
          category = "Decisions",
          items = decisions.map { it.content },
        ),
      )
    }

    // Add discussions
    if (discussions.isNotEmpty()) {
      minutes.add(
        MeetingMinute(
          category = "Discussions",
          items = discussions.map {
            if (it.speaker != null) {
              "${it.speaker}: ${it.content}"
            } else {
              it.content
            }
          },
        ),
      )
    }

    // Add milestones
    if (milestones.isNotEmpty()) {
      minutes.add(
        MeetingMinute(
          category = "Milestones",
          items = milestones.map { it.content },
        ),
      )
    }

    return minutes
  }
}

/**
 * DTO for dimension scores returned by the backend API.
 * Backend returns: {"dimension": "clarity", "score": 76.54, "details": {...}, "periodType": "weekly", "periodDate": "2025-06-15"}
 */
@Serializable
data class DimensionScoreApiDto(
  val dimension: String = "",
  val score: Float? = null,
  val details: JsonElement? = null,
  val periodType: String = "",
  val periodDate: String = "",
) {
  fun toDomain(entityType: String, entityId: String, userId: String): DimensionScore = DimensionScore(
    id = 0, // Not provided by API, generate if needed
    entityType = entityType,
    entityId = entityId,
    dimension = dimension,
    score = score,
    details = details?.toString() ?: "{}",
    periodType = periodType,
    periodDate = periodDate,
    userId = userId,
    createdAt = "", // Not provided by API
    updatedAt = "", // Not provided by API
  )
}

@Serializable
data class PersonDto(
  val id: String,
  val name: String = "",
  val canonicalName: String = "",
  val role: String = "",
  val avatarUrl: String? = null,
  val email: String = "",
  val phone: String = "",
  val relatedProjects: List<String> = emptyList(),
  val relatedOrganizations: List<String> = emptyList(),
  val relatedMeetings: List<String> = emptyList(),
  val characteristics: List<String> = emptyList(),
  val lastInteraction: String = "",
  val skills: List<String> = emptyList(),
  val department: String? = null,
  val organizationId: String? = null,
  val aliases: List<String> = emptyList(),
  val dimensions: List<DimensionScoreApiDto> = emptyList(),
) {
  fun toDomain(userId: String = ""): Person = Person(
    id = id,
    name = name,
    canonicalName = canonicalName,
    role = role,
    avatarUrl = avatarUrl,
    email = email,
    phone = phone,
    relatedProjects = relatedProjects,
    relatedOrganizations = relatedOrganizations,
    relatedMeetings = relatedMeetings,
    characteristics = characteristics,
    lastInteraction = lastInteraction,
    skills = skills,
    department = department,
    organizationId = organizationId,
    aliases = aliases,
    dimensions = dimensions.map { it.toDomain("people", id, userId) },
  )
}

@Serializable
data class TaskDto(
  val id: String,
  val title: String = "",
  val subtitle: String = "",
  val context: String = "",
  val relatedProject: String? = null,
  val relatedPeople: List<String> = emptyList(),
  val dueInfo: String = "",
  val priority: String = "MEDIUM",
  val status: String = "PENDING",
  val needsConfirmation: Boolean = false,
  val isPinned: Boolean = false,
  val isArchived: Boolean = false,
  val session_id: String? = null,
  val segment_ids: List<Int> = emptyList(),
  val relatedOrganizations: List<String> = emptyList(),
  val relatedProjects: List<String> = emptyList(),
) {
  fun toDomain(): TaskMetadata = TaskMetadata(
    id = id,
    title = title,
    subtitle = subtitle,
    context = context,
    relatedProject = relatedProject ?: "",
    relatedPeople = relatedPeople,
    dueInfo = dueInfo,
    priority = TaskPriority.fromString(priority),
    status = when (status.uppercase()) {
      "PENDING", "TODO" -> TaskStatus.PENDING
      "IN_PROGRESS", "INPROGRESS" -> TaskStatus.IN_PROGRESS
      "IN_REVIEW", "INREVIEW" -> TaskStatus.IN_REVIEW
      "COMPLETED", "DONE" -> TaskStatus.COMPLETED
      "ARCHIVED" -> TaskStatus.ARCHIVED
      "APPROVED" -> TaskStatus.APPROVED
      "REJECTED" -> TaskStatus.REJECTED
      "REQUIRES_REAUTH" -> TaskStatus.REQUIRES_REAUTH
      else -> TaskStatus.PENDING
    },
    needsConfirmation = needsConfirmation,
    isPinned = isPinned,
    isArchived = isArchived,
    relatedMeetingId = session_id,
    relatedSegments = segment_ids,
    relatedOrganizations = relatedOrganizations,
    relatedProjects = relatedProjects,
  )
}

@Serializable
data class OrganizationDto(
  val id: String,
  val name: String = "",
  val canonicalName: String = "",
  val type: String? = null,
  val country: String? = null,
  val status: String = "active",
  val industry: String = "",
  val sizeHeadcount: Int? = null,
  val legalName: String? = null,
  val aliases: List<String> = emptyList(),
  val industryDomains: List<String> = emptyList(),
  val departments: List<String> = emptyList(),
  val relationshipScore: Int = 50,
  val relatedSessions: List<String> = emptyList(),
  val employees: List<String> = emptyList(),
  val relatedProjects: List<String> = emptyList(),
  val strategicFocus: String = "",
  val nextAction: String = "",
  val isPinned: Boolean = false,
  val dimensions: List<DimensionScoreApiDto> = emptyList(),
) {
  fun toDomain(userId: String = ""): Organization = Organization(
    id = id,
    name = name,
    canonicalName = canonicalName.ifEmpty { name },
    type = type,
    country = country,
    status = status,
    industry = industry,
    sizeHeadcount = sizeHeadcount,
    legalName = legalName,
    aliases = aliases,
    industryDomains = industryDomains,
    departments = departments,
    relatedSessions = relatedSessions,
    employees = employees,
    relatedProjects = relatedProjects,
    strategicFocus = strategicFocus,
    nextAction = nextAction,
    isPinned = isPinned,
    dimensions = dimensions.map { it.toDomain("organization", id, userId) },
  )
}

/**
 * DTO for risk data from meeting_risks table.
 */
@Serializable
data class RiskDto(
  val id: String? = null,
  val severity: String? = null,
  val description: String? = null,
  val mitigation: String? = null,
)

@Serializable
data class ProjectDto(
  val id: String,
  val name: String = "",
  val canonicalName: String = "",
  val type: String? = null,
  val status: String = "",
  val progress: Float = 0f,
  val lead: String = "",
  val team: List<String> = emptyList(),
  val aliases: List<String> = emptyList(),
  val stage: String = "",
  val budget: String = "",
  val isPinned: Boolean = false,
  val dimensions: List<DimensionScoreApiDto> = emptyList(),
  val goals: List<String> = emptyList(),
  val kpis: List<String> = emptyList(),
  val risks: List<RiskDto> = emptyList(),
  val relatedPeople: List<String> = emptyList(),
  val relatedOrganizations: List<String> = emptyList(),
  val relatedProjects: List<String> = emptyList(),
) {
  fun toDomain(userId: String = ""): Project = Project(
    id = id,
    name = name.ifEmpty { canonicalName },
    canonicalName = canonicalName,
    status = ProjectStatus.fromString(status),
    progress = progress,
    trend = ProjectTrend.STABLE,
    lead = lead,
    teamMembers = team,
    stage = stage,
    goals = goals,
    kpis = kpis,
    risks = risks.mapNotNull { it.description },
    budget = budget,
    isPinned = isPinned,
    dimensions = dimensions.map { it.toDomain("project", id, userId) },
    relatedProjects = relatedProjects,
    relatedPeople = relatedPeople,
    relatedOrganizations = relatedOrganizations,
  )
}

@Serializable
data class ChatMessageDto(
  val id: String,
  val text: String = "",
  val isUser: Boolean = false,
  val timestamp: Long = 0,
) {
  fun toDomain(): ChatMessage = ChatMessage(
    id = id,
    text = text,
    isUser = isUser,
    timestamp = timestamp,
  )
}

/**
 * DTO for KLIK Insights API response.
 * API returns: { "insights": [...], "sessions_analyzed": 8, "language": "en" }
 */
@Serializable
data class TracedSegmentDto(
  val session_id: String,
  val segment_id: String,
  val text: String,
  val score: Float,
  val speaker_name: String? = null,
  val start_time: Float = 0f,
  val end_time: Float = 0f,
  val meeting_title: String? = null,
  val meeting_time: String? = null,
  val matched_keywords: List<String> = emptyList(),
)

@Serializable
data class InsightsApiResponseDto(
  val insights: List<String> = emptyList(),
  val sessions_analyzed: Int = 0,
  val language: String = "en",
  val traced_segments: List<TracedSegmentDto> = emptyList(),
) {
  fun toDomain(): Insights = Insights(
    // Join all insights into a single summary text
    summary = insights.joinToString("\n\n"),
    highlights = insights,
    recommendations = emptyList(),
    tracedSegments = traced_segments.map { segment ->
      io.github.fletchmckee.liquid.samples.app.domain.entity.TracedSegment(
        sessionId = segment.session_id,
        segmentId = segment.segment_id,
        text = segment.text,
        score = segment.score,
        speakerName = segment.speaker_name,
        meetingTitle = segment.meeting_title,
        meetingTime = segment.meeting_time,
        matchedKeywords = segment.matched_keywords,
      )
    },
    generatedAt = Clock.System.now().toEpochMilliseconds(),
  )
}

@Serializable
data class UserDto(
  val id: String,
  val name: String = "",
  val email: String = "",
  val avatarUrl: String? = null,
  val planType: String,
) {
  fun toDomain(): User {
    val initials = name.split(" ")
      .mapNotNull { it.firstOrNull()?.uppercaseChar() }
      .take(2)
      .joinToString("")
      .ifEmpty { "?" }

    return User(
      id = id,
      name = name,
      email = email,
      initials = initials,
      planType = PlanType.fromTierCode(planType),
      avatarUrl = avatarUrl,
    )
  }
}

/**
 * DTO for Encourage API response.
 * API returns: { "message": "...", "active_sessions_count": 3, "language": "en" }
 */
@Serializable
data class EncourageApiResponseDto(
  val message: String = "",
  val active_sessions_count: Int = 0,
  val language: String = "en",
  val traced_segments: List<TracedSegmentDto> = emptyList(),
) {
  fun toDomain(): EncourageData = EncourageData(
    message = message,
    activeSessionsCount = active_sessions_count,
    language = language,
    tracedSegments = traced_segments.map { segment ->
      io.github.fletchmckee.liquid.samples.app.domain.entity.TracedSegment(
        sessionId = segment.session_id,
        segmentId = segment.segment_id,
        text = segment.text,
        score = segment.score,
        speakerName = segment.speaker_name,
        meetingTitle = segment.meeting_title,
        meetingTime = segment.meeting_time,
        matchedKeywords = segment.matched_keywords,
      )
    },
    generatedAt = Clock.System.now().toEpochMilliseconds(),
  )
}

/**
 * DTO for Worklife API response.
 * API returns: { "insights": [...], "projects_found": 3, "relationships_found": 15, "language": "en" }
 */
@Serializable
data class WorklifeApiResponseDto(
  val insights: List<String> = emptyList(),
  val projects_found: Int = 0,
  val relationships_found: Int = 0,
  val organizations_found: Int = 0,
  val people_found: Int = 0,
  val language: String = "en",
  val traced_segments: List<TracedSegmentDto> = emptyList(),
) {
  fun toDomain(): WorklifeData = WorklifeData(
    insights = insights,
    projectsFound = projects_found,
    relationshipsFound = relationships_found,
    organizationsFound = organizations_found,
    peopleFound = people_found,
    language = language,
    tracedSegments = traced_segments.map { segment ->
      io.github.fletchmckee.liquid.samples.app.domain.entity.TracedSegment(
        sessionId = segment.session_id,
        segmentId = segment.segment_id,
        text = segment.text,
        score = segment.score,
        speakerName = segment.speaker_name,
        meetingTitle = segment.meeting_title,
        meetingTime = segment.meeting_time,
        matchedKeywords = segment.matched_keywords,
      )
    },
    generatedAt = Clock.System.now().toEpochMilliseconds(),
  )
}

/**
 * Domain model for Encourage data (highlighted summary).
 */
data class EncourageData(
  val message: String,
  val activeSessionsCount: Int,
  val language: String,
  val tracedSegments: List<io.github.fletchmckee.liquid.samples.app.domain.entity.TracedSegment> = emptyList(),
  val generatedAt: Long,
)

/**
 * Domain model for Worklife data (full content insights).
 */
data class WorklifeData(
  val insights: List<String>,
  val projectsFound: Int,
  val relationshipsFound: Int,
  val organizationsFound: Int,
  val peopleFound: Int,
  val language: String,
  val tracedSegments: List<io.github.fletchmckee.liquid.samples.app.domain.entity.TracedSegment> = emptyList(),
  val generatedAt: Long,
)

// ==================== KK_exec Todo DTOs ====================

/**
 * Response wrapper for KK_exec todos list.
 * user_id is at the response level, not per-item.
 */
@Serializable
data class KKExecTodosResponse(
  val user_id: String = "",
  val items: List<KKExecTodoItemDto> = emptyList(),
  val total: Int = 0,
)

/**
 * Owner/Assigner reference DTO.
 */
@Serializable
data class KKExecOwnerDto(
  val name: String? = null,
  val voiceprint_id: String? = null,
)

/**
 * Entity references extracted by LLM.
 */
@Serializable
data class KKExecEntityRefsDto(
  val people: List<String> = emptyList(),
  val projects: List<String> = emptyList(),
  val organizations: List<String> = emptyList(),
)

/**
 * Execution specification from backend.
 * Shape: { "providers": ["slack", "system", ...] }
 */
@Serializable
data class KKExecSpecDto(
  val providers: List<String> = emptyList(),
)

/**
 * DTO for task execution result from KK_exec.
 * Maps to the JSONB `result` column in todo_execution_results table.
 *
 * `result` is JsonElement because the backend uses two shapes:
 *   - normal completion → JSON string (the agent's output text)
 *   - status="requires_reauth" → JSON object {"message", "provider", "reason"}
 *     (set by KK_exec/src/api/routes/todos.py when the reactive 401 path
 *     decides a credential is dead — see backend commit 58a2c76f)
 *
 * Old DTO had `result: String?` which would crash on the dict shape with
 * SerializationException. Use [extractResultText] / [extractReauthInfo] to
 * read the field safely.
 */
@Serializable
data class KKExecTaskResultDto(
  val result: JsonElement? = null,
  val status: String? = null,
  val message: String? = null,
  val agent_outputs: List<JsonElement> = emptyList(),
) {
  /**
   * Plain string view of `result` for normal-completion todos. Returns null
   * when `result` is the requires_reauth object shape (use [extractReauthInfo]
   * for that case).
   */
  fun extractResultText(): String? {
    val r = result ?: return null
    return (r as? JsonPrimitive)?.takeIf { it.isString }?.content
  }

  /**
   * Parse the `{"provider", "reason"}` payload the backend stamps onto
   * `task_result.result` when a todo's execution status is `requires_reauth`.
   * Returns null for any other shape.
   */
  fun extractReauthInfo(): ReauthInfo? {
    val r = result ?: return null
    val obj = (r as? JsonObject) ?: return null
    val provider = obj["provider"]?.let { it as? JsonPrimitive }?.content ?: return null
    val reason = obj["reason"]?.let { it as? JsonPrimitive }?.content
    return ReauthInfo(provider = provider, reason = reason)
  }

  /**
   * Extract the execution outcome summary from agent_outputs.
   * Prioritizes the execute agent's output (direct action result),
   * then falls back to explore agent's summary field.
   * Returns null if no structured outcome is available.
   */
  fun extractOutcomeSummary(): String? {
    if (agent_outputs.isEmpty()) return null
    try {
      // Look for execute agent first (direct action outcomes like "Sent email", "Posted message")
      for (elem in agent_outputs) {
        val obj = elem.jsonObject
        val agent = obj["agent"]?.jsonPrimitive?.content ?: continue
        val success = obj["success"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
        val output = obj["output"]?.jsonPrimitive?.content ?: continue
        if (agent == "execute" && success) {
          return output
        }
      }
      // Fall back to explore agent's structured summary
      for (elem in agent_outputs) {
        val obj = elem.jsonObject
        val agent = obj["agent"]?.jsonPrimitive?.content ?: continue
        val success = obj["success"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: true
        val output = obj["output"]?.jsonPrimitive?.content ?: continue
        if (agent == "explore" && success) {
          // Explore agent output is JSON with a "summary" field
          val parsed = try {
            Json.parseToJsonElement(output).jsonObject
          } catch (_: Exception) {
            null
          }
          if (parsed != null) {
            return parsed["summary"]?.jsonPrimitive?.content
          }
          return output
        }
      }
    } catch (_: Exception) {
      // Malformed agent_outputs, fall through
    }
    return null
  }

  /**
   * Extract source links from agent_outputs (explore agent's "sources" field).
   */
  fun extractOutcomeLinks(): List<String> {
    if (agent_outputs.isEmpty()) return emptyList()
    try {
      for (elem in agent_outputs) {
        val obj = elem.jsonObject
        val output = obj["output"]?.jsonPrimitive?.content ?: continue
        val parsed = try {
          Json.parseToJsonElement(output).jsonObject
        } catch (_: Exception) {
          null
        }
          ?: continue
        val sources = parsed["sources"]?.jsonArray ?: continue
        return sources.mapNotNull { (it as? JsonPrimitive)?.content }
      }
    } catch (_: Exception) {
      // Malformed, fall through
    }
    return emptyList()
  }
}

/**
 * DTO for todo items from KK_exec/KK_todo_extract.
 * Maps to the todo_items table in PostgreSQL.
 *
 * Categories:
 * - a_simple: Simple tasks using built-in tools (auto-executable)
 * - b_apis: External APIs with system keys (auto-executable)
 * - c_complex_level1: User OAuth integrations (auto-executable with OAuth)
 * - d_complex_level2: OAuth + memory context (auto-executable)
 * - e_complex_level3: OAuth + memory + sensitive ops (REQUIRES CONFIRMATION)
 * - f_cannotdo: Cannot execute (physical world, no integration)
 *
 * Note: The API returns id as String and session_id/user_id may be at wrapper level.
 */
@Serializable
data class KKExecTodoItemDto(
  val id: String, // API returns id as String, e.g. "119"
  val session_id: String? = null, // May be at wrapper level, not per-item
  val user_id: String? = null, // May be at wrapper level, not per-item
  val title: String = "",
  val description: String? = null,
  val category: String = "a_simple", // a_simple, b_apis, c_complex_level1, d_complex_level2, e_complex_level3, f_cannotdo
  val can_execute: Boolean = true,
  val out_of_scope_reason: String? = null,
  val is_sensitive: Boolean = false,
  // Note: backend only has is_sensitive: bool, no reasons array
  val owner: KKExecOwnerDto? = null,
  val assigner: KKExecOwnerDto? = null,
  val priority: String = "medium", // low, medium, high
  val deadline: String? = null, // YYYY-MM-DD
  val confidence: Float = 1.0f,
  val status: String = "pending", // pending, in_progress, completed, cancelled, failed
  val entity_refs: KKExecEntityRefsDto? = null,
  val exec_spec: KKExecSpecDto? = null,
  val segment_ids: List<Int> = emptyList(), // Segment IDs where this todo was mentioned
  val selected_sub_categories: List<String> = emptyList(), // Sub-categories for UI grouping (e.g., ["code_repository", "web_search"])
  val tool_category_group_id: String? = null, // SHA256[:16] hash for category grouping
  val execution_steps: List<KKExecExecutionStepDto> = emptyList(), // Execution steps from todo_execution_steps table
  val task_result: KKExecTaskResultDto? = null, // Execution result object from backend JSONB
  val tools_used: List<String> = emptyList(), // Tools used in execution (from backend)
  val execution_status: String? = null, // Execution status: pending/executing/completed/failed
  val created_at: String = "",
  val updated_at: String = "",
) {
  /**
   * Convert to domain model with user_id injected from wrapper.
   */
  fun toDomain(wrapperUserId: String = ""): KKExecTodoItem = KKExecTodoItem(
    id = id.toIntOrNull() ?: 0,
    sessionId = session_id ?: "",
    userId = user_id ?: wrapperUserId,
    title = title,
    description = description,
    category = KKExecCategory.fromString(category),
    canExecute = can_execute,
    outOfScopeReason = out_of_scope_reason,
    isSensitive = is_sensitive,
    sensitivityReasons = emptyList(), // Backend doesn't send reasons, only is_sensitive bool
    ownerName = owner?.name,
    ownerVoiceprintId = owner?.voiceprint_id,
    assignerName = assigner?.name,
    assignerVoiceprintId = assigner?.voiceprint_id,
    priority = KKExecPriority.fromString(priority),
    deadline = deadline,
    confidence = confidence,
    status = KKExecStatus.fromString(status),
    entityRefsPeople = entity_refs?.people ?: emptyList(),
    entityRefsProjects = entity_refs?.projects ?: emptyList(),
    entityRefsOrganizations = entity_refs?.organizations ?: emptyList(),
    execSpecProviders = exec_spec?.providers ?: emptyList(),
    segmentIds = segment_ids,
    toolCategoriesNeeded = selected_sub_categories,
    toolCategoryGroupId = tool_category_group_id,
    executionSteps = execution_steps.map { ExecutionStep.fromDto(it) },
    taskResult = task_result?.extractResultText(),
    executionOutcome = task_result?.extractOutcomeSummary(),
    executionLinks = task_result?.extractOutcomeLinks() ?: emptyList(),
    toolsUsed = tools_used,
    executionStatus = execution_status,
    reauthInfo = task_result?.extractReauthInfo(),
    createdAt = created_at,
    updatedAt = updated_at,
  )
}

/**
 * Execution result from KK_exec.
 */
@Serializable
data class KKExecExecutionResult(
  val todo_id: Int,
  val status: String = "pending", // pending, executing, completed, failed
  val result: String? = null,
  val error: String? = null,
  val executed_at: String? = null,
)

// ==================== KK_exec Approval DTOs ====================

/**
 * Response from POST /{session_id}/todo/{todo_id}/approve or /reject.
 * Approve now auto-executes and returns execution_result.
 * Shape: { todo_id, status, previous_status, reviewed_by, reviewed_at, reason, execution_result? }
 */
@Serializable
data class KKExecApprovalResult(
  val todo_id: String,
  val status: String, // approved, rejected
  val previous_status: String = "",
  val reviewed_by: String = "",
  val reviewed_at: String = "",
  val reason: String? = null,
  val execution_result: KKExecRetryExecutionResult? = null, // Present when approve triggers auto-execute
)

/**
 * Request body for approve endpoint.
 * Body: { "reason": "optional" }
 */
@Serializable
data class KKExecApproveRequest(
  val reason: String? = null,
)

/**
 * Request body for reject endpoint.
 * Body: { "reason": "optional" }
 */
@Serializable
data class KKExecRejectRequest(
  val reason: String? = null,
)

/**
 * Single item in a bulk review request.
 */
@Serializable
data class KKExecBulkReviewItem(
  val todo_id: Int,
  val action: String, // "approve" or "reject"
  val reason: String? = null,
)

/**
 * Request body for bulk review endpoint.
 * Body: { "items": [...] }
 */
@Serializable
data class KKExecBulkReviewRequest(
  val items: List<KKExecBulkReviewItem>,
)

/**
 * Response from bulk review endpoint.
 * Shape: { total, succeeded, failed, results: [...] }
 */
@Serializable
data class KKExecBulkReviewResult(
  val total: Int = 0,
  val succeeded: Int = 0,
  val failed: Int = 0,
  val results: List<KKExecBulkReviewItemResult> = emptyList(),
)

@Serializable
data class KKExecBulkReviewItemResult(
  val todo_id: String,
  val action: String = "", // approve, reject
  val status: String = "", // approved, rejected, error
  val success: Boolean = true,
  val error: String? = null,
)

// ==================== KK_exec Retry DTOs ====================

/**
 * Response from POST /{session_id}/todo/{todo_id}/retry.
 * Shape: { todo_id, status, previous_status, attempt_number, retry_limit, result }
 */
@Serializable
data class KKExecRetryResult(
  val todo_id: String, // Backend returns as string
  val status: String = "",
  val is_retry: Boolean = false,
  val previous_status: String = "",
  val attempt_number: Int = 1,
  val retry_limit: Int = 0,
  val result: KKExecRetryExecutionResult? = null,
)

@Serializable
data class KKExecRetryExecutionResult(
  val todo_id: String = "",
  val status: String = "",
  val result: JsonElement? = null,
  val error: String? = null,
  val executed_at: String? = null,
)

/**
 * Individual execution step from todo_execution_steps table.
 * Represents one tool call in the execution sequence.
 */
@Serializable
data class KKExecExecutionStepDto(
  val step_number: Int,
  val tool_name: String,
  val success: Boolean = true,
  val error_message: String? = null,
  val duration_ms: Int? = null,
  val output: kotlinx.serialization.json.JsonElement? = null, // JSONB output, for task_complete this has the result
)

/**
 * Domain model for execution step.
 */
data class ExecutionStep(
  val stepNumber: Int,
  val toolName: String,
  val success: Boolean,
  val errorMessage: String?,
  val durationMs: Int?,
  val output: String?, // Stringified output, used for task_complete result
) {
  companion object {
    fun fromDto(dto: KKExecExecutionStepDto): ExecutionStep = ExecutionStep(
      stepNumber = dto.step_number,
      toolName = dto.tool_name,
      success = dto.success,
      errorMessage = dto.error_message,
      durationMs = dto.duration_ms,
      output = dto.output?.toString(),
    )
  }
}

// ==================== KK_exec Domain Models ====================

/**
 * Category enum for KK_exec todos.
 * Determines execution requirements and sensitivity.
 */
enum class KKExecCategory {
  A_REMINDER, // Reminders, no execution needed
  A_TOOLS, // Simple tasks, built-in tools only
  B_APIS, // External APIs with system keys
  C_COMPLEX_LEVEL1, // User OAuth integrations
  D_COMPLEX_LEVEL2, // OAuth + memory context
  E_COMPLEX_LEVEL3, // OAuth + memory + SENSITIVE (requires confirmation)
  F_CANNOTDO, // Cannot execute (physical world)
  ;

  companion object {
    fun fromString(value: String): KKExecCategory = when (value.lowercase()) {
      "a_reminder" -> A_REMINDER
      "a_tools" -> A_TOOLS
      "b_apis" -> B_APIS
      "c_complex_level1" -> C_COMPLEX_LEVEL1
      "d_complex_level2" -> D_COMPLEX_LEVEL2
      "e_complex_level3" -> E_COMPLEX_LEVEL3
      "f_cannotdo" -> F_CANNOTDO
      else -> A_TOOLS
    }
  }

  /**
   * Whether this category requires user confirmation before execution.
   */
  val requiresConfirmation: Boolean
    get() = this == E_COMPLEX_LEVEL3

  /**
   * Whether this category can be executed automatically.
   */
  val isAutoExecutable: Boolean
    get() = this != F_CANNOTDO && this != E_COMPLEX_LEVEL3 && this != A_REMINDER
}

/**
 * Priority enum for KK_exec todos.
 */
enum class KKExecPriority {
  LOW,
  MEDIUM,
  HIGH,
  ;

  companion object {
    fun fromString(value: String): KKExecPriority = when (value.lowercase()) {
      "low" -> LOW
      "medium" -> MEDIUM
      "high" -> HIGH
      else -> MEDIUM
    }
  }
}

/**
 * Status enum for KK_exec todos.
 */
enum class KKExecStatus {
  PENDING,
  IN_REVIEW,
  RUNNING,
  EVALUATING,
  COMPLETED,
  FAILED,
  CANNOT_EXECUTE,
  APPROVED,
  REJECTED,
  ARCHIVED,

  /** Backend signal that an OAuth credential needs user reconnection
   *  before this todo can complete. Carries provider/reason in
   *  task_result.result; surfaced via ReauthInfo on the domain model. */
  REQUIRES_REAUTH,

  ;

  companion object {
    fun fromString(value: String): KKExecStatus = when (value.lowercase()) {
      "pending" -> PENDING
      "in_review" -> IN_REVIEW
      "running" -> RUNNING
      "evaluating" -> EVALUATING
      "completed" -> COMPLETED
      "failed" -> FAILED
      "cannot_execute" -> CANNOT_EXECUTE
      "approved" -> APPROVED
      "rejected" -> REJECTED
      "archived" -> ARCHIVED
      "requires_reauth" -> REQUIRES_REAUTH
      else -> PENDING
    }
  }
}

/**
 * Domain model for KK_exec todo items.
 * Used in the UI for displaying todos in the Function screen.
 */
data class KKExecTodoItem(
  val id: Int,
  val sessionId: String,
  val userId: String,
  val title: String,
  val description: String?,
  val category: KKExecCategory,
  val canExecute: Boolean,
  val outOfScopeReason: String?,
  val isSensitive: Boolean,
  val sensitivityReasons: List<String>,
  val ownerName: String?,
  val ownerVoiceprintId: String?,
  val assignerName: String?,
  val assignerVoiceprintId: String?,
  val priority: KKExecPriority,
  val deadline: String?,
  val confidence: Float,
  val status: KKExecStatus,
  val entityRefsPeople: List<String>,
  val entityRefsProjects: List<String>,
  val entityRefsOrganizations: List<String>,
  val execSpecProviders: List<String>,
  val segmentIds: List<Int>,
  val toolCategoriesNeeded: List<String>, // Tool categories for grouping
  val toolCategoryGroupId: String?, // SHA256[:16] hash for category grouping
  val executionSteps: List<ExecutionStep>, // Execution steps from todo_execution_steps
  val taskResult: String?, // Result from task_complete tool
  val executionOutcome: String?, // Structured execution outcome from agent_outputs
  val executionLinks: List<String>, // Links extracted from agent_outputs (sources)
  val toolsUsed: List<String>, // Tools used in execution
  val executionStatus: String?, // Execution status
  val reauthInfo: ReauthInfo? = null, // Non-null when execution status is requires_reauth
  val createdAt: String,
  val updatedAt: String,
) {
  /**
   * Convert to UI TaskMetadata for display in EventsScreen.
   * Returns the UI model TaskMetadata (from model package) for use in Compose screens.
   */
  fun toTaskMetadata(): UITaskMetadata = UITaskMetadata(
    id = id.toString(),
    title = title,
    subtitle = description ?: "",
    context = when {
      !canExecute -> outOfScopeReason ?: "Cannot execute"
      isSensitive -> "Requires confirmation"
      else -> "Auto-executable"
    },
    relatedProject = entityRefsProjects.firstOrNull() ?: "",
    relatedPeople = entityRefsPeople,
    dueInfo = deadline ?: "",
    priority = when (priority) {
      KKExecPriority.HIGH -> "High"
      KKExecPriority.MEDIUM -> "Normal"
      KKExecPriority.LOW -> "Low"
    },
    isPinned = false,
    description = description,
    // KK_exec integration fields
    kkExecCategory = category.name,
    kkExecCanExecute = canExecute,
    kkExecIsSensitive = isSensitive,
    kkExecSensitivityReasons = sensitivityReasons,
    // Prefer executionStatus (from execution result) over general status
    kkExecStatus = executionStatus?.uppercase() ?: status.name,
    kkExecSessionId = sessionId,
    kkExecTodoId = id,
    // Related entities
    relatedMeetingId = sessionId.takeIf { it.isNotBlank() },
    relatedSegments = segmentIds,
    relatedOrganizations = entityRefsOrganizations,
    relatedProjects = entityRefsProjects,
    toolCategoriesNeeded = toolCategoriesNeeded,
    toolCategoryGroupId = toolCategoryGroupId,
    // Execution progress fields
    executionSteps = executionSteps.map { step ->
      io.github.fletchmckee.liquid.samples.app.model.ExecutionStepUi(
        stepNumber = step.stepNumber,
        toolName = step.toolName,
        success = step.success,
        errorMessage = step.errorMessage,
        durationMs = step.durationMs,
        output = step.output,
      )
    },
    taskResult = taskResult ?: executionSteps.find { it.toolName == "task_complete" }?.output,
    executionOutcome = executionOutcome,
    executionLinks = executionLinks,
    // Use toolsUsed from execution if available, otherwise fall back to exec_spec providers
    plannedTools = toolsUsed.ifEmpty { execSpecProviders },
    // Use executionStatus if available, otherwise fall back to general status
    currentExecutingStep = if (executionStatus?.equals("executing", ignoreCase = true) == true ||
      status == KKExecStatus.RUNNING
    ) {
      executionSteps.size + 1
    } else {
      null
    },
    reauthInfo = reauthInfo,
    createdAt = createdAt,
  )
}
// Goal/Level DTOs

@Serializable
data class GoalListResponse(
  val goals: List<GoalDto>,
  val total: Int,
  val limit: Int,
  val offset: Int,
)

@Serializable
data class GoalDto(
  val id: Int,
  @SerialName("user_id") val userId: String,
  val goal: String,
  val category: String,
  @SerialName("timeline_months") val timelineMonths: Int,
  val confidence: Float,
  val status: String,
  @SerialName("current_progress") val currentProgress: Float,
  val milestones: List<GoalMilestoneDto>,
  val evidence: String = "",
  val risks: List<String> = emptyList(),
  @SerialName("related_project_ids") val relatedProjectIds: List<String> = emptyList(),
  @SerialName("related_todo_ids") val relatedTodoIds: List<Int> = emptyList(),
  @SerialName("created_at") val createdAt: String,
  @SerialName("target_end_date") val targetEndDate: String,
)

@Serializable
data class GoalMilestoneDto(
  val id: Int,
  val title: String,
  @SerialName("target_date") val targetDate: String,
  @SerialName("success_criteria") val successCriteria: String,
  val status: String,
  @SerialName("sequence_order") val sequenceOrder: Int,
)

@Serializable
data class UserLevelData(
  @SerialName("user_id") val userId: String,
  val level: Int,
  @SerialName("current_xp") val currentXp: Int,
  @SerialName("total_xp") val totalXp: Int,
  @SerialName("xp_to_next_level") val xpToNextLevel: Int,
  @SerialName("streak_days") val streakDays: Int,
  @SerialName("last_completion_date") val lastCompletionDate: String? = null,
  @SerialName("level_title") val levelTitle: String = "",
)

@Serializable
data class XpHistoryResponse(
  @SerialName("user_id") val userId: String,
  val total: Int,
  val limit: Int,
  val offset: Int,
  val items: List<XpHistoryItem> = emptyList(),
)

@Serializable
data class XpHistoryItem(
  val id: Int,
  @SerialName("created_at") val createdAt: String? = null,
  @SerialName("xp_earned") val xpEarned: Int,
  @SerialName("base_xp") val baseXp: Int,
  val multiplier: Float = 1.0f,
  @SerialName("todo_id") val todoId: Int? = null,
  @SerialName("session_id") val sessionId: String? = null,
)

// ==================== Growth Tree DTOs ====================

@Serializable
data class GrowthTreeResponse(
  val user: GrowthTreeUserSummary,
  @SerialName("timeline_nodes") val timelineNodes: List<GrowthTreeNode>,
  val edges: List<GrowthTreeEdge>,
  val achievements: List<GrowthTreeAchievement>,
  @SerialName("dimension_trends") val dimensionTrends: List<GrowthTreeDimensionTrend> = emptyList(),
  @SerialName("future_projection") val futureProjection: List<GrowthTreeFutureNode> = emptyList(),
  val meta: GrowthTreeMeta,
)

@Serializable
data class GrowthTreeUserSummary(
  @SerialName("user_id") val userId: String,
  @SerialName("display_name") val displayName: String,
  val level: Int,
  @SerialName("current_xp") val currentXp: Int,
  @SerialName("total_xp") val totalXp: Int,
  @SerialName("xp_to_next_level") val xpToNextLevel: Int,
  @SerialName("xp_progress_pct") val xpProgressPct: Float = 0f,
  @SerialName("streak_days") val streakDays: Int = 0,
  @SerialName("account_created_at") val accountCreatedAt: String = "",
  @SerialName("total_sessions") val totalSessions: Int = 0,
  @SerialName("total_people") val totalPeople: Int = 0,
  @SerialName("total_orgs") val totalOrgs: Int = 0,
  @SerialName("total_projects") val totalProjects: Int = 0,
  @SerialName("total_todos_completed") val totalTodosCompleted: Int = 0,
  @SerialName("total_todos_pending") val totalTodosPending: Int = 0,
  @SerialName("total_relationships") val totalRelationships: Int = 0,
)

@Serializable
data class GrowthTreeNode(
  val id: String,
  @SerialName("node_type") val nodeType: String,
  val label: String,
  val timestamp: String,
  val metadata: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap(),
  @SerialName("is_future") val isFuture: Boolean = false,
  @SerialName("parent_node_id") val parentNodeId: String? = null,
)

@Serializable
data class GrowthTreeEdge(
  val id: String,
  @SerialName("source_id") val sourceId: String,
  @SerialName("target_id") val targetId: String,
  @SerialName("relationship_type") val relationshipType: String,
  @SerialName("relationship_subtype") val relationshipSubtype: String,
  @SerialName("discovered_at") val discoveredAt: String? = null,
  val metadata: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap(),
)

@Serializable
data class GrowthTreeAchievement(
  val id: String,
  @SerialName("achievement_id") val achievementId: String,
  @SerialName("milestone_type") val milestoneType: String,
  @SerialName("level_reached") val levelReached: Int? = null,
  @SerialName("streak_days") val streakDays: Int? = null,
  @SerialName("xp_earned") val xpEarned: Int? = null,
  val details: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap(),
  @SerialName("earned_at") val earnedAt: String,
)

@Serializable
data class GrowthTreeDimensionTrend(
  val dimension: String,
  @SerialName("data_points") val dataPoints: List<GrowthTreeDimensionDataPoint> = emptyList(),
)

@Serializable
data class GrowthTreeDimensionDataPoint(
  @SerialName("period_date") val periodDate: String,
  val score: Float,
)

@Serializable
data class GrowthTreeFutureNode(
  val id: String,
  @SerialName("node_type") val nodeType: String,
  val label: String,
  @SerialName("target_date") val targetDate: String? = null,
  @SerialName("progress_pct") val progressPct: Float? = null,
  val metadata: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap(),
)

@Serializable
data class GrowthTreeMeta(
  @SerialName("from_date") val fromDate: String,
  @SerialName("to_date") val toDate: String,
  val today: String,
  @SerialName("total_nodes") val totalNodes: Int,
  @SerialName("total_edges") val totalEdges: Int,
  @SerialName("query_time_ms") val queryTimeMs: Int = 0,
)

// ==================== Notification DTOs ====================

@Serializable
data class NotificationDto(
  val id: Int,
  @SerialName("user_id") val userId: String,
  @SerialName("event_type") val eventType: String,
  @SerialName("event_id") val eventId: String? = null,
  val title: String? = null,
  val body: String? = null,
  val data: Map<String, JsonElement> = emptyMap(),
  @SerialName("is_read") val isRead: Boolean = false,
  @SerialName("created_at") val createdAt: String = "",
  @SerialName("read_at") val readAt: String? = null,
)

@Serializable
data class NotificationsResponseDto(
  val items: List<NotificationDto> = emptyList(),
  val total: Int = 0,
  @SerialName("unread_count") val unreadCount: Int = 0,
)

@Serializable
data class UnreadCountDto(
  val count: Int = 0,
)

// ==================== Subscription DTOs ====================

@Serializable
data class SubscriptionPlanDto(
  @SerialName("plan_code") val planCode: String,
  @SerialName("display_name") val displayName: String,
  @SerialName("asr_monthly_minutes") val asrMonthlyMinutes: Int = 0,
  @SerialName("storage_mb") val storageMb: Int = 0,
  @SerialName("cloud_backup_enabled") val cloudBackupEnabled: Boolean = false,
  val features: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap(),
  @SerialName("price_monthly_cents") val priceMonthlyInCents: Int = 0,
  @SerialName("price_yearly_cents") val priceYearlyInCents: Int = 0,
) {
  fun toDomain(): SubscriptionPlan = SubscriptionPlan(
    planCode = planCode,
    displayName = displayName,
    asrMonthlyMinutes = asrMonthlyMinutes,
    storageMb = storageMb,
    cloudBackupEnabled = cloudBackupEnabled,
    features = features.mapValues { (_, v) ->
      (v as? kotlinx.serialization.json.JsonPrimitive)?.content?.toBooleanStrictOrNull() ?: false
    },
    priceMonthlyInCents = priceMonthlyInCents,
    priceYearlyInCents = priceYearlyInCents,
  )
}

@Serializable
data class SubscriptionMeDto(
  @SerialName("user_id") val userId: String,
  @SerialName("plan_code") val planCode: String,
  @SerialName("display_name") val displayName: String,
  @SerialName("billing_cycle") val billingCycle: String = "monthly",
  val status: String = "active",
  val usage: SubscriptionUsageDto = SubscriptionUsageDto(),
) {
  fun toDomain(): Subscription = Subscription(
    userId = userId,
    planCode = planCode,
    displayName = displayName,
    billingCycle = billingCycle,
    status = status,
    usage = usage.toDomain(),
  )
}

@Serializable
data class SubscriptionUsageDto(
  @SerialName("asr_minutes_used") val asrMinutesUsed: Int = 0,
  @SerialName("asr_minutes_limit") val asrMinutesLimit: Int = 0,
  @SerialName("asr_minutes_remaining") val asrMinutesRemaining: Int = 0,
  @SerialName("storage_mb_used") val storageMbUsed: Int = 0,
  @SerialName("sessions_processed") val sessionsProcessed: Int = 0,
) {
  fun toDomain(): SubscriptionUsage = SubscriptionUsage(
    asrMinutesUsed = asrMinutesUsed,
    asrMinutesLimit = asrMinutesLimit,
    asrMinutesRemaining = asrMinutesRemaining,
    storageMbUsed = storageMbUsed,
    sessionsProcessed = sessionsProcessed,
  )
}

@Serializable
data class SubscriptionFeaturesDto(
  val tier: String,
  val features: SubscriptionFeatureFlagsDto,
) {
  fun toDomain(): SubscriptionFeatures = SubscriptionFeatures(
    tier = tier,
    memoryEnabled = features.memoryEnabled,
    goalsEnabled = features.goalsEnabled,
    executionMode = features.executionMode,
    knowledgeGraph = features.knowledgeGraph,
    riskAnalysis = features.riskAnalysis,
    summaryLevel = features.summaryLevel,
    cloudBackup = features.cloudBackup,
    insightsEnabled = features.insightsEnabled,
  )
}

@Serializable
data class SubscriptionFeatureFlagsDto(
  @SerialName("memory_enabled") val memoryEnabled: Boolean = false,
  @SerialName("goals_enabled") val goalsEnabled: Boolean = false,
  @SerialName("execution_mode") val executionMode: String = "passive",
  @SerialName("knowledge_graph") val knowledgeGraph: String = "basic",
  @SerialName("risk_analysis") val riskAnalysis: Boolean = false,
  @SerialName("summary_level") val summaryLevel: String = "basic",
  @SerialName("cloud_backup") val cloudBackup: Boolean = false,
  @SerialName("insights_enabled") val insightsEnabled: Boolean = false,
)

@Serializable
data class UsageDetailsDto(
  @SerialName("user_id") val userId: String,
  @SerialName("period_start") val periodStart: String,
  @SerialName("period_end") val periodEnd: String,
  @SerialName("asr_minutes_used") val asrMinutesUsed: Int = 0,
  @SerialName("asr_minutes_limit") val asrMinutesLimit: Int = 0,
  @SerialName("storage_mb_used") val storageMbUsed: Int = 0,
  @SerialName("storage_mb_limit") val storageMbLimit: Int = 0,
  @SerialName("sessions_processed") val sessionsProcessed: Int = 0,
) {
  fun toDomain(): UsageDetails = UsageDetails(
    userId = userId,
    periodStart = periodStart,
    periodEnd = periodEnd,
    asrMinutesUsed = asrMinutesUsed,
    asrMinutesLimit = asrMinutesLimit,
    storageMbUsed = storageMbUsed,
    storageMbLimit = storageMbLimit,
    sessionsProcessed = sessionsProcessed,
  )
}

@Serializable
data class SubscriptionChangeRequestDto(
  @SerialName("plan_code") val planCode: String,
  @SerialName("billing_cycle") val billingCycle: String,
)

@Serializable
data class SubscriptionChangeResponseDto(
  val status: String,
  @SerialName("plan_code") val planCode: String,
)

// ==================== Fixed Session Audio DTOs ====================

@Serializable
data class FixedSessionStartResponse(
  val status: String,
  @SerialName("session_id") val sessionId: String,
  @SerialName("recording_mode") val recordingMode: String,
  @SerialName("user_id") val userId: String,
  val timestamp: String,
)

@Serializable
data class FixedSessionStopResponse(
  val status: String,
  val message: String,
  @SerialName("session_id") val sessionId: String? = null,
  @SerialName("duration_seconds") val durationSeconds: Double? = null,
  @SerialName("audio_file") val audioFile: String? = null,
  @SerialName("error_type") val errorType: String? = null,
)

// ==================== Orchestrator status DTO ====================

/**
 * Mirror of KK_orchestrator's `/api/v1/pipeline/session/{sid}/status` response.
 * Status values: queued, running, succeeded, failed, cancelled.
 * Stage values: denoise, asr, diarization, speaker_names, knowledge_graph,
 *               meeting_minutes, tasks, dropbox (plus any new stages — we
 *               stringify them, so adding a stage on the backend won't break
 *               decoding).
 */
@Serializable
data class OrchestratorStatusDto(
  @SerialName("job_id") val jobId: String = "",
  @SerialName("session_id") val sessionId: String = "",
  val status: String = "",
  val stage: String? = null,
  @SerialName("progress_pct") val progressPct: Double? = null,
  val message: String? = null,
  val error: String? = null,
  @SerialName("is_terminal") val isTerminal: Boolean = false,
)

// ==================== Consent DTOs ====================

@Serializable
data class ConsentStatusResponse(
  @SerialName("is_granted") val isGranted: Boolean,
  @SerialName("consent_version") val consentVersion: String? = null,
  @SerialName("granted_at") val grantedAt: String? = null,
)

@Serializable
data class ConsentResponse(
  val status: String,
  @SerialName("consent_type") val consentType: String? = null,
  @SerialName("consent_version") val consentVersion: String? = null,
  val message: String? = null,
)

// ==================== Voiceprint DTOs ====================

@Serializable
data class VoiceprintDto(
  val id: String,
  val name: String,
  @SerialName("created_at") val createdAt: String,
  @SerialName("sample_count") val sampleCount: Int,
  @SerialName("last_updated") val lastUpdated: String,
)

// ==================== Privacy Settings DTOs ====================

// Unified privacy settings DTO matching backend GET/PUT /api/compliance/privacy-settings
@Serializable
data class PrivacySettingsDto(
  @SerialName("retention_days") val retentionDays: Int = 90, // -1 = unlimited
  @SerialName("auto_delete_enabled") val autoDeleteEnabled: Boolean = true,
  @SerialName("do_not_sell") val doNotSell: Boolean = false,
  @SerialName("limit_sensitive_pi") val limitSensitivePi: Boolean = false,
  @SerialName("memory_extraction_enabled") val memoryExtractionEnabled: Boolean = true,
  @SerialName("ai_training_opted_in") val aiTrainingOptedIn: Boolean = false,
  @SerialName("ai_training_data_types") val aiTrainingDataTypes: List<String> = emptyList(),
  @SerialName("voiceprint_enabled") val voiceprintEnabled: Boolean = false,
)

@Serializable
data class PrivacyRequestDto(
  @SerialName("request_id") val requestId: String,
  @SerialName("request_type") val requestType: String,
  val status: String,
  @SerialName("requested_at") val requestedAt: String,
  @SerialName("response_deadline") val responseDeadline: String,
)

// ==================== Notification Preferences DTOs ====================

@Serializable
data class NotificationPreferencesDto(
  @SerialName("meeting_reminders") val meetingReminders: Boolean = true,
  @SerialName("task_updates") val taskUpdates: Boolean = true,
  @SerialName("insights_digest") val insightsDigest: Boolean = true,
  @SerialName("push_enabled") val pushEnabled: Boolean = true,
)
