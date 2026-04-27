// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.domain.entity

import io.github.fletchmckee.liquid.samples.app.core.Result
import io.github.fletchmckee.liquid.samples.app.data.repository.CalendarRepositoryImpl
import io.github.fletchmckee.liquid.samples.app.data.source.inmemory.InMemoryCalendarDataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

/**
 * Locks in the NO SILENT SWALLOW rule: every session the data source produced — even
 * one with a placeholder title and zero content — must reach the UI. The repository
 * never filters; the UI is responsible for presenting empty cards. See
 * `~/.claude/projects/.../memory/feedback_no_silent_swallow.md`.
 */
class MeetingFilterTest {

  private fun emptyPlaceholderMeeting(id: String = "empty") = Meeting(
    id = id,
    title = "Meeting on Apr 24, 2026",
    date = LocalDate(2026, 4, 24),
    time = "5:45 PM",
    participants = emptyList(),
    summary = "",
    actionItems = emptyList(),
    minutes = emptyList(),
    transcript = null,
  )

  private fun realMeeting(id: String = "real") = Meeting(
    id = id,
    title = "Q3 项目规划会议",
    date = LocalDate(2026, 4, 24),
    time = "3:00 PM",
    participants = emptyList(),
    summary = "Discussed roadmap.",
    actionItems = emptyList(),
    minutes = emptyList(),
    transcript = null,
  )

  @Test
  fun repository_does_not_drop_empty_placeholder_meetings() = runTest {
    val ds = InMemoryCalendarDataSource()
    ds.setMeetings(listOf(emptyPlaceholderMeeting(), realMeeting()))
    val repo = CalendarRepositoryImpl(ds)

    val result = repo.getMeetingsForDate(LocalDate(2026, 4, 24))
    assertTrue(result is Result.Success, "getMeetingsForDate should succeed")
    val ids = result.data.map { it.id }
    assertEquals(setOf("empty", "real"), ids.toSet(), "Empty meetings must NOT be filtered out")
  }

  @Test
  fun meetings_flow_emits_every_meeting_including_empty() = runTest {
    val ds = InMemoryCalendarDataSource()
    ds.setMeetings(listOf(emptyPlaceholderMeeting("a"), emptyPlaceholderMeeting("b")))
    val repo = CalendarRepositoryImpl(ds)

    // refreshMeetingsInternal runs in init — pull the current value via getMeetingsForDate
    val result = repo.getMeetingsForDate(LocalDate(2026, 4, 24))
    assertTrue(result is Result.Success)
    assertEquals(2, result.data.size, "Both empty placeholder meetings must appear")
  }
}
