package com.mhss.app.domain.use_case

import com.mhss.app.datetime.DateTimeFormatter
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.model.effectiveDateRange
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

internal fun groupEventsByDay(
    events: List<CalendarEvent>,
    start: LocalDate,
    endExclusive: LocalDate,
    timeZone: TimeZone,
    formatter: DateTimeFormatter
): List<CalendarEventsDay> {
    val datedEvents = events.map { it to it.effectiveDateRange(timeZone) }

    return buildList {
        for (offset in 0 until start.daysUntil(endExclusive)) {
            val date = start.plus(offset, DateTimeUnit.DAY)
            val dayStartMillis = date.atStartOfDayIn(timeZone).toEpochMilliseconds()

            val dayEvents = datedEvents.mapNotNull { (event, dates) ->
                event.takeIf { date >= dates.first && date <= dates.second }
            }.sortedBy { maxOf(it.start, dayStartMillis) }

            if (dayEvents.isNotEmpty()) {
                add(
                    CalendarEventsDay(
                        monthName = formatter.monthName(date),
                        formattedDate = formatter.formatDateForMapping(date),
                        events = dayEvents
                    )
                )
            }
        }
    }
}
