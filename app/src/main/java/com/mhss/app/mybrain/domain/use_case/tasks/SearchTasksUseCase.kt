package com.mhss.app.mybrain.domain.use_case.tasks

import com.mhss.app.mybrain.domain.model.tasks.Task
import com.mhss.app.mybrain.domain.repository.tasks.TaskRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class SearchTasksUseCase(
    private val tasksRepository: TaskRepository
) {
    operator fun invoke(query: String): Flow<List<Task>> {
        return tasksRepository.searchTasks(query)
    }
}