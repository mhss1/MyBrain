package com.mhss.app.mybrain.presentation.main

import com.mhss.app.domain.model.Task


sealed class DashboardEvent {
    data class ReadPermissionChanged(val hasPermission: Boolean) : DashboardEvent()
    data class CompleteTask(val task: Task, val isCompleted: Boolean) : DashboardEvent()
    data object InitAll : DashboardEvent()
}