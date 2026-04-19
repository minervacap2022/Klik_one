package io.github.fletchmckee.liquid.samples.app.di

import io.github.fletchmckee.liquid.samples.app.data.repository.AuthRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.data.repository.CalendarRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.data.repository.ChatRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.data.repository.FeedbackRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.data.repository.GrowthRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.data.repository.OrganizationRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.data.repository.PersonRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.data.repository.ProjectRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.data.repository.TaskRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.data.repository.UserRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryCalendarDataSource
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryFeedbackDataSource
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryGrowthDataSource
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryOrganizationDataSource
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryPersonDataSource
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryProjectDataSource
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryTaskDataSource
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryUserDataSource
import io.github.fletchmckee.liquid.samples.app.domain.repository.CalendarRepository
import io.github.fletchmckee.liquid.samples.app.domain.repository.ChatRepository
import io.github.fletchmckee.liquid.samples.app.domain.repository.FeedbackRepository
import io.github.fletchmckee.liquid.samples.app.domain.repository.GrowthRepository
import io.github.fletchmckee.liquid.samples.app.domain.repository.OrganizationRepository
import io.github.fletchmckee.liquid.samples.app.domain.repository.PersonRepository
import io.github.fletchmckee.liquid.samples.app.domain.repository.ProjectRepository
import io.github.fletchmckee.liquid.samples.app.domain.repository.TaskRepository
import io.github.fletchmckee.liquid.samples.app.domain.repository.AuthRepository
import io.github.fletchmckee.liquid.samples.app.domain.repository.UserRepository
import io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskStatus
import io.github.fletchmckee.liquid.samples.app.data.source.json.toDomain
import io.github.fletchmckee.liquid.samples.app.domain.usecase.calendar.GetDailyBriefingUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.calendar.GetMeetingDetailsUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.calendar.GetMeetingsForDateUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.calendar.ObserveMeetingsUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.calendar.ToggleMeetingPinUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.chat.GetSuggestedQuestionsUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.chat.ObserveChatMessagesUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.chat.SendChatMessageUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.feedback.SubmitFeedbackUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.growth.GetAchievementsUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.growth.GetScenariosUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.organization.GetOrganizationsUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.organization.ToggleOrganizationPinUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.person.GetPeopleUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.person.TogglePersonPinUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.person.GetTopInfluencersUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.person.SearchPeopleUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.project.GetProjectsUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.project.ToggleProjectPinUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.task.GetTasksUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.task.GetTaskSummaryUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.task.ToggleTaskPinUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.task.UpdateTaskStatusUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.user.GetConnectedDevicesUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.user.GetCurrentUserUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.user.GetUserPreferencesUseCase
import io.github.fletchmckee.liquid.samples.app.domain.usecase.user.UpdateUserPreferencesUseCase
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskMetadata as DomainTaskMetadata
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskPriority
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.model.TaskMetadata as ModelTaskMetadata
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Manual Dependency Injection Container
 *
 * This provides a simple, compile-time safe DI solution for Kotlin Multiplatform.
 * Can be easily replaced with Koin or other DI frameworks when needed.
 *
 * PRODUCTION MODE ONLY - All data from real backend API.
 */
object AppModule {

    private var isInitialized = false
    private val initMutex = Mutex()

    /**
     * Initialize the app module and load data from real backend API.
     * Call this during app startup (e.g., in MainActivity or App composable).
     * Uses Environment configuration (defaults to PRODUCTION).
     *
     * Uses Mutex + NonCancellable to ensure:
     * 1. Only one initialization runs at a time (concurrent callers wait)
     * 2. Once started, initialization completes even if the calling coroutine
     *    is cancelled (e.g., iOS composition disposal during startup)
     */
    suspend fun initialize() {
        if (isInitialized) return

        initMutex.withLock {
            // Double-check after acquiring lock (another caller may have completed init)
            if (isInitialized) return

            // Run in NonCancellable context so init completes even if the calling
            // LaunchedEffect scope is cancelled by composition disposal.
            // Data is stored in singleton data sources, so it's available when
            // the composition is recreated.
            withContext(NonCancellable) {
                doInitialize()
            }
        }
    }

    private suspend fun doInitialize() {
        // Ensure CurrentUser is synced before any backend calls (User-Id header is required for user-scoped data)
        syncCurrentUser()

        KlikLogger.i("AppModule", "Initializing with REAL BACKEND API")
        KlikLogger.i("AppModule", "Backend URL: ${io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig.BASE_URL}")
        KlikLogger.i("AppModule", "User-Id: ${io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser.userId}")

        val userId = io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser.userId
        require(!userId.isNullOrBlank()) {
            "Missing User-Id. Please log in (or use your userId like user_xxx in the auth screen)."
        }

        // Load data from real backend API
        val fetcher = io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher

        // 0. Load current user profile from backend (with retry for transient failures)
        KlikLogger.d("AppModule", "Fetching current user profile from backend")
        var lastProfileError: Exception? = null
        for (attempt in 1..3) {
            try {
                val currentUser = fetcher.fetchCurrentUser()
                userDataSource.updateUser(currentUser)
                KlikLogger.i("AppModule", "Loaded user profile: ${currentUser.name} (${currentUser.email})")
                lastProfileError = null
                break
            } catch (e: Exception) {
                KlikLogger.e("AppModule", "Failed to fetch user profile (attempt $attempt/3): ${e.message}", e)
                lastProfileError = e
                if (attempt < 3) {
                    kotlinx.coroutines.delay(1000L * attempt)
                }
            }
        }
        if (lastProfileError != null) {
            throw Exception("User profile failed after 3 attempts: ${lastProfileError.message}", lastProfileError)
        }

        // 0b. Register push token with backend
        val pushService = io.github.fletchmckee.liquid.samples.app.platform.PushNotificationService()
        if (pushService.hasStoredToken()) {
            val registered = pushService.registerDeviceToken()
            KlikLogger.i("AppModule", "Push token registration: $registered")
        } else {
            KlikLogger.d("AppModule", "No push token available yet")
        }

        // Track failures so we can report them
        val failures = mutableListOf<String>()

        // Fetch all independent data in parallel using coroutineScope + async.
        // Each async block has its own try-catch so one failure doesn't cancel others.
        coroutineScope {
            // 1. Meetings
            val meetingsDeferred = async {
                try {
                    KlikLogger.d("AppModule", "Fetching meetings from backend")
                    val meetings = fetcher.fetchMeetings()
                    calendarDataSource.setMeetings(meetings)
                    io.github.fletchmckee.liquid.samples.app.model.meetingsState.value = meetings
                    KlikLogger.i("AppModule", "Loaded ${meetings.size} meetings from API")
                } catch (e: Exception) {
                    KlikLogger.e("AppModule", "Failed to fetch meetings: ${e.message}", e)
                    failures.add("meetings")
                }
            }

            // 2. Daily Briefing
            val briefingDeferred = async {
                try {
                    val briefing = fetcher.fetchDailyBriefing()
                    calendarDataSource.setBriefing(briefing)
                    KlikLogger.i("AppModule", "Daily briefing loaded: ${briefing != null}")
                } catch (e: Exception) {
                    KlikLogger.e("AppModule", "Failed to fetch daily briefing: ${e.message}", e)
                    failures.add("briefing")
                }
            }

            // 3. Tasks
            val tasksDeferred = async {
                try {
                    KlikLogger.d("AppModule", "Fetching tasks from backend")
                    val tasks = fetcher.fetchTasks()
                    taskDataSource.setTasks(tasks)
                    KlikLogger.i("AppModule", "Loaded ${tasks.size} tasks from API")
                } catch (e: Exception) {
                    KlikLogger.e("AppModule", "Failed to fetch tasks: ${e.message}", e)
                    failures.add("tasks")
                }
            }

            // 4. People
            val peopleDeferred = async {
                try {
                    KlikLogger.d("AppModule", "Fetching people from backend")
                    val people = fetcher.fetchPeople()
                    personDataSource.setPeople(people)
                    io.github.fletchmckee.liquid.samples.app.model.peopleState.value = people
                    KlikLogger.i("AppModule", "Loaded ${people.size} people from API")
                } catch (e: Exception) {
                    KlikLogger.e("AppModule", "Failed to fetch people: ${e.message}", e)
                    failures.add("people")
                }
            }

            // 5. Organizations
            val organizationsDeferred = async {
                try {
                    KlikLogger.d("AppModule", "Fetching organizations from backend")
                    val orgs = fetcher.fetchOrganizations()
                    organizationDataSource.setOrganizations(orgs)
                    io.github.fletchmckee.liquid.samples.app.model.organizationsState.value = orgs
                    KlikLogger.i("AppModule", "Loaded ${orgs.size} organizations from API")
                } catch (e: Exception) {
                    KlikLogger.e("AppModule", "Failed to fetch organizations: ${e.message}", e)
                    failures.add("organizations")
                }
            }

            // 6. Projects
            val projectsDeferred = async {
                try {
                    KlikLogger.d("AppModule", "Fetching projects from backend")
                    val projects = fetcher.fetchProjects()
                    projectDataSource.setProjects(projects)
                    io.github.fletchmckee.liquid.samples.app.model.projectsState.value = projects
                    KlikLogger.i("AppModule", "Loaded ${projects.size} projects from API")
                } catch (e: Exception) {
                    KlikLogger.e("AppModule", "Failed to fetch projects: ${e.message}", e)
                    failures.add("projects")
                }
            }

            // 7. Scenarios
            val scenariosDeferred = async {
                try {
                    KlikLogger.d("AppModule", "Fetching scenarios from backend")
                    val scenarios = fetcher.fetchScenarios()
                    growthDataSource.setScenarios(scenarios)
                    io.github.fletchmckee.liquid.samples.app.model.scenariosState.value = scenarios
                    KlikLogger.i("AppModule", "Loaded ${scenarios.size} scenarios from API")
                } catch (e: Exception) {
                    KlikLogger.e("AppModule", "Failed to fetch scenarios: ${e.message}", e)
                    failures.add("scenarios")
                }
            }

            // 8. Chat (messages + suggested questions)
            val chatDeferred = async {
                try {
                    KlikLogger.d("AppModule", "Fetching chat messages from backend")
                    val chatMessages = fetcher.fetchChatMessages()
                    val suggestions = fetcher.fetchSuggestedQuestions()
                    KlikLogger.i("AppModule", "Loaded ${chatMessages.size} chat messages from API (Note: using JSON data)")
                } catch (e: Exception) {
                    KlikLogger.e("AppModule", "Failed to fetch chat: ${e.message}", e)
                    failures.add("chat")
                }
            }

            // 9. Goals
            val goalsDeferred = async {
                try {
                    KlikLogger.d("AppModule", "Fetching goals from backend")
                    val goals = fetcher.fetchGoals(status = "active", limit = 10)
                    io.github.fletchmckee.liquid.samples.app.model.goalsState.value = goals
                    KlikLogger.i("AppModule", "Loaded ${goals?.goals?.size ?: 0} goals from API")
                } catch (e: Exception) {
                    KlikLogger.e("AppModule", "Failed to fetch goals: ${e.message}", e)
                    failures.add("goals")
                }
            }

            // 10. User Level/XP
            val levelDeferred = async {
                try {
                    KlikLogger.d("AppModule", "Fetching user level from backend")
                    val userLevel = fetcher.fetchUserLevel()
                    io.github.fletchmckee.liquid.samples.app.model.userLevelState.value = userLevel
                    KlikLogger.i("AppModule", "User level loaded: ${userLevel?.levelTitle ?: "None"} (Level ${userLevel?.level ?: 0})")
                } catch (e: Exception) {
                    KlikLogger.e("AppModule", "Failed to fetch user level: ${e.message}", e)
                    failures.add("userLevel")
                }
            }

            // Await all parallel fetches to complete
            meetingsDeferred.await()
            briefingDeferred.await()
            tasksDeferred.await()
            peopleDeferred.await()
            organizationsDeferred.await()
            projectsDeferred.await()
            scenariosDeferred.await()
            chatDeferred.await()
            goalsDeferred.await()
            levelDeferred.await()
        }

        if (failures.isNotEmpty()) {
            throw Exception("Initialization failed for: ${failures.joinToString()}")
        }

        KlikLogger.i("AppModule", "REAL DATA INITIALIZATION COMPLETE")

        isInitialized = true
    }

    /**
     * Force reload all data from backend API
     */
    suspend fun reload() {
        initMutex.withLock {
            isInitialized = false
        }
        initialize()
    }

    /**
     * Reset initialization state on logout so the next login triggers fresh data loading.
     * NOT a suspend function — safe to call from any context (e.g., LaunchedEffect).
     */
    fun resetForLogout() {
        isInitialized = false
    }

    /**
     * Reload just meetings data from backend API.
     * Called after name corrections or other feedback to get updated data.
     */
    suspend fun reloadMeetings() {
        KlikLogger.d("AppModule", "Reloading meetings from backend")
        val fetcher = io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher
        val meetings = fetcher.fetchMeetings()
        calendarDataSource.setMeetings(meetings)
        io.github.fletchmckee.liquid.samples.app.model.meetingsState.value = meetings
        // CRITICAL: Refresh repository's internal flow so UI gets updated
        calendarRepository.refreshMeetings()
        KlikLogger.i("AppModule", "Reloaded ${meetings.size} meetings from API")
    }

    /**
     * Reload just people data from backend API.
     * Called after name corrections to get updated canonical names for speakerMap.
     */
    suspend fun reloadPeople() {
        KlikLogger.d("AppModule", "Reloading people from backend")
        val fetcher = io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher
        val people = fetcher.fetchPeople()
        personDataSource.setPeople(people)
        io.github.fletchmckee.liquid.samples.app.model.peopleState.value = people
        KlikLogger.i("AppModule", "Reloaded ${people.size} people from API")
    }

    // ==================== Data Sources ====================
    // Data sources that get populated from the real backend API

    private val calendarDataSource: InMemoryCalendarDataSource by lazy { InMemoryCalendarDataSource() }
    private val taskDataSource: InMemoryTaskDataSource by lazy { InMemoryTaskDataSource() }
    private val personDataSource: InMemoryPersonDataSource by lazy { InMemoryPersonDataSource() }
    private val projectDataSource: InMemoryProjectDataSource by lazy { InMemoryProjectDataSource() }
    private val organizationDataSource: InMemoryOrganizationDataSource by lazy { InMemoryOrganizationDataSource() }
    private val growthDataSource: InMemoryGrowthDataSource by lazy { InMemoryGrowthDataSource() }
    private val userDataSource: InMemoryUserDataSource by lazy { InMemoryUserDataSource() }
    private val feedbackDataSource: InMemoryFeedbackDataSource by lazy { InMemoryFeedbackDataSource() }

    // ==================== Repositories ====================
    // All repositories use data sources populated from real backend API

    private var _calendarRepository: CalendarRepository? = null
    val calendarRepository: CalendarRepository
        get() = _calendarRepository ?: CalendarRepositoryImpl(
            dataSource = calendarDataSource
        ).also { _calendarRepository = it }

    private var _taskRepository: TaskRepository? = null
    val taskRepository: TaskRepository
        get() = _taskRepository ?: TaskRepositoryImpl(
            dataSource = taskDataSource
        ).also { _taskRepository = it }

    private var _personRepository: PersonRepository? = null
    val personRepository: PersonRepository
        get() = _personRepository ?: PersonRepositoryImpl(
            dataSource = personDataSource
        ).also { _personRepository = it }

    private var _projectRepository: ProjectRepository? = null
    val projectRepository: ProjectRepository
        get() = _projectRepository ?: ProjectRepositoryImpl(
            dataSource = projectDataSource
        ).also { _projectRepository = it }

    private var _organizationRepository: OrganizationRepository? = null
    val organizationRepository: OrganizationRepository
        get() = _organizationRepository ?: OrganizationRepositoryImpl(
            dataSource = organizationDataSource
        ).also { _organizationRepository = it }

    private var _growthRepository: GrowthRepository? = null
    val growthRepository: GrowthRepository
        get() = _growthRepository ?: GrowthRepositoryImpl(
            dataSource = growthDataSource
        ).also { _growthRepository = it }

    private var _userRepository: UserRepository? = null
    val userRepository: UserRepository
        get() = _userRepository ?: UserRepositoryImpl(
            dataSource = userDataSource
        ).also { _userRepository = it }

    private var _chatRepository: ChatRepository? = null
    val chatRepository: ChatRepository
        get() = _chatRepository ?: ChatRepositoryImpl().also { _chatRepository = it }

    private var _feedbackRepository: FeedbackRepository? = null
    val feedbackRepository: FeedbackRepository
        get() = _feedbackRepository ?: FeedbackRepositoryImpl(
            dataSource = feedbackDataSource
        ).also { _feedbackRepository = it }

    // ==================== Auth Repository ====================
    // Singleton auth repository for the entire app

    private var _authRepository: AuthRepository? = null
    val authRepository: AuthRepository
        get() = _authRepository ?: AuthRepositoryImpl().also { _authRepository = it }

    /**
     * Sync CurrentUser with auth state.
     * Call this after login/logout to update the global current user.
     */
    suspend fun syncCurrentUser() {
        val authState = authRepository.getAuthState()
        if (authState.isLoggedIn) {
            CurrentUser.setUser(authState.userId, authState.accessToken)
        } else {
            CurrentUser.clear()
        }
    }

    // ==================== Use Cases - Calendar ====================

    val getDailyBriefingUseCase: GetDailyBriefingUseCase
        get() = GetDailyBriefingUseCase(calendarRepository)

    val getMeetingsForDateUseCase: GetMeetingsForDateUseCase
        get() = GetMeetingsForDateUseCase(calendarRepository)

    val observeMeetingsUseCase: ObserveMeetingsUseCase
        get() = ObserveMeetingsUseCase(calendarRepository)

    val getMeetingDetailsUseCase: GetMeetingDetailsUseCase
        get() = GetMeetingDetailsUseCase(calendarRepository, personRepository, taskRepository)

    val toggleMeetingPinUseCase: ToggleMeetingPinUseCase
        get() = ToggleMeetingPinUseCase(calendarRepository)

    /**
     * Archive a meeting by ID.
     * This sets isArchived = true and removes it from the visible list.
     */
    suspend fun archiveMeeting(meetingId: String): io.github.fletchmckee.liquid.samples.app.core.Result<Unit> {
        return calendarRepository.archiveMeeting(meetingId)
    }

    /**
     * Archive a project by ID.
     * This sets isArchived = true and removes it from the visible list.
     */
    fun archiveProject(projectId: String): Boolean {
        return projectDataSource.archiveProject(projectId)
    }

    /**
     * Archive a person by ID.
     * This sets isArchived = true and removes it from the visible list.
     */
    fun archivePerson(personId: String): Boolean {
        return personDataSource.archivePerson(personId)
    }

    /**
     * Archive an organization by ID.
     * This sets isArchived = true and removes it from the visible list.
     */
    fun archiveOrganization(organizationId: String): Boolean {
        return organizationDataSource.archiveOrganization(organizationId)
    }

    /**
     * Unarchive a project by ID.
     * This sets isArchived = false and restores it to the visible list.
     */
    fun unarchiveProject(projectId: String): Boolean {
        return projectDataSource.unarchiveProject(projectId)
    }

    /**
     * Unarchive a person by ID.
     * This sets isArchived = false and restores it to the visible list.
     */
    fun unarchivePerson(personId: String): Boolean {
        return personDataSource.unarchivePerson(personId)
    }

    /**
     * Unarchive an organization by ID.
     * This sets isArchived = false and restores it to the visible list.
     */
    fun unarchiveOrganization(organizationId: String): Boolean {
        return organizationDataSource.unarchiveOrganization(organizationId)
    }

    // ==================== Use Cases - Tasks ====================

    val getTasksUseCase: GetTasksUseCase
        get() = GetTasksUseCase(taskRepository)

    val getTaskSummaryUseCase: GetTaskSummaryUseCase
        get() = GetTaskSummaryUseCase(taskRepository)

    val updateTaskStatusUseCase: UpdateTaskStatusUseCase
        get() = UpdateTaskStatusUseCase(taskRepository)

    val toggleTaskPinUseCase: ToggleTaskPinUseCase
        get() = ToggleTaskPinUseCase(taskRepository)

    // ==================== Use Cases - People ====================

    val getPeopleUseCase: GetPeopleUseCase
        get() = GetPeopleUseCase(personRepository)

    val getTopInfluencersUseCase: GetTopInfluencersUseCase
        get() = GetTopInfluencersUseCase(personRepository)

    val searchPeopleUseCase: SearchPeopleUseCase
        get() = SearchPeopleUseCase(personRepository)

    val togglePersonPinUseCase: TogglePersonPinUseCase
        get() = TogglePersonPinUseCase(personRepository)

    // ==================== Use Cases - Projects ====================

    val getProjectsUseCase: GetProjectsUseCase
        get() = GetProjectsUseCase(projectRepository)

    val toggleProjectPinUseCase: ToggleProjectPinUseCase
        get() = ToggleProjectPinUseCase(projectRepository)

    // ==================== Use Cases - Organizations ====================

    val getOrganizationsUseCase: GetOrganizationsUseCase
        get() = GetOrganizationsUseCase(organizationRepository)

    val toggleOrganizationPinUseCase: ToggleOrganizationPinUseCase
        get() = ToggleOrganizationPinUseCase(organizationRepository)

    // ==================== Use Cases - Growth ====================

    val getAchievementsUseCase: GetAchievementsUseCase
        get() = GetAchievementsUseCase(growthRepository)

    val getScenariosUseCase: GetScenariosUseCase
        get() = GetScenariosUseCase(growthRepository)

    // ==================== Use Cases - User ====================

    val getCurrentUserUseCase: GetCurrentUserUseCase
        get() = GetCurrentUserUseCase(userRepository)

    val getUserPreferencesUseCase: GetUserPreferencesUseCase
        get() = GetUserPreferencesUseCase(userRepository)

    val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase
        get() = UpdateUserPreferencesUseCase(userRepository)

    val getConnectedDevicesUseCase: GetConnectedDevicesUseCase
        get() = GetConnectedDevicesUseCase(userRepository)

    // ==================== Use Cases - Chat ====================

    val sendChatMessageUseCase: SendChatMessageUseCase
        get() = SendChatMessageUseCase(chatRepository)

    val getSuggestedQuestionsUseCase: GetSuggestedQuestionsUseCase
        get() = GetSuggestedQuestionsUseCase(chatRepository)

    val observeChatMessagesUseCase: ObserveChatMessagesUseCase
        get() = ObserveChatMessagesUseCase(chatRepository)

    // ==================== Use Cases - Feedback ====================

    val submitFeedbackUseCase: SubmitFeedbackUseCase
        get() = SubmitFeedbackUseCase(feedbackRepository)

    // ==================== Initial Preferences (for startup) ====================

    /**
     * Get initial user preferences synchronously for app startup.
     * This loads saved defaults (background, font) for immediate use in MainApp.
     * Returns null if no preferences are saved yet.
     */
    fun getInitialPreferences(): io.github.fletchmckee.liquid.samples.app.domain.entity.UserPreferences? {
        return userDataSource.getUserPreferences()
    }
}

object BuildConfig {
    const val VERSION_NAME = "1.0.0"
    const val VERSION_CODE = 1
}

/**
 * Extension function to convert model.TaskMetadata to domain.entity.TaskMetadata
 */
// ... (existing toDomainTaskMetadata)

private fun ModelTaskMetadata.toDomainTaskMetadata(): DomainTaskMetadata {
    return DomainTaskMetadata(
        id = id,
        title = title,
        subtitle = subtitle,
        context = context,
        relatedProject = relatedProject,
        relatedPeople = relatedPeople,
        dueInfo = dueInfo,
        priority = TaskPriority.fromString(priority),
        isPinned = isPinned
    )
}


