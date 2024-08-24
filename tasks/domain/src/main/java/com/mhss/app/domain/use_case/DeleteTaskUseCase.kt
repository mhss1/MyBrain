package com.mhss.app.domain.use_case

import com.mhss.app.alarm.use_case.DeleteAlarmUseCase
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.repository.TaskRepository
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