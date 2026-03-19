package com.mhss.app.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable

@Serializable
data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String? = null,
    val start: Long,
    val end: Long,
    val location: String? = null,
    val allDay: Boolean = false,
    val color: Int = 0,
    val calendarId: Long,
    val recurring: Boolean = false,
    val frequency: CalendarEventFrequency = CalendarEventFrequency.NEVER,
    val interval: Int = 1,
    val weekDays: Set<DayOfWeek> = emptySet(),
    val instanceDay: Long? = null,
)

enum class CalendarEventFrequency {
    NEVER,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}
