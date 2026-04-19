// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.storage

/**
 * Platform-specific secure storage for sensitive data like auth tokens.
 * - iOS: Uses Keychain for secure storage
 * - Android: Uses EncryptedSharedPreferences
 */
expect class SecureStorage() {
    /**
     * Save a string value
     */
    fun saveString(key: String, value: String)

    /**
     * Get a string value, or null if not found
     */
    fun getString(key: String): String?

    /**
     * Remove a value
     */
    fun remove(key: String)

    /**
     * Clear all stored values
     */
    fun clear()
}

/**
 * Keys for auth storage
 */
object AuthStorageKeys {
    const val IS_LOGGED_IN = "auth_is_logged_in"
    const val USER_ID = "auth_user_id"
    const val ACCESS_TOKEN = "auth_access_token"
    const val REFRESH_TOKEN = "auth_refresh_token"
    const val USER_NAME = "auth_user_name"
    const val USER_EMAIL = "auth_user_email"
    const val DEVICE_ID = "device_id"
}

/**
 * Keys for user preferences storage (local-only, not synced to backend)
 */
object PreferenceKeys {
    const val USER_PREFERENCES = "local_user_preferences_json"
}

/**
 * Keys for integration prompt preferences
 */
object IntegrationStorageKeys {
    // If "true", never show integration prompt again (user preference)
    const val NEVER_PROMPT_INTEGRATIONS = "integration_never_prompt"
    // Timestamp of last prompt dismissal (for session-level tracking)
    const val LAST_PROMPT_DISMISSED_AT = "integration_last_prompt_dismissed"
    // JSON array of providers that user has declined to connect
    const val DECLINED_PROVIDERS = "integration_declined_providers"
}

/**
 * Keys for Klik One first-run onboarding completion. Scoped by user_id so the
 * flow re-prompts when a different account signs in on the same device.
 */
object KlikOneOnboardingKeys {
    private const val COMPLETED_PREFIX = "klikone_onboarding_completed"
    private const val ROLE_PREFIX = "klikone_onboarding_role"

    fun completedKey(userId: String): String = "$COMPLETED_PREFIX:$userId"
    fun roleKey(userId: String): String = "$ROLE_PREFIX:$userId"
}

/**
 * Keys for Apple native integration storage.
 * Used to track permission status for Calendar and Reminders on iOS.
 */
object AppleIntegrationStorageKeys {
    // If "true", calendar permission has been granted
    const val CALENDAR_PERMISSION_GRANTED = "apple_calendar_granted"
    // If "true", reminders permission has been granted
    const val REMINDERS_PERMISSION_GRANTED = "apple_reminders_granted"
    // If "true", never show Apple integration prompt again
    const val NEVER_PROMPT_APPLE = "apple_integration_never_prompt"
    // Timestamp of last Apple permission request (to avoid spamming)
    const val LAST_PERMISSION_REQUEST_AT = "apple_last_permission_request"
    // JSON sets of synced IDs for deduplication (auto-sync to Apple Calendar/Reminders)
    const val SYNCED_CALENDAR_IDS = "apple_synced_calendar_ids"
    const val SYNCED_REMINDER_TODO_IDS = "apple_synced_reminder_todo_ids"
}
