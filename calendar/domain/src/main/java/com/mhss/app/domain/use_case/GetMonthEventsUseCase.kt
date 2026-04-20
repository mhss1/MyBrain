package com.mhss.app.domain.use_case

import com.mhss.app.domain.MONTH_GRID_CELL_COUNT
import com.mhss.app.domain.model.CalendarDay
import com.mhss.app.domain.model.CalendarEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atTime
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Factory
class GetMonthEventsUseCase(
    private val getEventsWithinRangeUseCase: GetEventsWithinRangeUseCase,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(
        month: YearMonth,
        excludedCalendars: List<Int>,
        firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY
    ): List<CalendarDay> {
        return withContext(defaultDispatcher) {
            val tz = TimeZone.currentSystemDefault()
            val firstOfMonth = month.firstDay
            val startOffset = firstOfMonth.dayOfWeek.dayNumberFrom(firstDayOfWeek)
            val startDate = firstOfMonth.minus(startOffset, DateTimeUnit.DAY)

            val gridDays = MONTH_GRID_CELL_COUNT
            val endDate = startDate.plus(gridDays.toLong(), DateTimeUnit.DAY)

            val startMillis = startDate.atTime(hour = 0, minute = 0).toInstant(tz).toEpochMilliseconds()
            val endMillis = endDate.atTime(hour = 23, minute = 59).toInstant(tz).toEpochMilliseconds()

            val events = getEventsWithinRangeUseCase(startMillis, endMillis, excludedCalendars)

            val eventsByDayIndex = buildMap<Int, MutableList<CalendarEvent>> {
                events.forEach { event ->
                    val zone = if (event.allDay) TimeZone.UTC else tz
                    val startDateTime = Instant.fromEpochMilliseconds(event.start).toLocalDateTime(zone)
                    val endDateTime = Instant.fromEpochMilliseconds(event.end).toLocalDateTime(zone)
                    val eventStartDate = startDateTime.date
                    val rawEndDate = endDateTime.date
                    val eventEndDate = if (
                        rawEndDate > eventStartDate &&
                        endDateTime.time == LocalTime(0, 0)
                    ) {
                        rawEndDate.minus(1, DateTimeUnit.DAY)
                    } else rawEndDate

                    val firstIndex = startDate.daysUntil(eventStartDate).coerceAtLeast(0)
                    val lastIndex = startDate.daysUntil(eventEndDate).coerceAtMost(gridDays - 1)
                    if (firstIndex > lastIndex) return@forEach
                    for (dayIndex in firstIndex..lastIndex) {
                        getOrPut(dayIndex) { mutableListOf() }.add(event)
                    }
                }
            }

            (0 until gridDays).map { index ->
                val dayDate = startDate.plus(index, DateTimeUnit.DAY)
                CalendarDay(
                    date = dayDate,
                    isCurrentMonth = dayDate.month == month.month && dayDate.year == month.year,
                    events = eventsByDayIndex[index].orEmpty()
                )
            }
        }
    }
}

fun DayOfWeek.dayNumberFrom(firstDayOfWeek: DayOfWeek): Int {
    val thisDayOrdinal = this.ordinal
    val firstDayOrdinal = firstDayOfWeek.ordinal
    return (thisDayOrdinal - firstDayOrdinal + 7) % 7
}

