package com.mhss.app.mybrain.presentation.calendar

import com.mhss.app.mybrain.domain.model.calendar.Calendar
import com.mhss.app.mybrain.domain.model.calendar.CalendarEvent

sealed class CalendarViewModelEvent {
    data class IncludeCalendar(val calendar: Calendar) : CalendarViewModelEvent()
    data class ReadPermissionChanged(val hasPermission: Boolean) : CalendarViewModelEvent()
    data class EditEvent(val event: CalendarEvent) : CalendarViewModelEvent()
    data class DeleteEvent(val event: CalendarEvent) : CalendarViewModelEvent()
    data class AddEvent(val event: CalendarEvent) : CalendarViewModelEvent()
    object ErrorDisplayed : CalendarViewModelEvent()
}
