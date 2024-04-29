package com.mhss.app.mybrain.data.local.dao

import androidx.room.*
import com.mhss.app.mybrain.domain.model.DiaryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary WHERE id = :id")
    suspend fun getEntry(id: Int): DiaryEntry

    @Query("SELECT * FROM diary WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    suspend fun getEntriesByTitle(query: String): List<DiaryEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(diary: DiaryEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(diary: List<DiaryEntry>)

    @Update
    suspend fun updateEntry(diary: DiaryEntry)

    @Delete
    suspend fun deleteEntry(diary: DiaryEntry)

}