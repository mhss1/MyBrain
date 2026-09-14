package com.mhss.app.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.RawQuery
import androidx.room3.RoomRawQuery
import androidx.room3.Update
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.mhss.app.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface TaskDao {

    @RawQuery(observedEntities = [TaskEntity::class])
    fun pageTasks(query: RoomRawQuery): PagingSource<Int, TaskEntity>

    fun getPagedTasks(orderBy: TaskOrder, order: QueryOrder, showCompleted: Boolean): PagingSource<Int, TaskEntity> =
        pageTasks(taskQuery(orderBy, order, showCompleted))

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' ORDER BY updated_date DESC")
    fun searchPagedTasks(query: String): PagingSource<Int, TaskEntity>

    @RawQuery(observedEntities = [TaskEntity::class])
    fun observeTasks(query: RoomRawQuery): Flow<List<TaskEntity>>

    fun getAllTasks(
        orderBy: TaskOrder,
        order: QueryOrder,
        showCompleted: Boolean
    ): Flow<List<TaskEntity>> {
        return observeTasks(taskQuery(orderBy, order, showCompleted))
    }

    fun getLimitedTasks(
        orderBy: TaskOrder,
        order: QueryOrder,
        showCompleted: Boolean,
        limit: Int
    ): Flow<List<TaskEntity>> = observeTasks(taskQuery(orderBy, order, showCompleted, limit))

    @Query("SELECT * FROM tasks")
    suspend fun getAllFullTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTask(id: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id IN (:ids)")
    suspend fun getTasksByIds(ids: List<String>): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE updated_date > :timestamp")
    suspend fun getTasksUpdatedAfter(timestamp: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE sync_seq > :seq AND sync_seq <= :maxSeq")
    suspend fun getTasksAfterSeq(seq: Long, maxSeq: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE alarmId = :alarmId")
    suspend fun getTaskByAlarm(alarmId: Int): TaskEntity?

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :title || '%'")
    fun getTasksByTitle(title: String): Flow<List<TaskEntity>>

    @Upsert
    suspend fun upsertTask(task: TaskEntity)

    @Upsert
    suspend fun upsertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("UPDATE tasks SET is_completed = :completed, sync_seq = :syncSeq, updated_date = :updatedDate WHERE id = :id")
    suspend fun updateCompleted(id: String, completed: Boolean, syncSeq: Long, updatedDate: Long)

}

enum class TaskOrder(val column: String) {
    TITLE("title"),
    CREATED_DATE("created_date"),
    UPDATED_DATE("updated_date"),
    PRIORITY("priority"),
    DUE_DATE("dueDate"),
    COMPLETED("is_completed")
}

private fun taskQuery(
    orderBy: TaskOrder,
    order: QueryOrder,
    showCompleted: Boolean,
    limit: Int? = null
): RoomRawQuery {
    val where = if (showCompleted) "" else " WHERE is_completed = 0"
    val orderBySql = when (orderBy) {
    TaskOrder.TITLE -> "title COLLATE NOCASE ${order.sql}"
    TaskOrder.DUE_DATE -> "dueDate ${order.sql} NULLS LAST"
    else -> "${orderBy.column} ${order.sql}"
    }
    val limitSql = limit?.let {
        require(it >= 0)
        " LIMIT $it"
    }.orEmpty()
    return RoomRawQuery("SELECT * FROM tasks$where ORDER BY $orderBySql$limitSql")
}
