package io.github.fletchmckee.liquid.samples.app.presentation.profile

import io.github.fletchmckee.liquid.samples.app.core.BaseViewModel
import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.di.AppModule
import io.github.fletchmckee.liquid.samples.app.domain.entity.Achievements
import io.github.fletchmckee.liquid.samples.app.domain.entity.Device
import io.github.fletchmckee.liquid.samples.app.domain.entity.IntegrationInfo
import io.github.fletchmckee.liquid.samples.app.domain.entity.LiquidGlassPreferences
import io.github.fletchmckee.liquid.samples.app.domain.entity.Scenario
import io.github.fletchmckee.liquid.samples.app.domain.entity.User
import io.github.fletchmckee.liquid.samples.app.domain.entity.UserPreferences
import io.github.fletchmckee.liquid.samples.app.data.repository.AuthRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.data.repository.IntegrationRepository
import io.github.fletchmckee.liquid.samples.app.data.repository.IntegrationRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.domain.repository.AuthRepository
import io.github.fletchmckee.liquid.samples.app.platform.OAUTH_CALLBACK_SCHEME
import io.github.fletchmckee.liquid.samples.app.platform.OAuthBrowser
import io.github.fletchmckee.liquid.samples.app.platform.OAuthSessionResult
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

/**
 * Profile section tabs
 */
enum class ProfileSection {
    OVERVIEW,
    SCENARIOS,
    SETTINGS,
    DEVICES
}

/**
 * UI State for ProfileScreen
 */
data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val preferences: UserPreferences? = null,
    val liquidGlassPreferences: LiquidGlassPreferences? = null,
    val achievements: Achievements? = null,
    val scenarios: List<Scenario> = emptyList(),
    val connectedDevices: List<Device> = emptyList(),
    val integrations: List<IntegrationInfo> = emptyList(),
    val isLoadingIntegrations: Boolean = false,
    val currentSection: ProfileSection = ProfileSection.OVERVIEW,
    val selectedBackgroundIndex: Int = 0,
    val selectedFontIndex: Int = 0,
    val defaultBackgroundIndex: Int = 0,
    val defaultFontIndex: Int = 2,
    val showLogoutConfirmation: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val showEditProfile: Boolean = false,
    val editName: String = "",
    val editEmail: String = "",
    val isSavingProfile: Boolean = false,
    val emailVerificationSent: Boolean = false,
    val error: String? = null
)

/**
 * One-time events for ProfileScreen
 */
sealed class ProfileEvent {
    data class ShowError(val message: String) : ProfileEvent()
    data class ShowSuccess(val message: String) : ProfileEvent()
    data object PreferencesSaved : ProfileEvent()
    data object LoggedOut : ProfileEvent()
    data class DeviceConnected(val device: Device) : ProfileEvent()
    data class DeviceDisconnected(val deviceId: String) : ProfileEvent()
    data class BackgroundChanged(val index: Int) : ProfileEvent()
    data class FontChanged(val index: Int) : ProfileEvent()
    data class DefaultBackgroundSet(val index: Int) : ProfileEvent()
    data class DefaultFontSet(val index: Int) : ProfileEvent()
    data class IntegrationAuthStarted(val provider: String) : ProfileEvent()
    data class IntegrationConnected(val provider: String) : ProfileEvent()
    data class IntegrationDisconnected(val provider: String) : ProfileEvent()
}

/**
 * ViewModel for ProfileScreen
 * Handles user profile, achievements, scenarios, settings, and integrations
 */
class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val integrationRepository: IntegrationRepository = IntegrationRepositoryImpl()
) : BaseViewModel<ProfileUiState, ProfileEvent>() {

    override val initialState = ProfileUiState()

    // Use Cases
    private val getCurrentUserUseCase = AppModule.getCurrentUserUseCase
    private val getUserPreferencesUseCase = AppModule.getUserPreferencesUseCase
    private val updateUserPreferencesUseCase = AppModule.updateUserPreferencesUseCase
    private val getConnectedDevicesUseCase = AppModule.getConnectedDevicesUseCase
    private val getAchievementsUseCase = AppModule.getAchievementsUseCase
    private val getScenariosUseCase = AppModule.getScenariosUseCase

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadCurrentUser()
        loadPreferences()
        loadAchievements()
        loadScenarios()
        loadConnectedDevices()
        loadIntegrations()
    }

    private fun loadCurrentUser() {
        launch {
            updateState { copy(isLoading = true) }
            when (val result = getCurrentUserUseCase()) {
                is Result.Success -> {
                    updateState {
                        copy(
                            user = result.data,
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
                    sendEvent(ProfileEvent.ShowError(result.message ?: "Failed to load user"))
                }
                is Result.Loading -> {
                    updateState { copy(isLoading = true) }
                }
            }
        }
    }

    private fun loadPreferences() {
        launch {
            when (val result = getUserPreferencesUseCase()) {
                is Result.Success -> {
                    // Sync language preference to RemoteDataFetcher on load
                    io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher.currentLanguage = result.data.language
                    updateState {
                        copy(
                            preferences = result.data,
                            liquidGlassPreferences = result.data.liquidGlassPreferences,
                            selectedBackgroundIndex = result.data.selectedBackgroundIndex,
                            selectedFontIndex = result.data.selectedFontIndex,
                            defaultBackgroundIndex = result.data.defaultBackgroundIndex,
                            defaultFontIndex = result.data.defaultFontIndex
                        )
                    }
                }
                is Result.Error -> {
                    sendEvent(ProfileEvent.ShowError(result.message ?: "Failed to load preferences"))
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    private fun loadAchievements() {
        launch {
            when (val result = getAchievementsUseCase()) {
                is Result.Success -> {
                    updateState { copy(achievements = result.data) }
                }
                is Result.Error -> {
                    sendEvent(ProfileEvent.ShowError(result.message ?: "Failed to load achievements"))
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    private fun loadScenarios() {
        launch {
            when (val result = getScenariosUseCase()) {
                is Result.Success -> {
                    updateState { copy(scenarios = result.data) }
                }
                is Result.Error -> {
                    sendEvent(ProfileEvent.ShowError(result.message ?: "Failed to load scenarios"))
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    private fun loadConnectedDevices() {
        launch {
            when (val result = getConnectedDevicesUseCase()) {
                is Result.Success -> {
                    updateState { copy(connectedDevices = result.data) }
                }
                is Result.Error -> {
                    sendEvent(ProfileEvent.ShowError(result.message ?: "Failed to load devices"))
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    fun setSection(section: ProfileSection) {
        updateState { copy(currentSection = section) }
    }

    fun updatePreferences(preferences: UserPreferences) {
        launch {
            when (val result = updateUserPreferencesUseCase(preferences)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            preferences = result.data,
                            liquidGlassPreferences = result.data.liquidGlassPreferences
                        )
                    }
                    sendEvent(ProfileEvent.PreferencesSaved)
                    sendEvent(ProfileEvent.ShowSuccess("Preferences saved"))
                }
                is Result.Error -> {
                    sendEvent(ProfileEvent.ShowError(result.message ?: "Failed to save preferences"))
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    fun selectBackground(index: Int) {
        updateState { copy(selectedBackgroundIndex = index) }

        // Update preferences with new background
        val currentPrefs = currentState.preferences
        if (currentPrefs != null) {
            updatePreferences(currentPrefs.copy(selectedBackgroundIndex = index))
            sendEvent(ProfileEvent.BackgroundChanged(index))
        }
    }

    fun selectFont(index: Int) {
        updateState { copy(selectedFontIndex = index) }

        // Update preferences with new font
        val currentPrefs = currentState.preferences
        if (currentPrefs != null) {
            updatePreferences(currentPrefs.copy(selectedFontIndex = index))
            sendEvent(ProfileEvent.FontChanged(index))
        }
    }

    /**
     * Set the default background (called on double-click).
     * This is persisted and will be used as the initial background.
     */
    fun setDefaultBackground(index: Int) {
        updateState { copy(defaultBackgroundIndex = index, selectedBackgroundIndex = index) }

        val currentPrefs = currentState.preferences
        if (currentPrefs != null) {
            updatePreferences(
                currentPrefs.copy(
                    defaultBackgroundIndex = index,
                    selectedBackgroundIndex = index
                )
            )
            sendEvent(ProfileEvent.DefaultBackgroundSet(index))
            sendEvent(ProfileEvent.ShowSuccess("Default background saved"))
        }
    }

    /**
     * Set the default font (called on double-click).
     * This is persisted and will be used as the initial font.
     */
    fun setDefaultFont(index: Int) {
        updateState { copy(defaultFontIndex = index, selectedFontIndex = index) }

        val currentPrefs = currentState.preferences
        if (currentPrefs != null) {
            updatePreferences(
                currentPrefs.copy(
                    defaultFontIndex = index,
                    selectedFontIndex = index
                )
            )
            sendEvent(ProfileEvent.DefaultFontSet(index))
            sendEvent(ProfileEvent.ShowSuccess("Default font saved"))
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        val currentPrefs = currentState.preferences
        if (currentPrefs != null) {
            updatePreferences(currentPrefs.copy(notificationsEnabled = enabled))
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        val currentPrefs = currentState.preferences
        if (currentPrefs != null) {
            updatePreferences(currentPrefs.copy(darkModeEnabled = enabled))
        }
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        val currentPrefs = currentState.preferences
        if (currentPrefs != null) {
            updatePreferences(currentPrefs.copy(hapticFeedbackEnabled = enabled))
        }
    }

    /**
     * Update the language preference.
     * Persists to user preferences and updates RemoteDataFetcher.currentLanguage
     * so the next LLM API call uses the selected language.
     */
    fun selectLanguage(languageCode: String) {
        io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher.currentLanguage = languageCode
        val currentPrefs = currentState.preferences
        if (currentPrefs != null) {
            val updatedPrefs = currentPrefs.copy(language = languageCode)
            updateState { copy(preferences = updatedPrefs) }
            updatePreferences(updatedPrefs)
        }
    }

    fun updateLiquidGlassPreferences(lgPrefs: LiquidGlassPreferences) {
        val currentPrefs = currentState.preferences
        if (currentPrefs != null) {
            updatePreferences(currentPrefs.copy(liquidGlassPreferences = lgPrefs))
        }
    }

    fun showLogoutConfirmation() {
        updateState { copy(showLogoutConfirmation = true) }
    }

    fun dismissLogoutConfirmation() {
        updateState { copy(showLogoutConfirmation = false) }
    }

    fun confirmLogout() {
        launch {
            authRepository.logout()
            updateState { copy(showLogoutConfirmation = false) }
            sendEvent(ProfileEvent.LoggedOut)
        }
    }

    /**
     * Logout without confirmation (direct call from UI)
     */
    fun logout() {
        launch {
            authRepository.logout()
            sendEvent(ProfileEvent.LoggedOut)
        }
    }

    fun showSettings() {
        updateState { copy(showSettingsSheet = true) }
    }

    fun dismissSettings() {
        updateState { copy(showSettingsSheet = false) }
    }

    fun clearError() {
        updateState { copy(error = null) }
    }

    fun refresh() {
        loadInitialData()
    }

    /**
     * Get achievement completion percentage
     */
    fun getAchievementProgress(): Float {
        val achievements = currentState.achievements ?: return 0f
        return achievements.totalEarned.toFloat() / achievements.totalPossible.toFloat()
    }

    /**
     * Get connected devices count
     */
    fun getConnectedDevicesCount(): Int {
        return currentState.connectedDevices.count { it.isConnected }
    }

    /**
     * Get user display name. Throws if user is not loaded.
     */
    fun getUserDisplayName(): String {
        return currentState.user?.name
            ?: throw IllegalStateException("User not loaded. Cannot get display name.")
    }

    /**
     * Get user initials for avatar
     */
    fun getUserInitials(): String {
        val name = currentState.user?.name ?: return "?"
        val parts = name.split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}"
            parts.isNotEmpty() -> parts[0].take(2)
            else -> "?"
        }.uppercase()
    }

    /**
     * Check if user is premium
     */
    fun isPremiumUser(): Boolean {
        return currentState.user?.isPremium == true
    }

    /**
     * Get growth statistics
     */
    fun getGrowthStats(): GrowthStats {
        val achievements = currentState.achievements
        return GrowthStats(
            totalAchievements = achievements?.totalEarned ?: 0,
            currentStreak = achievements?.streak?.currentStreak ?: 0,
            longestStreak = achievements?.streak?.longestStreak ?: 0,
            weeklyGoalProgress = achievements?.weeklyGoalProgress ?: 0f,
            monthlyGoalProgress = achievements?.monthlyGoalProgress ?: 0f
        )
    }

    // ==================== Profile Edit Methods ====================

    fun showEditProfile() {
        val user = currentState.user ?: return
        updateState {
            copy(
                showEditProfile = true,
                editName = user.name,
                editEmail = user.email
            )
        }
    }

    fun dismissEditProfile() {
        updateState { copy(showEditProfile = false, isSavingProfile = false) }
    }

    fun updateEditName(name: String) {
        updateState { copy(editName = name) }
    }

    fun updateEditEmail(email: String) {
        updateState { copy(editEmail = email) }
    }

    fun saveProfile() {
        val state = currentState
        val user = state.user ?: return

        if (state.editName.isBlank()) {
            sendEvent(ProfileEvent.ShowError("Name cannot be empty"))
            return
        }
        if (state.editEmail.isBlank()) {
            sendEvent(ProfileEvent.ShowError("Email cannot be empty"))
            return
        }

        launch {
            updateState { copy(isSavingProfile = true) }

            val updatedUser = user.copy(
                name = state.editName.trim(),
                email = state.editEmail.trim(),
                initials = state.editName.trim().split(" ").let { parts ->
                    when {
                        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}"
                        parts.isNotEmpty() -> parts[0].take(2)
                        else -> "?"
                    }.uppercase()
                }
            )

            val userRepo = AppModule.userRepository
            when (val result = userRepo.updateUser(updatedUser)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            user = result.data,
                            showEditProfile = false,
                            isSavingProfile = false
                        )
                    }
                    sendEvent(ProfileEvent.ShowSuccess("Profile updated"))
                }
                is Result.Error -> {
                    updateState { copy(isSavingProfile = false) }
                    sendEvent(ProfileEvent.ShowError(result.message ?: "Failed to update profile"))
                }
                is Result.Loading -> {}
            }
        }
    }

    // ==================== Email Verification Methods ====================

    fun requestEmailVerification() {
        launch {
            when (val result = authRepository.requestEmailVerification()) {
                is Result.Success -> {
                    updateState { copy(emailVerificationSent = true) }
                    sendEvent(ProfileEvent.ShowSuccess("Verification email sent"))
                }
                is Result.Error -> {
                    sendEvent(ProfileEvent.ShowError(result.message ?: "Failed to send verification email"))
                }
                is Result.Loading -> {}
            }
        }
    }

    // ==================== Integration Methods ====================

    /**
     * Load all integrations (providers + credentials)
     */
    private fun loadIntegrations() {
        launch {
            updateState { copy(isLoadingIntegrations = true) }
            val result = integrationRepository.getIntegrationInfoList()
            result.fold(
                onSuccess = { integrations ->
                    updateState {
                        copy(
                            integrations = integrations,
                            isLoadingIntegrations = false
                        )
                    }
                },
                onFailure = { error ->
                    KlikLogger.e("ProfileViewModel", "Failed to load integrations: ${error.message}")
                    updateState { copy(isLoadingIntegrations = false) }
                }
            )
        }
    }

    /**
     * Start OAuth authorization flow for a provider.
     *
     * Opens the auth URL in an in-app session (ASWebAuthenticationSession on
     * iOS, browser + deep-link return on Android). On both platforms the
     * upstream provider redirects to klik://oauth-callback?provider=X&...,
     * the system intercepts the scheme and hands the URL back — the user
     * never leaves Klik. We refresh the integrations list immediately so
     * the row flips to Connected without waiting for the next foreground.
     */
    fun authorizeIntegration(providerId: String) {
        launch {
            val result = integrationRepository.getAuthorizationUrl(providerId, callbackScheme = OAUTH_CALLBACK_SCHEME)
            result.fold(
                onSuccess = { response ->
                    sendEvent(ProfileEvent.IntegrationAuthStarted(providerId))
                    when (val outcome = OAuthBrowser.openOAuthSession(response.authorizationUrl, OAUTH_CALLBACK_SCHEME)) {
                        is OAuthSessionResult.Completed -> {
                            KlikLogger.i("ProfileViewModel", "OAuth completed for $providerId (success=${outcome.isSuccess})")
                            if (outcome.isSuccess) {
                                loadIntegrations()
                                sendEvent(ProfileEvent.IntegrationConnected(providerId))
                                sendEvent(ProfileEvent.ShowSuccess("Connected"))
                            } else {
                                sendEvent(ProfileEvent.ShowError("OAuth failed: ${outcome.errorCode ?: "unknown_error"}"))
                            }
                        }
                        is OAuthSessionResult.Cancelled -> {
                            KlikLogger.i("ProfileViewModel", "OAuth cancelled for $providerId")
                        }
                        is OAuthSessionResult.Error -> {
                            KlikLogger.e("ProfileViewModel", "OAuth session error for $providerId: ${outcome.message}")
                            sendEvent(ProfileEvent.ShowError("OAuth failed: ${outcome.message}"))
                        }
                    }
                },
                onFailure = { error ->
                    sendEvent(ProfileEvent.ShowError("Failed to get authorization URL: ${error.message}"))
                }
            )
        }
    }

    /**
     * Disconnect an integration provider
     */
    fun disconnectIntegration(providerId: String) {
        launch {
            val result = integrationRepository.disconnect(providerId)
            result.fold(
                onSuccess = {
                    // Refresh integrations list
                    loadIntegrations()
                    sendEvent(ProfileEvent.IntegrationDisconnected(providerId))
                    sendEvent(ProfileEvent.ShowSuccess("Disconnected successfully"))
                },
                onFailure = { error ->
                    sendEvent(ProfileEvent.ShowError("Failed to disconnect: ${error.message}"))
                }
            )
        }
    }

    /**
     * Refresh integrations after OAuth callback
     * Called when the app returns from browser OAuth flow
     */
    fun refreshIntegrations() {
        launch {
            integrationRepository.refreshIntegrations()
            loadIntegrations()
        }
    }

    /**
     * Get list of unconnected integrations
     */
    fun getUnconnectedIntegrations(): List<IntegrationInfo> {
        return currentState.integrations.filter { !it.connected && it.configured }
    }

    /**
     * Get list of connected integrations
     */
    fun getConnectedIntegrations(): List<IntegrationInfo> {
        return currentState.integrations.filter { it.connected }
    }

    /**
     * Check if there are any unconnected integrations
     */
    fun hasUnconnectedIntegrations(): Boolean {
        return getUnconnectedIntegrations().isNotEmpty()
    }
}

/**
 * Statistics about user growth
 */
data class GrowthStats(
    val totalAchievements: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val weeklyGoalProgress: Float,
    val monthlyGoalProgress: Float
)
