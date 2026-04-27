// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.usecase.person

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.repository.PersonRepository

/**
 * Use case for toggling person pin status.
 */
class TogglePersonPinUseCase(
  private val personRepository: PersonRepository,
) {
  suspend operator fun invoke(personId: String): Result<Person> = personRepository.togglePersonPin(personId)
}
