package com.mhss.app.mybrain.domain.model

data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String?,
    val start: Long,
    val end: Long,
    val location: String?,
    val allDay: Boolean,
    val color: Int,
    val calendarId: Long
)
