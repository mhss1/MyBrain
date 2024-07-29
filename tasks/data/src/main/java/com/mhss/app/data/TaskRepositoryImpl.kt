package com.mhss.app.data

import com.mhss.app.database.dao.TaskDao
import com.mhss.app.database.entity.toTask
import com.mhss.app.database.entity.toTaskEntity
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
            .flowOn(ioDispatcher)
            .map { tasks ->
                tasks.map { it.toTask() }
            }
    }

    override suspend fun getTaskById(id: Int): Task {
        return withContext(ioDispatcher) {
            taskDao.getTask(id).toTask()
        }
    }

    override fun searchTasks(title: String): Flow<List<Task>> {
        return taskDao.getTasksByTitle(title)
            .flowOn(ioDispatcher)
            .map { tasks ->
            tasks.map { it.toTask() }
        }
    }

    override suspend fun insertTask(task: Task): Long {
        return withContext(ioDispatcher) {
            taskDao.insertTask(task.toTaskEntity())
        }
    }

    override suspend fun updateTask(task: Task) {
        withContext(ioDispatcher) {
            taskDao.updateTask(task.toTaskEntity())
        }
    }

    override suspend fun completeTask(id: Int, completed: Boolean) {
        withContext(ioDispatcher) {
            taskDao.updateCompleted(id, completed)
        }
    }

    override suspend fun deleteTask(task: Task) {
        withContext(ioDispatcher) {
            taskDao.deleteTask(task.toTaskEntity())
        }
    }

}
