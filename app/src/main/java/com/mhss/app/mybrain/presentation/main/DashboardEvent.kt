package com.mhss.app.mybrain.presentation.main

import com.mhss.app.mybrain.domain.model.Task


sealed class DashboardEvent {
    data class ReadPermissionChanged(val hasPermission: Boolean) : DashboardEvent()
    data class UpdateTask(val task: Task) : DashboardEvent()
    object InitAll : DashboardEvent()
}