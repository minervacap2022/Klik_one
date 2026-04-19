package io.github.fletchmckee.liquid.samples.app.domain.usecase.person

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.repository.PersonRepository

/**
 * Use case for getting top influencers.
 */
class GetTopInfluencersUseCase(
    private val personRepository: PersonRepository
) {
    suspend operator fun invoke(limit: Int = 10): Result<List<Person>> {
        return personRepository.getTopInfluencers(limit)
    }
}
