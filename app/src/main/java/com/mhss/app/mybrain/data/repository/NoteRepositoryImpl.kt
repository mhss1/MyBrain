package com.mhss.app.mybrain.data.repository

import com.mhss.app.mybrain.data.local.dao.NoteDao
import com.mhss.app.mybrain.di.namedIoDispatcher
import com.mhss.app.mybrain.domain.model.Note
import com.mhss.app.mybrain.domain.model.NoteFolder
import com.mhss.app.mybrain.domain.repository.NoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    @Named(namedIoDispatcher) private val ioDispatcher: CoroutineDispatcher
) : NoteRepository {

    override fun getAllFolderlessNotes(): Flow<List<Note>> {
        return noteDao.getAllFolderlessNotes()
    }

    override suspend fun getNote(id: Int): Note {
        return withContext(ioDispatcher) {
            noteDao.getNote(id)
        }
    }

    override suspend fun searchNotes(query: String): List<Note> {
        return withContext(ioDispatcher) {
            noteDao.getNotesByTitle(query)
        }
    }

    override fun getNotesByFolder(folderId: Int): Flow<List<Note>> {
        return noteDao.getNotesByFolder(folderId)
    }

    override suspend fun addNote(note: Note) {
        withContext(ioDispatcher) {
            noteDao.insertNote(note)
        }
    }

    override suspend fun updateNote(note: Note) {
        withContext(ioDispatcher) {
            noteDao.updateNote(note)
        }
    }

    override suspend fun deleteNote(note: Note) {
        withContext(ioDispatcher) {
            noteDao.deleteNote(note)
        }
    }

    override suspend fun insertNoteFolder(folder: NoteFolder) {
        withContext(ioDispatcher) {
            noteDao.insertNoteFolder(folder)
        }
    }

    override suspend fun updateNoteFolder(folder: NoteFolder) {
        withContext(ioDispatcher) {
            noteDao.updateNoteFolder(folder)
        }
    }

    override suspend fun deleteNoteFolder(folder: NoteFolder) {
        withContext(ioDispatcher) {
            noteDao.deleteNoteFolder(folder)
        }
    }

    override fun getAllNoteFolders(): Flow<List<NoteFolder>> {
        return noteDao.getAllNoteFolders()
    }

    override suspend fun getNoteFolder(folderId: Int): NoteFolder? {
        return withContext(ioDispatcher) {
            noteDao.getNoteFolder(folderId)
        }
    }
}