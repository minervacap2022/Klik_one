// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.storage

import io.github.fletchmckee.liquid.samples.app.domain.entity.UserPreferences
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.serialization.json.Json

/**
 * Manages local user preferences storage.
 * User preferences (background, font, liquid glass settings) are stored
 * locally on the device and NOT synced to the backend database.
 */
class LocalPreferencesManager(private val storage: SecureStorage) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * Save user preferences to local storage.
     * @return true if save was successful
     */
    fun savePreferences(preferences: UserPreferences): Boolean {
        return try {
            val serializable = UserPreferencesSerializable.fromDomain(preferences)
            val jsonString = json.encodeToString(UserPreferencesSerializable.serializer(), serializable)
            storage.saveString(PreferenceKeys.USER_PREFERENCES, jsonString)
            KlikLogger.i("LocalPreferences", "Saved preferences: defaultBg=${preferences.defaultBackgroundIndex}, defaultFont=${preferences.defaultFontIndex}")
            true
        } catch (e: Exception) {
            KlikLogger.e("LocalPreferences", "Failed to save preferences: ${e.message}", e)
            false
        }
    }

    /**
     * Load user preferences from local storage.
     * @return UserPreferences or null if not found/failed
     */
    fun loadPreferences(): UserPreferences? {
        return try {
            val jsonString = storage.getString(PreferenceKeys.USER_PREFERENCES)
            if (jsonString != null) {
                val serializable = json.decodeFromString(UserPreferencesSerializable.serializer(), jsonString)
                val prefs = serializable.toDomain()
                KlikLogger.i("LocalPreferences", "Loaded preferences: defaultBg=${prefs.defaultBackgroundIndex}, defaultFont=${prefs.defaultFontIndex}")
                prefs
            } else {
                KlikLogger.i("LocalPreferences", "No saved preferences found")
                null
            }
        } catch (e: Exception) {
            KlikLogger.e("LocalPreferences", "Failed to load preferences: ${e.message}", e)
            null
        }
    }

    /**
     * Clear local preferences (used on logout or reset)
     */
    fun clearPreferences() {
        storage.remove(PreferenceKeys.USER_PREFERENCES)
        KlikLogger.i("LocalPreferences", "Cleared preferences")
    }

    /**
     * Check if local preferences exist
     */
    fun hasPreferences(): Boolean {
        return storage.getString(PreferenceKeys.USER_PREFERENCES) != null
    }
}
