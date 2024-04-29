package com.mhss.app.mybrain.data.local.dao

import androidx.room.*
import com.mhss.app.mybrain.domain.model.Note
import com.mhss.app.mybrain.domain.model.NoteFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNote(id: Int): Note

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    suspend fun getNotesByTitle(query: String): List<Note>

    @Query("SELECT * FROM notes WHERE folder_id = :folderName")
    fun getNotesByFolder(folderName: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotes(notes: List<Note>)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteFolder(folder: NoteFolder)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteFolders(folders: List<NoteFolder>)

    @Update
    suspend fun updateNoteFolder(folder: NoteFolder)

    @Delete
    suspend fun deleteNoteFolder(folder: NoteFolder)

    @Query("SELECT * FROM note_folders")
    fun getAllNoteFolders(): Flow<List<NoteFolder>>
}