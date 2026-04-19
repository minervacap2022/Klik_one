package io.github.fletchmckee.liquid.samples.app.domain.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Achievements
import io.github.fletchmckee.liquid.samples.app.domain.entity.Scenario
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for growth/achievements operations.
 */
interface GrowthRepository {

    /**
     * Get achievements as a reactive flow.
     */
    fun getAchievementsFlow(): Flow<Result<Achievements>>

    /**
     * Get current achievements.
     */
    suspend fun getAchievements(): Result<Achievements>

    /**
     * Get scenarios for categorization.
     */
    suspend fun getScenarios(): Result<List<Scenario>>

    /**
     * Get growth history over time.
     */
    suspend fun getGrowthHistory(days: Int = 30): Result<List<GrowthDataPoint>>

    /**
     * Get network growth statistics.
     */
    suspend fun getNetworkGrowthStats(): Result<NetworkGrowthStats>

    /**
     * Get engagement statistics.
     */
    suspend fun getEngagementStats(): Result<EngagementStats>

    /**
     * Refresh achievements from remote source.
     */
    suspend fun refreshAchievements(): Result<Unit>
}

/**
 * Data point for growth over time.
 */
data class GrowthDataPoint(
    val timestamp: Long,
    val networkGrowth: Int,
    val engagementScore: Int,
    val activeProjects: Int
)

/**
 * Network growth statistics.
 */
data class NetworkGrowthStats(
    val totalConnections: Int,
    val newConnectionsThisWeek: Int,
    val newConnectionsThisMonth: Int,
    val topIndustries: List<String>
)

/**
 * Engagement statistics.
 */
data class EngagementStats(
    val totalMeetings: Int,
    val meetingsThisWeek: Int,
    val averageMeetingDuration: Int, // in minutes
    val responseRate: Float // 0.0 - 1.0
)
