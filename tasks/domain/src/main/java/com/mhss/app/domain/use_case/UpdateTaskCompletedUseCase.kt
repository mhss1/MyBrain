package com.mhss.app.domain.use_case

import com.mhss.app.alarm.use_case.DeleteAlarmUseCase
import com.mhss.app.domain.repository.TaskRepository
import com.mhss.app.widget.WidgetUpdater
import org.koin.core.annotation.Single

@Single
class UpdateTaskCompletedUseCase(
    private val tasksRepository: TaskRepository,
    private val deleteAlarm: DeleteAlarmUseCase,
    private val widgetUpdater: WidgetUpdater
) {
    suspend operator fun invoke(taskId: Int, completed: Boolean) {
        tasksRepository.completeTask(taskId, completed)
        if (completed) {
            deleteAlarm(taskId)
        }
        widgetUpdater.updateAll(WidgetUpdater.WidgetType.Tasks)
    }
}