package com.mhss.app.mybrain.data.local.dao

import androidx.room.*
import com.mhss.app.mybrain.domain.model.DiaryEntry

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary")
    suspend fun getAllEntries(): List<DiaryEntry>

    @Query("SELECT * FROM diary WHERE id = :id")
    suspend fun getEntry(id: Int): DiaryEntry

    @Query("SELECT * FROM diary WHERE title LIKE '%' || :title || '%'")
    suspend fun getEntriesByTitle(title: String): List<DiaryEntry>

    @Insert
    suspend fun insertEntry(diary: DiaryEntry)

    @Update
    suspend fun updateEntry(diary: DiaryEntry)

    @Delete
    suspend fun deleteEntry(diary: DiaryEntry)

}