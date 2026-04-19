package io.github.fletchmckee.liquid.samples.app.domain.entity

/**
 * Domain entity representing a scenario card (Live, Engage, Strive, etc.)
 */
data class Scenario(
    val title: String,
    val count: Int,
    val colorHex: String
)

// ScenarioColorType enum removed as we now use dynamic hex colors
