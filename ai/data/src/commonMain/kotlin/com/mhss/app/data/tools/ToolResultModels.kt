package com.mhss.app.data.tools

import com.mhss.app.data.formatDateTimeForLLM
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.model.Task
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class NoteToolResult(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val pinned: Boolean
)

@Serializable
data class NoteFolderToolResult(
    val id: String,
    val name: String
)

@Serializable
data class TaskToolResult(
    val id: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val priority: String,
    val dueAt: String?,
    val subTasks: List<SubTaskToolResult>,
    val recurrence: String?
)

@Serializable
data class SubTaskToolResult(
    val title: String,
    val completed: Boolean
)

@Serializable
data class CalendarEventToolResult(
    val id: Long,
    val title: String,
    val startAt: String,
    val endAt: String,
    val description: String?,
    val location: String?,
    val allDay: Boolean,
    val calendarId: Long,
    val recurrence: String?
)

@Serializable
data class CalendarToolResult(
    val id: Long,
    val name: String,
    val account: String
)

@Serializable
data class DiaryEntryToolResult(
    val id: String,
    val title: String,
    val content: String,
    val mood: String,
    val createdAt: String
)

@Serializable
data class BookmarkToolResult(
    val id: String,
    val url: String,
    val title: String,
    val description: String,
    val createdAt: String
)

internal fun Note.toToolResult() = NoteToolResult(
    id = id,
    title = title,
    content = content,
    createdAt = createdDate.formatDateTimeForLLM(),
    updatedAt = updatedDate.formatDateTimeForLLM(),
    pinned = pinned
)

internal fun NoteFolder.toToolResult() = NoteFolderToolResult(
    id = id,
    name = name
)

internal fun Task.toToolResult() = TaskToolResult(
    id = id,
    title = title,
    description = description,
    completed = isCompleted,
    priority = priority.name,
    dueAt = dueDate?.formatDateTimeForLLM(),
    subTasks = subTasks.map { SubTaskToolResult(it.title, it.isCompleted) },
    recurrence = if (recurring) taskRecurrenceText() else null
)

internal fun CalendarEvent.toToolResult() = CalendarEventToolResult(
    id = id,
    title = title,
    startAt = start.formatDateTimeForLLM(),
    endAt = end.formatDateTimeForLLM(),
    description = description,
    location = location,
    allDay = allDay,
    calendarId = calendarId,
    recurrence = if (recurring) eventRecurrenceText() else null
)

internal fun Calendar.toToolResult() = CalendarToolResult(
    id = id,
    name = name,
    account = account
)

internal fun DiaryEntry.toToolResult() = DiaryEntryToolResult(
    id = id,
    title = title,
    content = content,
    mood = mood.name,
    createdAt = createdDate.formatDateTimeForLLM()
)

internal fun Bookmark.toToolResult() = BookmarkToolResult(
    id = id,
    url = url,
    title = title,
    description = description,
    createdAt = createdDate.formatDateTimeForLLM()
)

private fun Task.taskRecurrenceText(): String {
    val unit = when (frequency.name) {
        "EVERY_MINUTES" -> "minute"
        "HOURLY" -> "hour"
        "DAILY" -> "day"
        "WEEKLY" -> "week"
        "MONTHLY" -> "month"
        else -> "year"
    }
    return "Every $frequencyAmount ${if (frequencyAmount == 1) unit else "${unit}s"}"
}

private fun CalendarEvent.eventRecurrenceText(): String {
    val unit = when (frequency.name) {
        "DAILY" -> "day"
        "WEEKLY" -> "week"
        "MONTHLY" -> "month"
        else -> "year"
    }
    return buildString {
        append("Every $interval ${if (interval == 1) unit else "${unit}s"}")
        if (weekDays.isNotEmpty()) {
            append(" on ")
            append(weekDays.joinToString { it.name.lowercase() })
        }
    }
}
