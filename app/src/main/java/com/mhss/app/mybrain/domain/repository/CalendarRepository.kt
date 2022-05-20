package com.mhss.app.mybrain.domain.repository

import com.mhss.app.mybrain.domain.model.Calendar
import com.mhss.app.mybrain.domain.model.CalendarEvent

interface CalendarRepository {

    suspend fun getEvents(): List<CalendarEvent>

    suspend fun getCalendars(): List<Calendar>
}