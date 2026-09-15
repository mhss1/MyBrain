package com.mhss.app.domain.use_case

import androidx.paging.InvalidatingPagingSourceFactory
import androidx.paging.Pager
import androidx.paging.PagingData
import com.mhss.app.datetime.DateTimeFormatter
import com.mhss.app.datetime.currentLocalDate
import com.mhss.app.domain.EVENTS_LIST_MAX_DAYS
import com.mhss.app.domain.EventsListPagingConfig
import com.mhss.app.domain.repository.CalendarRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class GetPagedCalendarEventsUseCase(
    private val calendarRepository: CalendarRepository,
    private val dateTimeFormatter: DateTimeFormatter,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher
) {
    operator fun invoke(
        excludedCalendars: List<Int>,
        initialMonthIndex: Int = 0
    ): PagedCalendarEventsResult {
        val today = currentLocalDate()
        val months = eventsListMonths(today, dateTimeFormatter)
        val selectedMonth = initialMonthIndex.coerceIn(months.indices)

        val initialKey = maxOf(
            today,
            LocalDate(today.year, today.month, 1).plus(selectedMonth, DateTimeUnit.MONTH)
        )
        val events = channelFlow {
            val sourceFactory = InvalidatingPagingSourceFactory {
                CalendarEventsListPagingSource(
                    calendarRepository = calendarRepository,
                    dateTimeFormatter = dateTimeFormatter,
                    defaultDispatcher = defaultDispatcher,
                    excludedCalendars = excludedCalendars,
                    today = today
                )
            }
            launch {
                calendarRepository.observeChanges().collect { sourceFactory.invalidate() }
            }
            Pager(
                config = EventsListPagingConfig,
                initialKey = initialKey,
                pagingSourceFactory = sourceFactory
            ).flow.collect { send(it) }
        }
        return PagedCalendarEventsResult(events, months)
    }
}

data class PagedCalendarEventsResult(
    val events: Flow<PagingData<CalendarEventsDay>>,
    val months: List<CalendarListMonth>
)

data class CalendarListMonth(val index: Int, val name: String)

private fun eventsListMonths(
    today: LocalDate,
    dateTimeFormatter: DateTimeFormatter
): List<CalendarListMonth> = buildList {
    val endExclusive = today.plus(EVENTS_LIST_MAX_DAYS, DateTimeUnit.DAY)
    var month = LocalDate(today.year, today.month, 1)
    while (month < endExclusive) {
        add(CalendarListMonth(size, dateTimeFormatter.monthName(month)))
        month = month.plus(1, DateTimeUnit.MONTH)
    }
}
