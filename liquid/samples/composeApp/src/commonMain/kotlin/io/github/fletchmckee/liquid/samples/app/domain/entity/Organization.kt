package io.github.fletchmckee.liquid.samples.app.domain.entity

/**
 * Domain entity representing an organization.
 */
data class Organization(
    val id: String = "",
    val name: String,
    val canonicalName: String = "",
    val type: String? = null,
    val country: String? = null,
    val status: String = "active",
    val industry: String,
    val sizeHeadcount: Int? = null,
    val legalName: String? = null,
    
    val aliases: List<String> = emptyList(),
    val industryDomains: List<String> = emptyList(),
    val departments: List<String> = emptyList(),

    val relatedSessions: List<String>,
    val employees: List<String>,
    val relatedProjects: List<String>,
    
    val strengths: List<String> = emptyList(),
    val strategicFocus: String = "",
    val nextAction: String = "",
    
    val discoveredInSession: String? = null,
    val discoveredAt: String? = null,
    val discoveryMethod: String? = null,
    val discoverySegmentId: String? = null,
    val discoveryConfidence: Float? = null,
    
    val userId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    
    val dimensions: List<DimensionScore> = emptyList(),
    
    val isPinned: Boolean = false,
    val pinnedAt: Long? = null,
    val isArchived: Boolean = false
) {
    /** Derived from "pulse" dimension score */
    val relationshipScore: Int
        get() = dimensions.find { it.dimension == "pulse" }?.score?.toInt() ?: 50
}
