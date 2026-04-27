// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.network

import io.github.fletchmckee.liquid.samples.app.data.storage.AuthStorageKeys
import io.github.fletchmckee.liquid.samples.app.data.storage.SecureStorage
import io.github.fletchmckee.liquid.samples.app.getPlatform
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlin.random.Random

/**
 * Device information for API authentication.
 * Manages device identification and metadata.
 * Device ID is persisted to SecureStorage so it survives app restarts.
 */
object DeviceInfo {
  private var _deviceId: String? = null
  private val storage by lazy { SecureStorage() }

  /**
   * Get or generate a unique device ID.
   * First checks memory cache, then SecureStorage, then generates new.
   */
  fun getDeviceId(): String {
    if (_deviceId == null) {
      // Try to load from persistent storage
      val persisted = storage.getString(AuthStorageKeys.DEVICE_ID)
      if (persisted != null) {
        _deviceId = persisted
        KlikLogger.i("DeviceInfo", "Restored device_id from storage: $persisted")
      } else {
        // Generate a unique device ID
        // Format: klik_mobile_{platform}_{random}
        val randomSuffix = Random.nextInt(100000, 999999)
        _deviceId = "klik_mobile_${getPlatform()}_$randomSuffix"
        // Persist for future sessions
        storage.saveString(AuthStorageKeys.DEVICE_ID, _deviceId!!)
        KlikLogger.i("DeviceInfo", "Generated new device_id: $_deviceId")
      }
    }
    return _deviceId!!
  }

  /**
   * Set a custom device ID. Use this to restore a persisted device ID.
   */
  fun setDeviceId(deviceId: String) {
    _deviceId = deviceId
    storage.saveString(AuthStorageKeys.DEVICE_ID, deviceId)
  }

  /**
   * Get platform identifier string from the Platform object.
   */
  private fun getPlatformString(): String {
    val platformName = getPlatform().name.lowercase()
    return when {
      platformName.contains("ios") || platformName.contains("iphone") || platformName.contains("ipad") -> "ios"
      platformName.contains("android") -> "android"
      platformName.contains("java") || platformName.contains("jvm") -> "jvm"
      platformName.contains("web") || platformName.contains("browser") -> "web"
      else -> "mobile"
    }
  }

  /**
   * Get a human-readable device name.
   * Format: "Klik {Platform}" (e.g., "Klik iOS", "Klik Android")
   */
  fun getDeviceName(): String = when (getPlatformString()) {
    "ios" -> "Klik iOS"
    "android" -> "Klik Android"
    "jvm" -> "Klik Desktop"
    "web" -> "Klik Web"
    else -> "Klik Mobile"
  }

  /**
   * Get the device type for the backend.
   * Maps to backend's expected device types: phone_ios, phone_android, web, etc.
   */
  fun getDeviceType(): String = when (getPlatformString()) {
    "ios" -> "phone_ios"
    "android" -> "phone_android"
    "jvm" -> "desktop"
    "web" -> "web"
    else -> "mobile"
  }

  /**
   * Reset device ID (for testing or logout).
   * Note: Device ID is device-specific, not user-specific,
   * so we generally do NOT reset on logout.
   */
  fun reset() {
    _deviceId = null
    storage.remove(AuthStorageKeys.DEVICE_ID)
  }
}
