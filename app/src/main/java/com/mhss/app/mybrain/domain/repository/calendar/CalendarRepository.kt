package com.mhss.app.mybrain.domain.repository.calendar

import com.mhss.app.mybrain.domain.model.calendar.Calendar
import com.mhss.app.mybrain.domain.model.calendar.CalendarEvent

interface CalendarRepository {

    suspend fun getEvents(): List<CalendarEvent>

    suspend fun getCalendars(): List<Calendar>

    suspend fun addEvent(event: CalendarEvent)

    suspend fun deleteEvent(event: CalendarEvent)

    suspend fun updateEvent(event: CalendarEvent)

    suspend fun createCalendar()
}