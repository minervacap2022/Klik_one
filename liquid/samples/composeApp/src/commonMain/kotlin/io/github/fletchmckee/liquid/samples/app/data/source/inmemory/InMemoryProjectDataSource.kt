package io.github.fletchmckee.liquid.samples.app.data.source.inmemory

import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.domain.entity.ProjectStatus
import io.github.fletchmckee.liquid.samples.app.domain.entity.ProjectTrend
import io.github.fletchmckee.liquid.samples.app.domain.repository.ProjectStatistics
import kotlinx.datetime.Clock

/**
 * In-memory data source for project data.
 * Data is provided by the backend via RemoteDataFetcher.
 */
class InMemoryProjectDataSource {

    private val projects = mutableListOf<Project>()

    /**
     * Set projects externally
     */
    fun setProjects(projects: List<Project>) {
        this.projects.clear()
        this.projects.addAll(projects)
    }

    fun getProjects(): List<Project> = projects.filter { !it.isArchived }

    fun getProjectById(id: String): Project? = projects.find { it.id == id }

    fun getProjectsByStatus(status: ProjectStatus): List<Project> {
        return projects.filter { it.status == status }
    }

    fun getActiveProjects(): List<Project> {
        return projects.filter { it.status == ProjectStatus.ON_TRACK }
    }

    fun searchProjects(query: String): List<Project> {
        val lowerQuery = query.lowercase()
        return projects.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.lead.lowercase().contains(lowerQuery) ||
            it.stage.lowercase().contains(lowerQuery)
        }
    }

    fun getProjectsForPerson(personId: String): List<Project> {
        return projects.filter { project ->
            // Check relatedPeople list (primary)
            personId in project.relatedPeople ||
            // Also check teamMembers and lead for backward compatibility
            project.teamMembers.any { it.contains(personId, ignoreCase = true) } ||
            project.lead.contains(personId, ignoreCase = true)
        }
    }

    fun getProjectsForOrganization(organizationId: String): List<Project> {
        return projects.filter { project ->
            organizationId in project.relatedOrganizations
        }
    }

    fun createProject(project: Project): Project {
        val newProject = project.copy(
            id = "proj${projects.size + 1}"
        )
        projects.add(newProject)
        return newProject
    }

    fun updateProject(project: Project): Project? {
        val index = projects.indexOfFirst { it.id == project.id }
        if (index == -1) return null

        projects[index] = project
        return project
    }

    fun updateProjectStatus(projectId: String, status: ProjectStatus): Project? {
        val index = projects.indexOfFirst { it.id == projectId }
        if (index == -1) return null

        val updated = projects[index].copy(status = status)
        projects[index] = updated
        return updated
    }

    fun updateProjectProgress(projectId: String, progress: Int): Project? {
        val index = projects.indexOfFirst { it.id == projectId }
        if (index == -1) return null

        val currentProgress = projects[index].progress
        val newProgress = progress.coerceIn(0, 100) / 100f
        val trend = when {
            newProgress > currentProgress -> ProjectTrend.UP
            newProgress < currentProgress -> ProjectTrend.DOWN
            else -> ProjectTrend.STABLE
        }

        val updated = projects[index].copy(
            progress = newProgress,
            trend = trend
        )
        projects[index] = updated
        return updated
    }

    fun deleteProject(projectId: String): Boolean {
        return projects.removeAll { it.id == projectId }
    }

    fun getProjectStatistics(): ProjectStatistics {
        return ProjectStatistics(
            totalProjects = projects.size,
            activeProjects = projects.count { it.status == ProjectStatus.ON_TRACK },
            completedProjects = projects.count { it.status == ProjectStatus.COMPLETED },
            onHoldProjects = projects.count { it.status == ProjectStatus.ON_HOLD },
            averageProgress = (projects.map { it.progress }.average() * 100).toInt()
        )
    }

    fun toggleProjectPin(projectId: String): Project? {
        val index = projects.indexOfFirst { it.id == projectId }
        if (index == -1) return null

        val project = projects[index]
        val updated = project.copy(
            isPinned = !project.isPinned,
            pinnedAt = if (!project.isPinned) Clock.System.now().toEpochMilliseconds() else null
        )
        projects[index] = updated
        return updated
    }

    fun getPinnedProjects(): List<Project> {
        return projects.filter { it.isPinned && !it.isArchived }
            .sortedByDescending { it.pinnedAt }
    }

    fun archiveProject(projectId: String): Boolean {
        val index = projects.indexOfFirst { it.id == projectId }
        if (index == -1) return false

        projects[index] = projects[index].copy(isArchived = true, isPinned = false)
        return true
    }

    fun unarchiveProject(projectId: String): Boolean {
        val index = projects.indexOfFirst { it.id == projectId }
        if (index == -1) return false

        projects[index] = projects[index].copy(isArchived = false)
        return true
    }
}
