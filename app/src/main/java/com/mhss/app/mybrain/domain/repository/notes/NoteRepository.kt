package com.mhss.app.mybrain.domain.repository.notes

import com.mhss.app.mybrain.domain.model.notes.Note
import com.mhss.app.mybrain.domain.model.notes.NoteFolder
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getAllFolderlessNotes(): Flow<List<Note>>

    suspend fun getNote(id: Int): Note

    suspend fun searchNotes(query: String): List<Note>

    fun getNotesByFolder(folderId: Int): Flow<List<Note>>

    suspend fun addNote(note: Note)

    suspend fun updateNote(note: Note)

    suspend fun deleteNote(note: Note)

    suspend fun insertNoteFolder(folder: NoteFolder)

    suspend fun updateNoteFolder(folder: NoteFolder)

    suspend fun deleteNoteFolder(folder: NoteFolder)

    fun getAllNoteFolders(): Flow<List<NoteFolder>>

    suspend fun getNoteFolder(folderId: Int): NoteFolder?

}