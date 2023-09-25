package com.mhss.app.mybrain.domain.use_case.tasks

import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.domain.repository.TaskRepository
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(
    private val tasksRepository: TaskRepository,
) {
    suspend operator fun invoke(task: Task): Long {
        return tasksRepository.insertTask(task)
    }
}