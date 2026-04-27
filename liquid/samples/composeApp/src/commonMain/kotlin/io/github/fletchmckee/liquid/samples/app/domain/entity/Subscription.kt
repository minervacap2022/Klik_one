// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.entity

/**
 * Current user's subscription details from KK_subscription API.
 */
data class Subscription(
  val userId: String,
  val planCode: String,
  val displayName: String,
  val billingCycle: String,
  val status: String,
  val usage: SubscriptionUsage,
)

data class SubscriptionUsage(
  val asrMinutesUsed: Int,
  val asrMinutesLimit: Int,
  val asrMinutesRemaining: Int,
  val storageMbUsed: Int,
  val sessionsProcessed: Int,
)

/**
 * Subscription plan available for purchase/comparison.
 */
data class SubscriptionPlan(
  val planCode: String,
  val displayName: String,
  val asrMonthlyMinutes: Int,
  val storageMb: Int,
  val cloudBackupEnabled: Boolean,
  val features: Map<String, Boolean>,
  val priceMonthlyInCents: Int,
  val priceYearlyInCents: Int,
)

/**
 * Detailed usage statistics for the current billing period.
 */
data class UsageDetails(
  val userId: String,
  val periodStart: String,
  val periodEnd: String,
  val asrMinutesUsed: Int,
  val asrMinutesLimit: Int,
  val storageMbUsed: Int,
  val storageMbLimit: Int,
  val sessionsProcessed: Int,
)

/**
 * Feature flags for the current user's subscription tier.
 */
data class SubscriptionFeatures(
  val tier: String,
  val memoryEnabled: Boolean,
  val goalsEnabled: Boolean,
  val executionMode: String,
  val knowledgeGraph: String,
  val riskAnalysis: Boolean,
  val summaryLevel: String,
  val cloudBackup: Boolean,
  val insightsEnabled: Boolean,
)
