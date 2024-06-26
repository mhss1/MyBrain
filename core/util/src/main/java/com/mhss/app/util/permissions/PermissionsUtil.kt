package com.mhss.app.util.permissions

import android.Manifest
import android.annotation.SuppressLint

@SuppressLint("InlinedApi")
fun Permission.toAndroidPermission() = when (this) {
    Permission.WRITE_CALENDAR -> Manifest.permission.WRITE_CALENDAR
    Permission.READ_CALENDAR -> Manifest.permission.READ_CALENDAR
    Permission.NOTIFICATIONS -> Manifest.permission.POST_NOTIFICATIONS
    else -> ""
}