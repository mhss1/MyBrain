package com.mhss.app.mybrain.data.repository

import com.mhss.app.mybrain.data.local.dao.NoteDao
import com.mhss.app.mybrain.data.local.entity.toNote
import com.mhss.app.mybrain.data.local.entity.toNoteEntity
import com.mhss.app.mybrain.data.local.entity.toNoteFolder
import com.mhss.app.mybrain.data.local.entity.toNoteFolderEntity
import com.mhss.app.mybrain.di.namedIoDispatcher
import com.mhss.app.mybrain.domain.model.notes.Note
import com.mhss.app.mybrain.domain.model.notes.NoteFolder
import com.mhss.app.mybrain.domain.repository.notes.NoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
            .flowOn(ioDispatcher)
            .map { notes ->
                notes.map {
                    it.toNote()
                }
            }
    }

    override suspend fun getNote(id: Int): Note {
        return withContext(ioDispatcher) {
            noteDao.getNote(id).toNote()
        }
    }

    override suspend fun searchNotes(query: String): List<Note> {
        return withContext(ioDispatcher) {
            noteDao.getNotesByTitle(query).map {
                it.toNote()
            }
        }
    }

    override fun getNotesByFolder(folderId: Int): Flow<List<Note>> {
        return noteDao.getNotesByFolder(folderId)
            .flowOn(ioDispatcher)
            .map { notes ->
            notes.map { it.toNote() }
        }
    }

    override suspend fun addNote(note: Note) {
        withContext(ioDispatcher) {
            noteDao.insertNote(note.toNoteEntity())
        }
    }

    override suspend fun updateNote(note: Note) {
        withContext(ioDispatcher) {
            noteDao.updateNote(note.toNoteEntity())
        }
    }

    override suspend fun deleteNote(note: Note) {
        withContext(ioDispatcher) {
            noteDao.deleteNote(note.toNoteEntity())
        }
    }

    override suspend fun insertNoteFolder(folder: NoteFolder) {
        withContext(ioDispatcher) {
            noteDao.insertNoteFolder(folder.toNoteFolderEntity())
        }
    }

    override suspend fun updateNoteFolder(folder: NoteFolder) {
        withContext(ioDispatcher) {
            noteDao.updateNoteFolder(folder.toNoteFolderEntity())
        }
    }

    override suspend fun deleteNoteFolder(folder: NoteFolder) {
        withContext(ioDispatcher) {
            noteDao.deleteNoteFolder(folder.toNoteFolderEntity())
        }
    }

    override fun getAllNoteFolders(): Flow<List<NoteFolder>> {
        return noteDao.getAllNoteFolders()
            .flowOn(ioDispatcher)
            .map { folders ->
            folders.map { it.toNoteFolder() }
        }
    }

    override suspend fun getNoteFolder(folderId: Int): NoteFolder? {
        return withContext(ioDispatcher) {
            noteDao.getNoteFolder(folderId)?.toNoteFolder()
        }
    }
}