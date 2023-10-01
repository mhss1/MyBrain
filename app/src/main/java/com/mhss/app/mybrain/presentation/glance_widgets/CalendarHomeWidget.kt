package com.mhss.app.mybrain.presentation.glance_widgets

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import com.mhss.app.mybrain.di.CalendarWidgetEntryPoint
import com.mhss.app.mybrain.presentation.calendar.CalendarHomeScreenWidget
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.toIntList
import dagger.hilt.EntryPoints
import kotlinx.coroutines.flow.first

class CalendarHomeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val entryPoint = EntryPoints.get(context, CalendarWidgetEntryPoint::class.java)
        val includedCalendars = entryPoint.getSettingsUseCase().invoke(
            stringSetPreferencesKey(Constants.EXCLUDED_CALENDARS_KEY),
            emptySet()
        ).first()
        val events  = entryPoint.getAllEventsUseCase().invoke(includedCalendars.toIntList(), true)

        provideContent {
            val hasPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.READ_CALENDAR
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                )
            }

            CalendarHomeScreenWidget(
                events,
                hasPermission
            )
        }
    }
}

class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalendarHomeWidget()


}