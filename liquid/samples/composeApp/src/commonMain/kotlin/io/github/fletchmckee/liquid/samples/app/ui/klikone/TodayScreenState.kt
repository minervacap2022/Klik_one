// Copyright 2025, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.klikone

import io.github.fletchmckee.liquid.samples.app.domain.entity.Meeting

/**
 * Which card the Today screen's "RIGHT NOW" slot should render.
 */
sealed class RightNowCardState {
  object Recording : RightNowCardState()
  data class Live(val meeting: Meeting) : RightNowCardState()
  object Quiet : RightNowCardState()
}

/**
 * Decide which RIGHT NOW card to show.
 *
 * A Klik fixed-session recording always wins — the user sees the Stop pill.
 * Otherwise, show LiveSessionCard only for a real scheduled meeting on today's
 * date, identified by having at least one participant. This filters out the
 * zero-participant stub the backend creates for a just-completed Klik session,
 * which would otherwise hijack the slot and hide the record button.
 */
fun rightNowCardState(
  isRecording: Boolean,
  dayMeetings: List<Meeting>,
): RightNowCardState {
  if (isRecording) return RightNowCardState.Recording
  val live = dayMeetings.firstOrNull { !it.isPast && it.participants.isNotEmpty() }
  return if (live != null) RightNowCardState.Live(live) else RightNowCardState.Quiet
}
