// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.organization

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.repository.OrganizationRepository

/**
 * Use case for toggling organization pin status.
 */
class ToggleOrganizationPinUseCase(
  private val organizationRepository: OrganizationRepository,
) {
  suspend operator fun invoke(organizationId: String): Result<Organization> = organizationRepository.toggleOrganizationPin(organizationId)
}
