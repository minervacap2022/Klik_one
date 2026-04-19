package io.github.fletchmckee.liquid.samples.app.domain.entity

/**
 * Domain entity representing a project.
 */
data class Project(
    val id: String = "",
    val name: String,
    val canonicalName: String = "",
    val status: ProjectStatus,
    val type: String? = null,
    val progress: Float, // 0.0 to 1.0
    val trend: ProjectTrend,
    val lead: String,
    val teamMembers: List<String>,
    val stage: String, // e.g. "Active Planning"
    val goals: List<String>,
    val kpis: List<String>,
    val risks: List<String>,
    val budget: String, // Kept as String for formatted display e.g. "$800K"
    val scope: String? = null,
    val startDate: String? = null, // YYYY-MM-DD
    val endDate: String? = null,
    val repoRefs: List<String> = emptyList(),
    val docRefs: List<String> = emptyList(),
    val relatedMeetings: List<String> = emptyList(),
    val relatedProjects: List<String> = emptyList(),
    val relatedPeople: List<String> = emptyList(),
    val relatedOrganizations: List<String> = emptyList(),
    val spendToDate: Double? = null,
    val roiEstimate: Double? = null,
    val topics: List<String> = emptyList(),
    
    // Discovery info
    val discoveredInSession: String? = null,
    val discoveredAt: String? = null,
    val discoveryMethod: String? = null,
    val discoverySegmentId: String? = null,
    val discoveryConfidence: Float? = null,
    
    val userId: String? = null,
    val leadVoiceprintId: String? = null,
    val aliases: List<String> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
    
    val dimensions: List<DimensionScore> = emptyList(),
    
    val isPinned: Boolean = false,
    val pinnedAt: Long? = null,
    val isArchived: Boolean = false
)

enum class ProjectStatus(val label: String) {
    ON_TRACK("On Track"),
    AT_RISK("At Risk"),
    COMPLETED("Completed"),
    ON_HOLD("On Hold");

    companion object {
        fun fromString(value: String): ProjectStatus = when (value.lowercase().replace(" ", "_")) {
            "on_track", "ontrack", "active" -> ON_TRACK // Maps 'active' to ON_TRACK for now
            "at_risk", "atrisk" -> AT_RISK
            "completed" -> COMPLETED
            "on_hold", "onhold" -> ON_HOLD
            else -> ON_TRACK
        }
    }
}

enum class ProjectTrend(val label: String) {
    UP("Up"),
    DOWN("Down"),
    STABLE("Stable");

    companion object {
        fun fromString(value: String): ProjectTrend = when (value.lowercase()) {
            "up" -> UP
            "down" -> DOWN
            else -> STABLE
        }
    }
}
