package com.mhss.app.domain.use_case

import com.mhss.app.datetime.DateTimeFormatter
import com.mhss.app.datetime.currentLocalDate
import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.repository.CalendarRepository
import com.mhss.app.widget.WIDGET_ITEM_LIMIT
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetWidgetEventsUseCaseTest {
    @Test
    fun `widget limits fetched events without truncating multi-day rows`() = runTest {
        val repository = FakeCalendarRepository(listOf(eventSpanningDays(20)))
        val useCase = widgetUseCase(repository, StandardTestDispatcher(testScheduler))

        val result = useCase(listOf(4, 9))

        assertEquals(20, result.eventDays.sumOf { it.events.size })
        assertEquals(20, result.eventDays.size)
        assertEquals(WIDGET_ITEM_LIMIT, repository.requestedLimit)
        assertEquals(listOf(4, 9), repository.requestedExcludedCalendars)
    }

    @Test
    fun `widget returns empty result when repository has no events`() = runTest {
        val result = widgetUseCase(
            FakeCalendarRepository(emptyList()),
            StandardTestDispatcher(testScheduler)
        ).invoke(emptyList())

        assertTrue(result.eventDays.isEmpty())
        assertTrue(result.months.isEmpty())
    }

    @Test
    fun `normal event loading remains uncapped`() = runTest {
        val repository = FakeCalendarRepository(listOf(eventSpanningDays(20)))
        val useCase = GetAllEventsUseCase(
            repository,
            FakeDateTimeFormatter,
            StandardTestDispatcher(testScheduler)
        )

        val result = useCase(excluded = emptyList())

        assertEquals(20, result.eventDays.sumOf { it.events.size })
        assertNull(repository.requestedLimit)
    }

    @Test
    fun `widget propagates cancellation`() = runTest {
        val repository = FakeCalendarRepository(emptyList(), CancellationException("cancelled"))

        assertFailsWith<CancellationException> {
            widgetUseCase(repository, StandardTestDispatcher(testScheduler)).invoke(emptyList())
        }
    }

    private fun widgetUseCase(
        repository: CalendarRepository,
        dispatcher: CoroutineDispatcher
    ): GetWidgetEventsUseCase {
        return GetWidgetEventsUseCase(
            GetAllEventsUseCase(repository, FakeDateTimeFormatter, dispatcher)
        )
    }

    private fun eventSpanningDays(days: Int): CalendarEvent {
        val today = currentLocalDate()
        return CalendarEvent(
            id = 1,
            title = "Long event",
            start = today.atTime(0, 0).toInstant(TimeZone.UTC).toEpochMilliseconds(),
            end = today.plus(days, DateTimeUnit.DAY)
                .atTime(0, 0)
                .toInstant(TimeZone.UTC)
                .toEpochMilliseconds(),
            allDay = true,
            calendarId = 2
        )
    }
}

private class FakeCalendarRepository(
    private val events: List<CalendarEvent>,
    private val failure: Exception? = null
) : CalendarRepository {
    var requestedLimit: Int? = null
    var requestedExcludedCalendars: List<Int> = emptyList()

    override suspend fun getEvents(
        excludedCalendars: List<Int>,
        until: Long?,
        limit: Int?
    ): List<CalendarEvent> {
        failure?.let { throw it }
        requestedLimit = limit
        requestedExcludedCalendars = excludedCalendars
        return events
    }

    override suspend fun getEvents(
        start: Long,
        end: Long,
        excludedCalendars: List<Int>
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

private object FakeDateTimeFormatter : DateTimeFormatter {
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
