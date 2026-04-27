// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskPriority
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskStatus
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskSummary
import io.github.fletchmckee.liquid.samples.app.domain.entity.TodoItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for task and todo operations.
 */
interface TaskRepository {

  /**
   * Get all tasks as a reactive flow.
   */
  fun getTasksFlow(): Flow<Result<List<TaskMetadata>>>

  /**
   * Get all tasks.
   */
  suspend fun getTasks(): Result<List<TaskMetadata>>

  /**
   * Get a single task by ID.
   */
  suspend fun getTaskById(id: String): Result<TaskMetadata>

  /**
   * Get tasks filtered by status.
   */
  suspend fun getTasksByStatus(status: TaskStatus): Result<List<TaskMetadata>>

  /**
   * Get tasks filtered by priority.
   */
  suspend fun getTasksByPriority(priority: TaskPriority): Result<List<TaskMetadata>>

  /**
   * Get pinned tasks.
   */
  suspend fun getPinnedTasks(): Result<List<TaskMetadata>>

  /**
   * Get task summary (counts by status).
   */
  suspend fun getTaskSummary(): Result<TaskSummary>

  /**
   * Create a new task.
   */
  suspend fun createTask(task: TaskMetadata): Result<TaskMetadata>

  /**
   * Update an existing task.
   */
  suspend fun updateTask(task: TaskMetadata): Result<TaskMetadata>

  /**
   * Delete a task.
   */
  suspend fun deleteTask(taskId: String): Result<Unit>

  /**
   * Toggle task pin status.
   */
  suspend fun toggleTaskPin(taskId: String): Result<TaskMetadata>

  /**
   * Update task status.
   */
  suspend fun updateTaskStatus(taskId: String, status: TaskStatus): Result<TaskMetadata>

  /**
   * Get todo items for a specific task.
   */
  suspend fun getTodoItems(taskId: String): Result<List<TodoItem>>

  /**
   * Toggle todo item completion.
   */
  suspend fun toggleTodoItem(taskId: String, todoId: String): Result<TodoItem>

  /**
   * Get tasks related to a meeting.
   */
  suspend fun getTasksForMeeting(meetingId: String): Result<List<TaskMetadata>>

  /**
   * Get tasks related to a project.
   */
  suspend fun getTasksForProject(projectId: String): Result<List<TaskMetadata>>

  /**
   * Refresh tasks from remote source.
   */
  suspend fun refreshTasks(): Result<Unit>
}
