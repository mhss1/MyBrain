package com.mhss.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import com.mhss.app.database.DefaultPagingConfig

import com.mhss.app.data.mappers.toAiMessage
import com.mhss.app.data.mappers.toAssistantMessageEntity
import com.mhss.app.database.dao.AssistantDao
import com.mhss.app.database.dao.SyncDao
import com.mhss.app.database.dao.incrementAndGet
import com.mhss.app.database.entity.AssistantThreadEntity
import com.mhss.app.database.entity.DeletedEntityEntity
import com.mhss.app.database.entity.DeletedEntityType
import com.mhss.app.database.helpers.DatabaseTransactionProvider
import com.mhss.app.database.sync.LocalChangeObserver
import com.mhss.app.datetime.now
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AssistantThread
import com.mhss.app.domain.repository.AssistantChatRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Factory
class AssistantChatRepositoryImpl(
    private val assistantDao: AssistantDao,
    private val syncDao: SyncDao,
    private val changeObserver: LocalChangeObserver,
    private val transactionProvider: DatabaseTransactionProvider,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : AssistantChatRepository {

    override fun getPagedThreads(): Flow<PagingData<AssistantThread>> =
        Pager(config = DefaultPagingConfig) { assistantDao.getPagedThreads() }
            .flow.map { page -> page.map { it.toAssistantThread() } }

    override fun getAllThreads(): Flow<List<AssistantThread>> {
        return assistantDao.getAllThreads().map { entities ->
            entities.map { it.toAssistantThread() }
        }.flowOn(ioDispatcher)
    }

    override suspend fun saveThread(thread: AssistantThread): Unit = withContext(ioDispatcher) {
        transactionProvider.runInTransaction {
            assistantDao.upsertThread(thread.toEntity(syncSeq = syncDao.incrementAndGet()))
        }
        changeObserver.notifyChange()
    }

    override suspend fun deleteThread(threadId: String): Unit = withContext(ioDispatcher) {
        transactionProvider.runInTransaction {
            assistantDao.deleteThreadAndMessages(threadId)
            syncDao.insertDeletedEntity(
                DeletedEntityEntity(
                    id = Uuid.generateV7().toString(),
                    entityId = threadId,
                    entityType = DeletedEntityType.THREAD.key,
                    deletedAt = now(),
                    syncSeq = syncDao.incrementAndGet()
                )
            )
        }
        changeObserver.notifyChange()
    }

    override suspend fun deleteAllThreads(): Unit = withContext(ioDispatcher) {
        transactionProvider.runInTransaction {
            val threadIds = assistantDao.getAllThreadIds()
            assistantDao.deleteAllThreadsAndMessages()
            threadIds.forEach { thId ->
                syncDao.insertDeletedEntity(
                    DeletedEntityEntity(
                        id = Uuid.generateV7().toString(),
                        entityId = thId,
                        entityType = DeletedEntityType.THREAD.key,
                        deletedAt = now(),
                        syncSeq = syncDao.incrementAndGet()
                    )
                )
            }
        }
        changeObserver.notifyChange()
    }

    override fun getMessages(threadId: String): Flow<List<AiMessage>> {
        return assistantDao.getMessagesByThreadId(threadId).map { entities ->
            withContext(defaultDispatcher) {
                entities.mapNotNullTo(ArrayList(entities.size)) { it.toAiMessage() }
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun saveMessage(threadId: String, message: AiMessage): Unit =
        withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                val entity = message.toAssistantMessageEntity(threadId, syncDao.incrementAndGet())
                assistantDao.upsertMessage(entity)

                val threadEntity = assistantDao.getThreadsByIds(listOf(threadId)).firstOrNull()
                if (threadEntity != null) {
                    assistantDao.upsertThread(
                        threadEntity.copy(
                            updatedAt = entity.createdAt,
                            syncSeq = syncDao.incrementAndGet()
                        )
                    )
                }
            }
            changeObserver.notifyChange()
        }

    override suspend fun deleteMessage(messageId: String): Unit = withContext(ioDispatcher) {
        transactionProvider.runInTransaction {
            assistantDao.deleteMessage(messageId)
            syncDao.insertDeletedEntity(
                DeletedEntityEntity(
                    id = Uuid.generateV7().toString(),
                    entityId = messageId,
                    entityType = DeletedEntityType.MESSAGE.key,
                    deletedAt = now(),
                    syncSeq = syncDao.incrementAndGet()
                )
            )
        }
        changeObserver.notifyChange()
    }

    private fun AssistantThreadEntity.toAssistantThread(): AssistantThread {
        return AssistantThread(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun AssistantThread.toEntity(syncSeq: Long = 0L): AssistantThreadEntity {
        return AssistantThreadEntity(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            syncSeq = syncSeq
        )
    }

}
