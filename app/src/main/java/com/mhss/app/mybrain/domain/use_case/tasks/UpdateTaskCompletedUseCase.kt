package com.mhss.app.mybrain.domain.use_case.tasks

import com.mhss.app.mybrain.domain.repository.tasks.TaskRepository
import com.mhss.app.mybrain.domain.repository.widget.WidgetUpdater
import com.mhss.app.mybrain.domain.use_case.alarm.DeleteAlarmUseCase
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