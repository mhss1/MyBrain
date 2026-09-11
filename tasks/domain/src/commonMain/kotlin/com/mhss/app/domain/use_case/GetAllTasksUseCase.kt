package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.Task
import com.mhss.app.domain.repository.TaskRepository
import com.mhss.app.preferences.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetAllTasksUseCase(
    private val tasksRepository: TaskRepository
) {
    operator fun invoke(sortOrder: SortOrder, showCompleted: Boolean = true): Flow<List<Task>> {
        return tasksRepository.getAllTasks(sortOrder, showCompleted)
    }
}
