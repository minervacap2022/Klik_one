// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.network

import io.github.fletchmckee.liquid.samples.app.logging.KlikLogger

/**
 * Environment configuration for the application.
 * Supports multiple deployment environments: Development, Staging, Production.
 *
 * Set the environment by calling Environment.configure() during app initialization,
 * or it will default to Production.
 */
object Environment {

  enum class Type {
    DEVELOPMENT,
    STAGING,
    PRODUCTION,
  }

  /**
   * Configuration for a specific environment.
   */
  data class Config(
    val type: Type,
    val baseUrl: String,
    val authBaseUrl: String,
    val oauthBaseUrl: String,
    val insightsBaseUrl: String,
    val encourageBaseUrl: String,
    val worklifeBaseUrl: String,
    val askKlikBaseUrl: String,
    val execBaseUrl: String,
    val entityFeedbackBaseUrl: String,
    val notificationsBaseUrl: String,
    val goalBaseUrl: String,
    val treeBaseUrl: String,
    val subscriptionBaseUrl: String,
    val complianceBaseUrl: String,
    val bugReportBaseUrl: String,
    val logsBaseUrl: String,
    val webBaseUrl: String,
    val useHttps: Boolean = true,
  )

  // Predefined environment configurations
  // DEVELOPMENT config requires explicit Environment.configure(customConfig) with proper URLs
  // Do NOT hardcode IPs - use Environment.configure(Config(...)) with your local dev server URLs
  private val DEVELOPMENT: Config
    get() = throw IllegalStateException(
      "DEVELOPMENT environment requires explicit configuration. " +
        "Use Environment.configure(Config(...)) with your local development server URLs.",
    )

  private val STAGING = Config(
    type = Type.STAGING,
    baseUrl = "https://staging.hiklik.ai/api/v1",
    authBaseUrl = "https://staging.hiklik.ai/api/auth",
    oauthBaseUrl = "https://staging.hiklik.ai/api/auth",
    insightsBaseUrl = "https://staging.hiklik.ai/api/insights",
    encourageBaseUrl = "https://staging.hiklik.ai/api/encourage",
    worklifeBaseUrl = "https://staging.hiklik.ai/api/worklife",
    askKlikBaseUrl = "https://staging.hiklik.ai/api/chat",
    execBaseUrl = "https://staging.hiklik.ai/api/exec",
    entityFeedbackBaseUrl = "https://staging.hiklik.ai/api/entity-feedback",
    notificationsBaseUrl = "https://staging.hiklik.ai/api/notifications",
    goalBaseUrl = "https://staging.hiklik.ai/api/goal",
    treeBaseUrl = "https://staging.hiklik.ai/api/v1/growth",
    subscriptionBaseUrl = "https://staging.hiklik.ai/api/subscription",
    complianceBaseUrl = "https://staging.hiklik.ai/api/compliance",
    bugReportBaseUrl = "https://staging.hiklik.ai/api/bug-report",
    logsBaseUrl = "https://staging.hiklik.ai/api/logs",
    webBaseUrl = "https://staging.hiklik.ai",
    useHttps = true,
  )

  private val PRODUCTION = Config(
    type = Type.PRODUCTION,
    baseUrl = "https://hiklik.ai/api/v1",
    authBaseUrl = "https://hiklik.ai/api/auth",
    oauthBaseUrl = "https://hiklik.ai/api/auth",
    insightsBaseUrl = "https://hiklik.ai/api/insights",
    encourageBaseUrl = "https://hiklik.ai/api/encourage",
    worklifeBaseUrl = "https://hiklik.ai/api/worklife",
    askKlikBaseUrl = "https://hiklik.ai/api/chat",
    execBaseUrl = "https://hiklik.ai/api/exec",
    entityFeedbackBaseUrl = "https://hiklik.ai/api/entity-feedback",
    notificationsBaseUrl = "https://hiklik.ai/api/notifications",
    goalBaseUrl = "https://hiklik.ai/api/goal",
    treeBaseUrl = "https://hiklik.ai/api/v1/growth",
    subscriptionBaseUrl = "https://hiklik.ai/api/subscription",
    complianceBaseUrl = "https://hiklik.ai/api/compliance",
    bugReportBaseUrl = "https://hiklik.ai/api/bug-report",
    logsBaseUrl = "https://hiklik.ai/api/logs",
    webBaseUrl = "https://hiklik.ai",
    useHttps = true,
  )

  // Current active configuration - defaults to PRODUCTION
  private var currentConfig: Config = PRODUCTION

  /**
   * Configure the environment. Call this during app initialization.
   *
   * @param type The environment type to use
   * @throws IllegalStateException if DEVELOPMENT is used (requires custom config)
   */
  fun configure(type: Type) {
    currentConfig = when (type) {
      Type.DEVELOPMENT -> throw IllegalStateException(
        "DEVELOPMENT environment requires explicit configuration. " +
          "Use Environment.configure(Config(...)) with your local development server URLs.",
      )

      Type.STAGING -> STAGING

      Type.PRODUCTION -> PRODUCTION
    }
    KlikLogger.i("Environment", "Configured for ${type.name}, Base URL = ${currentConfig.baseUrl}")
  }

  /**
   * Configure with a custom config. Useful for testing or custom deployments.
   *
   * @param config Custom configuration
   */
  fun configure(config: Config) {
    currentConfig = config
    KlikLogger.i("Environment", "Configured with custom config for ${config.type.name}, Base URL = ${config.baseUrl}")
  }

  /**
   * Get the current environment configuration.
   */
  fun current(): Config = currentConfig
}
