package io.github.fletchmckee.liquid.samples.app.data.source.json

import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.domain.entity.ProjectStatus
import io.github.fletchmckee.liquid.samples.app.domain.entity.ProjectTrend
import kotlinx.serialization.Serializable

/**
 * DTO for projects JSON file
 */
@Serializable
data class ProjectsJsonDto(
    val projects: List<ProjectDto>
)

@Serializable
data class ProjectDto(
    val id: String,
    val canonical_name: String? = null,
    val type: String? = null,
    val status: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val user_id: String? = null,
    val stage: String? = null,
    val lead_voiceprint_id: String? = null,
    val budget: Double? = null,
    val aliases: List<String> = emptyList(),
    val scope: String? = null,
    val start_date: String? = null,
    val end_date: String? = null,
    val repo_refs: List<String> = emptyList(),
    val doc_refs: List<String> = emptyList(),
    val spend_to_date: Double? = null,
    val roi_estimate: Double? = null,
    val goals: List<String> = emptyList(),
    val kpis: List<String> = emptyList(),
    val risks: List<String> = emptyList(),
    val topics: List<String> = emptyList(),
    val discovered_in_session: String? = null,
    val discovered_at: String? = null,
    val discovery_method: String? = null,
    val discovery_segment_id: String? = null,
    val discovery_confidence: Float? = null,
    val dimension_scores: List<EntityDimensionScoreDto> = emptyList(),
    val lead: String? = null,
    val related_meetings: List<String> = emptyList(),
    val related_projects: List<String> = emptyList()
)

/**
 * Extension function to convert DTO to domain entity
 */
fun ProjectDto.toDomain(): Project {
    return Project(
        id = id,
        name = canonical_name ?: "",
        canonicalName = canonical_name ?: "",
        type = type,
        status = ProjectStatus.fromString(status ?: ""),
        stage = stage ?: "",
        leadVoiceprintId = lead_voiceprint_id,
        lead = lead ?: "", // Mapped from JSON

        budget = budget?.toString() ?: "",
        scope = scope,
        startDate = start_date,
        endDate = end_date,
        repoRefs = repo_refs,
        docRefs = doc_refs,
        spendToDate = spend_to_date,
        roiEstimate = roi_estimate,
        goals = goals,
        kpis = kpis,
        risks = risks,
        topics = topics,
        discoveredInSession = discovered_in_session,
        discoveredAt = discovered_at,
        discoveryMethod = discovery_method,
        discoverySegmentId = discovery_segment_id,
        discoveryConfidence = discovery_confidence,
        userId = user_id,
        aliases = aliases,
        createdAt = created_at,
        updatedAt = updated_at,
        dimensions = dimension_scores.map { it.toDomain() },
        trend = ProjectTrend.STABLE, // Default
        progress = 0f, // Default
        teamMembers = emptyList(), // Default
        relatedMeetings = related_meetings,
        relatedProjects = related_projects
    )
}
