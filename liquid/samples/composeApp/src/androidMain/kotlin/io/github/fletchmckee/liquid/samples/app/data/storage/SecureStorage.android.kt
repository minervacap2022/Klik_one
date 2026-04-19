// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

/**
 * Android implementation of SecureStorage using EncryptedSharedPreferences.
 * Automatically migrates data from unencrypted SharedPreferences on first use.
 */
actual class SecureStorage actual constructor() {

    private val prefs: SharedPreferences by lazy {
        val context = ApplicationContextProvider.context
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "klik_encrypted_storage",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            // Migrate from unencrypted prefs if needed
            migrateFromUnencrypted(context, encryptedPrefs)
            encryptedPrefs
        } catch (e: Exception) {
            KlikLogger.e(
                "SecureStorage",
                "Failed to create EncryptedSharedPreferences, falling back to standard: ${e.message}",
                e
            )
            context.getSharedPreferences("klik_secure_storage", Context.MODE_PRIVATE)
        }
    }

    /**
     * Migrate data from the old unencrypted SharedPreferences to encrypted storage.
     * Only runs once - removes old prefs after migration.
     */
    private fun migrateFromUnencrypted(context: Context, encryptedPrefs: SharedPreferences) {
        val oldPrefs = context.getSharedPreferences("klik_secure_storage", Context.MODE_PRIVATE)
        val allOldEntries = oldPrefs.all
        if (allOldEntries.isEmpty()) return

        KlikLogger.i("SecureStorage", "Migrating ${allOldEntries.size} entries from unencrypted storage")

        val editor = encryptedPrefs.edit()
        for ((key, value) in allOldEntries) {
            // Only migrate if not already in encrypted prefs
            if (!encryptedPrefs.contains(key)) {
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Set<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        editor.putStringSet(key, value as Set<String>)
                    }
                }
            }
        }
        editor.apply()

        // Clear old unencrypted prefs
        oldPrefs.edit().clear().apply()
        KlikLogger.i("SecureStorage", "Migration complete, cleared unencrypted storage")
    }

    actual fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
        KlikLogger.d("SecureStorage", "Saved key: $key")
    }

    actual fun getString(key: String): String? {
        val value = prefs.getString(key, null)
        if (value != null) {
            KlikLogger.d("SecureStorage", "Retrieved key: $key")
        }
        return value
    }

    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
        KlikLogger.d("SecureStorage", "Removed key: $key")
    }

    actual fun clear() {
        // Remove all auth-related keys
        prefs.edit().apply {
            remove(AuthStorageKeys.IS_LOGGED_IN)
            remove(AuthStorageKeys.USER_ID)
            remove(AuthStorageKeys.ACCESS_TOKEN)
            remove(AuthStorageKeys.REFRESH_TOKEN)
            remove(AuthStorageKeys.USER_NAME)
            remove(AuthStorageKeys.USER_EMAIL)
            remove(PreferenceKeys.USER_PREFERENCES)
        }.apply()
        KlikLogger.d("SecureStorage", "Cleared all auth keys")
    }
}

/**
 * Provides application context for Android platform.
 * Must be initialized in Application.onCreate()
 */
object ApplicationContextProvider {
    lateinit var context: Context
        private set

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }
}
