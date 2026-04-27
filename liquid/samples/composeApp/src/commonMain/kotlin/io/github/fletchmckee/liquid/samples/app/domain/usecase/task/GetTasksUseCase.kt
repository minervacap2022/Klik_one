// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.task

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskPriority
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskStatus
import io.github.fletchmckee.liquid.samples.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting tasks with optional filtering.
 */
class GetTasksUseCase(
  private val taskRepository: TaskRepository,
) {
  suspend operator fun invoke(
    status: TaskStatus? = null,
    priority: TaskPriority? = null,
    pinnedOnly: Boolean = false,
  ): Result<List<TaskMetadata>> = when {
    pinnedOnly -> taskRepository.getPinnedTasks()
    status != null -> taskRepository.getTasksByStatus(status)
    priority != null -> taskRepository.getTasksByPriority(priority)
    else -> taskRepository.getTasks()
  }

  fun observeTasks(): Flow<Result<List<TaskMetadata>>> = taskRepository.getTasksFlow()
}
