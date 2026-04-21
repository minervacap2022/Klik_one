// Copyright 2025, Klik
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
 * Mirrors the selection logic TodayScreen uses inline so it can be covered
 * by unit tests.
 */
fun rightNowCardState(
    isRecording: Boolean,
    dayMeetings: List<Meeting>,
): RightNowCardState {
    if (isRecording) return RightNowCardState.Recording
    val live = dayMeetings.firstOrNull { !it.isPast }
    return if (live != null) RightNowCardState.Live(live) else RightNowCardState.Quiet
}
