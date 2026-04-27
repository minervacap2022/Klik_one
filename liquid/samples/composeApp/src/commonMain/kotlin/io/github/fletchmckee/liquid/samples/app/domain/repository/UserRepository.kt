// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Device
import io.github.fletchmckee.liquid.samples.app.domain.entity.LiquidGlassPreferences
import io.github.fletchmckee.liquid.samples.app.domain.entity.User
import io.github.fletchmckee.liquid.samples.app.domain.entity.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user and preferences operations.
 */
interface UserRepository {

  /**
   * Get current user as a reactive flow.
   */
  fun getCurrentUserFlow(): Flow<Result<User>>

  /**
   * Get current user.
   */
  suspend fun getCurrentUser(): Result<User>

  /**
   * Update user profile.
   */
  suspend fun updateUser(user: User): Result<User>

  /**
   * Get user preferences.
   */
  suspend fun getUserPreferences(): Result<UserPreferences>

  /**
   * Update user preferences.
   */
  suspend fun updateUserPreferences(preferences: UserPreferences): Result<UserPreferences>

  /**
   * Update liquid glass preferences.
   */
  suspend fun updateLiquidGlassPreferences(preferences: LiquidGlassPreferences): Result<LiquidGlassPreferences>

  /**
   * Get connected devices.
   */
  suspend fun getConnectedDevices(): Result<List<Device>>

  /**
   * Add a connected device.
   */
  suspend fun addDevice(device: Device): Result<Device>

  /**
   * Remove a connected device.
   */
  suspend fun removeDevice(deviceId: String): Result<Unit>

  /**
   * Update device connection status.
   */
  suspend fun updateDeviceStatus(deviceId: String, isConnected: Boolean): Result<Device>

  /**
   * Update background preference.
   */
  suspend fun updateBackgroundIndex(index: Int): Result<UserPreferences>

  /**
   * Logout user.
   */
  suspend fun logout(): Result<Unit>

  /**
   * Refresh user data from remote source.
   */
  suspend fun refreshUser(): Result<Unit>
}
