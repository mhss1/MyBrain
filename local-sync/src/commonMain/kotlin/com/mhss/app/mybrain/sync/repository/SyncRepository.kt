package com.mhss.app.mybrain.sync.repository

import com.mhss.app.database.dao.AssistantDao
import com.mhss.app.database.dao.BookmarkDao
import com.mhss.app.database.dao.DiaryDao
import com.mhss.app.database.dao.NoteDao
import com.mhss.app.database.dao.SyncDao
import com.mhss.app.database.dao.TaskDao
import com.mhss.app.database.entity.AssistantMessageEntity
import com.mhss.app.database.entity.AssistantThreadEntity
import com.mhss.app.database.entity.DeletedEntityEntity
import com.mhss.app.database.entity.toBookmark
import com.mhss.app.database.entity.toDiaryEntry
import com.mhss.app.database.entity.toNote
import com.mhss.app.database.entity.toNoteFolder
import com.mhss.app.database.entity.toTask
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.model.Task
import org.koin.core.annotation.Single

interface SyncRepository {
    suspend fun getMaxSyncSequence(): Long
    suspend fun getNextSyncSequences(seq: Long, maxSeq: Long, limit: Int): List<Long>
    
    suspend fun getNotesAfterSeq(seq: Long, maxSeq: Long): List<Note>
    suspend fun getNoteFoldersAfterSeq(seq: Long, maxSeq: Long): List<NoteFolder>
    suspend fun getTasksAfterSeq(seq: Long, maxSeq: Long): List<Task>
    suspend fun getDiaryEntriesAfterSeq(seq: Long, maxSeq: Long): List<DiaryEntry>
    suspend fun getBookmarksAfterSeq(seq: Long, maxSeq: Long): List<Bookmark>
    suspend fun getThreadsAfterSeq(seq: Long, maxSeq: Long): List<AssistantThreadEntity>
    suspend fun getMessagesAfterSeq(seq: Long, maxSeq: Long): List<AssistantMessageEntity>
    suspend fun getDeletedEntitiesAfterSeq(seq: Long, maxSeq: Long): List<DeletedEntityEntity>
    suspend fun getDeletedEntityIds(entityType: String, entityIds: List<String>): List<String>
    suspend fun deletedEntityExists(entityType: String, entityId: String): Boolean
    suspend fun deleteExpiredTombstones(cutoff: Long, limit: Int): Int

    suspend fun getNotesByIds(ids: List<String>): List<Note>
    suspend fun getNoteFoldersByIds(ids: List<String>): List<NoteFolder>
    suspend fun getTasksByIds(ids: List<String>): List<Task>
    suspend fun getDiaryEntriesByIds(ids: List<String>): List<DiaryEntry>
    suspend fun getBookmarksByIds(ids: List<String>): List<Bookmark>
    suspend fun getThreadsByIds(ids: List<String>): List<AssistantThreadEntity>
    suspend fun getMessagesByIds(ids: List<String>): List<AssistantMessageEntity>

    suspend fun deleteNoteById(id: String)
    suspend fun deleteNoteFolderById(id: String)
    suspend fun deleteTaskById(id: String)
    suspend fun deleteDiaryEntryById(id: String)
    suspend fun deleteBookmarkById(id: String)
    suspend fun deleteThreadAndMessages(id: String)
    suspend fun deleteMessage(id: String)

    suspend fun insertDeletedEntity(deletedEntity: DeletedEntityEntity)

    suspend fun upsertThreads(threads: List<AssistantThreadEntity>)
    suspend fun upsertMessages(messages: List<AssistantMessageEntity>)
}

@Single
class SyncRepositoryImpl(
    private val noteDao: NoteDao,
    private val taskDao: TaskDao,
    private val diaryDao: DiaryDao,
    private val bookmarkDao: BookmarkDao,
    private val assistantDao: AssistantDao,
    private val syncDao: SyncDao
) : SyncRepository {

    override suspend fun getMaxSyncSequence(): Long {
        return syncDao.getLastSyncSequence()
    }

    override suspend fun getNextSyncSequences(
        seq: Long,
        maxSeq: Long,
        limit: Int
    ): List<Long> {
        return syncDao.getNextSyncSequences(seq, maxSeq, limit)
    }

    override suspend fun getNotesAfterSeq(seq: Long, maxSeq: Long): List<Note> {
        return noteDao.getNotesAfterSeq(seq, maxSeq).map { it.toNote() }
    }

    override suspend fun getNoteFoldersAfterSeq(seq: Long, maxSeq: Long): List<NoteFolder> {
        return noteDao.getNoteFoldersAfterSeq(seq, maxSeq).map { it.toNoteFolder() }
    }

    override suspend fun getTasksAfterSeq(seq: Long, maxSeq: Long): List<Task> {
        return taskDao.getTasksAfterSeq(seq, maxSeq).map { it.toTask() }
    }

    override suspend fun getDiaryEntriesAfterSeq(seq: Long, maxSeq: Long): List<DiaryEntry> {
        return diaryDao.getDiaryEntriesAfterSeq(seq, maxSeq).map { it.toDiaryEntry() }
    }

    override suspend fun getBookmarksAfterSeq(seq: Long, maxSeq: Long): List<Bookmark> {
        return bookmarkDao.getBookmarksAfterSeq(seq, maxSeq).map { it.toBookmark() }
    }

    override suspend fun getThreadsAfterSeq(seq: Long, maxSeq: Long): List<AssistantThreadEntity> {
        return assistantDao.getThreadsAfterSeq(seq, maxSeq)
    }

    override suspend fun getMessagesAfterSeq(seq: Long, maxSeq: Long): List<AssistantMessageEntity> {
        return assistantDao.getMessagesAfterSeq(seq, maxSeq)
    }

    override suspend fun getDeletedEntitiesAfterSeq(seq: Long, maxSeq: Long): List<DeletedEntityEntity> {
        return syncDao.getDeletedEntitiesUpdatedAfter(seq, maxSeq)
    }

    override suspend fun getDeletedEntityIds(
        entityType: String,
        entityIds: List<String>
    ): List<String> {
        return if (entityIds.isEmpty()) emptyList() else syncDao.getDeletedEntityIds(entityType, entityIds)
    }

    override suspend fun deletedEntityExists(entityType: String, entityId: String): Boolean {
        return syncDao.deletedEntityExists(entityType, entityId)
    }

    override suspend fun deleteExpiredTombstones(cutoff: Long, limit: Int): Int {
        return syncDao.deleteExpiredTombstones(cutoff, limit)
    }

    override suspend fun getNotesByIds(ids: List<String>): List<Note> {
        return noteDao.getNotesByIds(ids).map { it.toNote() }
    }

    override suspend fun getNoteFoldersByIds(ids: List<String>): List<NoteFolder> {
        return noteDao.getNoteFoldersByIds(ids).map { it.toNoteFolder() }
    }

    override suspend fun getTasksByIds(ids: List<String>): List<Task> {
        return taskDao.getTasksByIds(ids).map { it.toTask() }
    }

    override suspend fun getDiaryEntriesByIds(ids: List<String>): List<DiaryEntry> {
        return diaryDao.getEntriesByIds(ids).map { it.toDiaryEntry() }
    }

    override suspend fun getBookmarksByIds(ids: List<String>): List<Bookmark> {
        return bookmarkDao.getBookmarksByIds(ids).map { it.toBookmark() }
    }

    override suspend fun getThreadsByIds(ids: List<String>): List<AssistantThreadEntity> {
        return assistantDao.getThreadsByIds(ids)
    }

    override suspend fun getMessagesByIds(ids: List<String>): List<AssistantMessageEntity> {
        return assistantDao.getMessagesByIds(ids)
    }

    override suspend fun deleteNoteById(id: String) {
        noteDao.deleteNoteById(id)
    }

    override suspend fun deleteNoteFolderById(id: String) {
        noteDao.deleteNoteFolderById(id)
    }

    override suspend fun deleteTaskById(id: String) {
        taskDao.deleteTaskById(id)
    }

    override suspend fun deleteDiaryEntryById(id: String) {
        diaryDao.deleteEntryById(id)
    }

    override suspend fun deleteBookmarkById(id: String) {
        bookmarkDao.deleteBookmarkById(id)
    }

    override suspend fun deleteThreadAndMessages(id: String) {
        assistantDao.deleteThreadAndMessages(id)
    }

    override suspend fun deleteMessage(id: String) {
        assistantDao.deleteMessage(id)
    }

    override suspend fun insertDeletedEntity(deletedEntity: DeletedEntityEntity) {
        syncDao.insertDeletedEntity(deletedEntity)
    }

    override suspend fun upsertThreads(threads: List<AssistantThreadEntity>) {
        assistantDao.upsertThreads(threads)
    }

    override suspend fun upsertMessages(messages: List<AssistantMessageEntity>) {
        assistantDao.upsertMessages(messages)
    }
}
