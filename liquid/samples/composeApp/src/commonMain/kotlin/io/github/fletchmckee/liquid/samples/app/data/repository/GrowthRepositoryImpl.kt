package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryGrowthDataSource
import io.github.fletchmckee.liquid.samples.app.domain.entity.Achievements
import io.github.fletchmckee.liquid.samples.app.domain.entity.Scenario
import io.github.fletchmckee.liquid.samples.app.domain.repository.EngagementStats
import io.github.fletchmckee.liquid.samples.app.domain.repository.GrowthDataPoint
import io.github.fletchmckee.liquid.samples.app.domain.repository.GrowthRepository
import io.github.fletchmckee.liquid.samples.app.domain.repository.NetworkGrowthStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Implementation of GrowthRepository.
 * PRODUCTION: Requires InMemoryGrowthDataSource - no optional dependencies.
 */
class GrowthRepositoryImpl(
    private val dataSource: InMemoryGrowthDataSource
) : GrowthRepository {

    private val _achievementsFlow = MutableStateFlow<Result<Achievements>>(Result.Loading)

    init {
        refreshAchievementsInternal()
    }

    private fun refreshAchievementsInternal() {
        try {
            val achievements = dataSource.getAchievements()
                ?: throw IllegalStateException("Achievements data not loaded from backend")
            _achievementsFlow.value = Result.Success(achievements)
        } catch (e: Exception) {
            _achievementsFlow.value = Result.Error(e, "Failed to load achievements")
        }
    }

    override fun getAchievementsFlow(): Flow<Result<Achievements>> = _achievementsFlow

    override suspend fun getAchievements(): Result<Achievements> {
        return try {
            val achievements = dataSource.getAchievements()
                ?: throw IllegalStateException("Achievements data not loaded from backend")
            Result.Success(achievements)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get achievements")
        }
    }

    override suspend fun getScenarios(): Result<List<Scenario>> {
        return try {
            val scenarios = dataSource.getScenarios()
            Result.Success(scenarios)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get scenarios")
        }
    }

    override suspend fun getGrowthHistory(days: Int): Result<List<GrowthDataPoint>> {
        return try {
            val history = dataSource.getGrowthHistory(days)
            Result.Success(history)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get growth history")
        }
    }

    override suspend fun getNetworkGrowthStats(): Result<NetworkGrowthStats> {
        return try {
            val stats = dataSource.getNetworkGrowthStats()
                ?: throw IllegalStateException("NetworkGrowthStats data not loaded from backend")
            Result.Success(stats)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get network growth stats")
        }
    }

    override suspend fun getEngagementStats(): Result<EngagementStats> {
        return try {
            val stats = dataSource.getEngagementStats()
                ?: throw IllegalStateException("EngagementStats data not loaded from backend")
            Result.Success(stats)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get engagement stats")
        }
    }

    override suspend fun refreshAchievements(): Result<Unit> {
        return try {
            refreshAchievementsInternal()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to refresh achievements")
        }
    }
}
