package com.mhss.app.domain.repository

import com.mhss.app.domain.model.Task
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    fun getAllTasks(
        sortOrder: SortOrder = SortOrder.DueDate(SortType.ASC),
        showCompleted: Boolean = true
    ): Flow<List<Task>>

    suspend fun getTaskById(id: String): Task?

    suspend fun getTaskByAlarm(alarmId: Int): Task?

    fun searchTasks(title: String): Flow<List<Task>>

    suspend fun upsertTask(task: Task, notifyChange: Boolean = true)

    suspend fun upsertTasks(tasks: List<Task>, notifyChange: Boolean = true)

    suspend fun updateTask(task: Task)

    suspend fun completeTask(id: String, completed: Boolean)

    suspend fun deleteTask(task: Task)

}
