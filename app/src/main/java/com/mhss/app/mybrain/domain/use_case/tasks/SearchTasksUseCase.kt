package com.mhss.app.mybrain.domain.use_case.tasks

import com.mhss.app.mybrain.domain.repository.TaskRepository
import javax.inject.Inject

class SearchTasksUseCase @Inject constructor(
    private val tasksRepository: TaskRepository
) {
    suspend operator fun invoke(query: String) = tasksRepository.searchTasks(query)
}