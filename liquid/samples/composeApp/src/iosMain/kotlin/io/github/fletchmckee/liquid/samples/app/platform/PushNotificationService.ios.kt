package io.github.fletchmckee.liquid.samples.app.platform

import io.github.fletchmckee.liquid.samples.app.data.network.ApiConfig
import io.github.fletchmckee.liquid.samples.app.data.network.CurrentUser
import io.github.fletchmckee.liquid.samples.app.data.network.HttpClient
import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of PushNotificationService.
 *
 * Reads device token from NSUserDefaults (set by Swift PushNotificationService)
 * and sends it to KK_notifications backend.
 */
actual class PushNotificationService actual constructor() {

    private val deviceTokenKey = "apns_device_token"
    private val tokenTimestampKey = "apns_token_timestamp"

    /**
     * Register the device token with the KK_notifications backend.
     *
     * Reads token from NSUserDefaults and sends to backend.
     *
     * @return True if registration was successful
     */
    actual suspend fun registerDeviceToken(): Boolean {
        val token = getStoredToken()
        if (token == null) {
            KlikLogger.w("PushNotificationService", "No stored device token found")
            return false
        }
        return registerDeviceToken(token)
    }

    /**
     * Register a specific device token with the KK_notifications backend.
     *
     * @param token APNs device token string
     * @return True if registration was successful
     */
    actual suspend fun registerDeviceToken(token: String): Boolean {
        val userId = CurrentUser.userId
        if (userId == null) {
            KlikLogger.w("PushNotificationService", "Cannot register device: no user logged in")
            return false
        }

        val url = "${ApiConfig.NOTIFICATIONS_BASE_URL}${ApiConfig.Endpoints.REGISTER_DEVICE}"
        val body = buildJsonObject {
            put("device_token", token)
            put("platform", "ios")
        }.toString()

        KlikLogger.d("PushNotificationService", "Registering device token for user: $userId")

        val response = HttpClient.postUrl(url, body)
        
        return if (response != null) {
            KlikLogger.i("PushNotificationService", "Device token registered successfully")
            true
        } else {
            KlikLogger.e("PushNotificationService", "Failed to register device token")
            false
        }
    }

    /**
     * Unregister the current device token from the KK_notifications backend.
     *
     * @return True if unregistration was successful
     */
    actual suspend fun unregisterDeviceToken(): Boolean {
        val token = getStoredToken()
        if (token == null) {
            KlikLogger.w("PushNotificationService", "No stored device token to unregister")
            return false
        }

        val userId = CurrentUser.userId
        if (userId == null) {
            KlikLogger.w("PushNotificationService", "Cannot unregister device: no user logged in")
            return false
        }

        val url = "${ApiConfig.NOTIFICATIONS_BASE_URL}${ApiConfig.Endpoints.UNREGISTER_DEVICE}/$token"

        KlikLogger.d("PushNotificationService", "Unregistering device token for user: $userId")

        val response = HttpClient.deleteUrl(url)
        
        return if (response != null) {
            KlikLogger.i("PushNotificationService", "Device token unregistered successfully")
            // Clear local storage
            clearStoredToken()
            true
        } else {
            KlikLogger.e("PushNotificationService", "Failed to unregister device token")
            false
        }
    }

    /**
     * Get the stored device token from NSUserDefaults.
     *
     * @return Device token string, or null if not registered
     */
    actual fun getStoredToken(): String? {
        return NSUserDefaults.standardUserDefaults.stringForKey(deviceTokenKey)
    }

    /**
     * Check if the device has a registered push token.
     *
     * @return True if a token is stored
     */
    actual fun hasStoredToken(): Boolean {
        return getStoredToken() != null
    }

    /**
     * Clear the stored device token from NSUserDefaults.
     */
    private fun clearStoredToken() {
        val defaults = NSUserDefaults.standardUserDefaults
        defaults.removeObjectForKey(deviceTokenKey)
        defaults.removeObjectForKey(tokenTimestampKey)
        defaults.synchronize()
        KlikLogger.d("PushNotificationService", "Stored token cleared")
    }
}
