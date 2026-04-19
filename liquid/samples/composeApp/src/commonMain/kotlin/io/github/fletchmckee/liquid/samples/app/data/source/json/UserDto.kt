package io.github.fletchmckee.liquid.samples.app.data.source.json

import io.github.fletchmckee.liquid.samples.app.domain.entity.Device
import io.github.fletchmckee.liquid.samples.app.domain.entity.DeviceType
import io.github.fletchmckee.liquid.samples.app.domain.entity.LiquidGlassPreferences
import io.github.fletchmckee.liquid.samples.app.domain.entity.PlanType
import io.github.fletchmckee.liquid.samples.app.domain.entity.User
import io.github.fletchmckee.liquid.samples.app.domain.entity.UserPreferences
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * DTO for user.json file
 */
@Serializable
data class UserJsonDto(
    val user: UserDto,
    val preferences: UserPreferencesDto,
    val devices: List<DeviceDto>
)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val initials: String,
    val planType: String,
    val avatarUrl: String? = null
)

@Serializable
data class UserPreferencesDto(
    val selectedBackgroundIndex: Int,
    val selectedFontIndex: Int = 2,
    val defaultBackgroundIndex: Int = 0,
    val defaultFontIndex: Int = 2,
    val liquidGlassPreferences: LiquidGlassPreferencesDto,
    val notificationsEnabled: Boolean,
    val darkModeEnabled: Boolean,
    val hapticFeedbackEnabled: Boolean
)

@Serializable
data class LiquidGlassPreferencesDto(
    val transparency: Float,
    val frost: Float,
    val refraction: Float,
    val curve: Float,
    val edge: Float,
    val applyToCards: Boolean
)

@Serializable
data class DeviceDto(
    val id: String,
    val name: String,
    val type: String,
    val isConnected: Boolean,
    val lastSyncOffset: Long
)

/**
 * Extension functions to convert DTOs to domain entities
 */
fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        initials = initials,
        planType = PlanType.fromTierCode(planType),
        avatarUrl = avatarUrl
    )
}

fun UserPreferencesDto.toDomain(): UserPreferences {
    return UserPreferences(
        selectedBackgroundIndex = selectedBackgroundIndex,
        selectedFontIndex = selectedFontIndex,
        defaultBackgroundIndex = defaultBackgroundIndex,
        defaultFontIndex = defaultFontIndex,
        liquidGlassPreferences = liquidGlassPreferences.toDomain(),
        notificationsEnabled = notificationsEnabled,
        darkModeEnabled = darkModeEnabled,
        hapticFeedbackEnabled = hapticFeedbackEnabled
    )
}

/**
 * Convert domain entity back to DTO for serialization
 */
fun UserPreferences.toDto(): UserPreferencesDto {
    return UserPreferencesDto(
        selectedBackgroundIndex = selectedBackgroundIndex,
        selectedFontIndex = selectedFontIndex,
        defaultBackgroundIndex = defaultBackgroundIndex,
        defaultFontIndex = defaultFontIndex,
        liquidGlassPreferences = liquidGlassPreferences.toDto(),
        notificationsEnabled = notificationsEnabled,
        darkModeEnabled = darkModeEnabled,
        hapticFeedbackEnabled = hapticFeedbackEnabled
    )
}

fun LiquidGlassPreferences.toDto(): LiquidGlassPreferencesDto {
    return LiquidGlassPreferencesDto(
        transparency = transparency,
        frost = frost,
        refraction = refraction,
        curve = curve,
        edge = edge,
        applyToCards = applyToCards
    )
}

fun LiquidGlassPreferencesDto.toDomain(): LiquidGlassPreferences {
    return LiquidGlassPreferences(
        transparency = transparency,
        frost = frost,
        refraction = refraction,
        curve = curve,
        edge = edge,
        applyToCards = applyToCards
    )
}

fun DeviceDto.toDomain(): Device {
    val now = Clock.System.now().toEpochMilliseconds()
    return Device(
        id = id,
        name = name,
        type = DeviceType.entries.find { it.name.equals(type, ignoreCase = true) } ?: DeviceType.IPHONE,
        isConnected = isConnected,
        lastSync = now + lastSyncOffset
    )
}
