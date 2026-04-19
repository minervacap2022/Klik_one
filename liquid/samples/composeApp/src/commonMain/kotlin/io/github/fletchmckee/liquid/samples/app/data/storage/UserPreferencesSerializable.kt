// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.storage

import io.github.fletchmckee.liquid.samples.app.domain.entity.LiquidGlassPreferences
import io.github.fletchmckee.liquid.samples.app.domain.entity.UserPreferences
import kotlinx.serialization.Serializable

/**
 * Serializable version of UserPreferences for local JSON storage.
 * This is stored locally on the device and NOT synced to the backend.
 */
@Serializable
data class UserPreferencesSerializable(
    val selectedBackgroundIndex: Int = 27,  // Brown
    val selectedFontIndex: Int = 5,         // IBM Plex
    val defaultBackgroundIndex: Int = 27,   // Brown
    val defaultFontIndex: Int = 5,          // IBM Plex
    val liquidGlassPreferences: LiquidGlassPreferencesSerializable = LiquidGlassPreferencesSerializable(),
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val language: String = "en"
) {
    /**
     * Convert to domain entity
     */
    fun toDomain(): UserPreferences = UserPreferences(
        selectedBackgroundIndex = selectedBackgroundIndex,
        selectedFontIndex = selectedFontIndex,
        defaultBackgroundIndex = defaultBackgroundIndex,
        defaultFontIndex = defaultFontIndex,
        liquidGlassPreferences = liquidGlassPreferences.toDomain(),
        notificationsEnabled = notificationsEnabled,
        darkModeEnabled = darkModeEnabled,
        hapticFeedbackEnabled = hapticFeedbackEnabled,
        language = language
    )

    companion object {
        /**
         * Create from domain entity
         */
        fun fromDomain(prefs: UserPreferences): UserPreferencesSerializable =
            UserPreferencesSerializable(
                selectedBackgroundIndex = prefs.selectedBackgroundIndex,
                selectedFontIndex = prefs.selectedFontIndex,
                defaultBackgroundIndex = prefs.defaultBackgroundIndex,
                defaultFontIndex = prefs.defaultFontIndex,
                liquidGlassPreferences = LiquidGlassPreferencesSerializable.fromDomain(prefs.liquidGlassPreferences),
                notificationsEnabled = prefs.notificationsEnabled,
                darkModeEnabled = prefs.darkModeEnabled,
                hapticFeedbackEnabled = prefs.hapticFeedbackEnabled,
                language = prefs.language
            )
    }
}

@Serializable
data class LiquidGlassPreferencesSerializable(
    val transparency: Float = 0.95f,
    val frost: Float = 20f,
    val refraction: Float = 0.5f,
    val curve: Float = 0.5f,
    val edge: Float = 0.01f,
    val applyToCards: Boolean = false
) {
    fun toDomain(): LiquidGlassPreferences = LiquidGlassPreferences(
        transparency = transparency,
        frost = frost,
        refraction = refraction,
        curve = curve,
        edge = edge,
        applyToCards = applyToCards
    )

    companion object {
        fun fromDomain(prefs: LiquidGlassPreferences): LiquidGlassPreferencesSerializable =
            LiquidGlassPreferencesSerializable(
                transparency = prefs.transparency,
                frost = prefs.frost,
                refraction = prefs.refraction,
                curve = prefs.curve,
                edge = prefs.edge,
                applyToCards = prefs.applyToCards
            )
    }
}
