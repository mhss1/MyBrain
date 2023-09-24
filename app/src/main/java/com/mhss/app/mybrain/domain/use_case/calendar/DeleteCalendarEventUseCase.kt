package com.mhss.app.mybrain.domain.use_case.calendar

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.mhss.app.mybrain.domain.model.CalendarEvent
import com.mhss.app.mybrain.domain.repository.CalendarRepository
import com.mhss.app.mybrain.presentation.glance_widgets.CalendarHomeWidget
import javax.inject.Inject

class DeleteCalendarEventUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val context: Context
) {
    suspend operator fun invoke(event: CalendarEvent) {
        calendarRepository.deleteEvent(event)
        CalendarHomeWidget().updateAll(context)
    }
}