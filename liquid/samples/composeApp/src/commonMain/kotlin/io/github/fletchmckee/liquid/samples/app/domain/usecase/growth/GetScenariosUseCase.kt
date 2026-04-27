// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.growth

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Scenario
import io.github.fletchmckee.liquid.samples.app.domain.repository.GrowthRepository

/**
 * Use case for getting scenarios.
 */
class GetScenariosUseCase(
  private val growthRepository: GrowthRepository,
) {
  suspend operator fun invoke(): Result<List<Scenario>> = growthRepository.getScenarios()
}
