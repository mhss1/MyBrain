package com.mhss.app.mybrain.domain.use_case.tasks

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.mhss.app.mybrain.domain.model.Alarm
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.domain.repository.TaskRepository
import com.mhss.app.mybrain.domain.use_case.alarm.AddAlarmUseCase
import com.mhss.app.mybrain.domain.use_case.alarm.DeleteAlarmUseCase
import com.mhss.app.mybrain.presentation.glance_widgets.TasksHomeWidget
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val tasksRepository: TaskRepository,
    private val addAlarm: AddAlarmUseCase,
    private val deleteAlarm: DeleteAlarmUseCase,
    private val context: Context
) {
    suspend operator fun invoke(task: Task, oldTask: Task): Boolean {
        tasksRepository.updateTask(task)
        TasksHomeWidget().updateAll(context)
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