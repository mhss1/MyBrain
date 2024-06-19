package com.mhss.app.mybrain.data.repository

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.mhss.app.mybrain.domain.repository.widget.WidgetUpdater
import com.mhss.app.mybrain.presentation.glance_widgets.calendar.CalendarWidget
import com.mhss.app.mybrain.presentation.glance_widgets.tasks.TasksWidget
import org.koin.core.annotation.Single

@Single
class WidgetUpdaterImpl(
    private val context: Context
): WidgetUpdater {
    override suspend fun updateAll(type: WidgetUpdater.WidgetType) {
        when (type) {
            WidgetUpdater.WidgetType.Calendar -> {
                CalendarWidget().updateAll(context)
            }
            WidgetUpdater.WidgetType.Tasks -> {
                TasksWidget().updateAll(context)
            }
        }
    }

}