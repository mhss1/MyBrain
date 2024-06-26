package com.mhss.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String?,
    val start: Long,
    val end: Long,
    val location: String?,
    val allDay: Boolean = false,
    val color: Int = 0,
    val calendarId: Long,
    val recurring: Boolean = false,
    val frequency: CalendarEventFrequency = CalendarEventFrequency.NEVER,
)

enum class CalendarEventFrequency {
    NEVER,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}
