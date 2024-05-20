package com.mhss.app.mybrain.domain.use_case.calendar

import com.mhss.app.mybrain.domain.model.CalendarEvent
import com.mhss.app.mybrain.domain.repository.CalendarRepository
import com.mhss.app.mybrain.domain.repository.WidgetUpdater
import org.koin.core.annotation.Single

@Single
class AddCalendarEventUseCase(
    private val calendarEventRepository: CalendarRepository,
    private val widgetUpdater: WidgetUpdater
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
        widgetUpdater.updateAll(WidgetUpdater.WidgetType.Calendar)
    }

}