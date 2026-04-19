package io.github.fletchmckee.liquid.samples.app.data.source.inmemory

import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskPriority
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskStatus
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskSummary
import io.github.fletchmckee.liquid.samples.app.domain.entity.TodoItem
import kotlinx.datetime.Clock

/**
 * In-memory data source for task data.
 * Data is provided by the backend via RemoteDataFetcher.
 */
class InMemoryTaskDataSource {

    private val tasks = mutableListOf<TaskMetadata>()

    private val todoItems = mutableMapOf<String, MutableList<TodoItem>>()

    /**
     * Set tasks from backend.
     */
    fun setTasks(tasks: List<TaskMetadata>) {
        this.tasks.clear()
        this.tasks.addAll(tasks)
    }

    fun getTasks(): List<TaskMetadata> = tasks.filter { !it.isArchived }

    fun getTaskById(id: String): TaskMetadata? = tasks.find { it.id == id }

    fun getTasksByStatus(status: TaskStatus): List<TaskMetadata> {
        return tasks.filter { it.status == status && !it.isArchived }
    }

    fun getTasksByPriority(priority: TaskPriority): List<TaskMetadata> {
        return tasks.filter { it.priority == priority && !it.isArchived }
    }

    fun getPinnedTasks(): List<TaskMetadata> {
        return tasks.filter { it.isPinned && !it.isArchived }
            .sortedByDescending { it.pinnedAt }
    }

    fun getTaskSummary(): TaskSummary {
        val activeTasks = tasks.filter { !it.isArchived }
        val completedTasks = activeTasks.count { it.status == TaskStatus.COMPLETED }
        val totalTasks = activeTasks.size
        return TaskSummary(
            reviewCount = activeTasks.count { it.status == TaskStatus.PENDING || it.status == TaskStatus.IN_REVIEW },
            todoCount = activeTasks.count { it.status == TaskStatus.PENDING || it.status == TaskStatus.IN_PROGRESS },
            completedToday = completedTasks,
            summaryText = "You have $totalTasks active tasks",
            generatedAt = Clock.System.now().toEpochMilliseconds(),
            totalTasks = totalTasks,
            highPriorityCount = activeTasks.count { it.priority == TaskPriority.HIGH },
            overdueCount = 0,
            completionRate = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
        )
    }

    fun createTask(task: TaskMetadata): TaskMetadata {
        val newTask = task.copy(id = "task${tasks.size + 1}")
        tasks.add(newTask)
        return newTask
    }

    fun updateTask(task: TaskMetadata): TaskMetadata? {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index == -1) return null
        tasks[index] = task
        return task
    }

    fun deleteTask(taskId: String): Boolean {
        return tasks.removeAll { it.id == taskId }
    }

    fun toggleTaskPin(taskId: String): TaskMetadata? {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index == -1) return null

        val task = tasks[index]
        val updated = task.copy(
            isPinned = !task.isPinned,
            pinnedAt = if (!task.isPinned) Clock.System.now().toEpochMilliseconds() else null
        )
        tasks[index] = updated
        return updated
    }

    fun updateTaskStatus(taskId: String, status: TaskStatus): TaskMetadata? {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index == -1) return null

        val updated = tasks[index].copy(status = status)
        tasks[index] = updated
        return updated
    }

    fun getTodoItems(taskId: String): List<TodoItem> {
        return todoItems[taskId]?.toList() ?: emptyList()
    }

    fun toggleTodoItem(taskId: String, todoId: String): TodoItem? {
        val items = todoItems[taskId] ?: return null
        val index = items.indexOfFirst { it.id == todoId }
        if (index == -1) return null

        val item = items[index]
        val updated = item.copy(isCompleted = !item.isCompleted)
        items[index] = updated
        return updated
    }

    fun getTasksForMeeting(meetingId: String): List<TaskMetadata> {
        return tasks.filter { it.relatedMeetingId == meetingId }
    }

    fun getTasksForProject(projectId: String): List<TaskMetadata> {
        return tasks.filter {
            it.relatedProject == projectId || projectId in it.relatedProjects
        }
    }
}
