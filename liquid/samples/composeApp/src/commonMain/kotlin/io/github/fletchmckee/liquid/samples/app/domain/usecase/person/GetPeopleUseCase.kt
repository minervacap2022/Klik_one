// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.person

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.InfluenceTier
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting people with optional filtering.
 */
class GetPeopleUseCase(
  private val personRepository: PersonRepository,
) {
  suspend operator fun invoke(
    tier: InfluenceTier? = null,
    organizationId: String? = null,
    strongRelationshipsOnly: Boolean = false,
  ): Result<List<Person>> = when {
    strongRelationshipsOnly -> personRepository.getStrongRelationships()
    tier != null -> personRepository.getPeopleByTier(tier)
    organizationId != null -> personRepository.getPeopleByOrganization(organizationId)
    else -> personRepository.getPeople()
  }

  fun observePeople(): Flow<Result<List<Person>>> = personRepository.getPeopleFlow()
}
