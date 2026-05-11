// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.data

import io.github.fletchmckee.liquid.samples.app.domain.entity.Organization
import io.github.fletchmckee.liquid.samples.app.domain.entity.Person
import io.github.fletchmckee.liquid.samples.app.domain.entity.Project
import io.github.fletchmckee.liquid.samples.app.domain.entity.ProjectStatus
import io.github.fletchmckee.liquid.samples.app.domain.entity.ProjectTrend
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Locks in the "one rename code path" rule. Every long-press / detail-screen
 * rename funnels through these helpers, so:
 *
 *  - the targeted row gets name + canonicalName updated together (UI reads
 *    different fields in different places),
 *  - non-targeted rows are unchanged,
 *  - an unknown id is a no-op (no fabricated rows),
 *  - empty lists are handled.
 *
 * If any of these break we'd silently corrupt the network/people lists across
 * the seven UI entry points that share these functions.
 */
class EntityRenameOpsTest {

  private fun person(id: String, name: String = "n-$id", canonical: String = "c-$id") = Person(
    id = id,
    name = name,
    canonicalName = canonical,
    role = "",
  )

  private fun project(id: String, name: String = "n-$id", canonical: String = "c-$id") = Project(
    id = id,
    name = name,
    canonicalName = canonical,
    status = ProjectStatus.ON_TRACK,
    progress = 0f,
    trend = ProjectTrend.STABLE,
    lead = "",
    teamMembers = emptyList(),
    stage = "",
    goals = emptyList(),
    kpis = emptyList(),
    risks = emptyList(),
    budget = "",
  )

  private fun org(id: String, name: String = "n-$id", canonical: String = "c-$id") = Organization(
    id = id,
    name = name,
    canonicalName = canonical,
    industry = "",
    relatedSessions = emptyList(),
    employees = emptyList(),
    relatedProjects = emptyList(),
  )

  @Test
  fun renamePerson_updatesNameAndCanonicalOnTargetOnly() {
    val before = listOf(person("a"), person("b"), person("c"))
    val after = renamePersonInList(before, "b", "Bob")

    assertEquals("n-a", after[0].name)
    assertEquals("c-a", after[0].canonicalName)
    assertEquals("Bob", after[1].name)
    assertEquals("Bob", after[1].canonicalName)
    assertEquals("n-c", after[2].name)
    assertEquals("c-c", after[2].canonicalName)
  }

  @Test
  fun renamePerson_unknownIdIsNoOp() {
    val before = listOf(person("a"), person("b"))
    val after = renamePersonInList(before, "ghost", "Boo")
    assertEquals(before.map { it.name }, after.map { it.name })
    assertEquals(before.map { it.canonicalName }, after.map { it.canonicalName })
  }

  @Test
  fun renamePerson_emptyListReturnsEmpty() {
    assertEquals(emptyList(), renamePersonInList(emptyList(), "a", "X"))
  }

  @Test
  fun renameProject_updatesNameAndCanonicalOnTargetOnly() {
    val before = listOf(project("p1"), project("p2"))
    val after = renameProjectInList(before, "p2", "Alpha")
    assertEquals("n-p1", after[0].name)
    assertEquals("Alpha", after[1].name)
    assertEquals("Alpha", after[1].canonicalName)
  }

  @Test
  fun renameProject_unknownIdIsNoOp() {
    val before = listOf(project("p1"))
    val after = renameProjectInList(before, "ghost", "Alpha")
    assertEquals(before.map { it.name }, after.map { it.name })
  }

  @Test
  fun renameOrganization_updatesNameAndCanonicalOnTargetOnly() {
    val before = listOf(org("o1"), org("o2"))
    val after = renameOrganizationInList(before, "o1", "ACME")
    assertEquals("ACME", after[0].name)
    assertEquals("ACME", after[0].canonicalName)
    assertEquals("n-o2", after[1].name)
    assertEquals("c-o2", after[1].canonicalName)
  }

  @Test
  fun renameOrganization_unknownIdIsNoOp() {
    val before = listOf(org("o1"))
    val after = renameOrganizationInList(before, "ghost", "ACME")
    assertEquals(before.map { it.name }, after.map { it.name })
  }

  @Test
  fun rename_preservesListLength() {
    val pl = listOf(person("a"), person("b"), person("c"))
    assertEquals(pl.size, renamePersonInList(pl, "a", "X").size)
    assertEquals(pl.size, renamePersonInList(pl, "ghost", "X").size)
  }
}
