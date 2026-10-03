package com.mhss.app.data.use_case

import com.mhss.app.domain.exception.BackupDataException
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.Mood
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.model.Task
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
import com.mhss.app.storage.BufferedFileWriter
import com.mhss.app.storage.ReadJsonFileResult
import com.mhss.app.storage.StorageManager
import com.mhss.app.storage.WriteTextFileResult
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ExportJsonDataUseCaseImplTest {

    private lateinit var storage: RecordingStorageManager
    private lateinit var noteRepository: NoteRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var diaryRepository: DiaryRepository
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var useCase: ExportJsonDataUseCaseImpl

    @BeforeTest
    fun setUp() {
        storage = RecordingStorageManager()
        noteRepository = mock()
        taskRepository = mock()
        diaryRepository = mock()
        bookmarkRepository = mock()
        useCase = ExportJsonDataUseCaseImpl(
            storageManager = storage,
            noteRepository = noteRepository,
            taskRepository = taskRepository,
            diaryRepository = diaryRepository,
            bookmarkRepository = bookmarkRepository,
            ioDispatcher = Dispatchers.Unconfined
        )
    }

    @Test
    fun `exported pages decode as the original backup model`() = runTest {
        val notes = (0..100).map { index ->
            Note(
                id = index.toString().padStart(3, '0'),
                title = "Note $index",
                content = if (index == 100) "Text with ], commas, quotes \" and a newline\n" else "Content $index",
                folderId = "folder-001"
            )
        }
        val folders = listOf(NoteFolder(id = "folder-001", name = "Folder"))
        val tasks = listOf(Task(id = "task-001", title = "Task"))
        val diaryEntries = listOf(DiaryEntry(id = "diary-001", title = "Entry", mood = Mood.GOOD))
        val bookmarks =
            listOf(Bookmark(id = "bookmark-001", url = "https://example.com", title = "Example"))
        val noteRequests = mutableListOf<Pair<String, Int>>()

        everySuspend {
            noteRepository.getFullNotesPage(any(), any())
        } calls { (afterId: String, limit: Int) ->
            noteRequests.add(afterId to limit)
            notes.pageAfter(afterId, limit, Note::id)
        }
        everySuspend {
            noteRepository.getNoteFoldersPage(
                any(),
                any()
            )
        } calls { (afterId: String, limit: Int) ->
            folders.pageAfter(afterId, limit, NoteFolder::id)
        }
        everySuspend {
            taskRepository.getFullTasksPage(
                any(),
                any()
            )
        } calls { (afterId: String, limit: Int) ->
            tasks.pageAfter(afterId, limit, Task::id)
        }
        everySuspend {
            diaryRepository.getFullEntriesPage(
                any(),
                any()
            )
        } calls { (afterId: String, limit: Int) ->
            diaryEntries.pageAfter(afterId, limit, DiaryEntry::id)
        }
        everySuspend {
            bookmarkRepository.getFullBookmarksPage(
                any(),
                any()
            )
        } calls { (afterId: String, limit: Int) ->
            bookmarks.pageAfter(afterId, limit, Bookmark::id)
        }
        useCase.invoke(
            directoryUri = "directory",
            exportNotes = true,
            exportTasks = true,
            exportDiary = true,
            exportBookmarks = true,
            encrypted = false,
            password = null
        )

        val decoded = Json.decodeFromString<JsonBackupData>(storage.content)
        assertEquals(
            JsonBackupData(
                schemaVersion = JsonBackupData.CURRENT_SCHEMA_VERSION,
                notes = notes.map { it.toBackupNote() },
                noteFolders = folders.map { it.toBackupNoteFolder() },
                tasks = tasks.map { it.toBackupTask() },
                diary = diaryEntries.map { it.toBackupDiaryEntry() },
                bookmarks = bookmarks.map { it.toBackupBookmark() }
            ),
            decoded
        )
        assertEquals(listOf("" to 100, "099" to 100, "100" to 100), noteRequests)
        assertEquals("application/json", storage.mimeType)
        assertTrue(storage.fileName.startsWith("MyBrain_Backup_"))
        assertTrue(storage.fileName.endsWith(".json"))
    }

    @Test
    fun `disabled export types produce valid empty arrays without loading repositories`() =
        runTest {
            useCase.invoke(
                directoryUri = "directory",
                exportNotes = false,
                exportTasks = false,
                exportDiary = false,
                exportBookmarks = false,
                encrypted = false,
                password = null
            )

            assertEquals(
                JsonBackupData(schemaVersion = JsonBackupData.CURRENT_SCHEMA_VERSION),
                Json.decodeFromString<JsonBackupData>(storage.content)
            )
        }

    @Test
    fun `enabled export types with no records produce valid empty arrays`() = runTest {
        everySuspend { noteRepository.getFullNotesPage(any(), any()) } returns emptyList()
        everySuspend { noteRepository.getNoteFoldersPage(any(), any()) } returns emptyList()
        everySuspend { taskRepository.getFullTasksPage(any(), any()) } returns emptyList()
        everySuspend { diaryRepository.getFullEntriesPage(any(), any()) } returns emptyList()
        everySuspend { bookmarkRepository.getFullBookmarksPage(any(), any()) } returns emptyList()
        useCase.invoke(
            directoryUri = "directory",
            exportNotes = true,
            exportTasks = true,
            exportDiary = true,
            exportBookmarks = true,
            encrypted = false,
            password = null
        )

        assertEquals(
            JsonBackupData(schemaVersion = JsonBackupData.CURRENT_SCHEMA_VERSION),
            Json.decodeFromString<JsonBackupData>(storage.content)
        )
    }

    @Test
    fun `repository failures are reported as generic backup errors`() = runTest {
        everySuspend { noteRepository.getFullNotesPage(any(), any()) } throws IllegalStateException(
            "failure"
        )

        assertFailsWith<BackupDataException.GenericError> {
            useCase.invoke(
                directoryUri = "directory",
                exportNotes = true,
                exportTasks = false,
                exportDiary = false,
                exportBookmarks = false,
                encrypted = false,
                password = null
            )
        }
    }
}

private fun <T> List<T>.pageAfter(
    afterId: String,
    limit: Int,
    getId: (T) -> String
): List<T> = asSequence()
    .filter { getId(it) > afterId }
    .sortedBy(getId)
    .take(limit)
    .toList()

private class RecordingStorageManager : StorageManager {
    var content = ""
    var fileName = ""
    var mimeType = ""

    override suspend fun writeBufferedFile(
        directoryUri: String,
        fileName: String,
        mimeType: String,
        block: suspend BufferedFileWriter.() -> Unit
    ) {
        val content = StringBuilder()
        this.fileName = fileName
        this.mimeType = mimeType
        object : BufferedFileWriter {
            override suspend fun write(value: String, startIndex: Int, endIndex: Int) {
                content.append(value, startIndex, endIndex)
            }
        }.block()
        this.content = content.toString()
    }

    override suspend fun directoryExists(directoryUri: String): Boolean = unused()
    override suspend fun getDisplayName(directoryUri: String): String = unused()
    override suspend fun createUniqueDirectory(
        parentDirectoryUri: String,
        baseName: String
    ): String? = unused()

    override suspend fun listFileNames(directoryUri: String): Set<String> = unused()

    override suspend fun writeTextFile(
        directoryUri: String,
        preferredName: String,
        extension: String,
        mimeType: String,
        content: String,
        existingFileNames: MutableSet<String>
    ): WriteTextFileResult = unused()

    override suspend fun readJsonArraysFromFile(
        fileUri: String,
        arrayNames: Set<String>,
        onItem: suspend (arrayName: String, item: JsonElement) -> Unit
    ): ReadJsonFileResult = unused()

    private fun <T> unused(): T = error("Unused in test")
}
