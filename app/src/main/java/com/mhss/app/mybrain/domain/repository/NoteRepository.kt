package com.mhss.app.mybrain.domain.repository

import com.mhss.app.mybrain.domain.model.Note

interface NoteRepository {

    suspend fun getAllNotes(): List<Note>

    suspend fun getNote(id: Int): Note

    suspend fun searchNotes(query: String): List<Note>

    suspend fun addNote(note: Note)

    suspend fun updateNote(note: Note)

    suspend fun deleteNote(note: Note)

}