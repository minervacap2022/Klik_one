package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryTaskDataSource
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskPriority
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskStatus
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskSummary
import io.github.fletchmckee.liquid.samples.app.domain.entity.TodoItem
import io.github.fletchmckee.liquid.samples.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Implementation of TaskRepository.
 * PRODUCTION: Requires InMemoryTaskDataSource - no optional dependencies.
 */
class TaskRepositoryImpl(
    private val dataSource: InMemoryTaskDataSource
) : TaskRepository {

    private val _tasksFlow = MutableStateFlow<Result<List<TaskMetadata>>>(Result.Loading)

    init {
        refreshTasksInternal()
    }

    private fun refreshTasksInternal() {
        try {
            val tasks = dataSource.getTasks()
            _tasksFlow.value = Result.Success(tasks)
        } catch (e: Exception) {
            _tasksFlow.value = Result.Error(e, "Failed to load tasks")
        }
    }

    override fun getTasksFlow(): Flow<Result<List<TaskMetadata>>> = _tasksFlow

    override suspend fun getTasks(): Result<List<TaskMetadata>> {
        return try {
            val tasks = dataSource.getTasks()
            Result.Success(tasks)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get tasks")
        }
    }

    override suspend fun getTaskById(id: String): Result<TaskMetadata> {
        return try {
            val task = dataSource.getTaskById(id)
                ?: throw NoSuchElementException("Task not found: $id")
            Result.Success(task)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get task")
        }
    }

    override suspend fun getTasksByStatus(status: TaskStatus): Result<List<TaskMetadata>> {
        return try {
            val tasks = dataSource.getTasksByStatus(status)
            Result.Success(tasks)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get tasks by status")
        }
    }

    override suspend fun getTasksByPriority(priority: TaskPriority): Result<List<TaskMetadata>> {
        return try {
            val tasks = dataSource.getTasksByPriority(priority)
            Result.Success(tasks)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get tasks by priority")
        }
    }

    override suspend fun getPinnedTasks(): Result<List<TaskMetadata>> {
        return try {
            val tasks = dataSource.getPinnedTasks()
            Result.Success(tasks)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get pinned tasks")
        }
    }

    override suspend fun getTaskSummary(): Result<TaskSummary> {
        return try {
            val summary = dataSource.getTaskSummary()
            Result.Success(summary)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get task summary")
        }
    }

    override suspend fun createTask(task: TaskMetadata): Result<TaskMetadata> {
        return try {
            val created = dataSource.createTask(task)
            refreshTasksInternal()
            Result.Success(created)
        } catch (e: Exception) {
            Result.Error(e, "Failed to create task")
        }
    }

    override suspend fun updateTask(task: TaskMetadata): Result<TaskMetadata> {
        return try {
            val updated = dataSource.updateTask(task)
                ?: throw NoSuchElementException("Task not found: ${task.id}")
            refreshTasksInternal()
            Result.Success(updated)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update task")
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            val success = dataSource.deleteTask(taskId)
            if (!success) {
                throw NoSuchElementException("Task not found: $taskId")
            }
            refreshTasksInternal()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to delete task")
        }
    }

    override suspend fun toggleTaskPin(taskId: String): Result<TaskMetadata> {
        return try {
            val task = dataSource.toggleTaskPin(taskId)
                ?: throw NoSuchElementException("Task not found: $taskId")
            refreshTasksInternal()
            Result.Success(task)
        } catch (e: Exception) {
            Result.Error(e, "Failed to toggle task pin")
        }
    }

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<TaskMetadata> {
        return try {
            val task = dataSource.updateTaskStatus(taskId, status)
                ?: throw NoSuchElementException("Task not found: $taskId")
            refreshTasksInternal()
            Result.Success(task)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update task status")
        }
    }

    override suspend fun getTodoItems(taskId: String): Result<List<TodoItem>> {
        return try {
            val items = dataSource.getTodoItems(taskId)
            Result.Success(items)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get todo items")
        }
    }

    override suspend fun toggleTodoItem(taskId: String, todoId: String): Result<TodoItem> {
        return try {
            val item = dataSource.toggleTodoItem(taskId, todoId)
                ?: throw NoSuchElementException("Todo item not found: $todoId")
            Result.Success(item)
        } catch (e: Exception) {
            Result.Error(e, "Failed to toggle todo item")
        }
    }

    override suspend fun getTasksForMeeting(meetingId: String): Result<List<TaskMetadata>> {
        return try {
            val tasks = dataSource.getTasksForMeeting(meetingId)
            Result.Success(tasks)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get tasks for meeting")
        }
    }

    override suspend fun getTasksForProject(projectId: String): Result<List<TaskMetadata>> {
        return try {
            val tasks = dataSource.getTasksForProject(projectId)
            Result.Success(tasks)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get tasks for project")
        }
    }

    override suspend fun refreshTasks(): Result<Unit> {
        return try {
            refreshTasksInternal()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to refresh tasks")
        }
    }
}
