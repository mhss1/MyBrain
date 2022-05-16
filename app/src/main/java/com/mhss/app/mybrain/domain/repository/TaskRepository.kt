package com.mhss.app.mybrain.domain.repository

import com.mhss.app.mybrain.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    fun getAllTasks(): Flow<List<Task>>

    suspend fun getTaskById(id: Int): Task

    fun searchTasks(title: String): Flow<List<Task>>

    suspend fun insertTask(task: Task): Long

    suspend fun updateTask(task: Task)

    suspend fun completeTask(id: Int, completed: Boolean)

    suspend fun deleteTask(task: Task)

}