package io.github.fletchmckee.liquid.samples.app.domain.entity

/**
 * Domain entity representing the current user.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val initials: String,
    val planType: PlanType,
    val avatarUrl: String? = null
) {
    val isPremium: Boolean
        get() = planType == PlanType.PRO

    /**
     * Returns avatarUrl if set, otherwise generates a default avatar URL
     * using ui-avatars.com based on the user's name.
     */
    val displayAvatarUrl: String
        get() = avatarUrl ?: "https://ui-avatars.com/api/?name=${name.replace(" ", "+")}&size=128&background=random&color=fff&bold=true"
}

enum class PlanType(val label: String, val tierCode: String) {
    STARTER("Starter", "starter"),
    BASIC("Basic", "basic"),
    PRO("Pro", "pro");

    companion object {
        fun fromTierCode(code: String): PlanType = when (code.lowercase()) {
            "starter" -> STARTER
            "basic" -> BASIC
            "pro" -> PRO
            else -> throw IllegalArgumentException("Unknown subscription tier: $code")
        }
    }
}

/**
 * Connected device in user's ecosystem
 */
data class Device(
    val id: String,
    val name: String,
    val type: DeviceType,
    val isConnected: Boolean,
    val lastSync: Long? = null
)

enum class DeviceType {
    MACBOOK,
    IPHONE,
    IPAD,
    APPLE_WATCH,
    KLIK_GLASS,
    OTHER
}

/**
 * User preferences for app customization
 */
data class UserPreferences(
    val selectedBackgroundIndex: Int = 27,  // Brown
    val selectedFontIndex: Int = 5,         // IBM Plex
    val defaultBackgroundIndex: Int = 27,   // Brown - Persistent default - set by double-click
    val defaultFontIndex: Int = 5,          // IBM Plex - Persistent default - set by double-click
    val liquidGlassPreferences: LiquidGlassPreferences = LiquidGlassPreferences(),
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val language: String = "en"
)

data class LiquidGlassPreferences(
    val transparency: Float = 0.95f,
    val frost: Float = 20f,
    val refraction: Float = 0.5f,
    val curve: Float = 0.5f,
    val edge: Float = 0.01f,
    val applyToCards: Boolean = false
)
