package io.github.fletchmckee.liquid.samples.app.domain.entity

/**
 * Domain entity representing a task/todo item.
 */
data class TodoItem(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false,
    val type: TodoType = TodoType.TODO
)

enum class TodoType {
    INSIGHT,
    ACTION_REQUIRED,
    TODO,
    COMPLETED;

    companion object {
        fun fromString(value: String): TodoType = when (value.uppercase()) {
            "INSIGHT" -> INSIGHT
            "ACTION_REQUIRED", "ACTIONREQUIRED" -> ACTION_REQUIRED
            "COMPLETED" -> COMPLETED
            else -> TODO
        }
    }
}

/**
 * Rich task metadata for display
 */
data class TaskMetadata(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val context: String = "",
    val relatedProject: String = "",
    val relatedPeople: List<String> = emptyList(),
    val relatedMeeting: String? = null,
    val relatedMeetingId: String? = null,
    val dueInfo: String = "",
    val priority: TaskPriority = TaskPriority.NORMAL,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val pinnedAt: Long? = null,
    val status: TaskStatus = TaskStatus.PENDING,
    val needsConfirmation: Boolean = false,
    val artifact: TaskArtifact? = null,
    // Additional properties for EventsScreen
    val dueDate: String? = null,
    val projectName: String? = null,
    val isOverdue: Boolean = false,
    val description: String? = null,
    val artifacts: List<TaskArtifact> = emptyList(),
    val assignees: List<String> = emptyList(),
    val createdAt: Long = 0L,
    // KK_exec integration fields
    // Category determines execution requirements: a_simple, b_apis, c_complex_level1, d_complex_level2, e_complex_level3, f_cannotdo
    val kkExecCategory: String? = null,
    // Whether this task can be executed by KK_exec
    val kkExecCanExecute: Boolean? = null,
    // Whether this task requires user confirmation (e_complex_level3)
    val kkExecIsSensitive: Boolean? = null,
    // Execution status from KK_exec: PENDING, IN_PROGRESS, COMPLETED, CANCELLED, FAILED
    val kkExecStatus: String? = null,
    // Session ID this todo belongs to
    val kkExecSessionId: String? = null,
    // Todo ID in KK_exec database
    val kkExecTodoId: Int? = null,
    // Related entities - extracted from backend
    val relatedSegments: List<Int> = emptyList(),
    val relatedOrganizations: List<String> = emptyList(),
    val relatedProjects: List<String> = emptyList(),
    // OAuth reconnect payload — non-null when status is REQUIRES_REAUTH
    val reauthInfo: ReauthInfo? = null
) {
    /**
     * Check if this task is from KK_exec (has KK_exec integration fields).
     */
    val isFromKKExec: Boolean
        get() = kkExecTodoId != null

    /**
     * Check if this is a sensitive task requiring user confirmation.
     */
    val requiresConfirmation: Boolean
        get() = kkExecIsSensitive == true || kkExecCategory == "E_COMPLEX_LEVEL3"

    /**
     * Check if this task can be auto-executed.
     */
    val isAutoExecutable: Boolean
        get() = kkExecCanExecute == true && kkExecIsSensitive != true

    /**
     * Check if this task cannot be executed (f_cannotdo category).
     */
    val cannotExecute: Boolean
        get() = kkExecCanExecute == false || kkExecCategory == "F_CANNOTDO"
}

enum class TaskPriority {
    HIGH,
    MEDIUM,
    NORMAL,
    LOW;

    companion object {
        fun fromString(value: String): TaskPriority = when (value.uppercase()) {
            "HIGH" -> HIGH
            "MEDIUM" -> MEDIUM
            "LOW" -> LOW
            else -> NORMAL
        }
    }
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    IN_REVIEW,
    COMPLETED,
    ARCHIVED,
    APPROVED,
    REJECTED,

    /**
     * KK_exec executed the todo, the upstream provider returned 401, the
     * reactive refresh attempt also failed, so the user must reconnect the
     * OAuth integration before the todo can complete. Comes with a
     * [io.github.fletchmckee.liquid.samples.app.domain.entity.ReauthInfo]
     * payload (provider + reason) on the TaskMetadata.
     */
    REQUIRES_REAUTH;

    val displayName: String
        get() = when (this) {
            PENDING -> "Pending"
            IN_PROGRESS -> "In Progress"
            IN_REVIEW -> "Needs Approval"
            COMPLETED -> "Done"
            ARCHIVED -> "Archived"
            APPROVED -> "Approved"
            REJECTED -> "Rejected"
            REQUIRES_REAUTH -> "Reconnect needed"
        }
}

/**
 * Payload extracted from a todo's task_result.result when its execution status
 * is `requires_reauth`. The reactive 401 path stamps this so the UI can route
 * the user straight to the right OAuth reconnect flow.
 *
 * @Serializable so it can ride inside the @Serializable UI TaskMetadata that
 * gets persisted/restored as state. Pure data, no behavior.
 */
@kotlinx.serialization.Serializable
data class ReauthInfo(
    val provider: String,
    val reason: String? = null
)

/**
 * Artifact attached to completed tasks
 */
data class TaskArtifact(
    val type: ArtifactType,
    val title: String,
    val content: String = "",
    val sections: List<ArtifactSection> = emptyList()
)

enum class ArtifactType {
    DOCUMENT,
    PRESENTATION,
    SPREADSHEET,
    CODE,
    DESIGN,
    OTHER
}

data class ArtifactSection(
    val title: String,
    val content: String
)

/**
 * Summary of tasks for the events screen
 */
data class TaskSummary(
    val reviewCount: Int = 0,
    val todoCount: Int = 0,
    val completedToday: Int = 0,
    val summaryText: String = "",
    val generatedAt: Long = 0L,
    // Additional properties for EventsScreen
    val totalTasks: Int = 0,
    val highPriorityCount: Int = 0,
    val overdueCount: Int = 0,
    val completionRate: Float = 0f
)

/**
 * Tab types for events screen
 */
enum class TaskTabType {
    IN_REVIEW,
    PENDING,
    COMPLETED
}
