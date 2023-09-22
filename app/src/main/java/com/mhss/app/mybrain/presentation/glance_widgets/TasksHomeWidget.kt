package com.mhss.app.mybrain.presentation.glance_widgets

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.*
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import com.mhss.app.mybrain.domain.use_case.settings.GetSettingsUseCase
import com.mhss.app.mybrain.domain.use_case.tasks.GetAllTasksUseCase
import com.mhss.app.mybrain.presentation.tasks.TasksHomeScreenWidget
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TasksHomeWidget : GlanceAppWidget() {

    @Inject
    lateinit var getAllTasks: GetAllTasksUseCase

    @Inject
    lateinit var getSettings: GetSettingsUseCase

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val order = getSettings(
            intPreferencesKey(Constants.TASKS_ORDER_KEY),
            Order.DateModified(OrderType.ASC()).toInt()
        ).first()
        val showCompletedTasks = getSettings(
            booleanPreferencesKey(Constants.SHOW_COMPLETED_TASKS_KEY),
            false
        ).first()

        provideContent {
            val tasks by getAllTasks(order.toOrder(), showCompletedTasks).collectAsState(emptyList())
            TasksHomeScreenWidget(
                tasks
            )
        }
    }
}

@AndroidEntryPoint
class TasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TasksHomeWidget()
}