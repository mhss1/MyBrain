package com.mhss.app.mybrain.presentation.main

import com.mhss.app.domain.model.Task


sealed class DashboardEvent {
    data class ReadPermissionChanged(val hasPermission: Boolean) : DashboardEvent()
    data class UpdateTask(val task: Task) : DashboardEvent()
    data object InitAll : DashboardEvent()
}