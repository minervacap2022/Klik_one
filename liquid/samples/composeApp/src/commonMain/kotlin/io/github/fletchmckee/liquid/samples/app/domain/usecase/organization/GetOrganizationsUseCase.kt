// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.organization

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.repository.OrganizationRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting organizations.
 */
class GetOrganizationsUseCase(
  private val organizationRepository: OrganizationRepository,
) {
  suspend operator fun invoke(
    industry: String? = null,
    topOnly: Boolean = false,
    limit: Int = 10,
  ): Result<List<Organization>> = when {
    topOnly -> organizationRepository.getTopOrganizations(limit)
    industry != null -> organizationRepository.getOrganizationsByIndustry(industry)
    else -> organizationRepository.getOrganizations()
  }

  fun observeOrganizations(): Flow<Result<List<Organization>>> = organizationRepository.getOrganizationsFlow()
}
