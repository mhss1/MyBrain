package com.mhss.app.data

import com.mhss.app.database.dao.DiaryDao
import com.mhss.app.database.entity.*
import com.mhss.app.domain.repository.DiaryRepository
import com.mhss.app.domain.model.DiaryEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class DiaryRepositoryImpl(
    private val diaryDao: DiaryDao,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : DiaryRepository {

    override fun getAllEntries(): Flow<List<DiaryEntry>> {
        return diaryDao.getAllEntries()
            .flowOn(ioDispatcher)
            .map { entries ->
                entries.map { it.toDiaryEntry() }
            }
    }

    override suspend fun getEntry(id: Int): DiaryEntry {
        return withContext(ioDispatcher) {
            diaryDao.getEntry(id).toDiaryEntry()
        }
    }

    override suspend fun searchEntries(title: String): List<DiaryEntry> {
        return withContext(ioDispatcher) {
            diaryDao.getEntriesByTitle(title).map { it.toDiaryEntry() }
        }
    }

    override suspend fun addEntry(diary: DiaryEntry) {
        withContext(ioDispatcher) {
            diaryDao.insertEntry(diary.toDiaryEntryEntity())
        }
    }

    override suspend fun updateEntry(diary: DiaryEntry) {
        withContext(ioDispatcher) {
            diaryDao.updateEntry(diary.toDiaryEntryEntity())
        }
    }

    override suspend fun deleteEntry(diary: DiaryEntry) {
        withContext(ioDispatcher) {
            diaryDao.deleteEntry(diary.toDiaryEntryEntity())
        }
    }
}