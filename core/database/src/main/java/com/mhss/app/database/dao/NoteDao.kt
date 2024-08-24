package com.mhss.app.database.dao

import androidx.room.*
import com.mhss.app.database.entity.NoteEntity
import com.mhss.app.database.entity.NoteFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT title, SUBSTR(content, 1, 450) AS content, created_date, updated_date, pinned, folder_id, id FROM notes WHERE folder_id IS NULL")
    fun getAllFolderlessNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNote(id: Int): NoteEntity

    @Query("SELECT title, SUBSTR(content, 1, 250) AS content, created_date, updated_date, pinned, folder_id, id FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    suspend fun getNotesByTitle(query: String): List<NoteEntity>

    @Query("SELECT title, SUBSTR(content, 1, 450) AS content, created_date, updated_date, pinned, folder_id, id FROM notes WHERE folder_id = :folderId")
    fun getNotesByFolder(folderId: Int): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteFolder(folder: NoteFolderEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteFolders(folders: List<NoteFolderEntity>): List<Long>

    @Update
    suspend fun updateNoteFolder(folder: NoteFolderEntity)

    @Delete
    suspend fun deleteNoteFolder(folder: NoteFolderEntity)

    @Query("SELECT * FROM note_folders")
    fun getAllNoteFolders(): Flow<List<NoteFolderEntity>>

    @Query("SELECT * FROM note_folders WHERE id = :folderId")
    fun getNoteFolder(folderId: Int): NoteFolderEntity?
}