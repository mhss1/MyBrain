package com.mhss.app.presentation

import com.mhss.app.domain.model.Task

sealed class TaskDetailsEvent {
    data class ScreenOnStop(val task: Task): TaskDetailsEvent()
    data object DeleteTask : TaskDetailsEvent()
    data object ErrorDisplayed: TaskDetailsEvent()
}