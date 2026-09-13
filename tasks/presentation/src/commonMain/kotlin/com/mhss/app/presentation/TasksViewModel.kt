package com.mhss.app.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.mhss.app.datetime.now
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.use_case.GetAllTasksUseCase
import com.mhss.app.domain.use_case.SearchTasksUseCase
import com.mhss.app.domain.use_case.UpdateTaskCompletedUseCase
import com.mhss.app.domain.use_case.UpsertTaskUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.toInt
import com.mhss.app.preferences.domain.model.toOrder
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import com.mhss.app.ui.Res
import com.mhss.app.ui.error_empty_title
import com.mhss.app.ui.snackbar.showSnackbar
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
@KoinViewModel
class TasksViewModel(
    private val addTask: UpsertTaskUseCase,
    private val getAllTasks: GetAllTasksUseCase,
    private val completeTask: UpdateTaskCompletedUseCase,
    getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val searchTasksUseCase: SearchTasksUseCase,
    @Named("applicationScope") private val applicationScope: CoroutineScope,
) : ViewModel() {

    var tasksUiState by mutableStateOf(UiState())
        private set

    private val pagingRequest = MutableStateFlow<Pair<SortOrder, Boolean>>(SortOrder.DueDate(SortType.ASC) to false)
    val tasks = pagingRequest.flatMapLatest { getAllTasks.paged(it.first, it.second) }.cachedIn(viewModelScope)

    private val searchQuery = MutableStateFlow("")
    val searchResults = searchQuery.flatMapLatest {
        delay(250)
        searchTasksUseCase.paged(it)
    }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            combine(
                getPreference(
                    intPreferencesKey(PrefsConstants.TASKS_ORDER_KEY),
                    SortOrder.DueDate(SortType.ASC).toInt()
                ),
                getPreference(
                    booleanPreferencesKey(PrefsConstants.SHOW_COMPLETED_TASKS_KEY),
                    false
                )
            ) { order, showCompleted ->
                getTasks(order.toOrder(), showCompleted)
            }.collect()
        }
    }

    fun onEvent(event: TaskEvent) {
        when (event) {
            is TaskEvent.AddTask -> {
                applicationScope.launch {
                    if (event.input.title.isNotBlank()) {
                        val timestamp = now()
                        val task = Task(
                            title = event.input.title.trim(),
                            priority = event.input.priority,
                            dueDate = event.input.dueDate,
                            subTasks = event.input.subTasks.mapNotNull { title ->
                                title.trim().takeIf(String::isNotBlank)?.let(::SubTask)
                            },
                            createdDate = timestamp,
                            updatedDate = timestamp,
                            id = Uuid.generateV7().toString()
                        )
                        val scheduleSuccess = addTask(task)
                        if (!scheduleSuccess) tasksUiState = tasksUiState.copy(alarmError = true)
                    } else {
                        tasksUiState.snackbarHostState.showSnackbar(Res.string.error_empty_title)
                    }
                }
            }

            is TaskEvent.CompleteTask -> viewModelScope.launch {
                completeTask(event.task, event.complete)
            }

            TaskEvent.ErrorDisplayed -> {
                tasksUiState = tasksUiState.copy(alarmError = false)
            }

            is TaskEvent.UpdateOrder -> viewModelScope.launch {
                savePreference(
                    intPreferencesKey(PrefsConstants.TASKS_ORDER_KEY),
                    event.sortOrder.toInt()
                )
            }

            is TaskEvent.ShowCompletedTasks -> viewModelScope.launch {
                savePreference(
                    booleanPreferencesKey(PrefsConstants.SHOW_COMPLETED_TASKS_KEY),
                    event.showCompleted
                )
            }

            is TaskEvent.SearchTasks -> searchQuery.value = event.query
        }
    }

    data class UiState(
        val taskSortOrder: SortOrder = SortOrder.DueDate(SortType.ASC),
        val showCompletedTasks: Boolean = false,
        val alarmError: Boolean = false,
        val snackbarHostState: SnackbarHostState = SnackbarHostState()
    )

    private fun getTasks(sortOrder: SortOrder, showCompleted: Boolean) {
        pagingRequest.value = sortOrder to showCompleted
        tasksUiState = tasksUiState.copy(taskSortOrder = sortOrder, showCompletedTasks = showCompleted)
    }
}
