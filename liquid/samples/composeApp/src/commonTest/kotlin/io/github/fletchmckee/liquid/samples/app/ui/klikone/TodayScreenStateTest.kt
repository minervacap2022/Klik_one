// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class TodayScreenStateTest {

  private fun meeting(
    id: String = "m1",
    title: String = "Meeting on Apr 21, 2026",
    time: String = "5:21 PM",
    participants: List<io.github.fletchmckee.liquid.samples.app.domain.entity.Person> = emptyList(),
  ) = Meeting(
    id = id,
    title = title,
    date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    time = time,
    participants = participants,
    summary = "",
    actionItems = emptyList(),
  )

  @Test
  fun recording_flag_always_wins() {
    val state = rightNowCardState(
      isRecording = true,
      dayMeetings = listOf(meeting()),
    )
    assertEquals(RightNowCardState.Recording, state)
  }

  @Test
  fun no_meetings_falls_through_to_quiet() {
    val state = rightNowCardState(isRecording = false, dayMeetings = emptyList())
    assertEquals(RightNowCardState.Quiet, state)
  }

  // --- Bug repro: user records briefly, stops. Backend creates a zero-duration
  //     "Meeting on <date>" stub with 0 participants on today's date. Before
  //     the fix, this stub was treated as a live meeting and hijacked the
  //     RIGHT NOW slot with LiveSessionCard, hiding the record button so the
  //     user could not start a new recording.
  @Test
  fun stub_meeting_from_stopped_short_recording_does_not_hide_record_button() {
    val stub = meeting(
      title = "Meeting on Apr 21, 2026",
      time = "5:21 PM",
      participants = emptyList(),
    )
    val state = rightNowCardState(
      isRecording = false,
      dayMeetings = listOf(stub),
    )
    // Expected: QuietCard is shown so the user can tap to start a new recording.
    assertEquals(
      RightNowCardState.Quiet,
      state,
      "A zero-participant session stub must not occupy RIGHT NOW — the user must be able to start a new recording.",
    )
  }

  @Test
  fun real_scheduled_meeting_today_still_shows_live_card() {
    val realMeeting = meeting(
      title = "1:1 with Alex",
      time = "5:00 PM",
      participants = listOf(
        io.github.fletchmckee.liquid.samples.app.domain.entity.Person(
          id = "p1",
          name = "Alex",
          role = "Engineer",
        ),
      ),
    )
    val state = rightNowCardState(
      isRecording = false,
      dayMeetings = listOf(realMeeting),
    )
    assertTrue(state is RightNowCardState.Live, "Expected Live, got $state")
    assertEquals("1:1 with Alex", state.meeting.title)
  }
}
