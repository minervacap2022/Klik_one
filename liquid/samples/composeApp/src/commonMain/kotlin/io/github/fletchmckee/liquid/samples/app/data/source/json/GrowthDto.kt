// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.source.json

import io.github.fletchmckee.liquid.samples.app.domain.entity.Achievements
import io.github.fletchmckee.liquid.samples.app.domain.entity.Badge
import io.github.fletchmckee.liquid.samples.app.domain.entity.Scenario
import io.github.fletchmckee.liquid.samples.app.domain.entity.StreakInfo
import io.github.fletchmckee.liquid.samples.app.domain.repository.EngagementStats
import io.github.fletchmckee.liquid.samples.app.domain.repository.GrowthDataPoint
import io.github.fletchmckee.liquid.samples.app.domain.repository.NetworkGrowthStats
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Root DTO for growth.json file
 */
@Serializable
data class GrowthJsonDto(
  val achievements: AchievementsDto,
  val scenarios: List<ScenarioDto>,
  val growthHistory: List<GrowthDataPointDto> = emptyList(),
  val networkGrowthStats: NetworkGrowthStatsDto? = null,
  val engagementStats: EngagementStatsDto? = null,
  val dimensionScores: List<DimensionScoreDto> = emptyList(),
)

@Serializable
data class DimensionScoreDto(
  val id: Int,
  val entityType: String,
  val entityId: String,
  val dimension: String,
  val score: Float?,
  val details: JsonElement,
  val periodType: String,
  val periodDate: String,
  val userId: String,
  val createdAt: String,
  val updatedAt: String,
)

/**
 * DTO for achievements
 */
@Serializable
data class AchievementsDto(
  val progressPercent: Int,
  val title: String,
  val summary: String,
  val networkGrowth: Int,
  val engagementScore: Int,
  val activeProjects: Int,
  val generatedAt: Long,
  val totalEarned: Int? = null,
  val totalPossible: Int? = null,
  val streak: StreakInfoDto? = null,
  val weeklyGoalProgress: Float? = null,
  val monthlyGoalProgress: Float? = null,
  val badges: List<BadgeDto> = emptyList(),
)

/**
 * DTO for streak info
 */
@Serializable
data class StreakInfoDto(
  val currentStreak: Int = 0,
  val longestStreak: Int = 0,
  val lastActiveDate: Long? = null,
)

/**
 * DTO for badge
 */
@Serializable
data class BadgeDto(
  val id: String,
  val name: String,
  val description: String,
  val iconName: String,
  val earnedDate: Long? = null,
  val isEarned: Boolean = false,
)

/**
 * DTO for scenario
 */
@Serializable
data class ScenarioDto(
  val title: String,
  val count: Int,
  val colorType: String,
)

/**
 * DTO for growth data point
 */
@Serializable
data class GrowthDataPointDto(
  val timestamp: Long,
  val networkGrowth: Int,
  val engagementScore: Int,
  val activeProjects: Int,
)

/**
 * DTO for network growth stats
 */
@Serializable
data class NetworkGrowthStatsDto(
  val totalConnections: Int,
  val newConnectionsThisWeek: Int,
  val newConnectionsThisMonth: Int,
  val topIndustries: List<String> = emptyList(),
)

/**
 * DTO for engagement stats
 */
@Serializable
data class EngagementStatsDto(
  val totalMeetings: Int,
  val meetingsThisWeek: Int,
  val averageMeetingDuration: Int,
  val responseRate: Float,
)

// Extension functions to convert DTOs to domain entities

fun AchievementsDto.toDomain(): Achievements = Achievements(
  progressPercent = progressPercent,
  title = title,
  summary = summary,
  networkGrowth = networkGrowth,
  engagementScore = engagementScore,
  activeProjects = activeProjects,
  generatedAt = generatedAt,
  totalEarned = totalEarned ?: progressPercent,
  totalPossible = totalPossible ?: 100,
  streak = streak?.toDomain() ?: StreakInfo(),
  weeklyGoalProgress = weeklyGoalProgress ?: (progressPercent / 100f),
  monthlyGoalProgress = monthlyGoalProgress ?: (progressPercent / 100f),
  badges = badges.map { it.toDomain() },
)

fun StreakInfoDto.toDomain(): StreakInfo = StreakInfo(
  currentStreak = currentStreak,
  longestStreak = longestStreak,
  lastActiveDate = lastActiveDate,
)

fun BadgeDto.toDomain(): Badge = Badge(
  id = id,
  name = name,
  description = description,
  iconName = iconName,
  earnedDate = earnedDate,
  isEarned = isEarned,
)

fun ScenarioDto.toDomain(): Scenario {
  // Map legacy color types to new hex values
  val colorHex = when (colorType.uppercase()) {
    "LIVE" -> "FF2EC4B6"
    "ENGAGE" -> "FFA663CC"
    "STRIVE" -> "FFFF9F1C"
    else -> "FF2EC4B6" // Default to Live color
  }
  return Scenario(
    title = title,
    count = count,
    colorHex = colorHex,
  )
}

fun GrowthDataPointDto.toDomain(): GrowthDataPoint = GrowthDataPoint(
  timestamp = timestamp,
  networkGrowth = networkGrowth,
  engagementScore = engagementScore,
  activeProjects = activeProjects,
)

fun NetworkGrowthStatsDto.toDomain(): NetworkGrowthStats = NetworkGrowthStats(
  totalConnections = totalConnections,
  newConnectionsThisWeek = newConnectionsThisWeek,
  newConnectionsThisMonth = newConnectionsThisMonth,
  topIndustries = topIndustries,
)

fun DimensionScoreDto.toDomain(): io.github.fletchmckee.liquid.samples.app.domain.entity.DimensionScore = io.github.fletchmckee.liquid.samples.app.domain.entity.DimensionScore(
  id = id,
  entityType = entityType,
  entityId = entityId,
  dimension = dimension,
  score = score,
  details = details.toString(),
  periodType = periodType,
  periodDate = periodDate,
  userId = userId,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun EngagementStatsDto.toDomain(): EngagementStats = EngagementStats(
  totalMeetings = totalMeetings,
  meetingsThisWeek = meetingsThisWeek,
  averageMeetingDuration = averageMeetingDuration,
  responseRate = responseRate,
)
