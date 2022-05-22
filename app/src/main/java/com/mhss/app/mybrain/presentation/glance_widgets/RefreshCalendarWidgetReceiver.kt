package com.mhss.app.mybrain.presentation.glance_widgets

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mhss.app.mybrain.domain.model.CalendarEvent
import com.mhss.app.mybrain.domain.use_case.calendar.GetAllEventsUseCase
import com.mhss.app.mybrain.domain.use_case.settings.GetSettingsUseCase
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.toIntList
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class RefreshCalendarWidgetReceiver : BroadcastReceiver() {

    @Inject
    lateinit var getAllEventsUseCase: GetAllEventsUseCase
    @Inject
    lateinit var getSettings: GetSettingsUseCase

    override fun onReceive(context: Context, intent: Intent) {
         runBlocking {
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
                             this[stringPreferencesKey("events")] =
                                 events
                         }
                     }
                     CalendarHomeWidget().update(context, it)
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
                     CalendarHomeWidget().update(context, it)
                 }
             }
        }
    }
}