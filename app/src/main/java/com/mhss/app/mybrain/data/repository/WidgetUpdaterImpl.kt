package com.mhss.app.mybrain.data.repository

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.mhss.app.mybrain.domain.repository.WidgetUpdater
import com.mhss.app.mybrain.presentation.glance_widgets.CalendarHomeWidget
import com.mhss.app.mybrain.presentation.glance_widgets.TasksHomeWidget
import org.koin.core.annotation.Single

@Single
class WidgetUpdaterImpl(
    private val context: Context
): WidgetUpdater {
    override suspend fun updateAll(type: WidgetUpdater.WidgetType) {
        when (type) {
            WidgetUpdater.WidgetType.Calendar -> {
                CalendarHomeWidget().updateAll(context)
            }
            WidgetUpdater.WidgetType.Tasks -> {
                TasksHomeWidget().updateAll(context)
            }
        }
    }

}