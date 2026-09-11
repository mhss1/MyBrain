package com.mhss.app.presentation

import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.Task
import com.mhss.app.preferences.domain.model.SortOrder

sealed class TaskEvent {
    data class CompleteTask(val task: Task, val complete: Boolean) : TaskEvent()
    data class AddTask(val input: AddTaskInput) : TaskEvent()
    data class SearchTasks(val query: String) : TaskEvent()
    data class UpdateOrder(val sortOrder: SortOrder) : TaskEvent()
    data class ShowCompletedTasks(val showCompleted: Boolean) : TaskEvent()
    data object ErrorDisplayed: TaskEvent()
}

data class AddTaskInput(
    val title: String,
    val priority: Priority = Priority.LOW,
    val dueDate: Long = 0L,
    val subTasks: List<String> = emptyList(),
)
