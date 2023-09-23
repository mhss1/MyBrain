package com.mhss.app.mybrain.presentation.glance_widgets

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.*
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import com.mhss.app.mybrain.di.TasksWidgetEntryPoint
import com.mhss.app.mybrain.presentation.tasks.TasksHomeScreenWidget
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.*
import dagger.hilt.EntryPoints

class TasksHomeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val entryPoint = EntryPoints.get(context, TasksWidgetEntryPoint::class.java)

        provideContent {

            val order by entryPoint.getSettingsUseCase().invoke(
                intPreferencesKey(Constants.TASKS_ORDER_KEY),
                Order.DateModified(OrderType.ASC()).toInt()
            ).collectAsState(Order.DateModified(OrderType.ASC()).toInt())
            val showCompletedTasks by entryPoint.getSettingsUseCase().invoke(
                booleanPreferencesKey(Constants.SHOW_COMPLETED_TASKS_KEY),
                false
            ).collectAsState(false)
            val tasks by entryPoint.getAllTasksUseCase().invoke(order.toOrder(), showCompletedTasks).collectAsState(emptyList())

            TasksHomeScreenWidget(
                tasks
            )
        }
    }
}

class TasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TasksHomeWidget()
}