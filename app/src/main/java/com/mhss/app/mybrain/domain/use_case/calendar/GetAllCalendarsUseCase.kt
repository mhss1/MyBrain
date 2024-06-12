package com.mhss.app.mybrain.domain.use_case.calendar

import com.mhss.app.mybrain.di.namedDefaultDispatcher
import com.mhss.app.mybrain.domain.model.calendar.Calendar
import com.mhss.app.mybrain.domain.repository.calendar.CalendarRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class GetAllCalendarsUseCase(
    private val calendarRepository: CalendarRepository,
    @Named(namedDefaultDispatcher) private val defaultDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(excluded: List<Int>): Map<String, List<Calendar>> {
        return withContext(defaultDispatcher) {
            calendarRepository.getCalendars().map { it.copy(included = (it.id.toInt() !in excluded)) }.groupBy { it.account }
        }
    }
}