// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryUserDataSource
import io.github.fletchmckee.liquid.samples.app.domain.entity.Device
import io.github.fletchmckee.liquid.samples.app.domain.entity.LiquidGlassPreferences
import io.github.fletchmckee.liquid.samples.app.domain.entity.User
import io.github.fletchmckee.liquid.samples.app.domain.entity.UserPreferences
import io.github.fletchmckee.liquid.samples.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Implementation of UserRepository.
 * PRODUCTION: Requires InMemoryUserDataSource - no optional dependencies.
 */
class UserRepositoryImpl(
  private val dataSource: InMemoryUserDataSource,
) : UserRepository {

  private val _userFlow = MutableStateFlow<Result<User>>(Result.Loading)

  init {
    refreshUserInternal()
  }

  private fun refreshUserInternal() {
    try {
      val user = dataSource.getCurrentUser()
      _userFlow.value = Result.Success(user)
    } catch (e: Exception) {
      _userFlow.value = Result.Error(e, "Failed to load user")
    }
  }

  override fun getCurrentUserFlow(): Flow<Result<User>> = _userFlow

  override suspend fun getCurrentUser(): Result<User> = try {
    val user = dataSource.getCurrentUser()
    Result.Success(user)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get current user")
  }

  override suspend fun updateUser(user: User): Result<User> = try {
    val updated = dataSource.updateUser(user)
      ?: throw NoSuchElementException("Failed to update user")
    refreshUserInternal()
    Result.Success(updated)
  } catch (e: Exception) {
    Result.Error(e, "Failed to update user")
  }

  override suspend fun getUserPreferences(): Result<UserPreferences> = try {
    val prefs = dataSource.getUserPreferences()
    Result.Success(prefs)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get user preferences")
  }

  override suspend fun updateUserPreferences(preferences: UserPreferences): Result<UserPreferences> = try {
    val updated = dataSource.updateUserPreferences(preferences)
      ?: throw NoSuchElementException("Failed to update preferences")
    Result.Success(updated)
  } catch (e: Exception) {
    Result.Error(e, "Failed to update user preferences")
  }

  override suspend fun updateLiquidGlassPreferences(preferences: LiquidGlassPreferences): Result<LiquidGlassPreferences> = try {
    val updated = dataSource.updateLiquidGlassPreferences(preferences)
      ?: throw NoSuchElementException("Failed to update preferences")
    Result.Success(updated)
  } catch (e: Exception) {
    Result.Error(e, "Failed to update liquid glass preferences")
  }

  override suspend fun getConnectedDevices(): Result<List<Device>> = try {
    val devices = dataSource.getConnectedDevices()
    Result.Success(devices)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get connected devices")
  }

  override suspend fun addDevice(device: Device): Result<Device> = try {
    val added = dataSource.addDevice(device)
      ?: throw NoSuchElementException("Failed to add device")
    Result.Success(added)
  } catch (e: Exception) {
    Result.Error(e, "Failed to add device")
  }

  override suspend fun removeDevice(deviceId: String): Result<Unit> = try {
    val success = dataSource.removeDevice(deviceId)
    if (!success) {
      throw NoSuchElementException("Device not found: $deviceId")
    }
    Result.Success(Unit)
  } catch (e: Exception) {
    Result.Error(e, "Failed to remove device")
  }

  override suspend fun updateDeviceStatus(deviceId: String, isConnected: Boolean): Result<Device> = try {
    val device = dataSource.updateDeviceStatus(deviceId, isConnected)
      ?: throw NoSuchElementException("Device not found: $deviceId")
    Result.Success(device)
  } catch (e: Exception) {
    Result.Error(e, "Failed to update device status")
  }

  override suspend fun updateBackgroundIndex(index: Int): Result<UserPreferences> = try {
    val prefs = dataSource.updateBackgroundIndex(index)
      ?: throw NoSuchElementException("Failed to update background")
    Result.Success(prefs)
  } catch (e: Exception) {
    Result.Error(e, "Failed to update background index")
  }

  override suspend fun logout(): Result<Unit> = try {
    dataSource.logout()
    Result.Success(Unit)
  } catch (e: Exception) {
    Result.Error(e, "Failed to logout")
  }

  override suspend fun refreshUser(): Result<Unit> = try {
    refreshUserInternal()
    Result.Success(Unit)
  } catch (e: Exception) {
    Result.Error(e, "Failed to refresh user")
  }
}
