package com.mhss.app.mybrain.domain.use_case.tasks

import com.mhss.app.mybrain.domain.model.Alarm
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.domain.repository.TaskRepository
import com.mhss.app.mybrain.domain.repository.WidgetUpdater
import com.mhss.app.mybrain.domain.use_case.alarm.AddAlarmUseCase
import com.mhss.app.mybrain.domain.use_case.alarm.DeleteAlarmUseCase
import org.koin.core.annotation.Single

@Single
class UpdateTaskUseCase(
    private val tasksRepository: TaskRepository,
    private val addAlarm: AddAlarmUseCase,
    private val deleteAlarm: DeleteAlarmUseCase,
    private val widgetUpdater: WidgetUpdater
) {
    suspend operator fun invoke(task: Task, oldTask: Task): Boolean {
        tasksRepository.updateTask(task)
        widgetUpdater.updateAll(WidgetUpdater.WidgetType.Tasks)
        return if (task.dueDate != oldTask.dueDate) {
            if (task.dueDate != 0L) {
                val scheduleSuccess = addAlarm(
                    Alarm(
                        task.id,
                        task.dueDate
                    )
                )
                scheduleSuccess
            } else {
                deleteAlarm(task.id)
                true
            }
        } else true
    }
}