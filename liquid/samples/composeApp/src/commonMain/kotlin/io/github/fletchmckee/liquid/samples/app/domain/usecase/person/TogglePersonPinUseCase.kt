package io.github.fletchmckee.liquid.samples.app.domain.usecase.person

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.repository.PersonRepository

/**
 * Use case for toggling person pin status.
 */
class TogglePersonPinUseCase(
    private val personRepository: PersonRepository
) {
    suspend operator fun invoke(personId: String): Result<Person> {
        return personRepository.togglePersonPin(personId)
    }
}
