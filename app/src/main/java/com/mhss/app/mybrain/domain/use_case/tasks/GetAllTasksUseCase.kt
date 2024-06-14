package com.mhss.app.mybrain.domain.use_case.tasks

import com.mhss.app.mybrain.di.namedDefaultDispatcher
import com.mhss.app.mybrain.domain.model.tasks.Task
import com.mhss.app.mybrain.domain.repository.tasks.TaskRepository
import com.mhss.app.mybrain.domain.model.preferences.Order
import com.mhss.app.mybrain.domain.model.preferences.OrderType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class GetAllTasksUseCase(
    private val tasksRepository: TaskRepository,
    @Named(namedDefaultDispatcher) private val defaultDispatcher: CoroutineDispatcher
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
        }.flowOn(defaultDispatcher)
    }
}