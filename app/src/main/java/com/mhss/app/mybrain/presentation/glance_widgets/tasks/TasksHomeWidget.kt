package com.mhss.app.mybrain.presentation.glance_widgets.tasks

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.material3.ColorProviders
import com.mhss.app.mybrain.domain.model.preferences.Order
import com.mhss.app.mybrain.domain.model.preferences.OrderType
import com.mhss.app.mybrain.domain.model.preferences.booleanPreferencesKey
import com.mhss.app.mybrain.domain.model.preferences.intPreferencesKey
import com.mhss.app.mybrain.domain.model.preferences.toInt
import com.mhss.app.mybrain.domain.model.preferences.toOrder
import com.mhss.app.mybrain.domain.use_case.settings.GetPreferenceUseCase
import com.mhss.app.mybrain.domain.use_case.tasks.GetAllTasksUseCase
import com.mhss.app.mybrain.presentation.glance_widgets.WidgetTheme
import com.mhss.app.mybrain.presentation.glance_widgets.widgetDarkColorScheme
import com.mhss.app.mybrain.presentation.tasks.TasksHomeScreenWidget
import com.mhss.app.mybrain.util.Constants
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TasksHomeWidget : GlanceAppWidget(), KoinComponent {

    private val getSettings: GetPreferenceUseCase by inject()
    private val getAllTasks: GetAllTasksUseCase by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
            val order by getSettings(
                intPreferencesKey(Constants.TASKS_ORDER_KEY),
                Order.DateModified(OrderType.ASC).toInt()
            ).collectAsState(Order.DateModified(OrderType.ASC).toInt())
            val showCompletedTasks by getSettings(
                booleanPreferencesKey(Constants.SHOW_COMPLETED_TASKS_KEY),
                false
            ).collectAsState(false)
            val useMaterialYou by getSettings(
                booleanPreferencesKey(Constants.WIDGETS_MATERIAL_YOU),
                false
            ).collectAsState(false)
            val tasks by getAllTasks(order.toOrder(), showCompletedTasks).collectAsState(emptyList())

            WidgetTheme(
                if (useMaterialYou) {
                    GlanceTheme.colors
                } else ColorProviders(widgetDarkColorScheme)
            ) {
                TasksHomeScreenWidget(
                    tasks
                )
            }
        }
    }
}

class TasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TasksHomeWidget()
}