package com.mhss.app.domain.use_case

import com.mhss.app.datetime.lastWeekCutoff
import com.mhss.app.domain.repository.TaskRepository
import org.koin.core.annotation.Single

@Single
class GetTaskSummaryUseCase(
    private val tasksRepository: TaskRepository
) {
    operator fun invoke() = tasksRepository.getTaskSummary(lastWeekCutoff())
}
