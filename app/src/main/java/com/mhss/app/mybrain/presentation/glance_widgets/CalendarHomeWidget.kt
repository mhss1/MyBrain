package com.mhss.app.mybrain.presentation.glance_widgets

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mhss.app.mybrain.domain.model.CalendarEvent
import com.mhss.app.mybrain.domain.use_case.calendar.GetAllEventsUseCase
import com.mhss.app.mybrain.domain.use_case.settings.GetSettingsUseCase
import com.mhss.app.mybrain.presentation.calendar.CalendarHomeScreenWidget
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.toIntList
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class CalendarHomeWidget : GlanceAppWidget() {

    @Composable
    override fun Content() {
        val gson = Gson()
        val type = object: TypeToken<Map<String, List<CalendarEvent>>>() {}.type
        val prefs = currentState<Preferences>()
        val events: Map<String, List<CalendarEvent>> = gson.fromJson(prefs[stringPreferencesKey("events")], type) ?: emptyMap()
        val hasPermission = prefs[booleanPreferencesKey("hasPermission")] ?: false
        CalendarHomeScreenWidget(
            events,
            hasPermission
        )
    }
}

@AndroidEntryPoint
class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalendarHomeWidget()
    private val coroutineScope = MainScope()

    @Inject
    lateinit var getAllEventsUseCase: GetAllEventsUseCase
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
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val gson = Gson()
                val type = object: TypeToken<Map<String, List<CalendarEvent>>>() {}.type
                val includedCalendars = getSettings(
                    stringSetPreferencesKey(Constants.EXCLUDED_CALENDARS_KEY),
                    emptySet()
                ).first()
                val events = gson.toJson(getAllEventsUseCase(includedCalendars.toIntList()), type)
                val glanceId =
                    GlanceAppWidgetManager(context).getGlanceIds(CalendarHomeWidget::class.java).firstOrNull()

                glanceId?.let {
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, it) { pref ->
                        pref.toMutablePreferences().apply {
                            this[booleanPreferencesKey("hasPermission")] = true
                            this[stringPreferencesKey("events")] = events
                        }
                    }
                    glanceAppWidget.update(context, it)
                }
            } else {
                val glanceId =
                    GlanceAppWidgetManager(context).getGlanceIds(CalendarHomeWidget::class.java).firstOrNull()

                glanceId?.let {
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, it) { pref ->
                        pref.toMutablePreferences().apply {
                            this[booleanPreferencesKey("hasPermission")] = false
                        }
                    }
                    glanceAppWidget.update(context, it)
                }
            }
        }
    }

}