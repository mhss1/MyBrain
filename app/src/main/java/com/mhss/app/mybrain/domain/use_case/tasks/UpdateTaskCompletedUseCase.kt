package com.mhss.app.mybrain.domain.use_case.tasks

import com.mhss.app.mybrain.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskCompletedUseCase @Inject constructor(
    private val tasksRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int, completed: Boolean) = tasksRepository.completeTask(taskId, completed)
}