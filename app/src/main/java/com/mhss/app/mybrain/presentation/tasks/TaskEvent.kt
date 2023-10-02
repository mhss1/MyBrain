package com.mhss.app.mybrain.presentation.tasks

import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.util.settings.Order

sealed class TaskEvent {
    data class CompleteTask(val task: Task, val complete: Boolean) : TaskEvent()
    data class GetTask(val taskId: Int) : TaskEvent()
    data class AddTask(val task: Task) : TaskEvent()
    data class SearchTasks(val query: String) : TaskEvent()
    data class UpdateOrder(val order: Order) : TaskEvent()
    data class ShowCompletedTasks(val showCompleted: Boolean) : TaskEvent()
    data class UpdateTask(val task: Task, val dueDateUpdated: Boolean) : TaskEvent()
    data class DeleteTask(val task: Task) : TaskEvent()
    data object ErrorDisplayed: TaskEvent()
}