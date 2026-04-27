// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.repository

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.domain.entity.InfluenceTier
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.RelationshipStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for person/contact operations.
 */
interface PersonRepository {

  /**
   * Get all people as a reactive flow.
   */
  fun getPeopleFlow(): Flow<Result<List<Person>>>

  /**
   * Get all people.
   */
  suspend fun getPeople(): Result<List<Person>>

  /**
   * Get a person by ID.
   */
  suspend fun getPersonById(id: String): Result<Person>

  /**
   * Search people by name or role.
   */
  suspend fun searchPeople(query: String): Result<List<Person>>

  /**
   * Get people by influence tier.
   */
  suspend fun getPeopleByTier(tier: InfluenceTier): Result<List<Person>>

  /**
   * Get people by relationship status.
   */
  suspend fun getPeopleByRelationshipStatus(status: RelationshipStatus): Result<List<Person>>

  /**
   * Get people by organization.
   */
  suspend fun getPeopleByOrganization(organizationId: String): Result<List<Person>>

  /**
   * Get people involved in a meeting.
   */
  suspend fun getPeopleForMeeting(meetingId: String): Result<List<Person>>

  /**
   * Get people related to a project.
   */
  suspend fun getPeopleForProject(projectId: String): Result<List<Person>>

  /**
   * Get top influencers (S-tier people).
   */
  suspend fun getTopInfluencers(limit: Int = 10): Result<List<Person>>

  /**
   * Get people with strong relationships.
   */
  suspend fun getStrongRelationships(): Result<List<Person>>

  /**
   * Refresh people from remote source.
   */
  suspend fun refreshPeople(): Result<Unit>

  /**
   * Toggle pin status for a person.
   */
  suspend fun togglePersonPin(personId: String): Result<Person>

  /**
   * Get pinned people.
   */
  suspend fun getPinnedPeople(): Result<List<Person>>
}
