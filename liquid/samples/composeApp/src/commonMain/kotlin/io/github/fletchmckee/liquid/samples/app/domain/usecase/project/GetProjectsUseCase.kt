// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.project

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.domain.entity.ProjectStatus
import io.github.fletchmckee.liquid.samples.app.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting projects with optional filtering.
 */
class GetProjectsUseCase(
  private val projectRepository: ProjectRepository,
) {
  suspend operator fun invoke(
    status: ProjectStatus? = null,
    activeOnly: Boolean = false,
  ): Result<List<Project>> = when {
    activeOnly -> projectRepository.getActiveProjects()
    status != null -> projectRepository.getProjectsByStatus(status)
    else -> projectRepository.getProjects()
  }

  fun observeProjects(): Flow<Result<List<Project>>> = projectRepository.getProjectsFlow()
}
