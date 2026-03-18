package com.mhss.app.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.use_case.AddCalendarEventUseCase
import com.mhss.app.domain.use_case.DeleteCalendarEventUseCase
import com.mhss.app.domain.use_case.GetAllCalendarsUseCase
import com.mhss.app.domain.use_case.GetCalendarEventByIdUseCase
import com.mhss.app.domain.use_case.UpdateCalendarEventUseCase
import com.mhss.app.ui.R
import com.mhss.app.ui.snackbar.showSnackbar
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class CalendarEventDetailsViewModel(
    private val getCalendarEventById: GetCalendarEventByIdUseCase,
    private val getAllCalendars: GetAllCalendarsUseCase,
    private val addEvent: AddCalendarEventUseCase,
    private val updateEvent: UpdateCalendarEventUseCase,
    private val deleteEvent: DeleteCalendarEventUseCase,
    eventId: Long?
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    init {
        viewModelScope.launch {
            val event = eventId?.let { getCalendarEventById(it) }
            val calendars = getAllCalendars(emptyList()).values.flatten()
            if (eventId != null && event == null) {
                uiState.snackbarHostState.showSnackbar(R.string.error_item_not_found)
            }
            uiState = uiState.copy(
                event = event,
                calendarsList = calendars,
                isLoading = false
            )
        }
    }

    fun onEvent(event: CalendarEventDetailsEvent) {
        when (event) {
            is CalendarEventDetailsEvent.AddEvent -> viewModelScope.launch {
                if (event.event.title.isNotBlank()) {
                    if (event.event.end <= event.event.start) {
                        uiState.snackbarHostState.showSnackbar(R.string.error_invalid_event_time_range)
                    } else {
                        addEvent(event.event)
                        uiState = uiState.copy(navigateUp = true)
                    }
                } else {
                    uiState.snackbarHostState.showSnackbar(R.string.error_empty_title)
                }
            }

            is CalendarEventDetailsEvent.EditEvent -> viewModelScope.launch {
                if (event.event.title.isNotBlank()) {
                    if (event.event.end <= event.event.start) {
                        uiState.snackbarHostState.showSnackbar(R.string.error_invalid_event_time_range)
                    } else {
                        updateEvent(event.event)
                        uiState = uiState.copy(navigateUp = true)
                    }
                } else {
                    uiState.snackbarHostState.showSnackbar(R.string.error_empty_title)
                }
            }

            is CalendarEventDetailsEvent.DeleteEvent -> viewModelScope.launch {
                deleteEvent(event.event)
                uiState = uiState.copy(navigateUp = true)
            }

        }
    }

    data class UiState(
        val event: CalendarEvent? = null,
        val calendarsList: List<Calendar> = emptyList(),
        val isLoading: Boolean = true,
        val navigateUp: Boolean = false,
        val snackbarHostState: SnackbarHostState = SnackbarHostState()
    )
}

sealed class CalendarEventDetailsEvent {
    data class AddEvent(val event: CalendarEvent) : CalendarEventDetailsEvent()
    data class EditEvent(val event: CalendarEvent) : CalendarEventDetailsEvent()
    data class DeleteEvent(val event: CalendarEvent) : CalendarEventDetailsEvent()
}
