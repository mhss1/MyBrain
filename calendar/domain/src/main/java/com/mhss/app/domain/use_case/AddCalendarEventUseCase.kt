package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.repository.CalendarRepository
import com.mhss.app.widget.WidgetUpdater
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