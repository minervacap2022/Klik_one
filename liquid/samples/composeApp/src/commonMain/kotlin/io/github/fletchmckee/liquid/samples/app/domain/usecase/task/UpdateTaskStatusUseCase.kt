package io.github.fletchmckee.liquid.samples.app.domain.usecase.task

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskStatus
import io.github.fletchmckee.liquid.samples.app.domain.repository.TaskRepository

/**
 * Use case for updating task status.
 */
class UpdateTaskStatusUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, status: TaskStatus): Result<TaskMetadata> {
        return taskRepository.updateTaskStatus(taskId, status)
    }
}
