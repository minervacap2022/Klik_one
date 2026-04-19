package io.github.fletchmckee.liquid.samples.app.domain.entity

/**
 * Domain entity representing user achievements and growth stats.
 */
data class Achievements(
    val progressPercent: Int,
    val title: String,
    val summary: String,
    val networkGrowth: Int,
    val engagementScore: Int,
    val activeProjects: Int,
    val generatedAt: Long,
    // Extended properties for profile
    val totalEarned: Int = progressPercent,
    val totalPossible: Int = 100,
    val streak: StreakInfo = StreakInfo(),
    val weeklyGoalProgress: Float = progressPercent / 100f,
    val monthlyGoalProgress: Float = progressPercent / 100f,
    val badges: List<Badge> = emptyList()
)

data class StreakInfo(
    val currentStreak: Int = 7,
    val longestStreak: Int = 14,
    val lastActiveDate: Long? = null
)

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val earnedDate: Long? = null,
    val isEarned: Boolean = false
)
