// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.user

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.UserPreferences
import io.github.fletchmckee.liquid.samples.app.domain.repository.UserRepository

/**
 * Use case for getting user preferences.
 */
class GetUserPreferencesUseCase(
  private val userRepository: UserRepository,
) {
  suspend operator fun invoke(): Result<UserPreferences> = userRepository.getUserPreferences()
}
