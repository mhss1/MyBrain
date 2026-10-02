package com.mhss.app.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.mhss.app.database.entity.DeletedEntityEntity

@Dao
interface SyncDao {
    @Query("UPDATE sync_state SET last_seq = last_seq + 1 WHERE id = 1")
    suspend fun incrementSyncSequence()

    @Query("SELECT last_seq FROM sync_state WHERE id = 1")
    suspend fun getLastSyncSequence(): Long

    @Query(
        """
        SELECT sync_seq FROM (
            SELECT sync_seq FROM notes
            UNION ALL
            SELECT sync_seq FROM note_folders
            UNION ALL
            SELECT sync_seq FROM tasks
            UNION ALL
            SELECT sync_seq FROM diary
            UNION ALL
            SELECT sync_seq FROM bookmarks
            UNION ALL
            SELECT sync_seq FROM assistant_threads
            UNION ALL
            SELECT sync_seq FROM assistant_messages
            UNION ALL
            SELECT sync_seq FROM deleted_entities
        )
        WHERE sync_seq > :seq AND sync_seq <= :maxSeq
        ORDER BY sync_seq ASC
        LIMIT :limit
        """
    )
    suspend fun getNextSyncSequences(seq: Long, maxSeq: Long, limit: Int): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletedEntity(entity: DeletedEntityEntity)

    @Query("SELECT * FROM deleted_entities WHERE sync_seq > :seq AND sync_seq <= :maxSeq ORDER BY sync_seq ASC")
    suspend fun getDeletedEntitiesUpdatedAfter(seq: Long, maxSeq: Long): List<DeletedEntityEntity>

    @Query("SELECT entity_id FROM deleted_entities WHERE entity_type = :entityType AND entity_id IN (:entityIds)")
    suspend fun getDeletedEntityIds(entityType: String, entityIds: List<String>): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM deleted_entities WHERE entity_type = :entityType AND entity_id = :entityId)")
    suspend fun deletedEntityExists(entityType: String, entityId: String): Boolean

    @Query(
        """
        DELETE FROM deleted_entities
        WHERE id IN (
          SELECT id FROM deleted_entities
          WHERE deleted_at <= :cutoff
          AND (
            (SELECT MIN(last_acknowledged_local_seq) FROM paired_devices) IS NULL
            OR sync_seq <= (SELECT MIN(last_acknowledged_local_seq) FROM paired_devices)
          )
          ORDER BY sync_seq
          LIMIT :limit
        )
        """
    )
    suspend fun deleteExpiredTombstones(cutoff: Long, limit: Int): Int

}

suspend inline fun SyncDao.incrementAndGet(): Long {
    incrementSyncSequence()
    return getLastSyncSequence()
}
