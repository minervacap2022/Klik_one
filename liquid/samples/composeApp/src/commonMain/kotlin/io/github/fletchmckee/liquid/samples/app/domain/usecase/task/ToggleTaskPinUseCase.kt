package io.github.fletchmckee.liquid.samples.app.domain.usecase.task

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskMetadata
import io.github.fletchmckee.liquid.samples.app.domain.repository.TaskRepository

/**
 * Use case for toggling task pin status.
 */
class ToggleTaskPinUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String): Result<TaskMetadata> {
        return taskRepository.toggleTaskPin(taskId)
    }
}
