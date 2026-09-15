package com.mhss.app.domain.use_case

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingDataEvent
import androidx.paging.PagingState
import androidx.paging.PagingSource
import com.mhss.app.datetime.DateTimeFormatter
import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.repository.CalendarRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarEventsListPagingSourceTest {
    @Test
    fun `calendar changes refresh the pager and cancellation stops observation`() = runTest {
        val repository = FakePagingCalendarRepository(emptyList())
        val useCase = GetPagedCalendarEventsUseCase(
            repository,
            PagingDateTimeFormatter,
            StandardTestDispatcher(testScheduler)
        )
        var generations = 0
        val job = launch {
            useCase(emptyList()).events.collect { generations++ }
        }
        runCurrent()
        assertEquals(1, generations)
        assertEquals(1, repository.changes.subscriptionCount.value)

        repository.changes.emit(Unit)
        runCurrent()
        assertEquals(2, generations)

        job.cancelAndJoin()
        assertEquals(0, repository.changes.subscriptionCount.value)
    }

    @Test
    fun `configured day windows clip a spanning event without duplicate days`() = runTest {
        val today = LocalDate(2026, 1, 30)
        val event = event(today, today.plus(3, DateTimeUnit.DAY))
        val source = source(
            today,
            FakePagingCalendarRepository(listOf(event)),
            StandardTestDispatcher(testScheduler)
        )

        val january = source.load(refresh(today, 2))
        val february = source.load(append(today.plus(2, DateTimeUnit.DAY), 2))

        val januaryPage = assertIs<PagingSource.LoadResult.Page<LocalDate, CalendarEventsDay>>(january)
        val februaryPage = assertIs<PagingSource.LoadResult.Page<LocalDate, CalendarEventsDay>>(february)
        assertEquals(listOf("2026-01-30", "2026-01-31"), januaryPage.data.map { it.formattedDate })
        assertEquals(listOf("2026-02-01", "2026-02-02"), februaryPage.data.map { it.formattedDate })
        assertEquals(4, (januaryPage.data + februaryPage.data).map { it.formattedDate }.distinct().size)
    }

    @Test
    fun `paging advances through empty ranges and stops after 150 days`() = runTest {
        val today = LocalDate(2026, 1, 1)
        val eventDate = today.plus(95, DateTimeUnit.DAY)
        val repository = FakePagingCalendarRepository(listOf(
            event(eventDate, eventDate),
            event(today.plus(365, DateTimeUnit.DAY), today.plus(365, DateTimeUnit.DAY))
        ))
        val dispatcher = StandardTestDispatcher(testScheduler)
        val presenter = object : PagingDataPresenter<CalendarEventsDay>(dispatcher) {
            override suspend fun presentPagingDataEvent(event: PagingDataEvent<CalendarEventsDay>) {}
        }
        val job = launch {
            Pager(PagingConfig(pageSize = 30, initialLoadSize = 60, prefetchDistance = 1)) {
                source(today, repository, dispatcher)
            }.flow.collectLatest { presenter.collectFrom(it) }
        }
        runCurrent()
        assertEquals(listOf(eventDate.toString()), presenter.snapshot().items.map { it.formattedDate })
        assertEquals(listOf(7, 8), repository.excludedCalendars.distinct().single())
        presenter[0]
        runCurrent()
        assertEquals(today.plus(150, DateTimeUnit.DAY).atTime(0, 0)
            .toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(), repository.ranges.last().second)
        job.cancelAndJoin()
    }

    @Test
    fun `load size controls ranges and prepend meets the refresh boundary`() = runTest {
        val today = LocalDate(2026, 1, 1)
        val repository = FakePagingCalendarRepository(emptyList())
        val source = source(today, repository, StandardTestDispatcher(testScheduler))
        val start = today.plus(40, DateTimeUnit.DAY)
        val page = assertIs<PagingSource.LoadResult.Page<LocalDate, CalendarEventsDay>>(
            source.load(refresh(start, 12))
        )
        assertEquals(start, page.prevKey)
        assertEquals(start.plus(12, DateTimeUnit.DAY), page.nextKey)
        assertEquals(start, source.getRefreshKey(PagingState(
            pages = listOf(PagingSource.LoadResult.Page(
                data = listOf(CalendarEventsDay(formattedDate = "", monthName = "", events = emptyList())),
                prevKey = page.prevKey,
                nextKey = page.nextKey
            )),
            anchorPosition = 0,
            config = PagingConfig(12),
            leadingPlaceholderCount = 0
        )))
        val previous = assertIs<PagingSource.LoadResult.Page<LocalDate, CalendarEventsDay>>(
            source.load(PagingSource.LoadParams.Prepend(start, 50, false))
        )
        assertNull(previous.prevKey)
        assertEquals(start, previous.nextKey)
        assertEquals(2, repository.rangeRequestCount)
    }

    @Test
    fun `last range stops at the limit and excludes events beyond it`() = runTest {
        val today = LocalDate(2026, 1, 1)
        val lastDate = today.plus(149, DateTimeUnit.DAY)
        val repository = FakePagingCalendarRepository(listOf(
            event(lastDate, lastDate.plus(5, DateTimeUnit.DAY))
        ))
        val page = assertIs<PagingSource.LoadResult.Page<LocalDate, CalendarEventsDay>>(
            source(today, repository, StandardTestDispatcher(testScheduler))
                .load(append(today.plus(140, DateTimeUnit.DAY), 30))
        )
        assertEquals(listOf(lastDate.toString()), page.data.map { it.formattedDate })
        assertNull(page.nextKey)
    }

    @Test
    fun `load propagates repository cancellation`() = runTest {
        val today = LocalDate(2026, 1, 30)
        val source = source(
            today,
            FakePagingCalendarRepository(emptyList(), CancellationException("cancelled")),
            StandardTestDispatcher(testScheduler)
        )

        assertFailsWith<CancellationException> { source.load(refresh(today)) }
    }

    private fun source(
        today: LocalDate,
        repository: CalendarRepository,
        dispatcher: CoroutineDispatcher
    ) = CalendarEventsListPagingSource(
        calendarRepository = repository,
        dateTimeFormatter = PagingDateTimeFormatter,
        defaultDispatcher = dispatcher,
        excludedCalendars = listOf(7, 8),
        today = today
    )

    private fun refresh(key: LocalDate, size: Int = 60) = PagingSource.LoadParams.Refresh(
        key = key,
        loadSize = size,
        placeholdersEnabled = true
    )

    private fun append(key: LocalDate, size: Int = 30) = PagingSource.LoadParams.Append(
        key = key,
        loadSize = size,
        placeholdersEnabled = true
    )

    private fun event(startDate: LocalDate, endDate: LocalDate): CalendarEvent {
        val timeZone = TimeZone.currentSystemDefault()
        return CalendarEvent(
            id = 1,
            title = "Event",
            start = startDate.atTime(12, 0).toInstant(timeZone).toEpochMilliseconds(),
            end = endDate.plus(1, DateTimeUnit.DAY)
                .atTime(0, 0)
                .toInstant(timeZone)
                .toEpochMilliseconds(),
            calendarId = 1
        )
    }
}

private class FakePagingCalendarRepository(
    private val events: List<CalendarEvent>,
    private val failure: Exception? = null
) : CalendarRepository {
    val changes = MutableSharedFlow<Unit>()
    override fun observeChanges() = changes
    val excludedCalendars = mutableListOf<List<Int>>()
    var rangeRequestCount = 0
    val ranges = mutableListOf<Pair<Long, Long>>()

    override suspend fun getEvents(
        start: Long,
        end: Long,
        excludedCalendars: List<Int>
    ): List<CalendarEvent> {
        failure?.let { throw it }
        rangeRequestCount++
        ranges.add(start to end)
        this.excludedCalendars.add(excludedCalendars)
        return events.filter { it.start < end && it.end > start }
    }

    override suspend fun getEvents(
        excludedCalendars: List<Int>,
        until: Long?,
        limit: Int?
    ): List<CalendarEvent> = unsupported()

    override suspend fun searchEventsByTitleWithinRange(
        start: Long,
        end: Long,
        titleQuery: String,
        excludedCalendars: List<Int>
    ): List<CalendarEvent> = unsupported()

    override suspend fun getCalendars(): List<Calendar> = unsupported()
    override suspend fun getEventById(id: Long): CalendarEvent? = unsupported()
    override suspend fun addEvent(event: CalendarEvent): Long? = unsupported()
    override suspend fun deleteEvent(event: CalendarEvent) = unsupported<Unit>()
    override suspend fun updateEvent(event: CalendarEvent) = unsupported<Unit>()
    override suspend fun createCalendar() = unsupported<Unit>()

    private fun <T> unsupported(): T = error("Unused in test")
}

private object PagingDateTimeFormatter : DateTimeFormatter {
    override fun formatDateForMapping(date: LocalDate): String = date.toString()
    override fun monthName(date: LocalDate): String = date.month.toString()
    override fun formatEventsDayName(date: LocalDate): String = unsupported()
    override fun formatDateDependingOnDay(timestamp: Long): String = unsupported()
    override fun fullDate(timestamp: Long): String = unsupported()
    override fun formatDateForMapping(timestamp: Long): String = unsupported()
    override fun formatTime(timestamp: Long): String = unsupported()
    override fun formatDate(timestamp: Long, forceShowYear: Boolean): String = unsupported()
    override fun monthName(timestamp: Long): String = unsupported()
    override fun getDisplayName(dayOfWeek: DayOfWeek): String = unsupported()
    override val is24HourFormat: Boolean get() = unsupported()

    override fun formatEventStartEnd(
        start: Long,
        end: Long,
        allDayString: String,
        eventTimeAt: String,
        eventTime: String,
        location: String?,
        allDay: Boolean
    ): String = unsupported()

    private fun <T> unsupported(): T = error("Unused in test")
}
