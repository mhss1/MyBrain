package com.mhss.app.mybrain.presentation.glance_widgets

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.runBlocking


class RefreshCalendarWidgetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        runBlocking {
            val updateIntent = Intent(context, CalendarWidgetReceiver::class.java)
            updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

            val widgetManager = AppWidgetManager.getInstance(context)
            val ids = widgetManager.getAppWidgetIds(
                ComponentName(
                    context,
                    CalendarWidgetReceiver::class.java
                )
            )
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

            context.sendBroadcast(updateIntent)
        }
    }
}