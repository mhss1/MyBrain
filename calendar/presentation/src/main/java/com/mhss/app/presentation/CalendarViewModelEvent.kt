package com.mhss.app.presentation

import com.mhss.app.domain.model.Calendar
import kotlinx.datetime.LocalDate

sealed class CalendarViewModelEvent {
    data class IncludeCalendar(val calendar: Calendar) : CalendarViewModelEvent()
    data class ReadPermissionChanged(val hasPermission: Boolean) : CalendarViewModelEvent()
    data class ViewModeChanged(val isMonthView: Boolean) : CalendarViewModelEvent()
    data class MonthChanged(val newMonth: LocalDate) : CalendarViewModelEvent()
    data class SelectedDateChanged(val newDate: LocalDate) : CalendarViewModelEvent()
}
