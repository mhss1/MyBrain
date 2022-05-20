package com.mhss.app.mybrain.presentation.calendar

import com.mhss.app.mybrain.domain.model.Calendar

sealed class CalendarViewModelEvent {
    data class IncludeCalendar(val calendar: Calendar) : CalendarViewModelEvent()
    data class ReadPermissionChanged(val hasPermission: Boolean) : CalendarViewModelEvent()
}
