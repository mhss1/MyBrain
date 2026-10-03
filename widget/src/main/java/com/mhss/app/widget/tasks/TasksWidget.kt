package com.mhss.app.widget.tasks

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.material3.ColorProviders
import androidx.glance.currentState
import androidx.datastore.preferences.core.Preferences
import com.mhss.app.datetime.DateTimeFormatter
import com.mhss.app.datetime.LocalDateTimeFormatter
import com.mhss.app.domain.use_case.GetWidgetTasksUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.toInt
import com.mhss.app.preferences.domain.model.toSortOrder
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.ui.ThemeSettings
import com.mhss.app.widget.WidgetSettings
import com.mhss.app.widget.WidgetTheme
import com.mhss.app.widget.widgetDarkColorScheme
import com.mhss.app.widget.widgetLightColorScheme
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TasksWidget : GlanceAppWidget(), KoinComponent {

    private val getSettings: GetPreferenceUseCase by inject()
    private val getWidgetTasks: GetWidgetTasksUseCase by inject()
    private val dateTimeFormatter: DateTimeFormatter by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
            val widgetPreferences = currentState<Preferences>()
            val backgroundOpacity = WidgetSettings.backgroundOpacity(widgetPreferences)
            val sortOrder by getSettings(
                intPreferencesKey(PrefsConstants.TASKS_ORDER_KEY),
                SortOrder.DueDate(SortType.ASC).toInt()
            ).collectAsState(SortOrder.DueDate(SortType.ASC).toInt())
            val showCompletedTasks by getSettings(
                booleanPreferencesKey(PrefsConstants.SHOW_COMPLETED_TASKS_KEY),
                false
            ).collectAsState(false)
            val useMaterialYou by getSettings(
                booleanPreferencesKey(PrefsConstants.SETTINGS_MATERIAL_YOU),
                false
            ).collectAsState(false)
            val isSystemDarkMode = remember {
                val currentNightMode =
                    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                currentNightMode == Configuration.UI_MODE_NIGHT_YES
            }
            val themeSetting by getSettings(
                intPreferencesKey(PrefsConstants.SETTINGS_THEME_KEY),
                ThemeSettings.AUTO.value
            ).collectAsState(ThemeSettings.AUTO.value)
            val isDarkMode = remember(themeSetting, isSystemDarkMode) {
                themeSetting == ThemeSettings.DARK.value ||
                    (themeSetting == ThemeSettings.AUTO.value && isSystemDarkMode)
            }
            val tasksFlow = remember(sortOrder, showCompletedTasks) {
                getWidgetTasks(sortOrder.toSortOrder(), showCompletedTasks)
            }
            val tasks by tasksFlow.collectAsState(emptyList())

            CompositionLocalProvider(
                LocalDateTimeFormatter provides dateTimeFormatter
            ) {
                WidgetTheme(
                    if (useMaterialYou) GlanceTheme.colors
                    else if (isDarkMode) ColorProviders(widgetDarkColorScheme)
                    else ColorProviders(widgetLightColorScheme)

                ) {
                    TasksHomeScreenWidget(
                        tasks = tasks,
                        backgroundOpacity = backgroundOpacity
                    )
                }
            }
        }
    }
}

class TasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TasksWidget()
}
