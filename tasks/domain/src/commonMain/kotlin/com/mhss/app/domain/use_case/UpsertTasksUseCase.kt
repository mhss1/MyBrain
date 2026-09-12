package com.mhss.app.domain.use_case

import com.mhss.app.alarm.use_case.UpsertAlarmUseCase
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.repository.TaskRepository
import com.mhss.app.widget.WidgetUpdater
import org.koin.core.annotation.Factory
import kotlin.time.Clock.System.now

@Factory
class UpsertTasksUseCase(
    private val tasksRepository: TaskRepository,
    private val upsertAlarm: UpsertAlarmUseCase,
    private val widgetUpdater: WidgetUpdater
) {
    suspend operator fun invoke(
        tasks: List<Task>,
        updateWidget: Boolean = true,
        notifySyncChanges: Boolean = true
    ) {
        val nowMillis = now().toEpochMilliseconds()
        val finalTasks = tasks.map { task ->
            if (task.dueDate != null && task.dueDate > nowMillis) {
                val alarmId = upsertAlarm(task.alarmId ?: 0, task.dueDate)
                task.copy(alarmId = alarmId)
            } else {
                task
            }
        }

        tasksRepository.upsertTasks(finalTasks, notifyChange = notifySyncChanges)
        if (updateWidget) widgetUpdater.updateAll(WidgetUpdater.WidgetType.Tasks)
    }
}
