// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.user

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.LiquidGlassPreferences
import io.github.fletchmckee.liquid.samples.app.domain.entity.UserPreferences
import io.github.fletchmckee.liquid.samples.app.domain.repository.UserRepository

/**
 * Use case for updating user preferences.
 */
class UpdateUserPreferencesUseCase(
  private val userRepository: UserRepository,
) {
  suspend operator fun invoke(preferences: UserPreferences): Result<UserPreferences> = userRepository.updateUserPreferences(preferences)

  suspend fun updateLiquidGlass(preferences: LiquidGlassPreferences): Result<LiquidGlassPreferences> = userRepository.updateLiquidGlassPreferences(preferences)

  suspend fun updateBackground(index: Int): Result<UserPreferences> = userRepository.updateBackgroundIndex(index)
}
