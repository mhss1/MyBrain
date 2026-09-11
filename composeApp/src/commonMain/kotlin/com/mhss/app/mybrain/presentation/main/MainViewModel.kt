package com.mhss.app.mybrain.presentation.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.datetime.inTheLastWeek
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.use_case.CalendarEventsDay
import com.mhss.app.domain.use_case.GetAllEntriesUseCase
import com.mhss.app.domain.use_case.GetAllEventsUseCase
import com.mhss.app.domain.use_case.GetAllTasksUseCase
import com.mhss.app.domain.use_case.UpdateTaskCompletedUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.stringSetPreferencesKey
import com.mhss.app.preferences.domain.model.toInt
import com.mhss.app.preferences.domain.model.toOrder
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import com.mhss.app.ui.AppFont
import com.mhss.app.ui.FontSizeSettings
import com.mhss.app.ui.StartUpScreenSettings
import com.mhss.app.ui.ThemeSettings
import com.mhss.app.ui.toIntList
import com.mhss.app.mybrain.sync.SyncOrchestrator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
    private val getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val getAllTasks: GetAllTasksUseCase,
    private val getAllEntriesUseCase: GetAllEntriesUseCase,
    private val completeTask: UpdateTaskCompletedUseCase,
    private val getAllEventsUseCase: GetAllEventsUseCase,
    private val syncOrchestrator: SyncOrchestrator
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    fun syncAll() {
        syncOrchestrator.syncAllAsync()
    }

    fun startNetworkDiscovery() {
        syncOrchestrator.startNetworkDiscovery()
    }

    private var refreshTasksJob : Job? = null

    val lockApp = getPreference(booleanPreferencesKey(PrefsConstants.LOCK_APP_KEY), false)
    val themeMode = getPreference(intPreferencesKey(PrefsConstants.SETTINGS_THEME_KEY), ThemeSettings.AUTO.value)
    val defaultStartUpScreen = getPreference(intPreferencesKey(PrefsConstants.DEFAULT_START_UP_SCREEN_KEY), StartUpScreenSettings.SPACES.value)
    val font = getPreference(intPreferencesKey(PrefsConstants.APP_FONT_KEY), AppFont.IBM_PLEX.value)
    val fontSize = getPreference(intPreferencesKey(PrefsConstants.FONT_SIZE_KEY), FontSizeSettings.NORMAL.value)
    val blockScreenshots = getPreference(booleanPreferencesKey(PrefsConstants.BLOCK_SCREENSHOTS_KEY), false)
    val useMaterialYou = getPreference(booleanPreferencesKey(PrefsConstants.SETTINGS_MATERIAL_YOU), false)

    fun onDashboardEvent(event: DashboardEvent) {
        when(event) {
            is DashboardEvent.ReadPermissionChanged -> {
                if (event.hasPermission)
                    getCalendarEvents()
            }
            is DashboardEvent.CompleteTask -> viewModelScope.launch {
                completeTask(event.task, event.isCompleted)
            }
            DashboardEvent.InitAll -> collectDashboardData()
        }
    }

    data class UiState(
        val dashBoardTasks: List<Task> = emptyList(),
        val dashBoardEvents: List<CalendarEventsDay> = emptyList(),
        val summaryTasks: List<Task> = emptyList(),
        val dashBoardEntries: List<DiaryEntry> = emptyList()
    )

    private fun getCalendarEvents() = viewModelScope.launch {
        val excluded = getPreference(
            stringSetPreferencesKey(PrefsConstants.EXCLUDED_CALENDARS_KEY),
            emptySet()
        ).first()
        val events = getAllEventsUseCase(excluded.toIntList())
        uiState = uiState.copy(
            dashBoardEvents = events.eventDays
        )
    }

    private fun collectDashboardData() = viewModelScope.launch {
        combine(
            getPreference(
                intPreferencesKey(PrefsConstants.TASKS_ORDER_KEY),
                SortOrder.DueDate(SortType.ASC).toInt()
            ),
            getPreference(
                booleanPreferencesKey(PrefsConstants.SHOW_COMPLETED_TASKS_KEY),
                false
            ),
            getAllEntriesUseCase(SortOrder.DateCreated(SortType.ASC))
        ) { order, showCompleted, entries ->
            uiState = uiState.copy(
                dashBoardEntries = entries,
            )
            refreshTasks(order.toOrder(), showCompleted)
        }.collect()
    }

    private fun refreshTasks(sortOrder: SortOrder, showCompleted: Boolean) {
        refreshTasksJob?.cancel()
        refreshTasksJob = getAllTasks(sortOrder).onEach { tasks ->
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
