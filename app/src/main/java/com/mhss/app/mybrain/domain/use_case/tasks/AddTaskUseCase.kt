package com.mhss.app.mybrain.domain.use_case.tasks

import com.mhss.app.mybrain.domain.model.Alarm
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.domain.repository.TaskRepository
import com.mhss.app.mybrain.domain.repository.WidgetUpdater
import com.mhss.app.mybrain.domain.use_case.alarm.AddAlarmUseCase
import org.koin.core.annotation.Single

@Single
class AddTaskUseCase(
    private val tasksRepository: TaskRepository,
    private val addAlarm: AddAlarmUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val widgetUpdater: WidgetUpdater
) {
    suspend operator fun invoke(task: Task): Boolean {
        val id = tasksRepository.insertTask(task).toInt()
        widgetUpdater.updateAll(WidgetUpdater.WidgetType.Tasks)
        return if (task.dueDate != 0L){
            val success = addAlarm(
                Alarm(
                    id,
                    task.dueDate,
                )
            )
            if (!success) updateTask(task.copy(id = id, dueDate = 0L), task)
            success
        } else true
    }
}