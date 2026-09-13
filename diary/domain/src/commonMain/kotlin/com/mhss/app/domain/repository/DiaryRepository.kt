package com.mhss.app.domain.repository

import androidx.paging.PagingData
import com.mhss.app.domain.model.DiaryChartPoint

import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {

    fun getPagedEntries(sortOrder: SortOrder): Flow<PagingData<DiaryEntry>>

    fun searchPagedEntries(query: String): Flow<PagingData<DiaryEntry>>

    fun getChartPoints(from: Long, to: Long): Flow<List<DiaryChartPoint>>

    fun getAllEntries(
        sortOrder: SortOrder = SortOrder.DateCreated(SortType.DESC)
    ): Flow<List<DiaryEntry>>

    suspend fun getAllFullEntries(): List<DiaryEntry>

    suspend fun getEntry(id: String): DiaryEntry?

    suspend fun searchEntries(title: String): List<DiaryEntry>

    suspend fun addEntry(diary: DiaryEntry)

    suspend fun upsertEntries(entries: List<DiaryEntry>, notifyChange: Boolean = true)

    suspend fun updateEntry(diary: DiaryEntry)

    suspend fun deleteEntry(diary: DiaryEntry)

}
