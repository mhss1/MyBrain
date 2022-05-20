package com.mhss.app.mybrain.presentation.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.mybrain.domain.model.Calendar
import com.mhss.app.mybrain.domain.model.CalendarEvent
import com.mhss.app.mybrain.domain.use_case.calendar.GetAllCalendarsUseCase
import com.mhss.app.mybrain.domain.use_case.calendar.GetAllEventsUseCase
import com.mhss.app.mybrain.domain.use_case.settings.GetSettingsUseCase
import com.mhss.app.mybrain.domain.use_case.settings.SaveSettingsUseCase
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.date.monthName
import com.mhss.app.mybrain.util.settings.addAndToStringSet
import com.mhss.app.mybrain.util.settings.removeAndToStringSet
import com.mhss.app.mybrain.util.settings.toIntList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val getAllCalendarsUseCase: GetAllCalendarsUseCase,
    private val saveSettings: SaveSettingsUseCase,
    private val getSettings: GetSettingsUseCase
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    private var updateEventsJob : Job? = null

    fun onEvent(event: CalendarViewModelEvent) {
        when(event){
            is CalendarViewModelEvent.IncludeCalendar -> updateExcludedCalendars(event.calendar.id.toInt(), event.calendar.included)
            is CalendarViewModelEvent.ReadPermissionChanged -> {
                if (event.hasPermission) collectSettings()
                else updateEventsJob?.cancel()
            }
        }
    }

    private fun updateExcludedCalendars(id: Int, add: Boolean) {
        viewModelScope.launch {
            saveSettings(
                stringSetPreferencesKey(Constants.EXCLUDED_CALENDARS_KEY),
                if (add) uiState.excludedCalendars.addAndToStringSet(id)
                else uiState.excludedCalendars.removeAndToStringSet(id)
            )
        }
    }

    private fun collectSettings() {
            updateEventsJob = getSettings(
                stringSetPreferencesKey(Constants.EXCLUDED_CALENDARS_KEY),
                emptySet()
            ).onEach { calendarsSet ->
                val events = getAllEventsUseCase(calendarsSet.toIntList())
                val calendars = getAllCalendarsUseCase(calendarsSet.toIntList())
                val months = events.map {
                    it.value.first().start.monthName()
                }.distinct()
                uiState = uiState.copy(
                    excludedCalendars = calendarsSet.map { it.toInt() }.toMutableList(),
                    events = events,
                    calendars = calendars,
                    months = months
                )
            }.launchIn(viewModelScope)
    }

    data class UiState(
        val events: Map<String, List<CalendarEvent>> = emptyMap(),
        val calendars: Map<String, List<Calendar>> = emptyMap(),
        val excludedCalendars: MutableList<Int> = mutableListOf(),
        val months: List<String> = emptyList()
    )
}
