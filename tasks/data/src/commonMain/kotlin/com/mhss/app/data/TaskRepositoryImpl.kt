package com.mhss.app.data

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import com.mhss.app.database.DefaultPagingConfig
import com.mhss.app.database.dao.QueryOrder
import com.mhss.app.database.dao.SyncDao
import com.mhss.app.database.dao.TaskDao
import com.mhss.app.database.dao.TaskOrder
import com.mhss.app.database.dao.incrementAndGet
import com.mhss.app.database.entity.DeletedEntityEntity
import com.mhss.app.database.entity.DeletedEntityType
import com.mhss.app.database.entity.toTask
import com.mhss.app.database.entity.toTaskEntity
import com.mhss.app.database.helpers.DatabaseTransactionProvider
import com.mhss.app.database.sync.LocalChangeObserver
import com.mhss.app.datetime.now
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.model.TaskSummary
import com.mhss.app.domain.repository.TaskRepository
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import kotlin.uuid.Uuid
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
    private val syncDao: SyncDao,
    private val changeObserver: LocalChangeObserver,
    private val transactionProvider: DatabaseTransactionProvider,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : TaskRepository {

    override fun getPagedTasks(sortOrder: SortOrder, showCompleted: Boolean): Flow<PagingData<Task>> {
        val sortOrderBy = when (sortOrder) {
            is SortOrder.Alphabetical -> TaskOrder.TITLE
            is SortOrder.DateCreated -> TaskOrder.CREATED_DATE
            is SortOrder.DateModified -> TaskOrder.UPDATED_DATE
            is SortOrder.Priority -> TaskOrder.PRIORITY
            is SortOrder.DueDate -> TaskOrder.DUE_DATE
            is SortOrder.Done -> TaskOrder.COMPLETED
        }
        return Pager(config = DefaultPagingConfig) {
            taskDao.getPagedTasks(sortOrderBy, sortOrder.sortType.toQueryOrder(), showCompleted)
        }.flow.map { page -> page.map { it.toTask() } }
    }

    override fun searchPagedTasks(query: String): Flow<PagingData<Task>> =
        Pager(config = DefaultPagingConfig) {
            taskDao.searchPagedTasks(query)
        }.flow.map { page -> page.map { it.toTask() } }

    override fun getAllTasks(sortOrder: SortOrder, showCompleted: Boolean): Flow<List<Task>> {
        val sortOrderBy = when (sortOrder) {
            is SortOrder.Alphabetical -> TaskOrder.TITLE
            is SortOrder.DateCreated -> TaskOrder.CREATED_DATE
            is SortOrder.DateModified -> TaskOrder.UPDATED_DATE
            is SortOrder.Priority -> TaskOrder.PRIORITY
            is SortOrder.DueDate -> TaskOrder.DUE_DATE
            is SortOrder.Done -> TaskOrder.COMPLETED
        }
        return taskDao.getAllTasks(sortOrderBy, sortOrder.sortType.toQueryOrder(), showCompleted)
            .map { tasks -> tasks.map { it.toTask() } }
            .flowOn(ioDispatcher)
    }

    override fun getLimitedTasks(
        sortOrder: SortOrder,
        showCompleted: Boolean,
        limit: Int
    ): Flow<List<Task>> {
        val sortOrderBy = when (sortOrder) {
            is SortOrder.Alphabetical -> TaskOrder.TITLE
            is SortOrder.DateCreated -> TaskOrder.CREATED_DATE
            is SortOrder.DateModified -> TaskOrder.UPDATED_DATE
            is SortOrder.Priority -> TaskOrder.PRIORITY
            is SortOrder.DueDate -> TaskOrder.DUE_DATE
            is SortOrder.Done -> TaskOrder.COMPLETED
        }
        return taskDao.getLimitedTasks(sortOrderBy, sortOrder.sortType.toQueryOrder(), showCompleted, limit)
            .map { tasks -> tasks.map { it.toTask() } }
            .flowOn(ioDispatcher)
    }

    override fun getTaskSummary(createdAfter: Long): Flow<TaskSummary> =
        taskDao.getTaskSummary(createdAfter).flowOn(ioDispatcher)

    override suspend fun getTaskById(id: String): Task? {
        return withContext(ioDispatcher) {
            taskDao.getTask(id)?.toTask()
        }
    }

    override suspend fun getTaskByAlarm(alarmId: Int): Task? {
        return withContext(ioDispatcher) {
            taskDao.getTaskByAlarm(alarmId)?.toTask()
        }
    }

    override fun searchTasks(title: String): Flow<List<Task>> {
        return taskDao.getTasksByTitle(title)
            .flowOn(ioDispatcher)
            .map { tasks ->
                tasks.map { it.toTask() }
            }
    }

    override suspend fun upsertTask(task: Task, notifyChange: Boolean) {
        return withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                taskDao.upsertTask(task.toTaskEntity(syncSeq = syncDao.incrementAndGet()))
            }
            if (notifyChange) changeObserver.notifyChange()
        }
    }

    override suspend fun upsertTasks(tasks: List<Task>, notifyChange: Boolean) {
        withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                val stamped = tasks.map {
                    it.toTaskEntity(syncSeq = syncDao.incrementAndGet())
                }
                taskDao.upsertTasks(stamped)
            }
            if (notifyChange) changeObserver.notifyChange()
        }
    }

    override suspend fun updateTask(task: Task) {
        withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                taskDao.updateTask(task.toTaskEntity(syncSeq = syncDao.incrementAndGet()))
            }
            changeObserver.notifyChange()
        }
    }

    override suspend fun completeTask(id: String, completed: Boolean) {
        withContext(ioDispatcher) {
            val nowTime = now()
            transactionProvider.runInTransaction {
                taskDao.updateCompleted(id, completed, syncDao.incrementAndGet(), nowTime)
            }
            changeObserver.notifyChange()
        }
    }

    override suspend fun deleteTask(task: Task) {
        withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                taskDao.deleteTask(task.toTaskEntity())
                syncDao.insertDeletedEntity(
                    DeletedEntityEntity(
                        id = Uuid.generateV7().toString(),
                        entityId = task.id,
                        entityType = DeletedEntityType.TASK.key,
                        deletedAt = now(),
                        syncSeq = syncDao.incrementAndGet()
                    )
                )
            }
            changeObserver.notifyChange()
        }
    }
}

private fun SortType.toQueryOrder() = when (this) {
    SortType.ASC -> QueryOrder.ASC
    SortType.DESC -> QueryOrder.DESC
}
