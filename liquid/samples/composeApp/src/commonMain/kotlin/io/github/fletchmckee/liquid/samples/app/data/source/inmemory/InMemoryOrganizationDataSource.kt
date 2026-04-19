package io.github.fletchmckee.liquid.samples.app.data.source.inmemory

import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import kotlinx.datetime.Clock

/**
 * In-memory data source for organization data.
 * Data is provided by the backend via RemoteDataFetcher.
 */
class InMemoryOrganizationDataSource {

    private val organizations = mutableListOf<Organization>()

    fun setOrganizations(organizations: List<Organization>) {
        this.organizations.clear()
        this.organizations.addAll(organizations)
    }

    fun getOrganizations(): List<Organization> = organizations.filter { !it.isArchived }

    fun getOrganizationById(id: String): Organization? = organizations.find { it.id == id }

    fun searchOrganizations(query: String): List<Organization> {
        val lowerQuery = query.lowercase()
        return organizations.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.industry.lowercase().contains(lowerQuery)
        }
    }

    fun getOrganizationsByIndustry(industry: String): List<Organization> {
        return organizations.filter { it.industry.equals(industry, ignoreCase = true) }
    }

    fun getTopOrganizations(limit: Int): List<Organization> {
        return organizations
            .sortedByDescending { it.relationshipScore }
            .take(limit)
    }

    fun getOrganizationForPerson(personName: String): Organization? {
        return organizations.find { org ->
            org.employees.any { it.contains(personName, ignoreCase = true) }
        }
    }

    fun createOrganization(organization: Organization): Organization {
        val newOrg = organization.copy(id = "org${organizations.size + 1}")
        organizations.add(newOrg)
        return newOrg
    }

    fun updateOrganization(organization: Organization): Organization? {
        val index = organizations.indexOfFirst { it.id == organization.id }
        if (index == -1) return null

        organizations[index] = organization
        return organization
    }

    fun deleteOrganization(organizationId: String): Boolean {
        return organizations.removeAll { it.id == organizationId }
    }

    fun toggleOrganizationPin(organizationId: String): Organization? {
        val index = organizations.indexOfFirst { it.id == organizationId }
        if (index == -1) return null

        val org = organizations[index]
        val updated = org.copy(
            isPinned = !org.isPinned,
            pinnedAt = if (!org.isPinned) Clock.System.now().toEpochMilliseconds() else null
        )
        organizations[index] = updated
        return updated
    }

    fun getPinnedOrganizations(): List<Organization> {
        return organizations.filter { it.isPinned && !it.isArchived }
            .sortedByDescending { it.pinnedAt }
    }

    fun archiveOrganization(organizationId: String): Boolean {
        val index = organizations.indexOfFirst { it.id == organizationId }
        if (index == -1) return false

        organizations[index] = organizations[index].copy(isArchived = true, isPinned = false)
        return true
    }

    fun unarchiveOrganization(organizationId: String): Boolean {
        val index = organizations.indexOfFirst { it.id == organizationId }
        if (index == -1) return false

        organizations[index] = organizations[index].copy(isArchived = false)
        return true
    }
}
