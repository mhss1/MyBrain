package com.mhss.app.presentation

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.use_case.GetAllCalendarsUseCase
import com.mhss.app.domain.use_case.GetAllEventsUseCase
import com.mhss.app.domain.use_case.GetMonthEventsUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.stringSetPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import com.mhss.app.presentation.model.CalendarMonth
import com.mhss.app.ui.FirstDayOfWeekSettings
import com.mhss.app.ui.toIntList
import com.mhss.app.util.date.currentLocalDate
import com.mhss.app.util.date.formatDateForMapping
import com.mhss.app.util.date.monthName
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.minusMonth
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.plusMonth
import kotlinx.datetime.yearMonth
import org.koin.android.annotation.KoinViewModel


const val CALENDAR_START_PAGE = 24000
const val CALENDAR_TOTAL_PAGES = 48000

@KoinViewModel
class CalendarViewModel(
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getMonthEventsUseCase: GetMonthEventsUseCase,
    private val getAllCalendarsUseCase: GetAllCalendarsUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val getPreference: GetPreferenceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val loadMutex = Mutex()

    private var updateEventsJob: Job? = null
    private var viewModeJob: Job? = null

    fun loadMonth(month: YearMonth, forceRefresh: Boolean = false) {
        val loadedMonthValue = month.month.number
        val loadedMonths = _uiState.value.loadedMonths
        val firstDayOfWeek = _uiState.value.firstDayOfWeek
        viewModelScope.launch {
            loadMutex.withLock {
                val monthData = async {
                    if (!forceRefresh && loadedMonths.containsKey(loadedMonthValue)) null
                    else {
                        val days = getMonthEventsUseCase(month, _uiState.value.excludedCalendars, firstDayOfWeek)
                        CalendarMonth(month.month.number, days)
                    }
                }
                val prevMonthData = async {
                    val prevMonth = month.minusMonth()
                    if (!forceRefresh && loadedMonths.containsKey(prevMonth.month.number)) null
                    else {
                        val prevMonth = month.minus(1, DateTimeUnit.MONTH)
                        val days =
                            getMonthEventsUseCase(prevMonth, _uiState.value.excludedCalendars, firstDayOfWeek)
                        CalendarMonth(prevMonth.month.number, days)
                    }
                }
                val nextMonthData = async {
                    val nextMonth = month.plusMonth()
                    if (!forceRefresh && loadedMonths.containsKey(nextMonth.month.number)) null
                    else {
                        val days =
                            getMonthEventsUseCase(nextMonth, _uiState.value.excludedCalendars, firstDayOfWeek)
                        CalendarMonth(nextMonth.month.number, days)
                    }
                }

                val map = _uiState.value.loadedMonths

                // if we guarantee that the map will have at most 4 items, we won't have key collisions from different years.
                monthData.await()?.let { map[it.monthNumber] = it }
                prevMonthData.await()?.let { map[it.monthNumber] = it }
                nextMonthData.await()?.let { map[it.monthNumber] = it }

                // keeping max of 4 months in memory
                if (map.size > 4) {
                    map.remove(month.minus(3, DateTimeUnit.MONTH).month.number)
                    map.remove(month.plus(3, DateTimeUnit.MONTH).month.number)
                }
            }
        }
    }

    init {
        _uiState.update { it.copy(currentMonth = currentLocalDate()) }
        viewModelScope.launch {
            val value = getPreference(
                intPreferencesKey(PrefsConstants.FIRST_DAY_OF_WEEK_KEY),
                FirstDayOfWeekSettings.SUNDAY.value
            ).first()
            val firstDay = when (FirstDayOfWeekSettings.fromValue(value)) {
                FirstDayOfWeekSettings.SATURDAY -> DayOfWeek.SATURDAY
                FirstDayOfWeekSettings.SUNDAY -> DayOfWeek.SUNDAY
                FirstDayOfWeekSettings.MONDAY -> DayOfWeek.MONDAY
            }
            _uiState.update { it.copy(firstDayOfWeek = firstDay) }
        }
        collectViewMode()
    }

    fun onEvent(event: CalendarViewModelEvent) {
        when (event) {
            is CalendarViewModelEvent.IncludeCalendar -> updateExcludedCalendars(
                event.calendar.id.toInt(),
                event.calendar.included
            )

            is CalendarViewModelEvent.ReadPermissionChanged -> {
                if (event.hasPermission) collectSettings()
                else updateEventsJob?.cancel()
            }

            is CalendarViewModelEvent.MonthChanged -> {
                _uiState.update { it.copy(currentMonth = event.newMonth) }
            }

            is CalendarViewModelEvent.SelectedDateChanged -> {
                _uiState.update { it.copy(selectedDate = event.newDate) }
            }

            is CalendarViewModelEvent.ViewModeChanged -> {
                viewModelScope.launch {
                    savePreference(
                        booleanPreferencesKey(PrefsConstants.CALENDAR_VIEW_MODE_KEY),
                        event.isMonthView
                    )
                }
                _uiState.update { it.copy(isMonthView = event.isMonthView) }
            }
        }
    }

    private fun updateExcludedCalendars(id: Int, add: Boolean) {
        viewModelScope.launch {
            savePreference(
                stringSetPreferencesKey(PrefsConstants.EXCLUDED_CALENDARS_KEY),
                if (add) _uiState.value.excludedCalendars.addAndToStringSet(id)
                else _uiState.value.excludedCalendars.removeAndToStringSet(id)
            )
        }
    }

    private fun collectSettings() {
        updateEventsJob = getPreference(
            stringSetPreferencesKey(PrefsConstants.EXCLUDED_CALENDARS_KEY),
            emptySet()
        ).onEach { calendarsSet ->
            val calendars = getAllCalendarsUseCase(calendarsSet.toIntList())
            _uiState.update {state ->
                state.copy(
                    excludedCalendars = calendarsSet.map { it.toInt() }.toMutableList(),
                    calendars = calendars
                )
            }
            loadEvents()
        }.launchIn(viewModelScope)
    }

    private fun collectViewMode() {
        viewModeJob?.cancel()
        viewModeJob = getPreference(
            booleanPreferencesKey(PrefsConstants.CALENDAR_VIEW_MODE_KEY),
            false
        ).onEach { isMonthView ->
            _uiState.update { it.copy(isMonthView = isMonthView) }
            loadEvents()
        }.launchIn(viewModelScope)
    }

    private fun loadEvents() {
        if (_uiState.value.isMonthView) {
            loadMonth(_uiState.value.currentMonth.yearMonth, forceRefresh = true)
            _uiState.update { it.copy(events = emptyMap()) }
        } else {
            loadListEvents()
            _uiState.value.loadedMonths.clear()
        }
    }

    private fun loadListEvents() {
        viewModelScope.launch {
            val events = getAllEventsUseCase(_uiState.value.excludedCalendars) {
                it.start.formatDateForMapping()
            }
            val months = events.map {
                it.value.first().start.monthName()
            }.distinct()
            _uiState.update { it.copy(events = events, months = months) }
        }
    }

    data class UiState(
        val events: Map<String, List<CalendarEvent>> = emptyMap(),
        val calendars: Map<String, List<Calendar>> = emptyMap(),
        val excludedCalendars: List<Int> = listOf(),
        val months: List<String> = emptyList(),
        val isMonthView: Boolean = false,
        val currentMonth: LocalDate = currentLocalDate(),
        val selectedDate: LocalDate = currentLocalDate(),
        val loadedMonths: SnapshotStateMap<Int, CalendarMonth> = mutableStateMapOf(),
        val firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY
    )

    private fun List<Int>.addAndToStringSet(id: Int): Set<String> =
        (this + id).map { it.toString() }.toHashSet()

    private fun List<Int>.removeAndToStringSet(id: Int): Set<String> =
        this.filterNot { it == id }.map { it.toString() }.toHashSet()
}
