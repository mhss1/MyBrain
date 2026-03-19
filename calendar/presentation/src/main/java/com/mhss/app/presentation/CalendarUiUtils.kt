package com.mhss.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.res.stringResource
import com.mhss.app.domain.model.CalendarEventFrequency
import com.mhss.app.ui.R
import com.mhss.app.util.date.getDisplayName
import kotlinx.datetime.DayOfWeek

@Composable
fun CalendarEventFrequency.getCalendarFrequencyTitle(
    interval: Int = 1,
    weekDays: Set<DayOfWeek> = emptySet()
): String {
    val safeInterval = interval.coerceAtLeast(1)
    return when (this) {
        CalendarEventFrequency.DAILY -> {
            if (safeInterval == 1) {
                stringResource(R.string.every_day)
            } else {
                stringResource(
                    R.string.repeat_every_interval,
                    safeInterval,
                    getIntervalUnitTitle(safeInterval)
                )
            }
        }
        CalendarEventFrequency.WEEKLY -> {
            val dayLabel = weekDays
                .sortedBy { it.toRecurringSortOrder() }
                .joinToString(", ") { it.getDisplayName() }
            if (dayLabel.isBlank() && safeInterval == 1) {
                stringResource(R.string.every_week)
            } else if (dayLabel.isBlank()) {
                stringResource(
                    R.string.repeat_every_interval,
                    safeInterval,
                    getIntervalUnitTitle(safeInterval)
                )
            } else if (safeInterval == 1) {
                stringResource(R.string.every_week_on, dayLabel)
            } else {
                stringResource(
                    R.string.repeat_every_interval_on,
                    safeInterval,
                    getIntervalUnitTitle(safeInterval),
                    dayLabel
                )
            }
        }
        CalendarEventFrequency.MONTHLY -> {
            if (safeInterval == 1) {
                stringResource(R.string.every_month)
            } else {
                stringResource(
                    R.string.repeat_every_interval,
                    safeInterval,
                    getIntervalUnitTitle(safeInterval)
                )
            }
        }
        CalendarEventFrequency.YEARLY -> {
            if (safeInterval == 1) {
                stringResource(R.string.every_year)
            } else {
                stringResource(
                    R.string.repeat_every_interval,
                    safeInterval,
                    getIntervalUnitTitle(safeInterval)
                )
            }
        }
        CalendarEventFrequency.NEVER -> stringResource(R.string.do_not_repeat)
    }
}

@Composable
fun CalendarEventFrequency.getIntervalUnitTitle(interval: Int): String {
    val isSingular = interval.coerceAtLeast(1) == 1
    return when (this) {
        CalendarEventFrequency.DAILY -> stringResource(if (isSingular) R.string.day else R.string.days)
        CalendarEventFrequency.WEEKLY -> stringResource(if (isSingular) R.string.week else R.string.weeks)
        CalendarEventFrequency.MONTHLY -> stringResource(if (isSingular) R.string.month else R.string.months)
        CalendarEventFrequency.YEARLY -> stringResource(if (isSingular) R.string.year else R.string.years)
        CalendarEventFrequency.NEVER -> ""
    }
}

fun DayOfWeek.toRecurringSortOrder(): Int {
    return when (this) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }
}

val dayOfWeekSetSaver = listSaver(
    save = { selectedDays -> selectedDays.map(DayOfWeek::name) },
    restore = { savedDays ->
        savedDays.mapNotNull { dayName ->
            runCatching { DayOfWeek.valueOf(dayName) }.getOrNull()
        }.toSet()
    }
)
