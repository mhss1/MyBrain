package com.mhss.app.mybrain.presentation.tasks

import com.mhss.app.mybrain.domain.model.Task

sealed class TaskEvent {
    data class CompleteTask(val task: Task, val complete: Boolean) : TaskEvent()
    data class AddTask(val task: Task) : TaskEvent()
    data class GetTasks(val query: String) : TaskEvent()
    data class AddAlarm(val task: Task) : TaskEvent()
    object ErrorDisplayed: TaskEvent()
}