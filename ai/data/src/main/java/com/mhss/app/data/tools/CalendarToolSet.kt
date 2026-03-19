package com.mhss.app.data.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.mhss.app.data.llmDateTimeFormatUnicode
import com.mhss.app.data.parseDateTimeFromLLM
import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.model.CalendarEventFrequency
import com.mhss.app.domain.use_case.AddCalendarEventUseCase
import com.mhss.app.domain.use_case.GetAllCalendarsUseCase
import com.mhss.app.domain.use_case.GetEventsWithinRangeUseCase
import com.mhss.app.domain.use_case.SearchEventsByTitleWithinRangeUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.stringSetPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory
import java.util.Locale

@Factory
class CalendarToolSet(
    private val getEventsWithinRangeUseCase: GetEventsWithinRangeUseCase,
    private val searchEventsByTitleWithinRangeUseCase: SearchEventsByTitleWithinRangeUseCase,
    private val addCalendarEvent: AddCalendarEventUseCase,
    private val getAllCalendarsUseCase: GetAllCalendarsUseCase,
    private val getPreference: GetPreferenceUseCase
) : ToolSet {

    @Tool(GET_EVENTS_WITHIN_RANGE_TOOL)
    @LLMDescription("Get events within date range. If the user asks about the date/time of an event, use $FORMAT_DATE_TOOL to get accurate dates from the result.")
    suspend fun getEventsWithinRange(
        @LLMDescription("Format: $llmDateTimeFormatUnicode") startDateTime: String,
        @LLMDescription("Format: $llmDateTimeFormatUnicode") endDateTime: String
    ): GetEventsResult {
        val startMillis = startDateTime.parseDateTimeFromLLM()
            ?: throw IllegalArgumentException("Invalid start date format. The operation did not proceed.")
        val endMillis = endDateTime.parseDateTimeFromLLM()
            ?: throw IllegalArgumentException("Invalid end date format. The operation did not proceed.")
        return GetEventsResult(getEventsWithinRangeUseCase(startMillis, endMillis, getExcludedCalendars()))
    }

    @Tool(SEARCH_EVENTS_BY_NAME_WITHIN_RANGE_TOOL)
    @LLMDescription("Search for an event name within a date range. Useful for finding an event while using a large range comfortably (e.g 3 months) without needing to call getEventsWithinRange and polluting the results with unnecessary unrelated events. If the user asks about the date/time of an event, use $FORMAT_DATE_TOOL to get accurate dates from the result.")
    suspend fun searchEventsByNameWithinRange(
        @LLMDescription("Event name (or partial name) to search for.") eventName: String,
        @LLMDescription("Format: $llmDateTimeFormatUnicode") startDateTime: String,
        @LLMDescription("Format: $llmDateTimeFormatUnicode") endDateTime: String
    ): SearchEventsResult {
        val query = eventName.trim()
        if (query.isBlank()) throw IllegalArgumentException("Invalid event name. The operation did not proceed.")

        val startMillis = startDateTime.parseDateTimeFromLLM()
            ?: throw IllegalArgumentException("Invalid start date format. The operation did not proceed.")
        val endMillis = endDateTime.parseDateTimeFromLLM()
            ?: throw IllegalArgumentException("Invalid end date format. The operation did not proceed.")

        return SearchEventsResult(searchEventsByTitleWithinRangeUseCase(
            startMillis = startMillis,
            endMillis = endMillis,
            titleQuery = query,
            excludedCalendars = getExcludedCalendars()
        ))
    }

    @Tool(CREATE_EVENT_TOOL)
    @LLMDescription("Create event. Returns ID.")
    suspend fun createEvent(
        title: String,
        @LLMDescription("Format: $llmDateTimeFormatUnicode") start: String,
        @LLMDescription("Format: $llmDateTimeFormatUnicode") end: String,
        @LLMDescription("Use getAllCalendars to get ID") calendarId: Long,
        description: String? = null,
        location: String? = null,
        allDay: Boolean = false,
        recurring: Boolean = false,
        frequency: CalendarEventFrequency = CalendarEventFrequency.NEVER,
        @LLMDescription("Repeat interval. Minimum value is 1.") interval: Int = 1,
        @LLMDescription("Only for weekly repeats. Use weekday names or RFC codes such as MONDAY or MO.") weekDays: List<String> = emptyList()
    ): CalendarEventIdResult {
        val startMillis = start.parseDateTimeFromLLM()
            ?: throw IllegalArgumentException("Invalid start date format. The event was not created.")
        val endMillis = end.parseDateTimeFromLLM()
            ?: throw IllegalArgumentException("Invalid end date format. The event was not created.")
        val event = CalendarEvent(
            id = 0,
            title = title,
            description = description,
            start = startMillis,
            end = endMillis,
            location = location,
            allDay = allDay,
            calendarId = calendarId,
            recurring = recurring,
            frequency = frequency,
            interval = interval.coerceAtLeast(1),
            weekDays = weekDays.mapNotNull { it.toDayOfWeekOrNull() }.toHashSet()
        )
        return CalendarEventIdResult(createdEventId = addCalendarEvent(event))
    }

    @Tool(CREATE_EVENTS_TOOL)
    @LLMDescription("Create multiple events. Returns IDs.")
    suspend fun createEvents(
        events: List<CalendarEventInput>
    ): CalendarEventIdsResult {
        val ids = events.map { input ->
            val startMillis = input.start.parseDateTimeFromLLM()
                ?: throw IllegalArgumentException("Invalid start date format for event: ${input.title}. The events were not created.")
            val endMillis = input.end.parseDateTimeFromLLM()
                ?: throw IllegalArgumentException("Invalid end date format for event: ${input.title}. The events were not created.")
            val event = CalendarEvent(
                id = 0,
                title = input.title,
                description = input.description,
                start = startMillis,
                end = endMillis,
                location = input.location,
                allDay = input.allDay,
                calendarId = input.calendarId,
                recurring = input.recurring,
                frequency = input.frequency,
                interval = input.interval.coerceAtLeast(1),
                weekDays = input.weekDays.mapNotNull { it.toDayOfWeekOrNull() }.toHashSet()
            )
            addCalendarEvent(event)
        }
        return CalendarEventIdsResult(createdEventIds = ids)
    }

    @Tool(GET_ALL_CALENDARS_TOOL)
    @LLMDescription("Get all calendars (grouped by account).")
    suspend fun getAllCalendars() = GetCalendarsResult(getAllCalendarsUseCase(getExcludedCalendars()))

    private suspend fun getExcludedCalendars(): List<Int> {
        return getPreference(
            stringSetPreferencesKey(PrefsConstants.EXCLUDED_CALENDARS_KEY),
            emptySet()
        ).firstOrNull().orEmpty().mapNotNull { it.toIntOrNull() }
    }
}

private fun String.toDayOfWeekOrNull(): DayOfWeek? {
    return when (trim().uppercase(Locale.US)) {
        "MONDAY", "MON", "MO" -> DayOfWeek.MONDAY
        "TUESDAY", "TUE", "TU" -> DayOfWeek.TUESDAY
        "WEDNESDAY", "WED", "WE" -> DayOfWeek.WEDNESDAY
        "THURSDAY", "THU", "TH" -> DayOfWeek.THURSDAY
        "FRIDAY", "FRI", "FR" -> DayOfWeek.FRIDAY
        "SATURDAY", "SAT", "SA" -> DayOfWeek.SATURDAY
        "SUNDAY", "SUN", "SU" -> DayOfWeek.SUNDAY
        else -> null
    }
}

@Serializable
data class CalendarEventInput(
    val title: String,
    @param:LLMDescription("Format: $llmDateTimeFormatUnicode") val start: String,
    @param:LLMDescription("Format: $llmDateTimeFormatUnicode") val end: String,
    val calendarId: Long,
    val description: String? = null,
    val location: String? = null,
    val allDay: Boolean = false,
    val recurring: Boolean = false,
    val frequency: CalendarEventFrequency = CalendarEventFrequency.NEVER,
    val interval: Int = 1,
    val weekDays: List<String> = emptyList()
)

@Serializable
data class GetEventsResult(val events: List<CalendarEvent>)

@Serializable
data class SearchEventsResult(val events: List<CalendarEvent>)

@Serializable
data class GetCalendarsResult(val calendars: Map<String, List<Calendar>>)

@Serializable
data class CalendarEventIdResult(val createdEventId: Long?)

@Serializable
data class CalendarEventIdsResult(val createdEventIds: List<Long?>)
