// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.platform

/**
 * Push Notification Service for registering device tokens with KK_notifications backend.
 *
 * Platform-specific implementations handle:
 * - iOS: Reading device token from UserDefaults (set by Swift PushNotificationService)
 * - Sending token to KK_notifications API
 *
 * All operations are user-specific via JWT auth (user_id extracted from token).
 */
expect class PushNotificationService() {

  /**
   * Register the device token with the KK_notifications backend.
   *
   * On iOS, reads the token from UserDefaults (set by Swift layer).
   * Sends POST to /api/notifications/v1/devices/register.
   *
   * @return True if registration was successful
   */
  suspend fun registerDeviceToken(): Boolean

  /**
   * Register a specific device token with the KK_notifications backend.
   *
   * @param token APNs device token string
   * @return True if registration was successful
   */
  suspend fun registerDeviceToken(token: String): Boolean

  /**
   * Unregister the current device token from the KK_notifications backend.
   *
   * Call this on user logout to stop receiving notifications.
   *
   * @return True if unregistration was successful
   */
  suspend fun unregisterDeviceToken(): Boolean

  /**
   * Get the stored device token (if available).
   *
   * On iOS, reads from UserDefaults.
   *
   * @return Device token string, or null if not registered
   */
  fun getStoredToken(): String?

  /**
   * Check if the device has a registered push token.
   *
   * @return True if a token is stored
   */
  fun hasStoredToken(): Boolean
}
