package com.mhss.app.mybrain.domain.use_case.calendar

import com.mhss.app.mybrain.domain.model.calendar.CalendarEvent
import com.mhss.app.mybrain.domain.repository.calendar.CalendarRepository
import org.koin.core.annotation.Single

@Single
class GetAllEventsUseCase(
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(excluded: List<Int>, fromWidget: Boolean = false, groupBySelector: (CalendarEvent) -> String): Map<String, List<CalendarEvent>> {
        val events = try {
            calendarRepository.getEvents()
                .filter { it.calendarId.toInt() !in excluded }
        } catch (e: Exception) {
             return emptyMap()
        }
        return if (fromWidget)
            events.take(30).groupBy(groupBySelector)
        else
            events.groupBy(groupBySelector)

    }
}