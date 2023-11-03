package com.mhss.app.mybrain.domain.use_case.calendar

import com.mhss.app.mybrain.domain.model.CalendarEvent
import com.mhss.app.mybrain.domain.repository.CalendarRepository
import com.mhss.app.mybrain.util.date.formatDateForMapping
import javax.inject.Inject

class GetAllEventsUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(excluded: List<Int>, fromWidget: Boolean = false): Map<String, List<CalendarEvent>> {
        val events = try {
            calendarRepository.getEvents()
                .filter { it.calendarId.toInt() !in excluded }
        } catch (e: Exception) {
             return emptyMap()
        }
        return if (fromWidget)
            events.take(30).groupBy { event ->
                event.start.formatDateForMapping()
            }
        else
            events.groupBy { event ->
                event.start.formatDateForMapping()
            }

    }
}