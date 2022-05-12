package com.mhss.app.mybrain.data.repository

import com.mhss.app.mybrain.data.local.dao.TaskDao
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TaskRepositoryImpl (
    private val taskDao: TaskDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TaskRepository {

    override suspend fun getAllTasks(): List<Task> {
        return withContext(ioDispatcher) {
            taskDao.getAllTasks()
        }
    }

    override suspend fun getTaskById(id: Int): Task {
        return withContext(ioDispatcher) {
            taskDao.getTask(id)
        }
    }

    override suspend fun searchTasks(title: String): List<Task> {
        return withContext(ioDispatcher) {
            taskDao.getTasksByTitle(title)
        }
    }

    override suspend fun insertTask(task: Task) {
        withContext(ioDispatcher) {
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
