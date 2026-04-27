// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryProjectDataSource
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.domain.entity.ProjectStatus
import io.github.fletchmckee.liquid.samples.app.domain.repository.ProjectRepository
import io.github.fletchmckee.liquid.samples.app.domain.repository.ProjectStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Implementation of ProjectRepository.
 * PRODUCTION: Requires InMemoryProjectDataSource - no optional dependencies.
 */
class ProjectRepositoryImpl(
  private val dataSource: InMemoryProjectDataSource,
) : ProjectRepository {

  private val _projectsFlow = MutableStateFlow<Result<List<Project>>>(Result.Loading)

  init {
    refreshProjectsInternal()
  }

  private fun refreshProjectsInternal() {
    try {
      val projects = dataSource.getProjects()
      _projectsFlow.value = Result.Success(projects)
    } catch (e: Exception) {
      _projectsFlow.value = Result.Error(e, "Failed to load projects")
    }
  }

  override fun getProjectsFlow(): Flow<Result<List<Project>>> = _projectsFlow

  override suspend fun getProjects(): Result<List<Project>> = try {
    val projects = dataSource.getProjects()
    Result.Success(projects)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get projects")
  }

  override suspend fun getProjectById(id: String): Result<Project> = try {
    val project = dataSource.getProjectById(id)
      ?: throw NoSuchElementException("Project not found: $id")
    Result.Success(project)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get project")
  }

  override suspend fun getProjectsByStatus(status: ProjectStatus): Result<List<Project>> = try {
    val projects = dataSource.getProjectsByStatus(status)
    Result.Success(projects)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get projects by status")
  }

  override suspend fun getActiveProjects(): Result<List<Project>> = try {
    val projects = dataSource.getActiveProjects()
    Result.Success(projects)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get active projects")
  }

  override suspend fun searchProjects(query: String): Result<List<Project>> = try {
    val projects = dataSource.searchProjects(query)
    Result.Success(projects)
  } catch (e: Exception) {
    Result.Error(e, "Failed to search projects")
  }

  override suspend fun getProjectsForOrganization(organizationId: String): Result<List<Project>> = try {
    val projects = dataSource.getProjectsForOrganization(organizationId)
    Result.Success(projects)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get projects for organization")
  }

  override suspend fun getProjectsForPerson(personId: String): Result<List<Project>> = try {
    val projects = dataSource.getProjectsForPerson(personId)
    Result.Success(projects)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get projects for person")
  }

  override suspend fun createProject(project: Project): Result<Project> = try {
    val created = dataSource.createProject(project)
    refreshProjectsInternal()
    Result.Success(created)
  } catch (e: Exception) {
    Result.Error(e, "Failed to create project")
  }

  override suspend fun updateProject(project: Project): Result<Project> = try {
    val updated = dataSource.updateProject(project)
      ?: throw NoSuchElementException("Project not found: ${project.id}")
    refreshProjectsInternal()
    Result.Success(updated)
  } catch (e: Exception) {
    Result.Error(e, "Failed to update project")
  }

  override suspend fun updateProjectStatus(projectId: String, status: ProjectStatus): Result<Project> = try {
    val project = dataSource.updateProjectStatus(projectId, status)
      ?: throw NoSuchElementException("Project not found: $projectId")
    refreshProjectsInternal()
    Result.Success(project)
  } catch (e: Exception) {
    Result.Error(e, "Failed to update project status")
  }

  override suspend fun updateProjectProgress(projectId: String, progress: Int): Result<Project> = try {
    val project = dataSource.updateProjectProgress(projectId, progress)
      ?: throw NoSuchElementException("Project not found: $projectId")
    refreshProjectsInternal()
    Result.Success(project)
  } catch (e: Exception) {
    Result.Error(e, "Failed to update project progress")
  }

  override suspend fun deleteProject(projectId: String): Result<Unit> = try {
    val success = dataSource.deleteProject(projectId)
    if (!success) {
      throw NoSuchElementException("Project not found: $projectId")
    }
    refreshProjectsInternal()
    Result.Success(Unit)
  } catch (e: Exception) {
    Result.Error(e, "Failed to delete project")
  }

  override suspend fun getProjectStatistics(): Result<ProjectStatistics> = try {
    val stats = dataSource.getProjectStatistics()
    Result.Success(stats)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get project statistics")
  }

  override suspend fun refreshProjects(): Result<Unit> = try {
    refreshProjectsInternal()
    Result.Success(Unit)
  } catch (e: Exception) {
    Result.Error(e, "Failed to refresh projects")
  }

  override suspend fun toggleProjectPin(projectId: String): Result<Project> = try {
    val project = dataSource.toggleProjectPin(projectId)
      ?: throw NoSuchElementException("Project not found: $projectId")
    refreshProjectsInternal()
    Result.Success(project)
  } catch (e: Exception) {
    Result.Error(e, "Failed to toggle project pin")
  }

  override suspend fun getPinnedProjects(): Result<List<Project>> = try {
    val projects = dataSource.getPinnedProjects()
    Result.Success(projects)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get pinned projects")
  }
}
