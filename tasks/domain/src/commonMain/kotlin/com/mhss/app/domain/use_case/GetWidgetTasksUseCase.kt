package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.TaskRepository
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.widget.WIDGET_ITEM_LIMIT
import org.koin.core.annotation.Factory

@Factory
class GetWidgetTasksUseCase(
    private val tasksRepository: TaskRepository
) {
    operator fun invoke(sortOrder: SortOrder, showCompleted: Boolean) =
        tasksRepository.getLimitedTasks(sortOrder, showCompleted, WIDGET_ITEM_LIMIT)
}
