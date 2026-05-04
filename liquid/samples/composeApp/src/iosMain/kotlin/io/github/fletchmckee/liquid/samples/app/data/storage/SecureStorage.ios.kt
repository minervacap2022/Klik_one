// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package io.github.fletchmckee.liquid.samples.app.data.storage

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
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
import platform.Security.errSecDuplicateItem
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * iOS implementation of SecureStorage using the iOS Keychain.
 *
 * Stores sensitive data (auth tokens, credentials) encrypted at rest via the Keychain
 * Services API with `kSecClassGenericPassword` items.
 *
 * Two correctness properties enforced here that earlier revisions missed:
 *
 * 1. **No CFTypeRef leaks.** Earlier revisions called `CFBridgingRetain(...)` on every
 *    NSString/NSData going into a query dictionary, but the dictionary was created with
 *    null retain/release callbacks — meaning the dict didn't release them, and `CFRelease(dict)`
 *    leaked the bridged objects. Each `saveString` leaked ~3 retains. Now we use the standard
 *    `kCFTypeDictionaryKeyCallBacks` / `kCFTypeDictionaryValueCallBacks`, which makes the
 *    dictionary itself responsible for retaining/releasing values, and we hand off ownership
 *    via `CFBridgingRetain` exactly once.
 *
 * 2. **`kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly`.** Without an explicit accessibility
 *    class, items default to `kSecAttrAccessibleWhenUnlocked` and migrate via iCloud Keychain
 *    on device restore — meaning a restored device could carry forward another user's auth
 *    token. The `*ThisDeviceOnly` suffix prevents that migration; `AfterFirstUnlock` keeps the
 *    item available to background workers (e.g. notification handlers) that run before the
 *    user manually unlocks the screen.
 */
actual class SecureStorage actual constructor() {

  private val serviceName = "io.klik.app"

  actual fun saveString(key: String, value: String) {
    val valueData = dataFromString(value)
    if (valueData == null) {
      KlikLogger.e(TAG, "Failed to encode value to UTF-8 data for key: $key")
      return
    }

    withMutableDict { addQuery ->
      populateBaseAttributes(addQuery, key)
      // Bridge with explicit ownership transfer — the dictionary's value-callbacks will
      // CFRetain/CFRelease as needed; we balance the CFBridgingRetain by allowing the
      // dictionary to drop its reference when CFRelease(dict) runs.
      addCFBridgedValue(addQuery, kSecValueData, valueData)
      addCFValue(addQuery, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)

      val addStatus = SecItemAdd(addQuery as CFDictionaryRef, null)

      when (addStatus) {
        errSecSuccess -> KlikLogger.d(TAG, "Saved key to Keychain: $key")

        errSecDuplicateItem -> withMutableDict { searchQuery ->
          populateBaseAttributes(searchQuery, key)
          withMutableDict { updateAttrs ->
            addCFBridgedValue(updateAttrs, kSecValueData, valueData)
            addCFValue(updateAttrs, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
            val updateStatus = SecItemUpdate(searchQuery as CFDictionaryRef, updateAttrs as CFDictionaryRef)
            if (updateStatus == errSecSuccess) {
              KlikLogger.d(TAG, "Updated key in Keychain: $key")
            } else {
              KlikLogger.e(TAG, "Failed to update Keychain key: $key, OSStatus: $updateStatus")
            }
          }
        }

        else -> KlikLogger.e(TAG, "Failed to save to Keychain key: $key, OSStatus: $addStatus")
      }
    }
  }

  actual fun getString(key: String): String? = memScoped {
    var result: String? = null
    val resultVar = alloc<CFTypeRefVar>()
    withMutableDict { query ->
      populateBaseAttributes(query, key)
      addCFValue(query, kSecReturnData, kCFBooleanTrue)
      addCFValue(query, kSecMatchLimit, kSecMatchLimitOne)
      val status = SecItemCopyMatching(query as CFDictionaryRef, resultVar.ptr)
      when (status) {
        errSecSuccess -> {
          val cfData = resultVar.value
          if (cfData != null) {
            // CFBridgingRelease transfers ownership: the underlying CFData is released with
            // the bridged NSData when its Kotlin reference goes out of scope.
            val nsData = CFBridgingRelease(cfData) as? NSData
            result = nsData?.let { stringFromData(it) }
            if (result != null) {
              KlikLogger.d(TAG, "Retrieved key from Keychain: $key")
            }
          }
        }
        errSecItemNotFound -> {
          // Quiet: a missing key is the normal "not logged in" path.
        }
        else -> KlikLogger.e(TAG, "Failed to retrieve Keychain key: $key, OSStatus: $status")
      }
    }
    result
  }

  actual fun remove(key: String) {
    withMutableDict { query ->
      populateBaseAttributes(query, key)
      val status = SecItemDelete(query as CFDictionaryRef)
      when (status) {
        errSecSuccess -> KlikLogger.d(TAG, "Removed key from Keychain: $key")
        errSecItemNotFound -> KlikLogger.d(TAG, "Key not found in Keychain (already removed): $key")
        else -> KlikLogger.e(TAG, "Failed to remove Keychain key: $key, OSStatus: $status")
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
      PreferenceKeys.USER_PREFERENCES,
    ).forEach { key ->
      remove(key)
    }
    KlikLogger.d(TAG, "Cleared all auth keys and preferences from Keychain")
  }

  // ==================== Private Helpers ====================

  /**
   * Run [block] with a freshly-created CFMutableDictionaryRef using the standard
   * type-aware key/value callbacks. The dictionary is released after [block] returns,
   * regardless of how it returns. Values added via [addCFBridgedValue] / [addCFValue] are
   * retained by the dictionary and released when the dictionary is released.
   */
  private inline fun <T> withMutableDict(block: (CFMutableDictionaryRef) -> T): T {
    val dict = CFDictionaryCreateMutable(
      kCFAllocatorDefault,
      6,
      kCFTypeDictionaryKeyCallBacks.ptr,
      kCFTypeDictionaryValueCallBacks.ptr,
    )!!
    try {
      return block(dict)
    } finally {
      CFRelease(dict)
    }
  }

  /**
   * Populate the (class, service, account) tuple that identifies a Keychain item for
   * service `io.klik.app` and account [key]. Uses [addCFBridgedValue] for the bridged
   * NSString account so its retain count is balanced by the dictionary.
   */
  private fun populateBaseAttributes(dict: CFMutableDictionaryRef, key: String) {
    addCFValue(dict, kSecClass, kSecClassGenericPassword)
    addCFBridgedValue(dict, kSecAttrService, serviceName as NSString)
    addCFBridgedValue(dict, kSecAttrAccount, key as NSString)
  }

  /**
   * Add a non-bridged CFTypeRef value (e.g. one of the `kSec*` constants) to the dictionary.
   * The dictionary's value-callbacks will retain it; nothing extra to release here.
   */
  private fun addCFValue(dict: CFMutableDictionaryRef, key: CFTypeRef?, value: CFTypeRef?) {
    CFDictionaryAddValue(dict, key, value)
  }

  /**
   * Add a bridged Foundation value (NSString, NSData, etc.) to the dictionary.
   *
   * `CFBridgingRetain` returns a +1 CFTypeRef. The dictionary's value-callbacks retain
   * it again (+2). When the dictionary is released, its retain is dropped (+1). We
   * immediately release the bridged ref to balance, leaving the value at refcount 1
   * owned solely by the dictionary, which releases it on `CFRelease(dict)`.
   */
  private fun addCFBridgedValue(dict: CFMutableDictionaryRef, key: CFTypeRef?, value: Any) {
    val cfRef = CFBridgingRetain(value) ?: return
    try {
      CFDictionaryAddValue(dict, key, cfRef)
    } finally {
      CFRelease(cfRef)
    }
  }

  private fun dataFromString(value: String): NSData? {
    val nsString = value as NSString
    return nsString.dataUsingEncoding(NSUTF8StringEncoding)
  }

  private fun stringFromData(data: NSData): String? =
    NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String

  companion object {
    private const val TAG = "SecureStorage"
  }
}
