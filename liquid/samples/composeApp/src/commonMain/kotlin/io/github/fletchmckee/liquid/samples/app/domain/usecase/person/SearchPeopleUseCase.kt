package io.github.fletchmckee.liquid.samples.app.domain.usecase.person

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.repository.PersonRepository

/**
 * Use case for searching people.
 */
class SearchPeopleUseCase(
    private val personRepository: PersonRepository
) {
    suspend operator fun invoke(query: String): Result<List<Person>> {
        if (query.isBlank()) {
            return personRepository.getPeople()
        }
        return personRepository.searchPeople(query)
    }
}
