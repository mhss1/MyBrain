package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.TaskRepository
import org.koin.core.annotation.Single

@Single
class GetTaskByIdUseCase(
    private val tasksRepository: TaskRepository
) {
    suspend operator fun invoke(id: Int) = tasksRepository.getTaskById(id)
}