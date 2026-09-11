package com.mhss.app.database.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.RawQuery
import androidx.room3.RoomRawQuery
import androidx.room3.Update
import androidx.room3.Upsert
import com.mhss.app.database.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @RawQuery(observedEntities = [DiaryEntryEntity::class])
    fun observeEntries(query: RoomRawQuery): Flow<List<DiaryEntryEntity>>

    fun getAllEntries(orderBy: DiaryOrder, order: QueryOrder): Flow<List<DiaryEntryEntity>> {
        val orderBySql = when (orderBy) {
            DiaryOrder.TITLE -> "title COLLATE NOCASE ${order.sql}"
            else -> "${orderBy.column} ${order.sql}"
        }
        return observeEntries(
            RoomRawQuery(
                "SELECT title, SUBSTR(content, 1, 150) AS content, created_date, updated_date, mood, id, sync_seq FROM diary ORDER BY $orderBySql"
            )
        )
    }

    @Query("SELECT * FROM diary")
    suspend fun getAllFullEntries(): List<DiaryEntryEntity>

    @Query("SELECT * FROM diary WHERE id = :id")
    suspend fun getEntry(id: String): DiaryEntryEntity?

    @Query("SELECT * FROM diary WHERE id IN (:ids)")
    suspend fun getEntriesByIds(ids: List<String>): List<DiaryEntryEntity>

    @Query("SELECT * FROM diary WHERE updated_date > :timestamp")
    suspend fun getDiaryEntriesUpdatedAfter(timestamp: Long): List<DiaryEntryEntity>

    @Query("SELECT * FROM diary WHERE sync_seq > :seq AND sync_seq <= :maxSeq")
    suspend fun getDiaryEntriesAfterSeq(seq: Long, maxSeq: Long): List<DiaryEntryEntity>

    @Query("SELECT title, SUBSTR(content, 1, 100) AS content, created_date, updated_date, mood, id, sync_seq FROM diary WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    suspend fun getEntriesByTitle(query: String): List<DiaryEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(diary: DiaryEntryEntity)

    @Upsert
    suspend fun upsertEntries(diary: List<DiaryEntryEntity>)

    @Update
    suspend fun updateEntry(diary: DiaryEntryEntity)

    @Delete
    suspend fun deleteEntry(diary: DiaryEntryEntity)

    @Query("DELETE FROM diary WHERE id = :id")
    suspend fun deleteEntryById(id: String)

}

enum class DiaryOrder(val column: String) {
    TITLE("title"),
    CREATED_DATE("created_date"),
    UPDATED_DATE("updated_date")
}
