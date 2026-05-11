// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data

import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project

/**
 * Pure optimistic-rename helpers for the three renameable entity lists.
 *
 * Every long-press rename path in MainApp funnels through these so the wire-
 * shape, the fields we patch, and the matching rule (by id) can never drift
 * between detail screens, network rows, and session-detail chips.
 *
 * Both `name` and `canonicalName` are updated together — they must stay in
 * lockstep because the UI reads canonicalName in some places (e.g.
 * PersonDetail title) and name in others.
 */

fun renamePersonInList(people: List<Person>, id: String, newName: String): List<Person> =
  people.map { if (it.id == id) it.copy(name = newName, canonicalName = newName) else it }

fun renameProjectInList(projects: List<Project>, id: String, newName: String): List<Project> =
  projects.map { if (it.id == id) it.copy(name = newName, canonicalName = newName) else it }

fun renameOrganizationInList(
  orgs: List<Organization>,
  id: String,
  newName: String,
): List<Organization> =
  orgs.map { if (it.id == id) it.copy(name = newName, canonicalName = newName) else it }
