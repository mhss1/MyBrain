package com.mhss.app.mybrain.domain.use_case.calendar

import com.mhss.app.mybrain.domain.model.calendar.CalendarEvent
import com.mhss.app.mybrain.domain.repository.calendar.CalendarRepository
import com.mhss.app.mybrain.domain.repository.widget.WidgetUpdater
import org.koin.core.annotation.Single

@Single
class UpdateCalendarEventUseCase(
    private val calendarRepository: CalendarRepository,
    private val widgetUpdater: WidgetUpdater
) {
    suspend operator fun invoke(event: CalendarEvent) {
        calendarRepository.updateEvent(event)
        widgetUpdater.updateAll(WidgetUpdater.WidgetType.Calendar)
    }
}