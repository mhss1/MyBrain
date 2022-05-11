package com.mhss.app.mybrain.domain.use_case.calendar

import android.text.format.DateUtils
import com.mhss.app.mybrain.domain.model.CalendarEvent
import com.mhss.app.mybrain.domain.repository.CalendarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetTodayEventsUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(): List<CalendarEvent> {
        val allEvents = calendarRepository.getEvents()
        return withContext(Dispatchers.Default) { allEvents.filter { DateUtils.isToday(it.start) } }
    }
}