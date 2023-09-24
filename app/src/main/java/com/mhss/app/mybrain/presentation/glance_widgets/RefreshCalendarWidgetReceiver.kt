package com.mhss.app.mybrain.presentation.glance_widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.runBlocking


class RefreshCalendarWidgetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        runBlocking {
            CalendarHomeWidget().updateAll(context)
        }
    }
}