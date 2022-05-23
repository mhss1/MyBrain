package com.mhss.app.mybrain.presentation.glance_widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.domain.use_case.settings.GetSettingsUseCase
import com.mhss.app.mybrain.domain.use_case.tasks.GetAllTasksUseCase
import com.mhss.app.mybrain.presentation.tasks.TasksHomeScreenWidget
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TasksHomeWidget : GlanceAppWidget() {

    @Composable
    override fun Content() {
        val gson = Gson()
        val type = object : TypeToken<List<Task>>() {}.type
        val prefs = currentState<Preferences>()
        val tasks: List<Task> = gson.fromJson(prefs[stringPreferencesKey("tasks")], type) ?: emptyList()
        TasksHomeScreenWidget(
            tasks
        )
    }
}

@AndroidEntryPoint
class TasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TasksHomeWidget()
    private val coroutineScope = MainScope()

    @Inject
    lateinit var getAllTasks: GetAllTasksUseCase

    @Inject
    lateinit var getSettings: GetSettingsUseCase

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        observeData(context)
    }

    private fun observeData(context: Context) {
        coroutineScope.launch {
            val gson = Gson()
            val type = object : TypeToken<List<Task>>() {}.type
            val order = getSettings(
                intPreferencesKey(Constants.TASKS_ORDER_KEY),
                Order.DateModified(OrderType.ASC()).toInt()
            ).first()
            val showCompletedTasks = getSettings(
                booleanPreferencesKey(Constants.SHOW_COMPLETED_TASKS_KEY),
                false
            ).first()
            val tasks = getAllTasks(order.toOrder())
                .map { list ->
                if (showCompletedTasks)
                    list
                else
                    list.filter { !it.isCompleted }
            }.first()
            val tasksJson = gson.toJson(tasks, type)
            val glanceId =
                GlanceAppWidgetManager(context).getGlanceIds(TasksHomeWidget::class.java)
                    .firstOrNull()

            glanceId?.let {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, it) { pref ->
                    pref.toMutablePreferences().apply {
                        this[stringPreferencesKey("tasks")] = tasksJson
                    }
                }
                glanceAppWidget.update(context, it)
            }
        }
    }

}