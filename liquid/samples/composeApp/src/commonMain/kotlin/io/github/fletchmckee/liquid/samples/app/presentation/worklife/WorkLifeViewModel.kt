package io.github.fletchmckee.liquid.samples.app.presentation.worklife

import io.github.fletchmckee.liquid.samples.app.core.BaseViewModel
import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.di.AppModule
import io.github.fletchmckee.liquid.samples.app.domain.entity.Achievements
import io.github.fletchmckee.liquid.samples.app.domain.entity.InfluenceTier
import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.domain.entity.RelationshipStatus
import io.github.fletchmckee.liquid.samples.app.domain.entity.Scenario
import io.github.fletchmckee.liquid.samples.app.domain.repository.EngagementStats
import io.github.fletchmckee.liquid.samples.app.domain.repository.NetworkGrowthStats

/**
 * Filter options for people
 */
enum class PeopleFilter {
    ALL,
    ACTIVE_RELATIONSHIPS,
    TIER_S,
    TIER_A,
    TIER_B,
    NEED_ATTENTION
}

/**
 * View mode for the WorkLife screen
 */
enum class WorkLifeViewMode {
    PEOPLE,
    ORGANIZATIONS,
    NETWORK_MAP
}

/**
 * UI State for WorkLifeScreen
 */
data class WorkLifeUiState(
    val isLoading: Boolean = true,
    val people: List<Person> = emptyList(),
    val organizations: List<Organization> = emptyList(),
    val projects: List<Project> = emptyList(),
    val topInfluencers: List<Person> = emptyList(),
    val selectedPerson: Person? = null,
    val selectedOrganization: Organization? = null,
    val selectedProject: Project? = null,
    val currentFilter: PeopleFilter = PeopleFilter.ALL,
    val viewMode: WorkLifeViewMode = WorkLifeViewMode.PEOPLE,
    val searchQuery: String = "",
    val showPersonDetails: Boolean = false,
    val showOrganizationDetails: Boolean = false,
    val showProjectDetails: Boolean = false,
    val error: String? = null
    // Note: encourage/worklife data removed - now cached at MainApp level
)

/**
 * One-time events for WorkLifeScreen
 */
sealed class WorkLifeEvent {
    data class ShowError(val message: String) : WorkLifeEvent()
    data class NavigateToPersonDetails(val personId: String) : WorkLifeEvent()
    data class NavigateToOrganizationDetails(val orgId: String) : WorkLifeEvent()
    data class NavigateToProjectDetails(val projectId: String) : WorkLifeEvent()
    data class StartCall(val personId: String) : WorkLifeEvent()
    data class SendMessage(val personId: String) : WorkLifeEvent()
}

/**
 * ViewModel for WorkLifeScreen
 * Handles people, organizations, projects, and relationship management
 */
class WorkLifeViewModel : BaseViewModel<WorkLifeUiState, WorkLifeEvent>() {

    override val initialState = WorkLifeUiState()

    // Use Cases
    private val getPeopleUseCase = AppModule.getPeopleUseCase
    private val getTopInfluencersUseCase = AppModule.getTopInfluencersUseCase
    private val searchPeopleUseCase = AppModule.searchPeopleUseCase
    private val getOrganizationsUseCase = AppModule.getOrganizationsUseCase
    private val getProjectsUseCase = AppModule.getProjectsUseCase
    private val togglePersonPinUseCase = AppModule.togglePersonPinUseCase
    private val toggleOrganizationPinUseCase = AppModule.toggleOrganizationPinUseCase
    private val toggleProjectPinUseCase = AppModule.toggleProjectPinUseCase
    private val getAchievementsUseCase = AppModule.getAchievementsUseCase
    private val getScenariosUseCase = AppModule.getScenariosUseCase

    // Growth data - loaded from JSON via repository
    private var achievements: Achievements? = null
    private var scenarios: List<Scenario> = emptyList()
    private var networkGrowthStats: NetworkGrowthStats? = null
    private var engagementStats: EngagementStats? = null

    // Note: insights data now stored in UI state for proper recomposition

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadPeople()
        loadTopInfluencers()
        loadOrganizations()
        loadProjects()
        loadGrowthData()
        // Note: encourage/worklife data is now cached at MainApp level and passed as props
    }

    private fun loadGrowthData() {
        launch {
            // Load achievements from backend/JSON
            when (val result = getAchievementsUseCase()) {
                is Result.Success -> {
                    achievements = result.data
                }
                is Result.Error -> {
                    sendEvent(WorkLifeEvent.ShowError(result.message ?: "Failed to load achievements"))
                }
                is Result.Loading -> { /* No-op */ }
            }

            // Load scenarios from backend/JSON
            when (val result = getScenariosUseCase()) {
                is Result.Success -> {
                    scenarios = result.data
                }
                is Result.Error -> {
                    sendEvent(WorkLifeEvent.ShowError(result.message ?: "Failed to load scenarios"))
                }
                is Result.Loading -> { /* No-op */ }
            }

            // Load network stats from repository
            val growthRepo = AppModule.growthRepository
            when (val result = growthRepo.getNetworkGrowthStats()) {
                is Result.Success -> {
                    networkGrowthStats = result.data
                }
                is Result.Error -> { /* Ignore - optional */ }
                is Result.Loading -> { /* No-op */ }
            }

            // Load engagement stats from repository
            when (val result = growthRepo.getEngagementStats()) {
                is Result.Success -> {
                    engagementStats = result.data
                }
                is Result.Error -> { /* Ignore - optional */ }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    // Note: encourage/worklife data loading removed - now cached at MainApp level

    /**
     * Get achievements directly from backend/JSON - NO CALCULATIONS
     */
    fun getAchievements(): Achievements? = achievements

    /**
     * Get scenarios directly from backend/JSON
     */
    fun getScenarios(): List<Scenario> = scenarios

    /**
     * Get network growth stats directly from backend/JSON
     */
    fun getNetworkGrowthStats(): NetworkGrowthStats? = networkGrowthStats

    /**
     * Get engagement stats directly from backend/JSON
     */
    fun getEngagementStats(): EngagementStats? = engagementStats

    private fun loadPeople() {
        launch {
            updateState { copy(isLoading = true) }
            when (val result = getPeopleUseCase()) {
                is Result.Success -> {
                    updateState {
                        copy(
                            people = result.data,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                    sendEvent(WorkLifeEvent.ShowError(result.message ?: "Failed to load people"))
                }
                is Result.Loading -> {
                    updateState { copy(isLoading = true) }
                }
            }
        }
    }

    private fun loadTopInfluencers() {
        launch {
            when (val result = getTopInfluencersUseCase()) {
                is Result.Success -> {
                    updateState { copy(topInfluencers = result.data) }
                }
                is Result.Error -> {
                    sendEvent(WorkLifeEvent.ShowError(result.message ?: "Failed to load top influencers"))
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    private fun loadOrganizations() {
        launch {
            when (val result = getOrganizationsUseCase()) {
                is Result.Success -> {
                    updateState { copy(organizations = result.data) }
                }
                is Result.Error -> {
                    sendEvent(WorkLifeEvent.ShowError(result.message ?: "Failed to load organizations"))
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    private fun loadProjects() {
        launch {
            when (val result = getProjectsUseCase()) {
                is Result.Success -> {
                    updateState { copy(projects = result.data) }
                }
                is Result.Error -> {
                    sendEvent(WorkLifeEvent.ShowError(result.message ?: "Failed to load projects"))
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    fun setFilter(filter: PeopleFilter) {
        updateState { copy(currentFilter = filter) }
    }

    fun setViewMode(mode: WorkLifeViewMode) {
        updateState { copy(viewMode = mode) }
    }

    fun setSearchQuery(query: String) {
        updateState { copy(searchQuery = query) }
        if (query.isNotBlank()) {
            searchPeople(query)
        } else {
            loadPeople()
        }
    }

    private fun searchPeople(query: String) {
        launch {
            when (val result = searchPeopleUseCase(query)) {
                is Result.Success -> {
                    updateState { copy(people = result.data) }
                }
                is Result.Error -> {
                    sendEvent(WorkLifeEvent.ShowError(result.message ?: "Search failed"))
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    fun selectPerson(personId: String) {
        val person = currentState.people.find { it.id == personId }
            ?: currentState.topInfluencers.find { it.id == personId }
        updateState {
            copy(
                selectedPerson = person,
                showPersonDetails = person != null
            )
        }
        if (person != null) {
            sendEvent(WorkLifeEvent.NavigateToPersonDetails(personId))
        }
    }

    fun dismissPersonDetails() {
        updateState {
            copy(
                selectedPerson = null,
                showPersonDetails = false
            )
        }
    }

    fun selectOrganization(orgId: String) {
        val org = currentState.organizations.find { it.id == orgId }
        updateState {
            copy(
                selectedOrganization = org,
                showOrganizationDetails = org != null
            )
        }
        if (org != null) {
            sendEvent(WorkLifeEvent.NavigateToOrganizationDetails(orgId))
        }
    }

    fun dismissOrganizationDetails() {
        updateState {
            copy(
                selectedOrganization = null,
                showOrganizationDetails = false
            )
        }
    }

    fun selectProject(projectId: String) {
        val project = currentState.projects.find { it.id == projectId }
        updateState {
            copy(
                selectedProject = project,
                showProjectDetails = project != null
            )
        }
        if (project != null) {
            sendEvent(WorkLifeEvent.NavigateToProjectDetails(projectId))
        }
    }

    fun dismissProjectDetails() {
        updateState {
            copy(
                selectedProject = null,
                showProjectDetails = false
            )
        }
    }

    fun startCall(personId: String) {
        sendEvent(WorkLifeEvent.StartCall(personId))
    }

    fun sendMessage(personId: String) {
        sendEvent(WorkLifeEvent.SendMessage(personId))
    }

    fun clearError() {
        updateState { copy(error = null) }
    }

    fun refresh() {
        loadInitialData()
    }

    fun refreshProjects() {
        loadProjects()
    }

    fun refreshPeople() {
        loadPeople()
        loadTopInfluencers()
    }

    fun refreshOrganizations() {
        loadOrganizations()
    }

    fun togglePersonPin(personId: String) {
        launch {
            when (val result = togglePersonPinUseCase(personId)) {
                is Result.Success -> {
                    // Reload people to reflect changes
                    loadPeople()
                    loadTopInfluencers()
                }
                is Result.Error -> {
                    sendEvent(WorkLifeEvent.ShowError(result.message ?: "Failed to toggle person pin"))
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    fun toggleOrganizationPin(organizationId: String) {
        launch {
            when (val result = toggleOrganizationPinUseCase(organizationId)) {
                is Result.Success -> {
                    // Reload organizations to reflect changes
                    loadOrganizations()
                }
                is Result.Error -> {
                    sendEvent(WorkLifeEvent.ShowError(result.message ?: "Failed to toggle organization pin"))
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    fun toggleProjectPin(projectId: String) {
        launch {
            when (val result = toggleProjectPinUseCase(projectId)) {
                is Result.Success -> {
                    // Reload projects to reflect changes
                    loadProjects()
                }
                is Result.Error -> {
                    sendEvent(WorkLifeEvent.ShowError(result.message ?: "Failed to toggle project pin"))
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    /**
     * Get filtered people based on current filter, with pinned items first
     */
    fun getFilteredPeople(): List<Person> {
        var filtered = currentState.people

        filtered = when (currentState.currentFilter) {
            PeopleFilter.ALL -> filtered
            PeopleFilter.ACTIVE_RELATIONSHIPS -> filtered.filter {
                it.relationshipStatus == RelationshipStatus.STRONG
            }
            PeopleFilter.TIER_S -> filtered.filter { it.influenceTier == InfluenceTier.S }
            PeopleFilter.TIER_A -> filtered.filter { it.influenceTier == InfluenceTier.A }
            PeopleFilter.TIER_B -> filtered.filter { it.influenceTier == InfluenceTier.B }
            PeopleFilter.NEED_ATTENTION -> filtered.filter {
                it.relationshipStatus == RelationshipStatus.DEVELOPING
            }
        }

        // Sort with pinned items first (by pinnedAt descending), then non-pinned by id
        return filtered.sortedWith(
            compareByDescending<Person> { it.isPinned }
                .thenByDescending { it.pinnedAt ?: 0L }
        )
    }

    /**
     * Get organizations sorted with pinned items first
     */
    fun getSortedOrganizations(): List<Organization> {
        return currentState.organizations.sortedWith(
            compareByDescending<Organization> { it.isPinned }
                .thenByDescending { it.pinnedAt ?: 0L }
        )
    }

    /**
     * Get projects sorted with pinned items first
     */
    fun getSortedProjects(): List<Project> {
        return currentState.projects.sortedWith(
            compareByDescending<Project> { it.isPinned }
                .thenByDescending { it.pinnedAt ?: 0L }
        )
    }

    /**
     * Get people grouped by influence tier
     */
    fun getPeopleByTier(): Map<InfluenceTier, List<Person>> {
        return getFilteredPeople().groupBy { it.influenceTier }
    }

    /**
     * Get people grouped by related organizations
     */
    fun getPeopleByOrganization(): Map<String, List<Person>> {
        return getFilteredPeople()
            .flatMap { person -> person.relatedOrganizations.map { org -> org to person } }
            .groupBy({ it.first }, { it.second })
    }

    /**
     * Get people who need attention (developing relationships)
     */
    fun getPeopleNeedingAttention(): List<Person> {
        return currentState.people.filter {
            it.relationshipStatus == RelationshipStatus.DEVELOPING
        }
    }

    /**
     * Get relationship health score (0-100)
     */
    fun getRelationshipHealthScore(): Int {
        val total = currentState.people.size
        if (total == 0) return 100

        val strong = currentState.people.count {
            it.relationshipStatus == RelationshipStatus.STRONG
        }
        return (strong * 100) / total
    }

    /**
     * Get people statistics
     */
    fun getPeopleStats(): PeopleStats {
        return PeopleStats(
            total = currentState.people.size,
            tierS = currentState.people.count { it.influenceTier == InfluenceTier.S },
            tierA = currentState.people.count { it.influenceTier == InfluenceTier.A },
            tierB = currentState.people.count { it.influenceTier == InfluenceTier.B },
            strongRelationships = currentState.people.count {
                it.relationshipStatus == RelationshipStatus.STRONG
            },
            developingRelationships = currentState.people.count {
                it.relationshipStatus == RelationshipStatus.DEVELOPING
            }
        )
    }
}

/**
 * Statistics about people in the network
 */
data class PeopleStats(
    val total: Int,
    val tierS: Int,
    val tierA: Int,
    val tierB: Int,
    val strongRelationships: Int,
    val developingRelationships: Int
)
