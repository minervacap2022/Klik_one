// Copyright 2026, Colin McKee
// SPDX-License-Identifier: Apache-2.0
package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.fletchmckee.liquid.samples.app.model.getDaysInMonth
import io.github.fletchmckee.liquid.samples.app.model.getDaysInPrevMonth
import io.github.fletchmckee.liquid.samples.app.model.getFirstDayOfWeek
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkMuted
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkPrimary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkSecondary
import io.github.fletchmckee.liquid.samples.app.theme.KlikInkTertiary
import io.github.fletchmckee.liquid.samples.app.theme.KlikPaperCard
import io.github.fletchmckee.liquid.samples.app.ui.klikone.K1Type
import io.github.fletchmckee.liquid.samples.app.ui.klikone.k1Clickable
import kotlinx.datetime.LocalDate

@Composable
fun MiniCalendar(
  currentMonth: Int,
  currentYear: Int,
  selectedDate: LocalDate,
  todayDate: LocalDate,
  meetingsCountByDay: Map<Int, Int>,
  onDateSelected: (LocalDate) -> Unit,
  onMonthChange: (Int, Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  // Clamp month/year into valid ranges so upstream state glitches can't crash date construction.
  val safeMonth = currentMonth.coerceIn(1, 12)
  val safeYear = currentYear.coerceIn(1970, 9999)

  Column(
    modifier = modifier.fillMaxWidth(),
  ) {
    K1CalendarHeader(
      month = safeMonth,
      year = safeYear,
      onPreviousMonth = {
        val newMonth = if (safeMonth == 1) 12 else safeMonth - 1
        val newYear = if (safeMonth == 1) safeYear - 1 else safeYear
        onMonthChange(newMonth, newYear)
      },
      onNextMonth = {
        val newMonth = if (safeMonth == 12) 1 else safeMonth + 1
        val newYear = if (safeMonth == 12) safeYear + 1 else safeYear
        onMonthChange(newMonth, newYear)
      },
    )

    Spacer(Modifier.height(12.dp))

    K1WeekdayRow()

    Spacer(Modifier.height(6.dp))

    K1DateGrid(
      year = safeYear,
      month = safeMonth,
      selectedDate = selectedDate,
      todayDate = todayDate,
      meetingsCountByDay = meetingsCountByDay,
      onDateSelected = onDateSelected,
    )
  }
}

@Composable
private fun K1CalendarHeader(
  month: Int,
  year: Int,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
) {
  val monthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
  )
  val monthShort = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
  )

  val prevIdx = if (month == 1) 11 else month - 2
  val nextIdx = if (month == 12) 0 else month

  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = "${monthNames[month - 1]} $year",
      style = K1Type.h3.copy(color = KlikInkPrimary, fontWeight = FontWeight.Medium),
    )
    Spacer(Modifier.size(12.dp))
    Text(
      text = monthShort[prevIdx].uppercase(),
      style = K1Type.eyebrow.copy(color = KlikInkMuted),
      modifier = Modifier
        .k1Clickable(onClick = onPreviousMonth)
        .padding(horizontal = 8.dp, vertical = 6.dp),
    )
    Spacer(Modifier.size(2.dp))
    Text(
      text = monthShort[nextIdx].uppercase(),
      style = K1Type.eyebrow.copy(color = KlikInkMuted),
      modifier = Modifier
        .k1Clickable(onClick = onNextMonth)
        .padding(horizontal = 8.dp, vertical = 6.dp),
    )
  }
}

@Composable
private fun K1WeekdayRow() {
  val days = listOf("S", "M", "T", "W", "T", "F", "S")
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    days.forEach { d ->
      Box(
        modifier = Modifier.size(36.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = d,
          style = K1Type.metaSm.copy(color = KlikInkTertiary, fontWeight = FontWeight.Medium),
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}

@Composable
private fun K1DateGrid(
  year: Int,
  month: Int,
  selectedDate: LocalDate,
  todayDate: LocalDate,
  meetingsCountByDay: Map<Int, Int>,
  onDateSelected: (LocalDate) -> Unit,
) {
  val daysInMonth = getDaysInMonth(year, month)
  val firstDayOfWeek = getFirstDayOfWeek(year, month).coerceIn(0, 6)
  val prevMonthDays = getDaysInPrevMonth(year, month)

  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    for (row in 0 until 6) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        for (col in 0 until 7) {
          val cellIndex = row * 7 + col
          val (day, isCurrentMonth) = when {
            cellIndex < firstDayOfWeek ->
              (prevMonthDays - firstDayOfWeek + cellIndex + 1) to false

            cellIndex - firstDayOfWeek >= daysInMonth ->
              (cellIndex - firstDayOfWeek - daysInMonth + 1) to false

            else ->
              (cellIndex - firstDayOfWeek + 1) to true
          }

          // `getDaysInMonth` already accounts for leap years, so the
          // (year, month, day) tuple should always be valid here.
          // Catch IllegalArgumentException explicitly to guard
          // against month bounds drifting in `getDaysInMonth` —
          // we'd rather render an inactive cell than crash the grid.
          val cellDate: LocalDate? =
            if (isCurrentMonth && day in 1..daysInMonth) {
              try {
                LocalDate(year, month, day)
              } catch (e: IllegalArgumentException) {
                io.github.fletchmckee.liquid.samples.app.logging.KlikLogger.w(
                  "MiniCalendar",
                  "Skipping invalid cell date $year-$month-$day: ${e.message}",
                )
                null
              }
            } else {
              null
            }

          K1DateCell(
            day = day,
            isToday = cellDate != null && cellDate == todayDate,
            isSelected = cellDate != null && cellDate == selectedDate,
            isCurrentMonth = isCurrentMonth,
            meetingCount = if (isCurrentMonth) (meetingsCountByDay[day] ?: 0) else 0,
            onClick = { cellDate?.let { onDateSelected(it) } },
          )
        }
      }
    }
  }
}

@Composable
private fun K1DateCell(
  day: Int,
  isToday: Boolean,
  isSelected: Boolean,
  isCurrentMonth: Boolean,
  meetingCount: Int,
  onClick: () -> Unit,
) {
  val shape = RoundedCornerShape(10.dp)

  val textColor = when {
    isToday -> KlikPaperCard
    isSelected -> KlikInkPrimary
    isCurrentMonth -> KlikInkPrimary
    else -> KlikInkMuted
  }
  val bg = when {
    isToday -> KlikInkPrimary
    else -> Color.Transparent
  }

  // Stack the number box on top and the meeting dots underneath in a
  // Column so they never overlap. We always reserve the dot row's height
  // (4dp) so the row doesn't shift vertically between days that have
  // meetings and days that don't.
  Column(
    modifier = Modifier
      .size(width = 36.dp, height = 40.dp)
      .k1Clickable(enabled = isCurrentMonth, onClick = onClick),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier = Modifier
        .padding(top = 2.dp)
        .size(28.dp)
        .clip(shape)
        .background(bg, shape)
        .then(
          if (isSelected && !isToday) {
            Modifier.border(1.dp, KlikInkPrimary, shape)
          } else {
            Modifier
          },
        ),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = day.toString(),
        style = K1Type.bodySm.copy(
          color = textColor,
          fontWeight = if (isToday || isSelected) FontWeight.Medium else FontWeight.Normal,
        ),
        textAlign = TextAlign.Center,
      )
    }
    Spacer(Modifier.height(2.dp))
    Box(modifier = Modifier.height(4.dp), contentAlignment = Alignment.Center) {
      if (meetingCount > 0 && isCurrentMonth) {
        K1MeetingDots(count = meetingCount.coerceAtMost(3))
      }
    }
  }
}

@Composable
private fun K1MeetingDots(count: Int) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(2.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    repeat(count) {
      Box(
        modifier = Modifier
          .size(3.5.dp)
          .background(KlikInkSecondary, CircleShape),
      )
    }
  }
}
