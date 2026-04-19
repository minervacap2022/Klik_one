package io.github.fletchmckee.liquid.samples.app.data.source.inmemory

import io.github.fletchmckee.liquid.samples.app.data.source.remote.RemoteDataFetcher
import io.github.fletchmckee.liquid.samples.app.data.storage.LocalPreferencesManager
import io.github.fletchmckee.liquid.samples.app.data.storage.SecureStorage
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import io.github.fletchmckee.liquid.samples.app.domain.entity.Device
import io.github.fletchmckee.liquid.samples.app.domain.entity.LiquidGlassPreferences
import io.github.fletchmckee.liquid.samples.app.domain.entity.PlanType
import io.github.fletchmckee.liquid.samples.app.domain.entity.User
import io.github.fletchmckee.liquid.samples.app.domain.entity.UserPreferences
import kotlinx.datetime.Clock

/**
 * In-memory data source for user data.
 * User preferences are stored locally on the device.
 * User profile data comes from the backend via RemoteDataFetcher.
 */
class InMemoryUserDataSource {

    private val localPreferencesManager = LocalPreferencesManager(SecureStorage())

    // Default values (will be replaced by backend data or local storage)
    private var currentUser = User(
        id = "",
        name = "",
        email = "",
        initials = "",
        planType = PlanType.STARTER,
        avatarUrl = null
    )

    private var userPreferences = UserPreferences(
        selectedBackgroundIndex = 0,
        liquidGlassPreferences = LiquidGlassPreferences(
            transparency = 0.95f,
            frost = 20f,
            refraction = 0.5f,
            curve = 0.5f,
            edge = 0.01f,
            applyToCards = false
        ),
        notificationsEnabled = true,
        darkModeEnabled = false,
        hapticFeedbackEnabled = true
    )

    private val connectedDevices = mutableListOf<Device>()

    init {
        // Load local preferences on initialization
        loadLocalPreferences()
    }

    /**
     * Load user preferences from local device storage.
     * This is called on init and restores persistent defaults.
     */
    private fun loadLocalPreferences() {
        val savedPrefs = localPreferencesManager.loadPreferences()
        if (savedPrefs != null) {
            // Restore saved preferences including persistent defaults
            userPreferences = savedPrefs
            RemoteDataFetcher.currentLanguage = savedPrefs.language
            KlikLogger.i("InMemoryUserDataSource", "Loaded local preferences: defaultBg=${savedPrefs.defaultBackgroundIndex}, defaultFont=${savedPrefs.defaultFontIndex}, language=${savedPrefs.language}")
        } else {
            KlikLogger.i("InMemoryUserDataSource", "No saved preferences, using defaults")
        }
    }

    fun getCurrentUser(): User = currentUser

    fun updateUser(user: User): User {
        currentUser = user
        return currentUser
    }

    fun getUserPreferences(): UserPreferences = userPreferences

    fun updateUserPreferences(preferences: UserPreferences): UserPreferences {
        userPreferences = preferences
        // Sync language preference to RemoteDataFetcher for LLM API requests
        RemoteDataFetcher.currentLanguage = preferences.language
        // Persist to local device storage
        localPreferencesManager.savePreferences(preferences)
        return userPreferences
    }

    fun updateLiquidGlassPreferences(preferences: LiquidGlassPreferences): LiquidGlassPreferences {
        userPreferences = userPreferences.copy(liquidGlassPreferences = preferences)
        return preferences
    }

    fun getConnectedDevices(): List<Device> = connectedDevices.toList()

    fun addDevice(device: Device): Device {
        val newDevice = device.copy(
            id = "dev${connectedDevices.size + 1}",
            lastSync = Clock.System.now().toEpochMilliseconds()
        )
        connectedDevices.add(newDevice)
        return newDevice
    }

    fun removeDevice(deviceId: String): Boolean {
        return connectedDevices.removeAll { it.id == deviceId }
    }

    fun updateDeviceStatus(deviceId: String, isConnected: Boolean): Device? {
        val index = connectedDevices.indexOfFirst { it.id == deviceId }
        if (index == -1) return null

        val updated = connectedDevices[index].copy(
            isConnected = isConnected,
            lastSync = if (isConnected) Clock.System.now().toEpochMilliseconds() else connectedDevices[index].lastSync
        )
        connectedDevices[index] = updated
        return updated
    }

    fun updateBackgroundIndex(index: Int): UserPreferences {
        userPreferences = userPreferences.copy(selectedBackgroundIndex = index)
        return userPreferences
    }

    fun logout() {
        // Reset to default state
        currentUser = User(
            id = "",
            name = "",
            email = "",
            initials = "",
            planType = PlanType.STARTER,
            avatarUrl = null
        )
    }
}
