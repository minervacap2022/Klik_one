// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryOrganizationDataSource
import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.repository.OrganizationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Implementation of OrganizationRepository.
 * PRODUCTION: Requires InMemoryOrganizationDataSource - no optional dependencies.
 */
class OrganizationRepositoryImpl(
  private val dataSource: InMemoryOrganizationDataSource,
) : OrganizationRepository {

  private val _organizationsFlow = MutableStateFlow<Result<List<Organization>>>(Result.Loading)

  init {
    refreshOrganizationsInternal()
  }

  private fun refreshOrganizationsInternal() {
    try {
      val organizations = dataSource.getOrganizations()
      _organizationsFlow.value = Result.Success(organizations)
    } catch (e: Exception) {
      _organizationsFlow.value = Result.Error(e, "Failed to load organizations")
    }
  }

  override fun getOrganizationsFlow(): Flow<Result<List<Organization>>> = _organizationsFlow

  override suspend fun getOrganizations(): Result<List<Organization>> = try {
    val organizations = dataSource.getOrganizations()
    Result.Success(organizations)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get organizations")
  }

  override suspend fun getOrganizationById(id: String): Result<Organization> = try {
    val organization = dataSource.getOrganizationById(id)
      ?: throw NoSuchElementException("Organization not found: $id")
    Result.Success(organization)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get organization")
  }

  override suspend fun searchOrganizations(query: String): Result<List<Organization>> = try {
    val organizations = dataSource.searchOrganizations(query)
    Result.Success(organizations)
  } catch (e: Exception) {
    Result.Error(e, "Failed to search organizations")
  }

  override suspend fun getOrganizationsByIndustry(industry: String): Result<List<Organization>> = try {
    val organizations = dataSource.getOrganizationsByIndustry(industry)
    Result.Success(organizations)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get organizations by industry")
  }

  override suspend fun getTopOrganizations(limit: Int): Result<List<Organization>> = try {
    val organizations = dataSource.getTopOrganizations(limit)
    Result.Success(organizations)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get top organizations")
  }

  override suspend fun getOrganizationForPerson(personId: String): Result<Organization?> = try {
    val organization = dataSource.getOrganizationForPerson(personId)
    Result.Success(organization)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get organization for person")
  }

  override suspend fun createOrganization(organization: Organization): Result<Organization> = try {
    val created = dataSource.createOrganization(organization)
    refreshOrganizationsInternal()
    Result.Success(created)
  } catch (e: Exception) {
    Result.Error(e, "Failed to create organization")
  }

  override suspend fun updateOrganization(organization: Organization): Result<Organization> = try {
    val updated = dataSource.updateOrganization(organization)
      ?: throw NoSuchElementException("Organization not found: ${organization.id}")
    refreshOrganizationsInternal()
    Result.Success(updated)
  } catch (e: Exception) {
    Result.Error(e, "Failed to update organization")
  }

  override suspend fun deleteOrganization(organizationId: String): Result<Unit> = try {
    val success = dataSource.deleteOrganization(organizationId)
    if (!success) {
      throw NoSuchElementException("Organization not found: $organizationId")
    }
    refreshOrganizationsInternal()
    Result.Success(Unit)
  } catch (e: Exception) {
    Result.Error(e, "Failed to delete organization")
  }

  override suspend fun refreshOrganizations(): Result<Unit> = try {
    refreshOrganizationsInternal()
    Result.Success(Unit)
  } catch (e: Exception) {
    Result.Error(e, "Failed to refresh organizations")
  }

  override suspend fun toggleOrganizationPin(organizationId: String): Result<Organization> = try {
    val organization = dataSource.toggleOrganizationPin(organizationId)
      ?: throw NoSuchElementException("Organization not found: $organizationId")
    refreshOrganizationsInternal()
    Result.Success(organization)
  } catch (e: Exception) {
    Result.Error(e, "Failed to toggle organization pin")
  }

  override suspend fun getPinnedOrganizations(): Result<List<Organization>> = try {
    val organizations = dataSource.getPinnedOrganizations()
    Result.Success(organizations)
  } catch (e: Exception) {
    Result.Error(e, "Failed to get pinned organizations")
  }
}
