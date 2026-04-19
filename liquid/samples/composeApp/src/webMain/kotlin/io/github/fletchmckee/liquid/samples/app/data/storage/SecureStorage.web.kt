// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.storage

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

/**
 * Web implementation of SecureStorage.
 * Uses in-memory storage as a stub - data will not persist across page reloads.
 */
actual class SecureStorage actual constructor() {

    private val memoryStorage = mutableMapOf<String, String>()

    actual fun saveString(key: String, value: String) {
        memoryStorage[key] = value
        KlikLogger.d("SecureStorage", "Saved key: $key (in-memory)")
    }

    actual fun getString(key: String): String? {
        val value = memoryStorage[key]
        if (value != null) {
            KlikLogger.d("SecureStorage", "Retrieved key: $key (in-memory)")
        }
        return value
    }

    actual fun remove(key: String) {
        memoryStorage.remove(key)
        KlikLogger.d("SecureStorage", "Removed key: $key (in-memory)")
    }

    actual fun clear() {
        memoryStorage.clear()
        KlikLogger.d("SecureStorage", "Cleared all keys (in-memory)")
    }
}
