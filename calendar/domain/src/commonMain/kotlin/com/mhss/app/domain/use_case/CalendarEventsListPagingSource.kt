package com.mhss.app.domain.use_case

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mhss.app.datetime.DateTimeFormatter
import com.mhss.app.domain.EVENTS_LIST_MAX_DAYS
import com.mhss.app.domain.repository.CalendarRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus

internal class CalendarEventsListPagingSource(
    private val calendarRepository: CalendarRepository,
    private val dateTimeFormatter: DateTimeFormatter,
    private val defaultDispatcher: CoroutineDispatcher,
    private val excludedCalendars: List<Int>,
    private val today: LocalDate
) : PagingSource<LocalDate, CalendarEventsDay>() {
    private val timeZone = TimeZone.currentSystemDefault()
    private val endExclusive = today.plus(EVENTS_LIST_MAX_DAYS, DateTimeUnit.DAY)

    override fun getRefreshKey(state: PagingState<LocalDate, CalendarEventsDay>): LocalDate? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor) ?: return null
        return page.prevKey ?: today
    }

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, CalendarEventsDay> {
        return try {
            val range = dateRangeFor(params)
            val days = withContext(defaultDispatcher) {
                val events = calendarRepository.getEvents(
                    range.start.atStartOfDayIn(timeZone).toEpochMilliseconds(),
                    range.endExclusive.atStartOfDayIn(timeZone).toEpochMilliseconds(),
                    excludedCalendars
                )

                groupEventsByDay(events, range.start, range.endExclusive, timeZone, dateTimeFormatter)
            }
            LoadResult.Page(
                data = days,
                prevKey = range.start.takeIf { it > today },
                nextKey = range.endExclusive.takeIf { it < endExclusive }
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    private fun dateRangeFor(params: LoadParams<LocalDate>): EventsListDateRange {
        val date = (params.key ?: today).coerceIn(today, endExclusive)
        return if (params is LoadParams.Prepend) {
            EventsListDateRange(
                start = maxOf(today, date.minus(params.loadSize, DateTimeUnit.DAY)),
                endExclusive = date
            )
        } else {
            EventsListDateRange(
                start = date,
                endExclusive = minOf(endExclusive, date.plus(params.loadSize, DateTimeUnit.DAY))
            )
        }
    }
}

private data class EventsListDateRange(val start: LocalDate, val endExclusive: LocalDate)
