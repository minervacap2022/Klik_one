package io.github.fletchmckee.liquid.samples.app.domain.usecase.project

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.domain.repository.ProjectRepository

/**
 * Use case for toggling project pin status.
 */
class ToggleProjectPinUseCase(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(projectId: String): Result<Project> {
        return projectRepository.toggleProjectPin(projectId)
    }
}
