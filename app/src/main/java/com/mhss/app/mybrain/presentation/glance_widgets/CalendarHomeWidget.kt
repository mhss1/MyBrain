package com.mhss.app.mybrain.presentation.glance_widgets

import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import com.mhss.app.mybrain.domain.use_case.calendar.GetAllEventsUseCase
import com.mhss.app.mybrain.domain.use_case.settings.GetSettingsUseCase
import com.mhss.app.mybrain.presentation.calendar.CalendarHomeScreenWidget
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.toIntList
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CalendarHomeWidget : GlanceAppWidget() {

    @Inject
    lateinit var getAllEventsUseCase: GetAllEventsUseCase
    @Inject
    lateinit var getSettings: GetSettingsUseCase

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val includedCalendars = getSettings(
            stringSetPreferencesKey(Constants.EXCLUDED_CALENDARS_KEY),
            emptySet()
        ).first()
        val events = getAllEventsUseCase(includedCalendars.toIntList(), true)

        provideContent {
            CalendarHomeScreenWidget(
                events
            )
        }
    }
}

@AndroidEntryPoint
class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalendarHomeWidget()
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }
}