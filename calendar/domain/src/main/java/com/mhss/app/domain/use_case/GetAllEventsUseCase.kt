package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.repository.CalendarRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class GetAllEventsUseCase(
    private val calendarRepository: CalendarRepository,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(
        excluded: List<Int>,
        until: Long? = null,
        fromWidget: Boolean = false,
        groupBySelector: (CalendarEvent) -> String
    ): Map<String, List<CalendarEvent>> {
        return withContext(defaultDispatcher) {
            try {
                calendarRepository.getEvents()
                    .filter { event ->
                        (until?.let { event.start <= it } ?: true) &&
                        event.calendarId.toInt() !in excluded
                    }
                    .run {
                        if (fromWidget) take(25).groupBy(groupBySelector)
                        else groupBy(groupBySelector)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyMap()
            }
        }
    }
}