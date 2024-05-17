package com.mhss.app.mybrain.data.repository

import com.mhss.app.mybrain.data.local.dao.TaskDao
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val ioDispatcher: CoroutineDispatcher
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }

    override suspend fun getTaskById(id: Int): Task {
        return withContext(ioDispatcher) {
            taskDao.getTask(id)
        }
    }

    override fun searchTasks(title: String): Flow<List<Task>> {
        return taskDao.getTasksByTitle(title)
    }

    override suspend fun insertTask(task: Task): Long {
        return withContext(ioDispatcher) {
            taskDao.insertTask(task)
        }
    }

    override suspend fun updateTask(task: Task) {
        withContext(ioDispatcher) {
            taskDao.updateTask(task)
        }
    }

    override suspend fun completeTask(id: Int, completed: Boolean) {
        withContext(ioDispatcher) {
            taskDao.updateCompleted(id, completed)
        }
    }

    override suspend fun deleteTask(task: Task) {
        withContext(ioDispatcher) {
            taskDao.deleteTask(task)
        }
    }

}
