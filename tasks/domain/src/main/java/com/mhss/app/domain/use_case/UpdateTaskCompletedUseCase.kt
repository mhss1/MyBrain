package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.Task
import org.koin.core.annotation.Single

@Single
class UpdateTaskCompletedUseCase(
    private val upsertTask: UpsertTaskUseCase,
) {
    suspend operator fun invoke(task: Task, completed: Boolean) {
        upsertTask(
            task = task.copy(isCompleted = completed),
            previousTask = task,
            updateWidget = true
        )
    }
}
