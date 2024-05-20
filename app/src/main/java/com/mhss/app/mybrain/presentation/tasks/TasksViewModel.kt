package com.mhss.app.mybrain.presentation.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.app.getString
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.domain.use_case.settings.GetPreferenceUseCase
import com.mhss.app.mybrain.domain.use_case.settings.SavePreferenceUseCase
import com.mhss.app.mybrain.domain.use_case.tasks.*
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.Order
import com.mhss.app.mybrain.util.settings.OrderType
import com.mhss.app.mybrain.util.settings.toInt
import com.mhss.app.mybrain.util.settings.toOrder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TasksViewModel(
    private val addTask: AddTaskUseCase,
    private val getAllTasks: GetAllTasksUseCase,
    private val getTaskUseCase: GetTaskByIdUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val completeTask: UpdateTaskCompletedUseCase,
    getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val deleteTask: DeleteTaskUseCase,
    private val searchTasksUseCase: SearchTasksUseCase
) : ViewModel() {

    var tasksUiState by mutableStateOf(UiState())
        private set
    var taskDetailsUiState by mutableStateOf(TaskUiState())
        private set

    private var getTasksJob: Job? = null
    private var searchTasksJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                getPreference(
                    intPreferencesKey(Constants.TASKS_ORDER_KEY),
                    Order.DateModified(OrderType.ASC()).toInt()
                ),
                getPreference(
                    booleanPreferencesKey(Constants.SHOW_COMPLETED_TASKS_KEY),
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
                if (event.task.title.isNotBlank()) {
                    viewModelScope.launch {
                        val scheduleSuccess = addTask(event.task)
                        if (!scheduleSuccess) {
                            tasksUiState = tasksUiState.copy(
                                error = getString(R.string.no_alarm_permission),
                                errorAlarm = true
                            )
                        }
                    }
                } else
                    tasksUiState = tasksUiState.copy(error = getString(R.string.error_empty_title))
            }

            is TaskEvent.CompleteTask -> viewModelScope.launch {
                completeTask(event.task.id, event.complete)
            }

            TaskEvent.ErrorDisplayed -> {
                tasksUiState = tasksUiState.copy(error = null, errorAlarm = false)
                taskDetailsUiState = taskDetailsUiState.copy(error = null, errorAlarm = false)
            }

            is TaskEvent.UpdateOrder -> viewModelScope.launch {
                savePreference(
                    intPreferencesKey(Constants.TASKS_ORDER_KEY),
                    event.order.toInt()
                )
            }

            is TaskEvent.ShowCompletedTasks -> viewModelScope.launch {
                savePreference(
                    booleanPreferencesKey(Constants.SHOW_COMPLETED_TASKS_KEY),
                    event.showCompleted
                )
            }

            is TaskEvent.SearchTasks -> {
                viewModelScope.launch {
                    searchTasks(event.query)
                }
            }

            is TaskEvent.UpdateTask -> viewModelScope.launch {
                if (event.task.title.isBlank())
                    taskDetailsUiState =
                        taskDetailsUiState.copy(error = getString(R.string.error_empty_title))
                else {
                    val scheduleAlarmSuccess = updateTask(
                        event.task.copy(updatedDate = System.currentTimeMillis()),
                        taskDetailsUiState.task
                    )
                    taskDetailsUiState = if (scheduleAlarmSuccess) {
                        taskDetailsUiState.copy(navigateUp = true)
                    } else {
                        taskDetailsUiState.copy(
                            error = getString(R.string.no_alarm_permission),
                            errorAlarm = true
                        )
                    }
                }
            }

            is TaskEvent.DeleteTask -> viewModelScope.launch {
                deleteTask(event.task)
                taskDetailsUiState = taskDetailsUiState.copy(navigateUp = true)
            }

            is TaskEvent.GetTask -> viewModelScope.launch {
                taskDetailsUiState = taskDetailsUiState.copy(
                    task = getTaskUseCase(event.taskId)
                )
            }
        }
    }

    data class UiState(
        val tasks: List<Task> = emptyList(),
        val taskOrder: Order = Order.DateModified(OrderType.ASC()),
        val showCompletedTasks: Boolean = false,
        val error: String? = null,
        val errorAlarm: Boolean = false,
        val searchTasks: List<Task> = emptyList()
    )

    data class TaskUiState(
        val task: Task = Task(""),
        val navigateUp: Boolean = false,
        val error: String? = null,
        val errorAlarm: Boolean = false
    )

    private fun getTasks(order: Order, showCompleted: Boolean) {
        getTasksJob?.cancel()
        getTasksJob = getAllTasks(order)
            .map { list ->
                if (showCompleted)
                    list
                else
                    list.filter { !it.isCompleted }
            }.onEach { tasks ->
                tasksUiState = tasksUiState.copy(
                    tasks = tasks,
                    taskOrder = order,
                    showCompletedTasks = showCompleted
                )
            }.launchIn(viewModelScope)
    }

    private fun searchTasks(query: String) {
        searchTasksJob?.cancel()
        searchTasksJob = searchTasksUseCase(query).onEach { tasks ->
            tasksUiState = tasksUiState.copy(
                searchTasks = tasks
            )
        }.launchIn(viewModelScope)
    }
}