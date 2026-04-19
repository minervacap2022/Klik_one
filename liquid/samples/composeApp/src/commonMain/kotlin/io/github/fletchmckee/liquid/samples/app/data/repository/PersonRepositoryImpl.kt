package io.github.fletchmckee.liquid.samples.app.data.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryPersonDataSource
import io.github.fletchmckee.liquid.samples.app.domain.entity.InfluenceTier
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.RelationshipStatus
import io.github.fletchmckee.liquid.samples.app.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Implementation of PersonRepository.
 * PRODUCTION: Requires InMemoryPersonDataSource - no optional dependencies.
 */
class PersonRepositoryImpl(
    private val dataSource: InMemoryPersonDataSource
) : PersonRepository {

    private val _peopleFlow = MutableStateFlow<Result<List<Person>>>(Result.Loading)

    init {
        refreshPeopleInternal()
    }

    private fun refreshPeopleInternal() {
        try {
            val people = dataSource.getPeople()
            _peopleFlow.value = Result.Success(people)
        } catch (e: Exception) {
            _peopleFlow.value = Result.Error(e, "Failed to load people")
        }
    }

    override fun getPeopleFlow(): Flow<Result<List<Person>>> = _peopleFlow

    override suspend fun getPeople(): Result<List<Person>> {
        return try {
            val people = dataSource.getPeople()
            Result.Success(people)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get people")
        }
    }

    override suspend fun getPersonById(id: String): Result<Person> {
        return try {
            val person = dataSource.getPersonById(id)
                ?: throw NoSuchElementException("Person not found: $id")
            Result.Success(person)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get person")
        }
    }

    override suspend fun searchPeople(query: String): Result<List<Person>> {
        return try {
            val people = dataSource.searchPeople(query)
            Result.Success(people)
        } catch (e: Exception) {
            Result.Error(e, "Failed to search people")
        }
    }

    override suspend fun getPeopleByTier(tier: InfluenceTier): Result<List<Person>> {
        return try {
            val people = dataSource.getPeopleByTier(tier)
            Result.Success(people)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get people by tier")
        }
    }

    override suspend fun getPeopleByRelationshipStatus(status: RelationshipStatus): Result<List<Person>> {
        return try {
            val people = dataSource.getPeopleByRelationshipStatus(status)
            Result.Success(people)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get people by relationship status")
        }
    }

    override suspend fun getPeopleByOrganization(organizationId: String): Result<List<Person>> {
        return try {
            val people = dataSource.getPeopleByOrganization(organizationId)
            Result.Success(people)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get people by organization")
        }
    }

    override suspend fun getPeopleForMeeting(meetingId: String): Result<List<Person>> {
        return try {
            val people = dataSource.getPeopleForMeeting(meetingId)
            Result.Success(people)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get people for meeting")
        }
    }

    override suspend fun getPeopleForProject(projectId: String): Result<List<Person>> {
        return try {
            val people = dataSource.getPeopleForProject(projectId)
            Result.Success(people)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get people for project")
        }
    }

    override suspend fun getTopInfluencers(limit: Int): Result<List<Person>> {
        return try {
            val people = dataSource.getTopInfluencers(limit)
            Result.Success(people)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get top influencers")
        }
    }



    override suspend fun getStrongRelationships(): Result<List<Person>> {
        return try {
            val people = dataSource.getStrongRelationships()
            Result.Success(people)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get strong relationships")
        }
    }

    override suspend fun refreshPeople(): Result<Unit> {
        return try {
            refreshPeopleInternal()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to refresh people")
        }
    }

    override suspend fun togglePersonPin(personId: String): Result<Person> {
        return try {
            val person = dataSource.togglePersonPin(personId)
                ?: throw NoSuchElementException("Person not found: $personId")
            refreshPeopleInternal()
            Result.Success(person)
        } catch (e: Exception) {
            Result.Error(e, "Failed to toggle person pin")
        }
    }

    override suspend fun getPinnedPeople(): Result<List<Person>> {
        return try {
            val people = dataSource.getPinnedPeople()
            Result.Success(people)
        } catch (e: Exception) {
            Result.Error(e, "Failed to get pinned people")
        }
    }
}
