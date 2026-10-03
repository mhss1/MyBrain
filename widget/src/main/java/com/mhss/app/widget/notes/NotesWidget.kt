package com.mhss.app.widget.notes

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.material3.ColorProviders
import com.mhss.app.domain.use_case.GetWidgetNotesUseCase
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

class NotesWidget : GlanceAppWidget(), KoinComponent {

    private val getSettings: GetPreferenceUseCase by inject()
    private val getWidgetNotes: GetWidgetNotesUseCase by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
            val widgetPreferences = currentState<Preferences>()
            val backgroundOpacity = WidgetSettings.backgroundOpacity(widgetPreferences)
            val sortOrder by getSettings(
                intPreferencesKey(PrefsConstants.NOTES_ORDER_KEY),
                SortOrder.DateModified(SortType.DESC).toInt()
            ).collectAsState(SortOrder.DateModified(SortType.DESC).toInt())
            val useMaterialYou by getSettings(
                booleanPreferencesKey(PrefsConstants.SETTINGS_MATERIAL_YOU),
                false
            ).collectAsState(false)
            val showAllNotes by getSettings(
                booleanPreferencesKey(PrefsConstants.SHOW_ALL_NOTES_KEY),
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
            val notesFlow = remember(sortOrder, showAllNotes) {
                getWidgetNotes(sortOrder.toSortOrder(), showAllNotes)
            }
            val notes by notesFlow.collectAsState(emptyList())

            WidgetTheme(
                if (useMaterialYou) GlanceTheme.colors
                else if (isDarkMode) ColorProviders(widgetDarkColorScheme)
                else ColorProviders(widgetLightColorScheme)
            ) {
                NotesHomeScreenWidget(
                    notes = notes,
                    backgroundOpacity = backgroundOpacity
                )
            }
        }
    }
}

class NotesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NotesWidget()
}
