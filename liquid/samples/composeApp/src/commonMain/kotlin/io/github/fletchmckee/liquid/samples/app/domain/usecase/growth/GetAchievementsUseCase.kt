package io.github.fletchmckee.liquid.samples.app.domain.usecase.growth

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Achievements
import io.github.fletchmckee.liquid.samples.app.domain.repository.GrowthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting achievements.
 */
class GetAchievementsUseCase(
    private val growthRepository: GrowthRepository
) {
    suspend operator fun invoke(): Result<Achievements> {
        return growthRepository.getAchievements()
    }

    fun observe(): Flow<Result<Achievements>> {
        return growthRepository.getAchievementsFlow()
    }
}
