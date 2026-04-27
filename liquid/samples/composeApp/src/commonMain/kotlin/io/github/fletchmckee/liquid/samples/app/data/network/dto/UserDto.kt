// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.network.dto

import io.github.fletchmckee.liquid.samples.app.domain.entity.Device
import io.github.fletchmckee.liquid.samples.app.domain.entity.DeviceType
import io.github.fletchmckee.liquid.samples.app.domain.entity.LiquidGlassPreferences
import io.github.fletchmckee.liquid.samples.app.domain.entity.PlanType
import io.github.fletchmckee.liquid.samples.app.domain.entity.User
import io.github.fletchmckee.liquid.samples.app.domain.entity.UserPreferences

/**
 * DTO for User from API
 */
data class UserDto(
  val id: String,
  val name: String,
  val email: String,
  val initials: String,
  val planType: String,
  val avatarUrl: String?,
) {
  fun toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    initials = initials,
    planType = parsePlanType(planType),
    avatarUrl = avatarUrl,
  )

  private fun parsePlanType(type: String): PlanType = PlanType.fromTierCode(type)

  companion object {
    fun fromDomain(user: User): UserDto = UserDto(
      id = user.id,
      name = user.name,
      email = user.email,
      initials = user.initials,
      planType = user.planType.name,
      avatarUrl = user.avatarUrl,
    )
  }
}

/**
 * DTO for UserPreferences
 */
data class UserPreferencesDto(
  val selectedBackgroundIndex: Int,
  val liquidGlassPreferences: LiquidGlassPreferencesDto,
  val notificationsEnabled: Boolean,
  val darkModeEnabled: Boolean,
  val hapticFeedbackEnabled: Boolean,
) {
  fun toDomain(): UserPreferences = UserPreferences(
    selectedBackgroundIndex = selectedBackgroundIndex,
    liquidGlassPreferences = liquidGlassPreferences.toDomain(),
    notificationsEnabled = notificationsEnabled,
    darkModeEnabled = darkModeEnabled,
    hapticFeedbackEnabled = hapticFeedbackEnabled,
  )

  companion object {
    fun fromDomain(prefs: UserPreferences): UserPreferencesDto = UserPreferencesDto(
      selectedBackgroundIndex = prefs.selectedBackgroundIndex,
      liquidGlassPreferences = LiquidGlassPreferencesDto.fromDomain(prefs.liquidGlassPreferences),
      notificationsEnabled = prefs.notificationsEnabled,
      darkModeEnabled = prefs.darkModeEnabled,
      hapticFeedbackEnabled = prefs.hapticFeedbackEnabled,
    )
  }
}

/**
 * DTO for LiquidGlassPreferences
 */
data class LiquidGlassPreferencesDto(
  val transparency: Float,
  val frost: Float,
  val refraction: Float,
  val curve: Float,
  val edge: Float,
  val applyToCards: Boolean,
) {
  fun toDomain(): LiquidGlassPreferences = LiquidGlassPreferences(
    transparency = transparency,
    frost = frost,
    refraction = refraction,
    curve = curve,
    edge = edge,
    applyToCards = applyToCards,
  )

  companion object {
    fun fromDomain(prefs: LiquidGlassPreferences): LiquidGlassPreferencesDto = LiquidGlassPreferencesDto(
      transparency = prefs.transparency,
      frost = prefs.frost,
      refraction = prefs.refraction,
      curve = prefs.curve,
      edge = prefs.edge,
      applyToCards = prefs.applyToCards,
    )
  }
}

/**
 * DTO for Device
 */
data class DeviceDto(
  val id: String,
  val name: String,
  val type: String,
  val isConnected: Boolean,
  val lastSync: Long? = null,
) {
  fun toDomain(): Device = Device(
    id = id,
    name = name,
    type = parseDeviceType(type),
    isConnected = isConnected,
    lastSync = lastSync,
  )

  private fun parseDeviceType(type: String): DeviceType = when (type.uppercase()) {
    "IPHONE" -> DeviceType.IPHONE
    "IPAD" -> DeviceType.IPAD
    "MACBOOK" -> DeviceType.MACBOOK
    "APPLE_WATCH" -> DeviceType.APPLE_WATCH
    "KLIK_GLASS" -> DeviceType.KLIK_GLASS
    else -> DeviceType.OTHER
  }

  companion object {
    fun fromDomain(device: Device): DeviceDto = DeviceDto(
      id = device.id,
      name = device.name,
      type = device.type.name,
      isConnected = device.isConnected,
      lastSync = device.lastSync,
    )
  }
}

/**
 * Request body for updating user preferences
 */
data class UpdateUserPreferencesRequest(
  val selectedBackgroundIndex: Int? = null,
  val liquidGlassPreferences: LiquidGlassPreferencesDto? = null,
  val notificationsEnabled: Boolean? = null,
  val darkModeEnabled: Boolean? = null,
  val hapticFeedbackEnabled: Boolean? = null,
)

/**
 * Request body for adding a device
 */
data class AddDeviceRequest(
  val name: String,
  val type: String,
)
