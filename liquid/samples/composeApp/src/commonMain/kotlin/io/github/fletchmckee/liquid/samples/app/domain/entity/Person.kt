package io.github.fletchmckee.liquid.samples.app.domain.entity

/**
 * Domain entity representing a person/contact.
 */
data class Person(
    val id: String = "",
    val name: String,
    val canonicalName: String = "",
    val role: String, // mapped from jobTitle or similar
    val avatarUrl: String? = null,
    val email: String = "",
    val phone: String = "",
    val relatedProjects: List<String> = emptyList(),
    val relatedOrganizations: List<String> = emptyList(),
    val relatedMeetings: List<String> = emptyList(),
    val characteristics: List<String> = emptyList(),
    val lastInteraction: String = "No recent contact",

    // New fields from Schema
    val title: String? = null,
    val timezone: String? = null,
    val status: String = "active",
    val skills: List<String> = emptyList(),
    val seniority: String? = null,
    val personalityTags: List<String> = emptyList(),
    val discoverySegmentId: String? = null,
    val discoveryMethod: String? = null,
    val discoveryConfidence: Float? = null,
    val discoveredInSession: String? = null,
    val discoveredAt: String? = null,
    val department: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val organizationId: String? = null,
    val aliases: List<String> = emptyList(),

    val dimensions: List<DimensionScore> = emptyList(),

    val isPinned: Boolean = false,
    val pinnedAt: Long? = null,
    val isArchived: Boolean = false
) {
    /** Derived from "voice" dimension score */
    val influenceTier: InfluenceTier
        get() {
            val voiceScore = dimensions.find { it.dimension == "voice" }?.score ?: return InfluenceTier.B
            return when {
                voiceScore >= 80f -> InfluenceTier.S
                voiceScore >= 50f -> InfluenceTier.A
                else -> InfluenceTier.B
            }
        }

    /** Derived from "connection" dimension score */
    val relationshipStatus: RelationshipStatus
        get() {
            val connectionScore = dimensions.find { it.dimension == "connection" }?.score ?: return RelationshipStatus.DEVELOPING
            return when {
                connectionScore >= 70f -> RelationshipStatus.STRONG
                connectionScore >= 40f -> RelationshipStatus.NEUTRAL
                else -> RelationshipStatus.DEVELOPING
            }
        }

    val displayAvatarUrl: String
        get() = avatarUrl ?: "https://ui-avatars.com/api/?name=${name.replace(" ", "+")}&size=128&background=random&color=fff&bold=true"
}

enum class InfluenceTier(val label: String) {
    S("S"),   // Highest influence
    A("A"),   // High influence
    B("B");   // Normal influence

    companion object {
        fun fromString(value: String): InfluenceTier = when (value.uppercase()) {
            "S" -> S
            "A" -> A
            else -> B
        }
    }
}

enum class RelationshipStatus(val label: String) {
    STRONG("Strong"),
    NEUTRAL("Neutral"),
    DEVELOPING("Developing");

    companion object {
        fun fromString(value: String): RelationshipStatus = when (value.lowercase()) {
            "strong" -> STRONG
            "neutral" -> NEUTRAL
            else -> DEVELOPING
        }
    }
}
