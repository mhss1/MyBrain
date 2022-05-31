package com.mhss.app.mybrain.domain.repository

import com.mhss.app.mybrain.domain.model.Note
import com.mhss.app.mybrain.domain.model.NoteFolder
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getAllNotes(): Flow<List<Note>>

    suspend fun getNote(id: Int): Note

    suspend fun searchNotes(query: String): List<Note>

    suspend fun getNotesByFolder(folder: String): Flow<List<Note>>

    suspend fun addNote(note: Note)

    suspend fun updateNote(note: Note)

    suspend fun deleteNote(note: Note)

    suspend fun insertNoteFolder(folder: NoteFolder)

    suspend fun updateNoteFolder(folder: NoteFolder)

    suspend fun deleteNoteFolder(folder: NoteFolder)

    suspend fun getAllNoteFolders(): Flow<List<NoteFolder>>

}