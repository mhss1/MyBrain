package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.repository.CalendarRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class GetAllCalendarsUseCase(
    private val calendarRepository: CalendarRepository,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(excluded: List<Int>): Map<String, List<Calendar>> {
        return withContext(defaultDispatcher) {
            calendarRepository.getCalendars().map { it.copy(included = (it.id.toInt() !in excluded)) }.groupBy { it.account }
        }
    }
}