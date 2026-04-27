// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.project

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.domain.repository.ProjectRepository

/**
 * Use case for toggling project pin status.
 */
class ToggleProjectPinUseCase(
  private val projectRepository: ProjectRepository,
) {
  suspend operator fun invoke(projectId: String): Result<Project> = projectRepository.toggleProjectPin(projectId)
}
