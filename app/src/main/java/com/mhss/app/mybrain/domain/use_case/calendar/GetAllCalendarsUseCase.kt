package com.mhss.app.mybrain.domain.use_case.calendar

import com.mhss.app.mybrain.domain.model.calendar.Calendar
import com.mhss.app.mybrain.domain.repository.calendar.CalendarRepository
import org.koin.core.annotation.Single

@Single
class GetAllCalendarsUseCase(
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(excluded: List<Int>): Map<String, List<Calendar>> {
        return calendarRepository.getCalendars().map { it.copy(included = (it.id.toInt() !in excluded)) }.groupBy { it.account }
    }
}