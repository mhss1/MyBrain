package com.mhss.app.widget.tasks

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.material3.ColorProviders
import com.mhss.app.util.Constants
import com.mhss.app.domain.use_case.GetAllTasksUseCase
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.widget.WidgetTheme
import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.preferences.domain.model.OrderType
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.toInt
import com.mhss.app.preferences.domain.model.toOrder
import com.mhss.app.widget.widgetDarkColorScheme
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TasksWidget : GlanceAppWidget(), KoinComponent {

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
                booleanPreferencesKey(Constants.SETTINGS_MATERIAL_YOU),
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
    override val glanceAppWidget: GlanceAppWidget = TasksWidget()
}