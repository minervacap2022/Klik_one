// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for organization operations.
 */
interface OrganizationRepository {

  /**
   * Get all organizations as a reactive flow.
   */
  fun getOrganizationsFlow(): Flow<Result<List<Organization>>>

  /**
   * Get all organizations.
   */
  suspend fun getOrganizations(): Result<List<Organization>>

  /**
   * Get an organization by ID.
   */
  suspend fun getOrganizationById(id: String): Result<Organization>

  /**
   * Search organizations by name.
   */
  suspend fun searchOrganizations(query: String): Result<List<Organization>>

  /**
   * Get organizations by industry.
   */
  suspend fun getOrganizationsByIndustry(industry: String): Result<List<Organization>>

  /**
   * Get organizations sorted by relationship score.
   */
  suspend fun getTopOrganizations(limit: Int = 10): Result<List<Organization>>

  /**
   * Get organization for a person.
   */
  suspend fun getOrganizationForPerson(personId: String): Result<Organization?>

  /**
   * Create a new organization.
   */
  suspend fun createOrganization(organization: Organization): Result<Organization>

  /**
   * Update an organization.
   */
  suspend fun updateOrganization(organization: Organization): Result<Organization>

  /**
   * Delete an organization.
   */
  suspend fun deleteOrganization(organizationId: String): Result<Unit>

  /**
   * Refresh organizations from remote source.
   */
  suspend fun refreshOrganizations(): Result<Unit>

  /**
   * Toggle pin status for an organization.
   */
  suspend fun toggleOrganizationPin(organizationId: String): Result<Organization>

  /**
   * Get pinned organizations.
   */
  suspend fun getPinnedOrganizations(): Result<List<Organization>>
}
