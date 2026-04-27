// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.storage

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import java.util.prefs.Preferences

/**
 * JVM (Desktop) implementation of SecureStorage using Java Preferences API.
 */
actual class SecureStorage actual constructor() {

  private val prefs: Preferences = Preferences.userNodeForPackage(SecureStorage::class.java)

  actual fun saveString(key: String, value: String) {
    prefs.put(key, value)
    prefs.flush()
    KlikLogger.d("SecureStorage", "Saved key: $key")
  }

  actual fun getString(key: String): String? {
    val value = prefs.get(key, null)
    if (value != null) {
      KlikLogger.d("SecureStorage", "Retrieved key: $key")
    }
    return value
  }

  actual fun remove(key: String) {
    prefs.remove(key)
    prefs.flush()
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
      PreferenceKeys.USER_PREFERENCES,
    ).forEach { key ->
      prefs.remove(key)
    }
    prefs.flush()
    KlikLogger.d("SecureStorage", "Cleared all auth keys")
  }
}
