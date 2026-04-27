// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.source.inmemory

import io.github.fletchmckee.liquid.samples.app.domain.entity.Achievements
import io.github.fletchmckee.liquid.samples.app.domain.entity.Scenario
import io.github.fletchmckee.liquid.samples.app.domain.repository.EngagementStats
import io.github.fletchmckee.liquid.samples.app.domain.repository.GrowthDataPoint
import io.github.fletchmckee.liquid.samples.app.domain.repository.NetworkGrowthStats

/**
 * In-memory data source for growth/achievements data.
 * Data is provided by the backend via RemoteDataFetcher.
 */
class InMemoryGrowthDataSource {

  private var achievements: Achievements? = null
  private var scenarios: MutableList<Scenario> = mutableListOf()
  private var growthHistory: MutableList<GrowthDataPoint> = mutableListOf()
  private var networkGrowthStats: NetworkGrowthStats? = null
  private var engagementStats: EngagementStats? = null

  /**
   * Set achievements from backend.
   */
  fun setAchievements(newAchievements: Achievements) {
    achievements = newAchievements
  }

  /**
   * Set scenarios from backend.
   */
  fun setScenarios(newScenarios: List<Scenario>) {
    scenarios.clear()
    scenarios.addAll(newScenarios)
  }

  /**
   * Set growth history from backend.
   */
  fun setGrowthHistory(history: List<GrowthDataPoint>) {
    growthHistory.clear()
    growthHistory.addAll(history)
  }

  /**
   * Set network growth stats from backend.
   */
  fun setNetworkGrowthStats(stats: NetworkGrowthStats?) {
    networkGrowthStats = stats
  }

  /**
   * Set engagement stats from backend.
   */
  fun setEngagementStats(stats: EngagementStats?) {
    engagementStats = stats
  }

  fun getAchievements(): Achievements? = achievements

  fun getScenarios(): List<Scenario> = scenarios.toList()

  fun getGrowthHistory(days: Int): List<GrowthDataPoint> = growthHistory.takeLast(days)

  fun getNetworkGrowthStats(): NetworkGrowthStats? = networkGrowthStats

  fun getEngagementStats(): EngagementStats? = engagementStats
}
