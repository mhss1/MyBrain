package com.mhss.app.mybrain.domain.use_case.calendar

import com.mhss.app.mybrain.domain.model.CalendarEvent
import com.mhss.app.mybrain.domain.repository.CalendarRepository
import com.mhss.app.mybrain.util.date.formatDay
import javax.inject.Inject

class GetAllEventsUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(excluded: List<Int>): Map<String, List<CalendarEvent>> {
        return calendarRepository.getEvents()
            .filter { it.calendarId.toInt() !in excluded }
            .groupBy { event ->
                    event.start.formatDay()
            }
    }
}