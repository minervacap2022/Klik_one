package io.github.fletchmckee.liquid.samples.app.data.source.json

import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskPriority
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskStatus
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskSummary
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * DTO for tasks JSON file
 */
@Serializable
data class TasksJsonDto(
    val tasks: List<TaskDto>,
    val taskSummary: TaskSummaryDto? = null
)

@Serializable
data class TaskDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val status: String,
    val priority: String,
    val dueDate: String? = null,
    val dueTime: String? = null,
    val assigneeId: String? = null,
    val assigneeName: String? = null,
    val projectId: String? = null,
    val projectName: String? = null,
    val meetingId: String? = null,
    val meetingName: String? = null,
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

@Serializable
data class TaskSummaryDto(
    val total: Int = 0,
    val completed: Int = 0,
    val inProgress: Int = 0,
    val todo: Int = 0,
    val blocked: Int = 0,
    val overdue: Int = 0,
    val dueToday: Int = 0,
    val dueThisWeek: Int = 0,
    val highPriority: Int = 0,
    val summaryText: String? = null
)

/**
 * Extension functions to convert DTOs to domain entities
 */
fun TaskDto.toDomain(): TaskMetadata {
    val taskStatus = when (status.uppercase()) {
        "PENDING", "TODO" -> TaskStatus.PENDING
        "IN_PROGRESS" -> TaskStatus.IN_PROGRESS
        "IN_REVIEW", "BLOCKED" -> TaskStatus.IN_REVIEW
        "COMPLETED", "DONE" -> TaskStatus.COMPLETED
        "ARCHIVED" -> TaskStatus.ARCHIVED
        "APPROVED" -> TaskStatus.APPROVED
        "REJECTED" -> TaskStatus.REJECTED
        else -> TaskStatus.PENDING
    }

    val taskPriority = TaskPriority.fromString(priority)

    return TaskMetadata(
        id = id,
        title = title,
        subtitle = description ?: "",
        context = description ?: "",
        relatedProject = projectName ?: "",
        relatedPeople = if (assigneeName != null) listOf(assigneeName) else emptyList(),
        relatedMeeting = meetingName,
        relatedMeetingId = meetingId,
        dueInfo = buildDueInfo(dueDate, dueTime),
        priority = taskPriority,
        isPinned = isPinned,
        isArchived = isArchived,
        pinnedAt = if (isPinned) createdAt else null,
        status = taskStatus,
        artifact = null,
        dueDate = dueDate,
        projectName = projectName,
        isOverdue = false, // Would need date comparison logic
        description = description,
        artifacts = emptyList(),
        assignees = if (assigneeName != null) listOf(assigneeName) else emptyList(),
        createdAt = createdAt
    )
}

fun TaskSummaryDto.toDomain(): TaskSummary {
    return TaskSummary(
        reviewCount = blocked + inProgress,
        todoCount = todo,
        completedToday = completed,
        summaryText = summaryText ?: "You have $total active tasks",
        generatedAt = Clock.System.now().toEpochMilliseconds(),
        totalTasks = total,
        highPriorityCount = highPriority,
        overdueCount = overdue,
        completionRate = if (total > 0) completed.toFloat() / total else 0f
    )
}

private fun buildDueInfo(dueDate: String?, dueTime: String?): String {
    if (dueDate == null) return ""
    val timeStr = if (dueTime != null) " at $dueTime" else ""
    return "Due $dueDate$timeStr"
}
