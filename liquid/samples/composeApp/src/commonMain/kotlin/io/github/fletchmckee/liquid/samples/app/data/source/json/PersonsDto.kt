package io.github.fletchmckee.liquid.samples.app.data.source.json

import io.github.fletchmckee.liquid.samples.app.domain.entity.DimensionScore
import io.github.fletchmckee.liquid.samples.app.domain.entity.InfluenceTier
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.RelationshipStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Full DTO for entity dimension scores matching database structure.
 */
@Serializable
data class EntityDimensionScoreDto(
    val id: Int = 0,
    @SerialName("entity_type") val entityType: String = "",
    @SerialName("entity_id") val entityId: String = "",
    val dimension: String,
    val score: Float? = null,
    val details: JsonElement,
    @SerialName("period_type") val periodType: String = "",
    @SerialName("period_date") val periodDate: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

/**
 * Convert entity dimension score DTO to domain entity
 */
fun EntityDimensionScoreDto.toDomain(): DimensionScore {
    return DimensionScore(
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
        updatedAt = updatedAt
    )
}

/**
 * DTO for persons JSON file
 */
@Serializable
data class PersonsJsonDto(
    val persons: List<PersonDto>,
    val organizations: List<OrganizationDto> = emptyList()
)

@Serializable
data class OrganizationsJsonDto(
    val organizations: List<OrganizationDto>
)

@Serializable
data class PersonDto(
    val id: String = "",
    val voiceprint_id: String? = null,
    val user_id: String? = null,
    val canonical_name: String? = null,
    val email: String? = null,
    val organization_id: String? = null,
    val seniority: String? = null,
    val status: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val title: String? = null,
    val department: String? = null,
    val skills: List<String> = emptyList(),
    val personality_tags: List<String> = emptyList(),
    val aliases: List<String> = emptyList(),
    val timezone: String? = null,
    val discovery_segment_id: String? = null,
    val discovery_method: String? = null,
    val discovery_confidence: Float? = null,
    val discovered_in_session: String? = null,
    val discovered_at: String? = null,
    val dimension_scores: List<EntityDimensionScoreDto> = emptyList(),
    val work_hours: List<String> = emptyList(),
    val related_meetings: List<String> = emptyList(),
    val related_projects: List<String> = emptyList(),
    val related_organizations: List<String> = emptyList(),
    val characteristics: List<String> = emptyList(),
    val role: String? = null,
    val influence_tier: String? = null,
    val relationship_status: String? = null
)

@Serializable
data class OrganizationDto(
    val id: String,
    val canonical_name: String? = null,
    val type: String? = null,
    val country: String? = null,
    val status: String? = null,
    val industry: String? = null,
    val size_headcount: Int? = null,
    val legal_name: String? = null,
    val user_id: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val aliases: List<String> = emptyList(),
    val industry_domains: List<String> = emptyList(),
    val departments: List<String> = emptyList(),
    val relationship_score: Int = 50,
    val related_sessions: List<String> = emptyList(),
    val related_projects: List<String> = emptyList(),
    val employees: List<String> = emptyList(),
    val strategic_focus: String = "",
    val next_action: String = "",
    val discovered_in_session: String? = null,
    val discovered_at: String? = null,
    val discovery_method: String? = null,
    val discovery_segment_id: String? = null,
    val discovery_confidence: Float? = null,
    val dimension_scores: List<EntityDimensionScoreDto> = emptyList()
)

/**
 * Extension functions to convert DTOs to domain entities
 */
fun PersonDto.toDomain(): Person {
    return Person(
        id = voiceprint_id ?: id,
        name = canonical_name ?: "",
        canonicalName = canonical_name ?: "",
        role = role ?: title ?: seniority ?: "", 
        email = email ?: "",
        organizationId = organization_id,
        department = department,
        status = status ?: "active",
        skills = skills,
        personalityTags = personality_tags,
        aliases = aliases,
        timezone = timezone,
        discoverySegmentId = discovery_segment_id,
        discoveryMethod = discovery_method,
        discoveryConfidence = discovery_confidence,
        discoveredInSession = discovered_in_session,
        discoveredAt = discovered_at,
        dimensions = dimension_scores.map { it.toDomain() },
        updatedAt = updated_at,
        relatedMeetings = related_meetings,
        relatedProjects = related_projects,
        relatedOrganizations = related_organizations,
        characteristics = characteristics
    )
}

fun OrganizationDto.toDomain(): io.github.fletchmckee.liquid.samples.app.domain.entity.Organization {
    return io.github.fletchmckee.liquid.samples.app.domain.entity.Organization(
        id = id,
        name = canonical_name ?: "",
        canonicalName = canonical_name ?: "",
        type = type,
        country = country,
        status = status ?: "active",
        industry = industry ?: "",
        sizeHeadcount = size_headcount,
        legalName = legal_name,
        aliases = aliases,
        industryDomains = industry_domains,
        departments = departments,
        relatedSessions = related_sessions,
        employees = employees,
        relatedProjects = related_projects,
        strategicFocus = strategic_focus,
        nextAction = next_action,
        discoveredInSession = discovered_in_session,
        discoveredAt = discovered_at,
        discoveryMethod = discovery_method,
        discoverySegmentId = discovery_segment_id,
        discoveryConfidence = discovery_confidence,
        userId = user_id,
        createdAt = created_at,
        updatedAt = updated_at,
        dimensions = dimension_scores.map { it.toDomain() }
    )
}
