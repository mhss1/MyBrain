package com.mhss.app.mybrain.presentation.glance_widgets

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.Settings
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.mhss.app.mybrain.presentation.main.MainActivity
import com.mhss.app.mybrain.util.Constants

class AddEventAction : ActionCallback {
    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(Intent.ACTION_INSERT).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.type = "vnd.android.cursor.item/event"
        context.startActivity(intent)
    }
}

class NavigateToCalendarAction : ActionCallback {
    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Constants.CALENDAR_SCREEN_URI.toUri(),
            context,
            MainActivity::class.java
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

class CalendarWidgetItemClick : ActionCallback {
    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        parameters[eventIdKey]?.let {
            val intent = Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI,
                it
            )
            context.startActivity(intent)
        }
    }
}

class GoToSettingsAction : ActionCallback {
    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
    }
}

class RefreshCalendarAction : ActionCallback {
    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val updateIntent = Intent(context, RefreshCalendarWidgetReceiver::class.java)
        context.sendBroadcast(updateIntent)
    }
}


val eventIdKey = ActionParameters.Key<Long>("eventId")