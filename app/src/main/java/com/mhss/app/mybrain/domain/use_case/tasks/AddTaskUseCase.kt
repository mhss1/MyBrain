package com.mhss.app.mybrain.domain.use_case.tasks

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.mhss.app.mybrain.domain.model.Alarm
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.domain.repository.TaskRepository
import com.mhss.app.mybrain.domain.use_case.alarm.AddAlarmUseCase
import com.mhss.app.mybrain.presentation.glance_widgets.TasksHomeWidget
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(
    private val tasksRepository: TaskRepository,
    private val addAlarm: AddAlarmUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val context: Context
) {
    suspend operator fun invoke(task: Task): Boolean {
        val id = tasksRepository.insertTask(task).toInt()
        TasksHomeWidget().updateAll(context)
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