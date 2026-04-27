// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.domain.entity.ProjectStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for project operations.
 */
interface ProjectRepository {

  /**
   * Get all projects as a reactive flow.
   */
  fun getProjectsFlow(): Flow<Result<List<Project>>>

  /**
   * Get all projects.
   */
  suspend fun getProjects(): Result<List<Project>>

  /**
   * Get a project by ID.
   */
  suspend fun getProjectById(id: String): Result<Project>

  /**
   * Get projects filtered by status.
   */
  suspend fun getProjectsByStatus(status: ProjectStatus): Result<List<Project>>

  /**
   * Get active projects.
   */
  suspend fun getActiveProjects(): Result<List<Project>>

  /**
   * Search projects by name.
   */
  suspend fun searchProjects(query: String): Result<List<Project>>

  /**
   * Get projects for an organization.
   */
  suspend fun getProjectsForOrganization(organizationId: String): Result<List<Project>>

  /**
   * Get projects involving a person.
   */
  suspend fun getProjectsForPerson(personId: String): Result<List<Project>>

  /**
   * Create a new project.
   */
  suspend fun createProject(project: Project): Result<Project>

  /**
   * Update a project.
   */
  suspend fun updateProject(project: Project): Result<Project>

  /**
   * Update project status.
   */
  suspend fun updateProjectStatus(projectId: String, status: ProjectStatus): Result<Project>

  /**
   * Update project progress.
   */
  suspend fun updateProjectProgress(projectId: String, progress: Int): Result<Project>

  /**
   * Delete a project.
   */
  suspend fun deleteProject(projectId: String): Result<Unit>

  /**
   * Get project statistics.
   */
  suspend fun getProjectStatistics(): Result<ProjectStatistics>

  /**
   * Refresh projects from remote source.
   */
  suspend fun refreshProjects(): Result<Unit>

  /**
   * Toggle pin status for a project.
   */
  suspend fun toggleProjectPin(projectId: String): Result<Project>

  /**
   * Get pinned projects.
   */
  suspend fun getPinnedProjects(): Result<List<Project>>
}

/**
 * Statistics summary for projects.
 */
data class ProjectStatistics(
  val totalProjects: Int,
  val activeProjects: Int,
  val completedProjects: Int,
  val onHoldProjects: Int,
  val averageProgress: Int,
)
