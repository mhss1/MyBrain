package com.mhss.app.database.dao

import androidx.paging.PagingSource
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.mhss.app.database.entity.AssistantMessageEntity
import com.mhss.app.database.entity.AssistantThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface AssistantDao {

    @Query("SELECT * FROM assistant_threads ORDER BY updated_at DESC")
    fun getAllThreads(): Flow<List<AssistantThreadEntity>>

    @Query("SELECT * FROM assistant_threads ORDER BY updated_at DESC")
    fun getPagedThreads(): PagingSource<Int, AssistantThreadEntity>

    @Upsert
    suspend fun upsertThread(thread: AssistantThreadEntity)

    @Query("UPDATE assistant_threads SET updated_at = :updatedAt WHERE id = :threadId")
    suspend fun updateThreadLastActive(threadId: String, updatedAt: Long)

    @Query("DELETE FROM assistant_threads WHERE id = :threadId")
    suspend fun deleteThread(threadId: String)

    @Query("DELETE FROM assistant_messages WHERE thread_id = :threadId")
    suspend fun deleteMessagesByThreadId(threadId: String)

    @Query("DELETE FROM assistant_threads")
    suspend fun deleteAllThreads()

    @Query("DELETE FROM assistant_messages")
    suspend fun deleteAllMessages()

    @Transaction
    suspend fun deleteThreadAndMessages(threadId: String) {
        deleteMessagesByThreadId(threadId)
        deleteThread(threadId)
    }

    @Transaction
    suspend fun deleteAllThreadsAndMessages() {
        deleteAllMessages()
        deleteAllThreads()
    }

    @Query("SELECT * FROM assistant_messages WHERE thread_id = :threadId ORDER BY created_at DESC")
    fun getMessagesByThreadId(threadId: String): Flow<List<AssistantMessageEntity>>

    @Upsert
    suspend fun upsertMessage(message: AssistantMessageEntity)

    @Transaction
    suspend fun insertMessageAndUpdateThread(message: AssistantMessageEntity) {
        upsertMessage(message)
        updateThreadLastActive(message.threadId, message.createdAt)
    }

    @Query("DELETE FROM assistant_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("SELECT id FROM assistant_threads")
    suspend fun getAllThreadIds(): List<String>

    @Query("SELECT * FROM assistant_threads WHERE id IN (:ids)")
    suspend fun getThreadsByIds(ids: List<String>): List<AssistantThreadEntity>

    @Query("SELECT * FROM assistant_threads WHERE updated_at > :timestamp")
    suspend fun getThreadsUpdatedAfter(timestamp: Long): List<AssistantThreadEntity>

    @Query("SELECT * FROM assistant_threads WHERE sync_seq > :seq AND sync_seq <= :maxSeq")
    suspend fun getThreadsAfterSeq(seq: Long, maxSeq: Long): List<AssistantThreadEntity>

    @Query("SELECT * FROM assistant_messages WHERE id IN (:ids)")
    suspend fun getMessagesByIds(ids: List<String>): List<AssistantMessageEntity>

    @Query("SELECT * FROM assistant_messages WHERE created_at > :timestamp")
    suspend fun getMessagesCreatedAfter(timestamp: Long): List<AssistantMessageEntity>

    @Query("SELECT * FROM assistant_messages WHERE sync_seq > :seq AND sync_seq <= :maxSeq")
    suspend fun getMessagesAfterSeq(seq: Long, maxSeq: Long): List<AssistantMessageEntity>

    @Upsert
    suspend fun upsertThreads(threads: List<AssistantThreadEntity>)

    @Upsert
    suspend fun upsertMessages(messages: List<AssistantMessageEntity>)
}
