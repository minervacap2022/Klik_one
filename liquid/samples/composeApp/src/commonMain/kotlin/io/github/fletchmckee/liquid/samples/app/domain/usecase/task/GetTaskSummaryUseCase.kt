package io.github.fletchmckee.liquid.samples.app.domain.usecase.task

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.TaskSummary
import io.github.fletchmckee.liquid.samples.app.domain.repository.TaskRepository

/**
 * Use case for getting task summary statistics.
 */
class GetTaskSummaryUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(): Result<TaskSummary> {
        return taskRepository.getTaskSummary()
    }
}
