// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.storage

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.browser.localStorage

/**
 * JS (Browser) implementation of SecureStorage using localStorage.
 * Note: localStorage is not truly secure; for sensitive data in production,
 * consider server-side storage with secure cookies.
 */
actual class SecureStorage actual constructor() {

    actual fun saveString(key: String, value: String) {
        localStorage.setItem(key, value)
        KlikLogger.d("SecureStorage", "Saved key: $key")
    }

    actual fun getString(key: String): String? {
        val value = localStorage.getItem(key)
        if (value != null) {
            KlikLogger.d("SecureStorage", "Retrieved key: $key")
        }
        return value
    }

    actual fun remove(key: String) {
        localStorage.removeItem(key)
        KlikLogger.d("SecureStorage", "Removed key: $key")
    }

    actual fun clear() {
        // Remove all auth-related keys
        listOf(
            AuthStorageKeys.IS_LOGGED_IN,
            AuthStorageKeys.USER_ID,
            AuthStorageKeys.ACCESS_TOKEN,
            AuthStorageKeys.REFRESH_TOKEN,
            AuthStorageKeys.USER_NAME,
            AuthStorageKeys.USER_EMAIL,
            PreferenceKeys.USER_PREFERENCES
        ).forEach { key ->
            localStorage.removeItem(key)
        }
        KlikLogger.d("SecureStorage", "Cleared all auth keys")
    }
}
