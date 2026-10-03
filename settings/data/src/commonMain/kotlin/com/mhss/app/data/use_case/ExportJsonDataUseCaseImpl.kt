package com.mhss.app.data.use_case

import com.mhss.app.domain.exception.BackupDataException
import com.mhss.app.domain.model.backup.BackupBookmark
import com.mhss.app.domain.model.backup.BackupDiaryEntry
import com.mhss.app.domain.model.backup.BackupNote
import com.mhss.app.domain.model.backup.BackupNoteFolder
import com.mhss.app.domain.model.backup.BackupTask
import com.mhss.app.domain.model.backup.JsonBackupData
import com.mhss.app.domain.model.backup.toBackupBookmark
import com.mhss.app.domain.model.backup.toBackupDiaryEntry
import com.mhss.app.domain.model.backup.toBackupNote
import com.mhss.app.domain.model.backup.toBackupNoteFolder
import com.mhss.app.domain.model.backup.toBackupTask
import com.mhss.app.domain.repository.BookmarkRepository
import com.mhss.app.domain.repository.DiaryRepository
import com.mhss.app.domain.repository.NoteRepository
import com.mhss.app.domain.repository.TaskRepository
import com.mhss.app.domain.use_case.`interface`.ExportJsonDataUseCase
import com.mhss.app.storage.BufferedFileWriter
import com.mhss.app.storage.StorageManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class ExportJsonDataUseCaseImpl(
    private val storageManager: StorageManager,
    private val noteRepository: NoteRepository,
    private val taskRepository: TaskRepository,
    private val diaryRepository: DiaryRepository,
    private val bookmarkRepository: BookmarkRepository,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : ExportJsonDataUseCase {

    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
    }

    override suspend fun invoke(
        directoryUri: String,
        exportNotes: Boolean,
        exportTasks: Boolean,
        exportDiary: Boolean,
        exportBookmarks: Boolean,
        encrypted: Boolean,
        password: String?
    ) {
        withContext(ioDispatcher) {
            try {
                storageManager.writeBufferedFile(
                    directoryUri = directoryUri,
                    fileName = "MyBrain_Backup_${System.currentTimeMillis()}.json",
                    mimeType = "application/json"
                ) {
                    write("{\"schemaVersion\":${JsonBackupData.CURRENT_SCHEMA_VERSION},")

                    writePagedArray(
                        propertyName = "notes",
                        enabled = exportNotes,
                        serializer = BackupNote.serializer(),
                        getId = BackupNote::id
                    ) { afterId, limit ->
                        noteRepository.getFullNotesPage(afterId, limit).map { it.toBackupNote() }
                    }

                    write(",")

                    writePagedArray(
                        propertyName = "noteFolders",
                        enabled = exportNotes,
                        serializer = BackupNoteFolder.serializer(),
                        getId = BackupNoteFolder::id
                    ) { afterId, limit ->
                        noteRepository.getNoteFoldersPage(afterId, limit).map { it.toBackupNoteFolder() }
                    }

                    write(",")

                    writePagedArray(
                        propertyName = "tasks",
                        enabled = exportTasks,
                        serializer = BackupTask.serializer(),
                        getId = BackupTask::id
                    ) { afterId, limit ->
                        taskRepository.getFullTasksPage(afterId, limit).map { it.toBackupTask() }
                    }

                    write(",")

                    writePagedArray(
                        propertyName = "diary",
                        enabled = exportDiary,
                        serializer = BackupDiaryEntry.serializer(),
                        getId = BackupDiaryEntry::id
                    ) { afterId, limit ->
                        diaryRepository.getFullEntriesPage(afterId, limit).map { it.toBackupDiaryEntry() }
                    }

                    write(",")

                    writePagedArray(
                        propertyName = "bookmarks",
                        enabled = exportBookmarks,
                        serializer = BackupBookmark.serializer(),
                        getId = BackupBookmark::id
                    ) { afterId, limit ->
                        bookmarkRepository.getFullBookmarksPage(afterId, limit).map { it.toBackupBookmark() }
                    }

                    write("}")
                }
            } catch (e: BackupDataException) {
                throw e
            } catch (_: Exception) {
                throw BackupDataException.GenericError()
            }
        }
    }

    private suspend fun <T> BufferedFileWriter.writePagedArray(
        propertyName: String,
        enabled: Boolean,
        serializer: KSerializer<T>,
        getId: (T) -> String,
        loadPage: suspend (afterId: String, limit: Int) -> List<T>
    ) {
        write("\"$propertyName\":[")
        if (enabled) {
            var afterId = ""
            var firstPage = true
            var page = loadPage(afterId, PAGE_SIZE)
            while (page.isNotEmpty()) {
                val encodedPage = json.encodeToString(ListSerializer(serializer), page)
                if (!firstPage) write(",")
                // Skip the page brackets so its items can be appended to the open backup array.
                write(encodedPage, startIndex = 1, endIndex = encodedPage.lastIndex)

                firstPage = false
                afterId = getId(page.last())
                page = loadPage(afterId, PAGE_SIZE)
            }
        }
        write("]")
    }

    private companion object {
        const val PAGE_SIZE = 100
    }
}
