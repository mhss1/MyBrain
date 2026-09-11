package com.mhss.app.data

import com.mhss.app.database.dao.DiaryDao
import com.mhss.app.database.dao.DiaryOrder
import com.mhss.app.database.dao.QueryOrder
import com.mhss.app.database.dao.SyncDao
import com.mhss.app.database.dao.incrementAndGet
import com.mhss.app.database.entity.DeletedEntityEntity
import com.mhss.app.database.entity.DeletedEntityType
import com.mhss.app.database.entity.toDiaryEntry
import com.mhss.app.database.entity.toDiaryEntryEntity
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.repository.DiaryRepository
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import com.mhss.app.database.sync.LocalChangeObserver
import com.mhss.app.database.helpers.DatabaseTransactionProvider
import com.mhss.app.datetime.now
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class DiaryRepositoryImpl(
    private val diaryDao: DiaryDao,
    private val syncDao: SyncDao,
    private val changeObserver: LocalChangeObserver,
    private val transactionProvider: DatabaseTransactionProvider,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : DiaryRepository {

    override fun getAllEntries(sortOrder: SortOrder): Flow<List<DiaryEntry>> {
        val sortOrderBy = when (sortOrder) {
            is SortOrder.Alphabetical -> DiaryOrder.TITLE
            is SortOrder.DateCreated -> DiaryOrder.CREATED_DATE
            else -> DiaryOrder.UPDATED_DATE
        }
        return diaryDao.getAllEntries(sortOrderBy, sortOrder.sortType.toQueryOrder())
            .map { entries -> entries.map { it.toDiaryEntry() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getAllFullEntries(): List<DiaryEntry> {
        return withContext(ioDispatcher) {
            diaryDao.getAllFullEntries().map { it.toDiaryEntry() }
        }
    }

    override suspend fun getEntry(id: String): DiaryEntry? {
        return withContext(ioDispatcher) {
            diaryDao.getEntry(id)?.toDiaryEntry()
        }
    }

    override suspend fun searchEntries(title: String): List<DiaryEntry> {
        return withContext(ioDispatcher) {
            diaryDao.getEntriesByTitle(title).map { it.toDiaryEntry() }
        }
    }

    override suspend fun upsertEntries(entries: List<DiaryEntry>, notifyChange: Boolean) {
        withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                val stamped = entries.map {
                    it.toDiaryEntryEntity(syncSeq = syncDao.incrementAndGet())
                }
                diaryDao.upsertEntries(stamped)
            }
            if (notifyChange) changeObserver.notifyChange()
        }
    }

    override suspend fun addEntry(diary: DiaryEntry) {
        return withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                diaryDao.insertEntry(diary.toDiaryEntryEntity(syncSeq = syncDao.incrementAndGet()))
            }
            changeObserver.notifyChange()
        }
    }

    override suspend fun updateEntry(diary: DiaryEntry) {
        withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                diaryDao.updateEntry(diary.toDiaryEntryEntity(syncSeq = syncDao.incrementAndGet()))
            }
            changeObserver.notifyChange()
        }
    }

    override suspend fun deleteEntry(diary: DiaryEntry) {
        withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                diaryDao.deleteEntry(diary.toDiaryEntryEntity())
                syncDao.insertDeletedEntity(
                    DeletedEntityEntity(
                        id = Uuid.generateV7().toString(),
                        entityId = diary.id,
                        entityType = DeletedEntityType.DIARY.key,
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
