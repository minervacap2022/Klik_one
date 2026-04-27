// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.user

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.User
import io.github.fletchmckee.liquid.samples.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting current user.
 */
class GetCurrentUserUseCase(
  private val userRepository: UserRepository,
) {
  suspend operator fun invoke(): Result<User> = userRepository.getCurrentUser()

  fun observe(): Flow<Result<User>> = userRepository.getCurrentUserFlow()
}
