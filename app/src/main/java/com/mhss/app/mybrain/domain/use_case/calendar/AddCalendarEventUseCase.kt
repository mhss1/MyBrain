package com.mhss.app.mybrain.domain.use_case.calendar

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.mhss.app.mybrain.domain.model.CalendarEvent
import com.mhss.app.mybrain.domain.repository.CalendarRepository
import com.mhss.app.mybrain.presentation.glance_widgets.CalendarHomeWidget
import javax.inject.Inject

class AddCalendarEventUseCase @Inject constructor(
    private val calendarEventRepository: CalendarRepository,
    private val context: Context
) {
    suspend operator fun invoke(calendarEvent: CalendarEvent) {
        val calendars = calendarEventRepository.getCalendars()
        if (calendars.isNotEmpty()) {
            calendarEventRepository.addEvent(calendarEvent)
        } else {
            calendarEventRepository.createCalendar()
            val calendar = calendarEventRepository.getCalendars().first()
            calendarEventRepository.addEvent(calendarEvent.copy(calendarId = calendar.id))
        }
        CalendarHomeWidget().updateAll(context)
    }

}