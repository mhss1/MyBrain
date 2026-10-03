package com.mhss.app.widget.calendar

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.material3.ColorProviders
import com.mhss.app.datetime.DateTimeFormatter
import com.mhss.app.datetime.LocalDateTimeFormatter
import com.mhss.app.domain.use_case.GetAllEventsResult
import com.mhss.app.domain.use_case.GetWidgetEventsUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.stringSetPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.ui.ThemeSettings
import com.mhss.app.ui.toIntList
import com.mhss.app.widget.WidgetSettings
import com.mhss.app.widget.WidgetTheme
import com.mhss.app.widget.widgetDarkColorScheme
import com.mhss.app.widget.widgetLightColorScheme
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CalendarWidget : GlanceAppWidget(), KoinComponent {

    private val getSettings: GetPreferenceUseCase by inject()
    private val getWidgetEvents: GetWidgetEventsUseCase by inject()
    private val dateTimeFormatter: DateTimeFormatter by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val includedCalendars = getSettings(
            stringSetPreferencesKey(PrefsConstants.EXCLUDED_CALENDARS_KEY),
            emptySet()
        ).first()
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val events = if (hasPermission) {
            getWidgetEvents(includedCalendars.toIntList())
        } else {
            GetAllEventsResult(emptyList(), emptyList())
        }

        provideContent {
            val widgetPreferences = currentState<Preferences>()
            val backgroundOpacity = WidgetSettings.backgroundOpacity(widgetPreferences)
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

            CompositionLocalProvider(
                LocalDateTimeFormatter provides dateTimeFormatter
            ) {
                WidgetTheme(
                    if (useMaterialYou) GlanceTheme.colors
                    else if (isDarkMode) ColorProviders(widgetDarkColorScheme)
                    else ColorProviders(widgetLightColorScheme)
                ) {
                    CalendarHomeScreenWidget(
                        events.eventDays,
                        hasPermission,
                        backgroundOpacity
                    )
                }
            }

        }
    }
}

class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalendarWidget()
}
