// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data.source.inmemory

import io.github.fletchmckee.liquid.samples.app.domain.entity.InfluenceTier
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.RelationshipStatus
import kotlinx.datetime.Clock

/**
 * In-memory data source for person/contact data.
 * Data is provided by the backend via RemoteDataFetcher.
 */
class InMemoryPersonDataSource {

  private val people = mutableListOf<Person>()

  /**
   * Set people externally
   */
  fun setPeople(people: List<Person>) {
    this.people.clear()
    this.people.addAll(people)
  }

  fun getPeople(): List<Person> = people.filter { !it.isArchived }

  fun getPersonById(id: String): Person? = people.find { it.id == id }

  fun searchPeople(query: String): List<Person> {
    val lowerQuery = query.lowercase()
    return people.filter {
      it.name.lowercase().contains(lowerQuery) ||
        it.role.lowercase().contains(lowerQuery) ||
        it.relatedOrganizations.any { org -> org.lowercase().contains(lowerQuery) }
    }
  }

  fun getPeopleByTier(tier: InfluenceTier): List<Person> = people.filter { it.influenceTier == tier }

  fun getPeopleByRelationshipStatus(status: RelationshipStatus): List<Person> = people.filter { it.relationshipStatus == status }

  fun getPeopleByOrganization(organization: String): List<Person> = people.filter {
    it.relatedOrganizations.any { org -> org.equals(organization, ignoreCase = true) }
  }

  private val meetingParticipants = mutableMapOf<String, List<Person>>()

  fun setMeetingParticipants(map: Map<String, List<Person>>) {
    meetingParticipants.clear()
    meetingParticipants.putAll(map)
  }

  fun getPeopleForMeeting(meetingId: String): List<Person> = meetingParticipants[meetingId] ?: emptyList()

  fun getPeopleForProject(projectId: String): List<Person> = people.filter { it.relatedProjects.contains(projectId) }

  fun getTopInfluencers(limit: Int): List<Person> = people
    .filter { it.influenceTier == InfluenceTier.S }
    .take(limit)

  fun getStrongRelationships(): List<Person> = people.filter { it.relationshipStatus == RelationshipStatus.STRONG }

  fun togglePersonPin(personId: String): Person? {
    val index = people.indexOfFirst { it.id == personId }
    if (index == -1) return null

    val person = people[index]
    val updated = person.copy(
      isPinned = !person.isPinned,
      pinnedAt = if (!person.isPinned) Clock.System.now().toEpochMilliseconds() else null,
    )
    people[index] = updated
    return updated
  }

  fun getPinnedPeople(): List<Person> = people.filter { it.isPinned && !it.isArchived }
    .sortedByDescending { it.pinnedAt }

  fun archivePerson(personId: String): Boolean {
    val index = people.indexOfFirst { it.id == personId }
    if (index == -1) return false

    people[index] = people[index].copy(isArchived = true, isPinned = false)
    return true
  }

  fun unarchivePerson(personId: String): Boolean {
    val index = people.indexOfFirst { it.id == personId }
    if (index == -1) return false

    people[index] = people[index].copy(isArchived = false)
    return true
  }
}
