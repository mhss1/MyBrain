package com.mhss.app.util.permissions

/*
TODO: Implement when migrating to Kotlin Multiplatform

@Composable
expect fun rememberPermissionState(
    permission: Permission,
): PermissionState
 */

interface PermissionState {

    var isGranted: Boolean

    var shouldShowRationale: Boolean

    fun launchRequest()

    fun refresh()

    fun openAppSettings()
}


enum class Permission {
    READ_CALENDAR,
    WRITE_CALENDAR,
    SCHEDULE_ALARMS,
    NOTIFICATIONS
}