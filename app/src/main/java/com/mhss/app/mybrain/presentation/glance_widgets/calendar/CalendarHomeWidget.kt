package com.mhss.app.mybrain.presentation.glance_widgets.calendar

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.mhss.app.mybrain.domain.model.preferences.stringSetPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import com.mhss.app.mybrain.domain.use_case.calendar.GetAllEventsUseCase
import com.mhss.app.mybrain.domain.use_case.settings.GetPreferenceUseCase
import com.mhss.app.mybrain.presentation.calendar.CalendarHomeScreenWidget
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.date.formatDateForMapping
import com.mhss.app.mybrain.util.settings.toIntList
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CalendarHomeWidget : GlanceAppWidget(), KoinComponent {

    private val getSettings: GetPreferenceUseCase by inject()
    private val getAllEvents: GetAllEventsUseCase by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val includedCalendars = getSettings(
            stringSetPreferencesKey(Constants.EXCLUDED_CALENDARS_KEY),
            emptySet()
        ).first()
        val events  = getAllEvents(includedCalendars.toIntList(), true) {
            it.start.formatDateForMapping()
        }

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