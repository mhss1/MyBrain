package com.mhss.app.mybrain.domain.use_case.tasks

import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.domain.repository.TaskRepository
import com.mhss.app.mybrain.util.settings.Order
import com.mhss.app.mybrain.util.settings.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAllTasksUseCase @Inject constructor(
    private val tasksRepository: TaskRepository
) {
    operator fun invoke(order: Order, showCompleted: Boolean = true): Flow<List<Task>> {
        return tasksRepository.getAllTasks().map { tasks ->
            when (order.orderType) {
                is OrderType.ASC -> {
                    when (order) {
                        is Order.Alphabetical -> tasks.sortedBy { it.title }
                        is Order.DateCreated -> tasks.sortedBy { it.createdDate }
                        is Order.DateModified -> tasks.sortedBy { it.updatedDate }
                        is Order.Priority -> tasks.sortedBy { it.priority }
                        is Order.DueDate -> tasks.sortedWith(compareBy({ it.dueDate == 0L }, { it.dueDate }))
                    }
                }
                is OrderType.DESC -> {
                    when (order) {
                        is Order.Alphabetical -> tasks.sortedByDescending { it.title }
                        is Order.DateCreated -> tasks.sortedByDescending { it.createdDate }
                        is Order.DateModified -> tasks.sortedByDescending { it.updatedDate }
                        is Order.Priority -> tasks.sortedByDescending { it.priority }
                        is Order.DueDate -> tasks.sortedWith(compareBy({ it.dueDate == 0L }, { it.dueDate })).reversed()
                    }
                }
            }
        }.map { list ->
            if (showCompleted)
                list
            else
                list.filter { !it.isCompleted }
        }
    }
}