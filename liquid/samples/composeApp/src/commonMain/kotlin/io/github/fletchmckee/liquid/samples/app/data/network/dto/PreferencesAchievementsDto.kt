// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.network.dto

import kotlinx.serialization.Serializable

/**
 * Wire format for `/api/v1/user/preferences` (mobile API, port 8004).
 * Mirrors `UserPreferencesDto` in `KK_frontendmobile/KK_frontendios/dto_models.py`.
 */
@Serializable
data class RemoteLiquidGlassPreferencesDto(
  val transparency: Float = 0.95f,
  val frost: Float = 20f,
  val refraction: Float = 0.5f,
  val curve: Float = 0.5f,
  val edge: Float = 0.01f,
  val applyToCards: Boolean = false,
)

@Serializable
data class RemoteUserPreferencesDto(
  val selectedBackgroundIndex: Int = 0,
  val selectedFontIndex: Int = 2,
  val liquidGlassPreferences: RemoteLiquidGlassPreferencesDto = RemoteLiquidGlassPreferencesDto(),
  val notificationsEnabled: Boolean = true,
  val darkModeEnabled: Boolean = false,
  val hapticFeedbackEnabled: Boolean = true,
)

/**
 * Wire format for `/api/v1/achievements`. One badge per row.
 * `progress`/`target` are integer counters (e.g. 23/100 sessions).
 * `earnedAt` is ms since epoch when this user crossed the threshold; null if not earned yet.
 */
@Serializable
data class AchievementBadgeDto(
  val key: String,
  val title: String,
  val summary: String,
  val earned: Boolean,
  val progress: Int,
  val target: Int,
  val earnedAt: Long? = null,
)

@Serializable
data class AchievementsListDto(
  val achievements: List<AchievementBadgeDto>,
  val totalEarned: Int,
  val totalPossible: Int,
)
