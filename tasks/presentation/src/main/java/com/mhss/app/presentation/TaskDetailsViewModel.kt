package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.use_case.DeleteTaskUseCase
import com.mhss.app.domain.use_case.GetTaskByIdUseCase
import com.mhss.app.domain.use_case.UpdateTaskUseCase
import com.mhss.app.util.date.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class TaskDetailsViewModel(
    private val getTask: GetTaskByIdUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val deleteTask: DeleteTaskUseCase,
    @Named("applicationScope") private val applicationScope: CoroutineScope,
    taskId: Int
) : ViewModel() {

    var taskDetailsUiState by mutableStateOf(TaskDetailsUiState())
        private set

    init {
        viewModelScope.launch {
            val task = getTask(taskId)
            taskDetailsUiState = taskDetailsUiState.copy(
               task = task
            )
        }
    }

    fun onEvent(event: TaskDetailsEvent) {
        when (event) {

            TaskDetailsEvent.ErrorDisplayed -> {
                taskDetailsUiState = taskDetailsUiState.copy(error = null, errorAlarm = false)
            }
            // Using applicationScope to avoid cancelling when the user exits the screen
            // and the view model is cleared before the job finishes
            is TaskDetailsEvent.ScreenOnStop -> applicationScope.launch {
                if (!taskDetailsUiState.navigateUp) {
                    if (taskChanged(taskDetailsUiState.task!!, event.task)) {
                        val newTask = taskDetailsUiState.task!!.copy(
                            title = event.task.title.ifBlank { "Untitled" },
                            description = event.task.description,
                            dueDate = event.task.dueDate,
                            priority = event.task.priority,
                            subTasks = event.task.subTasks,
                            recurring = event.task.recurring,
                            frequency = event.task.frequency,
                            frequencyAmount = event.task.frequencyAmount,
                            isCompleted = event.task.isCompleted,
                            updatedDate = now()
                        )
                        updateTask(
                            newTask,
                            event.task.dueDate != taskDetailsUiState.task!!.dueDate
                        )
                        taskDetailsUiState = taskDetailsUiState.copy(task = newTask)
                    }
                }
            }

            is TaskDetailsEvent.DeleteTask -> viewModelScope.launch {
                deleteTask(taskDetailsUiState.task!!)
                taskDetailsUiState = taskDetailsUiState.copy(navigateUp = true)
            }
        }
    }

    data class TaskDetailsUiState(
        val task: Task? = null,
        val navigateUp: Boolean = false,
        val error: Int? = null,
        val errorAlarm: Boolean = false,
    )

    private fun taskChanged(
        task: Task,
        newTask: Task
    ): Boolean {
        return task.title != newTask.title ||
                task.description != newTask.description ||
                task.dueDate != newTask.dueDate ||
                task.isCompleted != newTask.isCompleted ||
                task.priority != newTask.priority ||
                task.subTasks != newTask.subTasks ||
                task.recurring != newTask.recurring ||
                task.frequency != newTask.frequency ||
                task.frequencyAmount != newTask.frequencyAmount
    }

}