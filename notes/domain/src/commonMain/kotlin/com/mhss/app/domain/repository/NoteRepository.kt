package com.mhss.app.domain.repository

import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getAllFolderlessNotes(
        sortOrder: SortOrder = SortOrder.DateModified(SortType.DESC)
    ): Flow<List<Note>>

    fun getAllNotes(
        sortOrder: SortOrder = SortOrder.DateModified(SortType.DESC)
    ): Flow<List<Note>>

    suspend fun getAllFullNotes(): List<Note>

    suspend fun getNote(id: String): Note?

    suspend fun searchNotes(query: String): List<Note>

    fun getNotesByFolder(
        folderId: String,
        sortOrder: SortOrder = SortOrder.DateModified(SortType.DESC)
    ): Flow<List<Note>>

    suspend fun upsertNote(note: Note, currentFolderId: String? = null): String

    suspend fun upsertNotes(notes: List<Note>, notifyChange: Boolean = true): List<String>

    suspend fun deleteNote(note: Note)

    suspend fun insertNoteFolder(folderName: String): String

    suspend fun upsertNoteFolders(folders: List<NoteFolder>, notifyChange: Boolean = true)

    suspend fun updateNoteFolder(folder: NoteFolder)

    suspend fun deleteNoteFolder(folder: NoteFolder)

    fun getAllNoteFolders(): Flow<List<NoteFolder>>

    suspend fun getNoteFolder(folderId: String): NoteFolder?

    suspend fun searchFoldersByName(name: String): List<NoteFolder>

}
