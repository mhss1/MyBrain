package com.mhss.app.mybrain.data.repository

import com.mhss.app.mybrain.data.local.dao.DiaryDao
import com.mhss.app.mybrain.data.local.entity.toDiaryEntry
import com.mhss.app.mybrain.data.local.entity.toDiaryEntryEntity
import com.mhss.app.mybrain.di.namedIoDispatcher
import com.mhss.app.mybrain.domain.model.diary.DiaryEntry
import com.mhss.app.mybrain.domain.repository.diary.DiaryRepository
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
    @Named(namedIoDispatcher) private val ioDispatcher: CoroutineDispatcher
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