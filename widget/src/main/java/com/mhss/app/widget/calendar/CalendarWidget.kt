package com.mhss.app.widget.calendar

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.material3.ColorProviders
import com.mhss.app.util.Constants
import com.mhss.app.domain.use_case.GetAllEventsUseCase
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.widget.WidgetTheme
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.stringSetPreferencesKey
import com.mhss.app.ui.toIntList
import com.mhss.app.util.date.formatDateForMapping
import com.mhss.app.widget.widgetDarkColorScheme
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CalendarWidget : GlanceAppWidget(), KoinComponent {

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
            val useMaterialYou by getSettings(
                booleanPreferencesKey(Constants.SETTINGS_MATERIAL_YOU),
                false
            ).collectAsState(false)

            val hasPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.READ_CALENDAR
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                )
            }

            WidgetTheme(
                if (useMaterialYou) {
                    GlanceTheme.colors
                } else ColorProviders(widgetDarkColorScheme)
            ) {
                CalendarHomeScreenWidget(
                    events,
                    hasPermission
                )
            }

        }
    }
}

class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalendarWidget()
}