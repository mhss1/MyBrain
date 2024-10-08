package com.mhss.app.mybrain.presentation.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.use_case.GetAllEntriesUseCase
import com.mhss.app.domain.use_case.GetAllEventsUseCase
import com.mhss.app.domain.use_case.GetAllTasksUseCase
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.use_case.UpdateTaskCompletedUseCase
import com.mhss.app.preferences.domain.model.*
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import com.mhss.app.ui.StartUpScreenSettings
import com.mhss.app.ui.ThemeSettings
import com.mhss.app.ui.theme.Rubik
import com.mhss.app.ui.toInt
import com.mhss.app.ui.toIntList
import com.mhss.app.util.date.formatDateForMapping
import com.mhss.app.util.date.inTheLastWeek
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
    private val getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val getAllTasks: GetAllTasksUseCase,
    private val getAllEntriesUseCase: GetAllEntriesUseCase,
    private val completeTask: UpdateTaskCompletedUseCase,
    private val getAllEventsUseCase: GetAllEventsUseCase
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
    private set

    private var refreshTasksJob : Job? = null

    val lockApp = getPreference(booleanPreferencesKey(PrefsConstants.LOCK_APP_KEY), false)
    val themeMode = getPreference(intPreferencesKey(PrefsConstants.SETTINGS_THEME_KEY), ThemeSettings.AUTO.value)
    val defaultStartUpScreen = getPreference(intPreferencesKey(PrefsConstants.DEFAULT_START_UP_SCREEN_KEY), StartUpScreenSettings.SPACES.value)
    val font = getPreference(intPreferencesKey(PrefsConstants.APP_FONT_KEY), Rubik.toInt())
    val blockScreenshots = getPreference(booleanPreferencesKey(PrefsConstants.BLOCK_SCREENSHOTS_KEY), false)
    val useMaterialYou = getPreference(booleanPreferencesKey(PrefsConstants.SETTINGS_MATERIAL_YOU), false)

    fun onDashboardEvent(event: DashboardEvent) {
        when(event) {
            is DashboardEvent.ReadPermissionChanged -> {
                if (event.hasPermission)
                    getCalendarEvents()
            }
            is DashboardEvent.CompleteTask -> viewModelScope.launch {
                completeTask(event.task.id, event.isCompleted)
            }
            DashboardEvent.InitAll -> collectDashboardData()
        }
    }

    data class UiState(
        val dashBoardTasks: List<Task> = emptyList(),
        val dashBoardEvents: Map<String, List<CalendarEvent>> = emptyMap(),
        val summaryTasks: List<Task> = emptyList(),
        val dashBoardEntries: List<DiaryEntry> = emptyList()
    )

    private fun getCalendarEvents() = viewModelScope.launch {
        val excluded = getPreference(
            stringSetPreferencesKey(PrefsConstants.EXCLUDED_CALENDARS_KEY),
            emptySet()
        ).first()
        val events = getAllEventsUseCase(excluded.toIntList()) {
            it.start.formatDateForMapping()
        }
        uiState = uiState.copy(
            dashBoardEvents = events
        )
    }

    private fun collectDashboardData() = viewModelScope.launch {
        combine(
            getPreference(
                intPreferencesKey(PrefsConstants.TASKS_ORDER_KEY),
                Order.DateModified(OrderType.ASC).toInt()
            ),
            getPreference(
                booleanPreferencesKey(PrefsConstants.SHOW_COMPLETED_TASKS_KEY),
                false
            ),
            getAllEntriesUseCase(Order.DateCreated(OrderType.ASC))
        ) { order, showCompleted, entries ->
            uiState = uiState.copy(
                dashBoardEntries = entries,
            )
            refreshTasks(order.toOrder(), showCompleted)
        }.collect()
    }

    private fun refreshTasks(order: Order, showCompleted: Boolean) {
        refreshTasksJob?.cancel()
        refreshTasksJob = getAllTasks(order).onEach { tasks ->
                uiState = uiState.copy(
                    dashBoardTasks = if (showCompleted) tasks else tasks.filter { !it.isCompleted },
                    summaryTasks = tasks.filter { it.createdDate.inTheLastWeek() }
                )
            }.launchIn(viewModelScope)
    }

    fun disableAppLock() = viewModelScope.launch {
        savePreference(booleanPreferencesKey(PrefsConstants.LOCK_APP_KEY), false)
    }

}