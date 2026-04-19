package io.github.fletchmckee.liquid.samples.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import io.github.fletchmckee.liquid.samples.app.theme.EngageColor
import io.github.fletchmckee.liquid.samples.app.theme.KlikPrimary
import io.github.fletchmckee.liquid.samples.app.theme.StriveColor
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Month/Year Header
        CalendarHeader(
            month = currentMonth,
            year = currentYear,
            onPreviousMonth = {
                val newMonth = if (currentMonth == 1) 12 else currentMonth - 1
                val newYear = if (currentMonth == 1) currentYear - 1 else currentYear
                onMonthChange(newMonth, newYear)
            },
            onNextMonth = {
                val newMonth = if (currentMonth == 12) 1 else currentMonth + 1
                val newYear = if (currentMonth == 12) currentYear + 1 else currentYear
                onMonthChange(newMonth, newYear)
            }
        )

        Spacer(Modifier.height(8.dp))

        // Weekday labels
        WeekdayRow()

        Spacer(Modifier.height(4.dp))

        // Date grid
        DateGrid(
            year = currentYear,
            month = currentMonth,
            selectedDate = selectedDate,
            todayDate = todayDate,
            meetingsCountByDay = meetingsCountByDay,
            onDateSelected = onDateSelected
        )
    }
}

@Composable
private fun CalendarHeader(
    month: Int,
    year: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val monthShortNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    val prevMonthIndex = if (month == 1) 11 else month - 2
    val nextMonthIndex = if (month == 12) 0 else month

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start, // Left aligned
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Month (Short)
        Text(
            text = monthShortNames[prevMonthIndex],
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onPreviousMonth() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        // Current Month Year
        Text(
            text = "${monthNames[month - 1]} $year",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        // Next Month (Short)
        Text(
            text = monthShortNames[nextMonthIndex],
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onNextMonth() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun WeekdayRow() {
    val days = listOf("S", "M", "T", "W", "T", "F", "S")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { day ->
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DateGrid(
    year: Int,
    month: Int,
    selectedDate: LocalDate,
    todayDate: LocalDate,
    meetingsCountByDay: Map<Int, Int>,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = getDaysInMonth(year, month)
    val firstDayOfWeek = getFirstDayOfWeek(year, month)
    val prevMonthDays = getDaysInPrevMonth(year, month)

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (row in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val (day, isCurrentMonth) = when {
                        cellIndex < firstDayOfWeek -> {
                            // Previous month days
                            Pair(prevMonthDays - firstDayOfWeek + cellIndex + 1, false)
                        }
                        cellIndex - firstDayOfWeek >= daysInMonth -> {
                            // Next month days
                            Pair(cellIndex - firstDayOfWeek - daysInMonth + 1, false)
                        }
                        else -> {
                            // Current month days
                            Pair(cellIndex - firstDayOfWeek + 1, true)
                        }
                    }

                    val cellDate = if (isCurrentMonth) {
                        LocalDate(year, month, day)
                    } else null

                    val isToday = cellDate == todayDate
                    val isSelected = cellDate == selectedDate

                    DateCell(
                        day = day,
                        isToday = isToday,
                        isSelected = isSelected,
                        isCurrentMonth = isCurrentMonth,
                        meetingCount = if (isCurrentMonth) meetingsCountByDay[day] ?: 0 else 0,
                        onClick = {
                            cellDate?.let { onDateSelected(it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    meetingCount: Int,
    onClick: () -> Unit
) {
    val cellShape = RoundedCornerShape(8.dp)

    val backgroundColor = when {
        isToday -> KlikPrimary
        else -> Color.Transparent
    }

    val textColor = when {
        isToday -> Color.White
        isSelected -> KlikPrimary
        isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }

    Box(
        modifier = Modifier.size(36.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Date box - clickable area only covers this, not the dots
        // Fixed position at top with consistent spacing
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(28.dp)
                .clip(cellShape)
                .clickable(enabled = isCurrentMonth) { onClick() }
                .background(backgroundColor, cellShape)
                .then(
                    if (isSelected && !isToday) {
                        Modifier.border(2.dp, KlikPrimary, cellShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }

        // Meeting dots - fixed position at bottom
        if (meetingCount > 0 && isCurrentMonth) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 1.dp)
            ) {
                MeetingDots(count = meetingCount.coerceAtMost(3))
            }
        }
    }
}

@Composable
private fun MeetingDots(count: Int) {
    val dotColor = when (count) {
        1 -> KlikPrimary
        2 -> EngageColor
        else -> StriveColor
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(dotColor, CircleShape)
            )
        }
    }
}
