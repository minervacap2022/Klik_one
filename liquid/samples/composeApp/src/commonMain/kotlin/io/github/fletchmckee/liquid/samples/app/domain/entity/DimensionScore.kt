package io.github.fletchmckee.liquid.samples.app.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class DimensionScore(
    val id: Int,
    val entityType: String, // user, people, project, organization
    val entityId: String,
    val dimension: String, // energy, focus_orbit, flow, etc.
    val score: Float?,
    val details: String, // JSON string
    val periodType: String,
    val periodDate: String,
    val userId: String,
    val createdAt: String,
    val updatedAt: String
)
