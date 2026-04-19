// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package io.github.fletchmckee.liquid.samples.app.data.storage

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecClass
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.Security.errSecSuccess
import platform.Security.errSecItemNotFound
import platform.Security.errSecDuplicateItem

/**
 * iOS implementation of SecureStorage using the iOS Keychain.
 * Stores sensitive data (auth tokens, credentials) encrypted at rest
 * via the Keychain Services API with kSecClassGenericPassword items.
 */
actual class SecureStorage actual constructor() {

    private val serviceName = "io.klik.app"

    actual fun saveString(key: String, value: String) {
        val valueData = dataFromString(value)
        if (valueData == null) {
            KlikLogger.e(TAG, "Failed to encode value to UTF-8 data for key: $key")
            return
        }

        // Try to add first; if duplicate, update instead
        val addQuery = buildAddQuery(key, valueData)
        val addStatus = SecItemAdd(addQuery, null)
        CFRelease(addQuery)

        when (addStatus) {
            errSecSuccess -> {
                KlikLogger.d(TAG, "Saved key to Keychain: $key")
            }
            errSecDuplicateItem -> {
                // Item already exists, update it
                val searchQuery = buildSearchQuery(key)
                val updateAttrs = buildUpdateAttributes(valueData)
                val updateStatus = SecItemUpdate(searchQuery, updateAttrs)
                CFRelease(searchQuery)
                CFRelease(updateAttrs)

                if (updateStatus == errSecSuccess) {
                    KlikLogger.d(TAG, "Updated key in Keychain: $key")
                } else {
                    KlikLogger.e(TAG, "Failed to update Keychain key: $key, OSStatus: $updateStatus")
                }
            }
            else -> {
                KlikLogger.e(TAG, "Failed to save to Keychain key: $key, OSStatus: $addStatus")
            }
        }
    }

    actual fun getString(key: String): String? {
        return memScoped {
            val result = alloc<CFTypeRefVar>()
            val query = buildRetrieveQuery(key)
            val status = SecItemCopyMatching(query, result.ptr)
            CFRelease(query)

            when (status) {
                errSecSuccess -> {
                    val cfData = result.value
                    if (cfData != null) {
                        val nsData = CFBridgingRelease(cfData) as? NSData
                        val stringValue = nsData?.let { stringFromData(it) }
                        if (stringValue != null) {
                            KlikLogger.d(TAG, "Retrieved key from Keychain: $key")
                        }
                        stringValue
                    } else {
                        null
                    }
                }
                errSecItemNotFound -> {
                    null
                }
                else -> {
                    KlikLogger.e(TAG, "Failed to retrieve Keychain key: $key, OSStatus: $status")
                    null
                }
            }
        }
    }

    actual fun remove(key: String) {
        val query = buildSearchQuery(key)
        val status = SecItemDelete(query)
        CFRelease(query)

        when (status) {
            errSecSuccess -> {
                KlikLogger.d(TAG, "Removed key from Keychain: $key")
            }
            errSecItemNotFound -> {
                KlikLogger.d(TAG, "Key not found in Keychain (already removed): $key")
            }
            else -> {
                KlikLogger.e(TAG, "Failed to remove Keychain key: $key, OSStatus: $status")
            }
        }
    }

    actual fun clear() {
        listOf(
            AuthStorageKeys.IS_LOGGED_IN,
            AuthStorageKeys.USER_ID,
            AuthStorageKeys.ACCESS_TOKEN,
            AuthStorageKeys.REFRESH_TOKEN,
            AuthStorageKeys.USER_NAME,
            AuthStorageKeys.USER_EMAIL,
            PreferenceKeys.USER_PREFERENCES
        ).forEach { key ->
            remove(key)
        }
        KlikLogger.d(TAG, "Cleared all auth keys and preferences from Keychain")
    }

    // ==================== Private Helpers ====================

    /**
     * Build a base search query dictionary with class, service, and account.
     * Used for delete and as the search portion of update.
     */
    private fun buildSearchQuery(key: String): CFDictionaryRef {
        val dict = CFDictionaryCreateMutable(kCFAllocatorDefault, 4, null, null)!!
        CFDictionaryAddValue(dict, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(dict, kSecAttrService, CFBridgingRetain(serviceName as NSString))
        CFDictionaryAddValue(dict, kSecAttrAccount, CFBridgingRetain(key as NSString))
        @Suppress("UNCHECKED_CAST")
        return dict as CFDictionaryRef
    }

    /**
     * Build an add query with class, service, account, and value data.
     */
    private fun buildAddQuery(key: String, valueData: NSData): CFDictionaryRef {
        val dict = CFDictionaryCreateMutable(kCFAllocatorDefault, 4, null, null)!!
        CFDictionaryAddValue(dict, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(dict, kSecAttrService, CFBridgingRetain(serviceName as NSString))
        CFDictionaryAddValue(dict, kSecAttrAccount, CFBridgingRetain(key as NSString))
        CFDictionaryAddValue(dict, kSecValueData, CFBridgingRetain(valueData))
        @Suppress("UNCHECKED_CAST")
        return dict as CFDictionaryRef
    }

    /**
     * Build an attributes dictionary for updating the value data.
     */
    private fun buildUpdateAttributes(valueData: NSData): CFDictionaryRef {
        val dict = CFDictionaryCreateMutable(kCFAllocatorDefault, 1, null, null)!!
        CFDictionaryAddValue(dict, kSecValueData, CFBridgingRetain(valueData))
        @Suppress("UNCHECKED_CAST")
        return dict as CFDictionaryRef
    }

    /**
     * Build a retrieve query that returns the stored data.
     */
    private fun buildRetrieveQuery(key: String): CFDictionaryRef {
        val dict = CFDictionaryCreateMutable(kCFAllocatorDefault, 5, null, null)!!
        CFDictionaryAddValue(dict, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(dict, kSecAttrService, CFBridgingRetain(serviceName as NSString))
        CFDictionaryAddValue(dict, kSecAttrAccount, CFBridgingRetain(key as NSString))
        CFDictionaryAddValue(dict, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(dict, kSecMatchLimit, kSecMatchLimitOne)
        @Suppress("UNCHECKED_CAST")
        return dict as CFDictionaryRef
    }

    /**
     * Encode a String to NSData using UTF-8.
     */
    private fun dataFromString(value: String): NSData? {
        @Suppress("CAST_NEVER_SUCCEEDS")
        val nsString = value as NSString
        return nsString.dataUsingEncoding(NSUTF8StringEncoding)
    }

    /**
     * Decode NSData back to a String using UTF-8.
     */
    private fun stringFromData(data: NSData): String? {
        return NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
    }

    companion object {
        private const val TAG = "SecureStorage"
    }
}
