package com.mhss.app.domain.use_case

import com.mhss.app.datetime.DateTimeFormatter
import com.mhss.app.datetime.currentLocalDate
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.model.effectiveDateRange
import com.mhss.app.domain.repository.CalendarRepository
import com.mhss.app.widget.WIDGET_ITEM_LIMIT
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.daysUntil
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Single
class GetAllEventsUseCase(
    private val calendarRepository: CalendarRepository,
    private val dateTimeFormatter: DateTimeFormatter,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(
        excluded: List<Int>,
        until: Long? = null,
        fromWidget: Boolean = false
    ): GetAllEventsResult {
        return withContext(defaultDispatcher) {
            try {
                val itemLimit = if (fromWidget) WIDGET_ITEM_LIMIT else null
                val events = calendarRepository.getEvents(excluded, until, itemLimit)

                val tz = TimeZone.currentSystemDefault()
                val today = currentLocalDate()
                val maxDate = today.plus(180, DateTimeUnit.DAY)

                val dayCount = today.daysUntil(maxDate) + 1
                val eventsByDayIndex = arrayOfNulls<MutableList<CalendarEvent>>(dayCount)
                events.forEach { event ->
                    val (startDate, endDate) = event.effectiveDateRange(tz)

                    val loopStart = if (startDate < today) today else startDate
                    val loopEnd = if (endDate > maxDate) maxDate else endDate
                    val firstIndex = today.daysUntil(loopStart)
                    val lastIndex = today.daysUntil(loopEnd)
                    if (firstIndex > lastIndex) return@forEach

                    for (dayIndex in firstIndex..lastIndex) {
                        val list = eventsByDayIndex[dayIndex]
                            ?: ArrayList<CalendarEvent>().also { eventsByDayIndex[dayIndex] = it }
                        list.add(event)
                    }
                }

                val eventDays = ArrayList<CalendarEventsDay>(eventsByDayIndex.count { it != null })
                val months = ArrayList<String>()
                var lastYear = Int.MIN_VALUE
                var lastMonth = Int.MIN_VALUE

                eventsByDayIndex.forEachIndexed { dayIndex, list ->
                    if (list == null) return@forEachIndexed
                    val date = today.plus(dayIndex, DateTimeUnit.DAY)
                    val dayMillis = date.atTime(0, 0).toInstant(tz).toEpochMilliseconds()
                    list.sortBy { if (it.start < dayMillis) dayMillis else it.start }
                    val monthName = dateTimeFormatter.monthName(date)
                    eventDays.add(
                        CalendarEventsDay(
                            monthName = monthName,
                            formattedDate = dateTimeFormatter.formatDateForMapping(date),
                            events = list
                        )
                    )

                    if (date.year != lastYear || date.month.number != lastMonth) {
                        months.add(monthName)
                        lastYear = date.year
                        lastMonth = date.month.number
                    }
                }

                GetAllEventsResult(eventDays, months)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                GetAllEventsResult(emptyList(), emptyList())
            }
        }
    }
}

@Serializable
data class GetAllEventsResult(
    @SerialName("eventDays") val eventDays: List<CalendarEventsDay>,
    @SerialName("months") val months: List<String>
)

@Serializable
data class CalendarEventsDay(
    @SerialName("formattedDate") val formattedDate: String,
    @SerialName("events") val events: List<CalendarEvent>,
    @SerialName("monthName") val monthName: String
)
