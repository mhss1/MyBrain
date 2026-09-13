package com.mhss.app.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.RawQuery
import androidx.room3.RoomRawQuery
import androidx.room3.Transaction
import androidx.room3.Update
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.mhss.app.database.entity.NoteEntity
import com.mhss.app.database.entity.NoteFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface NoteDao {

    @RawQuery(observedEntities = [NoteEntity::class])
    fun pageNotes(query: RoomRawQuery): PagingSource<Int, NoteEntity>

    fun getPagedNotes(orderBy: NoteOrder, order: QueryOrder, showAllNotes: Boolean): PagingSource<Int, NoteEntity> =
        pageNotes(noteQuery(if (showAllNotes) null else "folder_id IS NULL", orderBy, order))

    fun getPagedNotesByFolder(folderId: String, orderBy: NoteOrder, order: QueryOrder): PagingSource<Int, NoteEntity> =
        pageNotes(noteQuery("folder_id = ?", orderBy, order) { it.bindText(1, folderId) })

    @Query("SELECT title, SUBSTR(content, 1, 100) AS content, created_date, updated_date, pinned, folder_id, id, sync_seq FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY pinned DESC, updated_date DESC")
    fun searchPagedNotes(query: String): PagingSource<Int, NoteEntity>

    @RawQuery(observedEntities = [NoteEntity::class])
    fun observeNotes(query: RoomRawQuery): Flow<List<NoteEntity>>

    fun getAllFolderlessNotes(orderBy: NoteOrder, order: QueryOrder): Flow<List<NoteEntity>> =
        observeNotes(noteQuery("folder_id IS NULL", orderBy, order))

    fun getAllNotes(orderBy: NoteOrder, order: QueryOrder): Flow<List<NoteEntity>> =
        observeNotes(noteQuery(null, orderBy, order))

    @Query("SELECT * FROM notes")
    suspend fun getAllFullNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNote(id: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE id IN (:ids)")
    suspend fun getNotesByIds(ids: List<String>): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE updated_date > :timestamp")
    suspend fun getNotesUpdatedAfter(timestamp: Long): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE sync_seq > :seq AND sync_seq <= :maxSeq")
    suspend fun getNotesAfterSeq(seq: Long, maxSeq: Long): List<NoteEntity>

    @Query("SELECT id FROM notes WHERE folder_id = :folderId")
    suspend fun getNoteIdsByFolder(folderId: String): List<String>

    @Query("SELECT title, SUBSTR(content, 1, 100) AS content, created_date, updated_date, pinned, folder_id, id, sync_seq FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    suspend fun getNotesByTitle(query: String): List<NoteEntity>

    fun getNotesByFolder(
        folderId: String,
        orderBy: NoteOrder,
        order: QueryOrder
    ): Flow<List<NoteEntity>> = observeNotes(
        noteQuery("folder_id = ?", orderBy, order) { it.bindText(1, folderId) }
    )

    @Query("DELETE FROM notes WHERE folder_id = :folderId")
    suspend fun deleteNotesByFolderId(folderId: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    @Upsert
    suspend fun upsertNotes(notes: List<NoteEntity>)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteFolder(folder: NoteFolderEntity)

    @Upsert
    suspend fun upsertNoteFolders(folders: List<NoteFolderEntity>)

    @Update
    suspend fun updateNoteFolder(folder: NoteFolderEntity)

    @Delete
    suspend fun deleteNoteFolder(folder: NoteFolderEntity)

    @Query("DELETE FROM note_folders WHERE id = :folderId")
    suspend fun deleteNoteFolderById(folderId: String)

    @Transaction
    suspend fun deleteFolderAndNotes(folderId: String) {
        deleteNotesByFolderId(folderId)
        deleteNoteFolderById(folderId)
    }

    @Query("SELECT * FROM note_folders")
    fun getAllNoteFolders(): Flow<List<NoteFolderEntity>>

    @Query("SELECT * FROM note_folders WHERE sync_seq > :seq AND sync_seq <= :maxSeq")
    suspend fun getNoteFoldersAfterSeq(seq: Long, maxSeq: Long): List<NoteFolderEntity>

    @Query("SELECT * FROM note_folders WHERE id = :folderId")
    suspend fun getNoteFolder(folderId: String): NoteFolderEntity?

    @Query("SELECT * FROM note_folders WHERE id IN (:ids)")
    suspend fun getNoteFoldersByIds(ids: List<String>): List<NoteFolderEntity>

    @Query("SELECT * FROM note_folders WHERE name = :name")
    suspend fun getNoteFolderByName(name: String): NoteFolderEntity?

    @Query("SELECT * FROM note_folders WHERE name LIKE '%' || :name || '%'")
    suspend fun searchFolderByName(name: String): List<NoteFolderEntity>
}

private fun noteQuery(
    where: String?,
    orderBy: NoteOrder,
    order: QueryOrder,
    bind: (androidx.sqlite.SQLiteStatement) -> Unit = {}
): RoomRawQuery {
    val whereSql = where?.let { " WHERE $it" }.orEmpty()
    val column = when (orderBy) {
        NoteOrder.TITLE -> "title COLLATE NOCASE"
        else -> orderBy.column
    }
    return RoomRawQuery(
        "SELECT title, SUBSTR(content, 1, 150) AS content, created_date, updated_date, pinned, folder_id, id, sync_seq FROM notes$whereSql ORDER BY pinned DESC, $column ${order.sql}",
        bind
    )
}

enum class NoteOrder(val column: String) {
    TITLE("title"),
    CREATED_DATE("created_date"),
    UPDATED_DATE("updated_date")
}
