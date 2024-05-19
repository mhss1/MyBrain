package com.mhss.app.mybrain.domain.use_case.tasks

import com.mhss.app.mybrain.domain.repository.TaskRepository
import com.mhss.app.mybrain.domain.use_case.alarm.DeleteAlarmUseCase
import org.koin.core.annotation.Single

@Single
class UpdateTaskCompletedUseCase(
    private val tasksRepository: TaskRepository,
    private val deleteAlarm: DeleteAlarmUseCase,
    ) {
    suspend operator fun invoke(taskId: Int, completed: Boolean) {
        tasksRepository.completeTask(taskId, completed)
        if (completed) {
            deleteAlarm(taskId)
        }
    }
}