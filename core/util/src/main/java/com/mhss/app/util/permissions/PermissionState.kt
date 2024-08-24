package com.mhss.app.util.permissions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

@Stable
class AndroidPermissionState(
    private val permission: Permission,
    private val context: Context,
    private val activity: Activity?
) : PermissionState {

    override var shouldShowRationale by mutableStateOf(false)

    override var isGranted by mutableStateOf(getPermissionStatus())

    override fun launchRequest() {
        if (permission == Permission.SCHEDULE_ALARMS) {
            openAppSettings()
        } else {
            launcher?.launch(permission.toAndroidPermission())
        }
    }

    internal var launcher: ActivityResultLauncher<String>? = null

    override fun refresh() {
        isGranted = getPermissionStatus()
    }

    private fun getPermissionStatus(): Boolean {
        val granted = permission == Permission.SCHEDULE_ALARMS ||
                ContextCompat.checkSelfPermission(context, permission.toAndroidPermission()) ==
                PackageManager.PERMISSION_GRANTED

        shouldShowRationale = !granted && activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(
                it, permission.toAndroidPermission()
            )
        } ?: true
        return granted
    }

    @SuppressLint("InlinedApi")
    override fun openAppSettings() {
        val intentAction = if (permission == Permission.SCHEDULE_ALARMS) {
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
        } else {
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        }

        val intent = Intent().apply {
            action = intentAction
            data = Uri.parse("package:" + context.applicationContext.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

@Composable
fun rememberPermissionState(
    permission: Permission,
): PermissionState {
    val context = LocalContext.current
    val permissionState = remember(permission) {
        AndroidPermissionState(permission, context, context.getActivity())
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissionState.refresh()
    }

    LifecycleResumeEffect(permission, launcher) {
        if (!permissionState.isGranted) {
            permissionState.refresh()
        }
        if (permissionState.launcher == null) {
            permissionState.launcher = launcher
        }
        onPauseOrDispose {
            permissionState.launcher = null
        }
    }

    return permissionState
}

fun Context.getActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}