package com.mhss.app.domain.repository

import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.model.CalendarEvent

interface CalendarRepository {

    suspend fun getEvents(
        excludedCalendars: List<Int> = emptyList(),
        until: Long? = null,
        limit: Int? = null
    ): List<CalendarEvent>

    suspend fun getEvents(start: Long, end: Long, excludedCalendars: List<Int> = emptyList()): List<CalendarEvent>

    suspend fun searchEventsByTitleWithinRange(
        start: Long,
        end: Long,
        titleQuery: String,
        excludedCalendars: List<Int> = emptyList()
    ): List<CalendarEvent>

    suspend fun getCalendars(): List<Calendar>

    suspend fun getEventById(id: Long): CalendarEvent?

    suspend fun addEvent(event: CalendarEvent): Long?

    suspend fun deleteEvent(event: CalendarEvent)

    suspend fun updateEvent(event: CalendarEvent)

    suspend fun createCalendar()
}
