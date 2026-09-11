package com.mhss.app.data.impl

import com.mhss.app.data.storage.MarkdownFileManager
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.repository.NoteRepository
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class MarkdownNoteRepositoryImpl(
    private val markdownFileManager: MarkdownFileManager,
    private val rootId: String,
    private val defaultDispatcher: CoroutineDispatcher,
) : NoteRepository {

    override fun getAllFolderlessNotes(sortOrder: SortOrder): Flow<List<Note>> {
        return markdownFileManager.getFolderNotesFlow(rootId)
            .map { it.sorted(sortOrder) }
            .flowOn(defaultDispatcher)
    }

    override fun getAllNotes(sortOrder: SortOrder): Flow<List<Note>> {
        return markdownFileManager.getAllNotesFlow(rootId)
            .map { it.sorted(sortOrder) }
            .flowOn(defaultDispatcher)
    }

    override suspend fun getAllFullNotes(): List<Note> {
        return getAllNotes().first()
    }

    override suspend fun getNote(id: String): Note {
        return markdownFileManager.getNote(id)
    }

    override suspend fun searchNotes(query: String): List<Note> {
        return markdownFileManager.searchNotes(query, rootId)
    }

    override fun getNotesByFolder(folderId: String, sortOrder: SortOrder): Flow<List<Note>> {
        return markdownFileManager.getFolderNotesFlow(folderId)
            .map { it.sorted(sortOrder) }
            .flowOn(defaultDispatcher)
    }

    override suspend fun upsertNote(note: Note, currentFolderId: String?): String {
        return markdownFileManager.upsertNote(note, currentFolderId, rootId)
    }

    override suspend fun upsertNotes(notes: List<Note>, notifyChange: Boolean): List<String> {
        return notes.map {
            upsertNote(it, null)
        }
    }

    override suspend fun deleteNote(note: Note) {
        markdownFileManager.deleteNote(note, rootId)
    }

    override suspend fun upsertNoteFolders(folders: List<NoteFolder>, notifyChange: Boolean) {
        folders.forEach {
            markdownFileManager.createFolder(it.name, rootId)
        }
    }

    override suspend fun insertNoteFolder(folderName: String): String {
        return markdownFileManager.createFolder(folderName, rootId)
    }

    override suspend fun updateNoteFolder(folder: NoteFolder) {
        markdownFileManager.updateFolder(folder.id, folder.name.trim(), rootId)
    }

    override suspend fun deleteNoteFolder(folder: NoteFolder) {
        markdownFileManager.deleteFolder(folder.id, rootId)
    }

    override fun getAllNoteFolders(): Flow<List<NoteFolder>> {
        return markdownFileManager.getFolderFoldersFlow(rootId)
    }

    override suspend fun getNoteFolder(folderId: String): NoteFolder? {
        if (folderId == rootId) return null
        return markdownFileManager.getFolder(folderId)
    }

    override suspend fun searchFoldersByName(name: String): List<NoteFolder> {
        return markdownFileManager.searchFolderByName(name, rootId)
    }
}

private fun List<Note>.sorted(sortOrder: SortOrder): List<Note> {
    val valueComparator = when (sortOrder) {
        is SortOrder.Alphabetical -> Comparator<Note> { first, second ->
            first.title.compareTo(second.title, ignoreCase = true)
        }
        is SortOrder.DateCreated -> compareBy<Note> { it.createdDate }
        else -> compareBy<Note> { it.updatedDate }
    }
    val comparator = when (sortOrder.sortType) {
        SortType.ASC -> valueComparator
        SortType.DESC -> valueComparator.reversed()
    }
    return sortedWith(compareByDescending<Note> { it.pinned }.then(comparator))
}
