package io.github.fletchmckee.liquid.samples.app.domain.usecase.growth

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Scenario
import io.github.fletchmckee.liquid.samples.app.domain.repository.GrowthRepository

/**
 * Use case for getting scenarios.
 */
class GetScenariosUseCase(
    private val growthRepository: GrowthRepository
) {
    suspend operator fun invoke(): Result<List<Scenario>> {
        return growthRepository.getScenarios()
    }
}
