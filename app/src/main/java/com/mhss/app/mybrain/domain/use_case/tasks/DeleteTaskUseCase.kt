package com.mhss.app.mybrain.domain.use_case.tasks

import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.domain.repository.TaskRepository
import com.mhss.app.mybrain.domain.use_case.alarm.DeleteAlarmUseCase
import org.koin.core.annotation.Single

@Single
class DeleteTaskUseCase(
    private val taskRepository: TaskRepository,
    private val deleteAlarm: DeleteAlarmUseCase
) {
    suspend operator fun invoke(task: Task) {
        taskRepository.deleteTask(task)
        if (task.dueDate != 0L)
            deleteAlarm(task.id)
    }
}