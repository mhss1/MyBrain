package com.mhss.app.mybrain.domain.use_case.calendar

import com.mhss.app.mybrain.domain.model.CalendarEvent
import com.mhss.app.mybrain.domain.repository.CalendarRepository
import com.mhss.app.mybrain.domain.repository.WidgetUpdater
import org.koin.core.annotation.Single

@Single
class DeleteCalendarEventUseCase(
    private val calendarRepository: CalendarRepository,
    private val widgetUpdater: WidgetUpdater
) {
    suspend operator fun invoke(event: CalendarEvent) {
        calendarRepository.deleteEvent(event)
        widgetUpdater.updateAll(WidgetUpdater.WidgetType.Calendar)
    }
}