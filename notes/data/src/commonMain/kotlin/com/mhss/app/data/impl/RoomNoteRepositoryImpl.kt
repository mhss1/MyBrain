package com.mhss.app.data.impl

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import com.mhss.app.database.DefaultPagingConfig
import com.mhss.app.database.dao.NoteDao
import com.mhss.app.database.dao.NoteOrder
import com.mhss.app.database.dao.QueryOrder
import com.mhss.app.database.dao.SyncDao
import com.mhss.app.database.dao.incrementAndGet
import com.mhss.app.database.entity.DeletedEntityEntity
import com.mhss.app.database.entity.DeletedEntityType
import com.mhss.app.database.entity.NoteFolderEntity
import com.mhss.app.database.entity.toNote
import com.mhss.app.database.entity.toNoteEntity
import com.mhss.app.database.entity.toNoteFolder
import com.mhss.app.database.entity.toNoteFolderEntity
import com.mhss.app.database.helpers.DatabaseTransactionProvider
import com.mhss.app.database.sync.LocalChangeObserver
import com.mhss.app.datetime.now
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteException
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.repository.NoteRepository
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named

@OptIn(ExperimentalUuidApi::class)
class RoomNoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val syncDao: SyncDao,
    private val changeObserver: LocalChangeObserver,
    private val transactionProvider: DatabaseTransactionProvider,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : NoteRepository {

    override fun getPagedNotes(sortOrder: SortOrder, showAllNotes: Boolean): Flow<PagingData<Note>> {
        return Pager(config = DefaultPagingConfig) {
            noteDao.getPagedNotes(sortOrder.toNoteOrder(), sortOrder.sortType.toQueryOrder(), showAllNotes)
        }.flow.map { page -> page.map { it.toNote() } }
    }

    override fun searchPagedNotes(query: String): Flow<PagingData<Note>> =
        Pager(config = DefaultPagingConfig) {
            noteDao.searchPagedNotes(query)
        }.flow.map { page -> page.map { it.toNote() } }

    override fun getPagedNotesByFolder(folderId: String, sortOrder: SortOrder): Flow<PagingData<Note>> =
        Pager(config = DefaultPagingConfig) {
            noteDao.getPagedNotesByFolder(folderId, sortOrder.toNoteOrder(), sortOrder.sortType.toQueryOrder())
        }.flow.map { page -> page.map { it.toNote() } }

    override fun getAllFolderlessNotes(sortOrder: SortOrder): Flow<List<Note>> {
        return noteDao.getAllFolderlessNotes(sortOrder.toNoteOrder(), sortOrder.sortType.toQueryOrder())
            .map { notes ->
                notes.map {
                    it.toNote()
                }
            }
            .flowOn(ioDispatcher)
    }

    override fun getAllNotes(sortOrder: SortOrder): Flow<List<Note>> {
        return noteDao.getAllNotes(sortOrder.toNoteOrder(), sortOrder.sortType.toQueryOrder())
            .map { notes ->
                notes.map {
                    it.toNote()
                }
            }
            .flowOn(ioDispatcher)
    }

    override fun getLimitedNotes(
        sortOrder: SortOrder,
        showAllNotes: Boolean,
        limit: Int
    ): Flow<List<Note>> {
        return noteDao.getLimitedNotes(
            sortOrder.toNoteOrder(),
            sortOrder.sortType.toQueryOrder(),
            showAllNotes,
            limit
        ).map { notes -> notes.map { it.toNote() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getAllFullNotes(): List<Note> {
        return withContext(ioDispatcher) {
            noteDao.getAllFullNotes().map { it.toNote() }
        }
    }

    override suspend fun getFullNotesPage(afterId: String, limit: Int): List<Note> {
        return withContext(ioDispatcher) {
            noteDao.getFullNotesPage(afterId, limit).map { it.toNote() }
        }
    }

    override suspend fun getNote(id: String): Note? {
        return withContext(ioDispatcher) {
            noteDao.getNote(id)?.toNote()
        }
    }

    override suspend fun searchNotes(query: String): List<Note> {
        return withContext(ioDispatcher) {
            noteDao.getNotesByTitle(query).map {
                it.toNote()
            }
        }
    }

    override fun getNotesByFolder(folderId: String, sortOrder: SortOrder): Flow<List<Note>> {
        return noteDao.getNotesByFolder(folderId, sortOrder.toNoteOrder(), sortOrder.sortType.toQueryOrder())
            .map { notes -> notes.map { it.toNote() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun upsertNote(note: Note, currentFolderId: String?): String {
        return withContext(ioDispatcher) {
            val id = note.id.ifBlank { Uuid.generateV7().toString() }
            transactionProvider.runInTransaction {
                noteDao.upsertNote(note.toNoteEntity(id = id, syncSeq = syncDao.incrementAndGet()))
            }
            changeObserver.notifyChange()
            id
        }
    }

    override suspend fun upsertNotes(notes: List<Note>, notifyChange: Boolean): List<String> {
        return withContext(ioDispatcher) {
            val notesWithIdsAndSeq = transactionProvider.runInTransaction {
                val stamped = notes.map {
                    val id = it.id.ifBlank { Uuid.generateV7().toString() }
                    it.toNoteEntity(id = id, syncSeq = syncDao.incrementAndGet())
                }
                noteDao.upsertNotes(stamped)
                stamped
            }
            if (notifyChange) changeObserver.notifyChange()
            notesWithIdsAndSeq.map { it.id }
        }
    }

    override suspend fun deleteNote(note: Note) {
        withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                noteDao.deleteNote(note.toNoteEntity())
                syncDao.insertDeletedEntity(
                    DeletedEntityEntity(
                        id = Uuid.generateV7().toString(),
                        entityId = note.id,
                        entityType = DeletedEntityType.NOTE.key,
                        deletedAt = now(),
                        syncSeq = syncDao.incrementAndGet()
                    )
                )
            }
            changeObserver.notifyChange()
        }
    }

    override suspend fun upsertNoteFolders(folders: List<NoteFolder>, notifyChange: Boolean) {
        withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                val stamped = folders.map {
                    it.toNoteFolderEntity(syncSeq = syncDao.incrementAndGet())
                }
                noteDao.upsertNoteFolders(stamped)
            }
            if (notifyChange) changeObserver.notifyChange()
        }
    }

    override suspend fun insertNoteFolder(folderName: String): String {
        return withContext(ioDispatcher) {
            if (noteDao.getNoteFolderByName(folderName) != null) {
                throw NoteException.FolderWithSameNameExists
            }
            val folderEntity = transactionProvider.runInTransaction {
                NoteFolderEntity(
                    id = Uuid.generateV7().toString(),
                    name = folderName,
                    syncSeq = syncDao.incrementAndGet(),
                    updatedDate = now()
                ).also { noteDao.insertNoteFolder(it) }
            }
            changeObserver.notifyChange()
            folderEntity.id
        }
    }

    override suspend fun updateNoteFolder(folder: NoteFolder) {
        withContext(ioDispatcher) {
            val existingFolder = noteDao.getNoteFolderByName(folder.name)
            if (existingFolder != null && existingFolder.id != folder.id) {
                throw NoteException.FolderWithSameNameExists
            }
            transactionProvider.runInTransaction {
                noteDao.updateNoteFolder(
                    folder.toNoteFolderEntity(syncSeq = syncDao.incrementAndGet())
                )
            }
            changeObserver.notifyChange()
        }
    }

    override suspend fun deleteNoteFolder(folder: NoteFolder) {
        withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                val noteIds = noteDao.getNoteIdsByFolder(folder.id)
                noteDao.deleteFolderAndNotes(folder.id)
                noteIds.forEach { noteId ->
                    syncDao.insertDeletedEntity(
                        DeletedEntityEntity(
                            id = Uuid.generateV7().toString(),
                            entityId = noteId,
                            entityType = DeletedEntityType.NOTE.key,
                            deletedAt = now(),
                            syncSeq = syncDao.incrementAndGet()
                        )
                    )
                }
                syncDao.insertDeletedEntity(
                    DeletedEntityEntity(
                        id = Uuid.generateV7().toString(),
                        entityId = folder.id,
                        entityType = DeletedEntityType.NOTE_FOLDER.key,
                        deletedAt = now(),
                        syncSeq = syncDao.incrementAndGet()
                    )
                )
            }
            changeObserver.notifyChange()
        }
    }

    override fun getPagedNoteFolders(): Flow<PagingData<NoteFolder>> =
        Pager(config = DefaultPagingConfig) { noteDao.getPagedNoteFolders() }
            .flow.map { page -> page.map { it.toNoteFolder() } }

    override fun getAllNoteFolders(): Flow<List<NoteFolder>> {
        return noteDao.getAllNoteFolders()
            .flowOn(ioDispatcher)
            .map { folders ->
                folders.map { it.toNoteFolder() }
            }
    }

    override suspend fun getNoteFoldersPage(afterId: String, limit: Int): List<NoteFolder> {
        return withContext(ioDispatcher) {
            noteDao.getNoteFoldersPage(afterId, limit).map { it.toNoteFolder() }
        }
    }

    override suspend fun getNoteFolder(folderId: String): NoteFolder? {
        return withContext(ioDispatcher) {
            noteDao.getNoteFolder(folderId)?.toNoteFolder()
        }
    }

    override suspend fun searchFoldersByName(name: String): List<NoteFolder> {
        return withContext(ioDispatcher) {
            noteDao.searchFolderByName(name).map { it.toNoteFolder() }
        }
    }
}

private fun SortOrder.toNoteOrder() = when (this) {
    is SortOrder.Alphabetical -> NoteOrder.TITLE
    is SortOrder.DateCreated -> NoteOrder.CREATED_DATE
    else -> NoteOrder.UPDATED_DATE
}

private fun SortType.toQueryOrder() = when (this) {
    SortType.ASC -> QueryOrder.ASC
    SortType.DESC -> QueryOrder.DESC
}
