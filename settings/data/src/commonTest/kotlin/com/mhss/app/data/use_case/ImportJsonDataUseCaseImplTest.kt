package com.mhss.app.data.use_case

import com.mhss.app.alarm.repository.AlarmRepository
import com.mhss.app.alarm.repository.AlarmScheduler
import com.mhss.app.alarm.use_case.DeleteAlarmUseCase
import com.mhss.app.alarm.use_case.UpsertAlarmUseCase
import com.mhss.app.database.helpers.DatabaseTransactionProvider
import com.mhss.app.domain.exception.BackupDataException
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.model.backup.BackupBookmark
import com.mhss.app.domain.model.backup.BackupDiaryEntry
import com.mhss.app.domain.model.backup.BackupNote
import com.mhss.app.domain.model.backup.BackupNoteFolder
import com.mhss.app.domain.model.backup.BackupTask
import com.mhss.app.domain.model.backup.JsonBackupData
import com.mhss.app.domain.repository.BookmarkRepository
import com.mhss.app.domain.repository.DiaryRepository
import com.mhss.app.domain.repository.NoteRepository
import com.mhss.app.domain.repository.TaskRepository
import com.mhss.app.domain.use_case.UpsertTaskUseCase
import com.mhss.app.storage.BufferedFileWriter
import com.mhss.app.storage.ReadJsonFileResult
import com.mhss.app.storage.StorageManager
import com.mhss.app.storage.WriteTextFileResult
import com.mhss.app.widget.WidgetUpdater
import dev.mokkery.answering.calls
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImportJsonDataUseCaseImplTest {

    private lateinit var storage: StreamingTestStorageManager
    private lateinit var transactionProvider: RecordingTransactionProvider
    private lateinit var noteRepository: NoteRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var diaryRepository: DiaryRepository
    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var noteFolderBatches: MutableList<List<NoteFolder>>
    private lateinit var noteBatches: MutableList<List<Note>>
    private lateinit var tasks: MutableList<Task>
    private lateinit var diaryBatches: MutableList<List<DiaryEntry>>
    private lateinit var bookmarkBatches: MutableList<List<Bookmark>>
    private lateinit var useCase: ImportJsonDataUseCaseImpl

    @BeforeTest
    fun setUp() {
        storage = StreamingTestStorageManager()
        transactionProvider = RecordingTransactionProvider()
        noteRepository = mock()
        taskRepository = mock()
        diaryRepository = mock()
        bookmarkRepository = mock()
        noteFolderBatches = mutableListOf()
        noteBatches = mutableListOf()
        tasks = mutableListOf()
        diaryBatches = mutableListOf()
        bookmarkBatches = mutableListOf()

        everySuspend {
            noteRepository.upsertNoteFolders(any(), any())
        } calls { (folders: List<NoteFolder>, _: Boolean) -> noteFolderBatches.add(folders) }
        everySuspend {
            noteRepository.upsertNotes(any(), any())
        } calls { (notes: List<Note>, _: Boolean) ->
            noteBatches.add(notes)
            emptyList()
        }
        everySuspend {
            taskRepository.upsertTask(any(), any())
        } calls { (task: Task, _: Boolean) -> tasks.add(task) }
        everySuspend {
            diaryRepository.upsertEntries(any(), any())
        } calls { (entries: List<DiaryEntry>, _: Boolean) -> diaryBatches.add(entries) }
        everySuspend {
            bookmarkRepository.upsertBookmarks(any(), any())
        } calls { (bookmarks: List<Bookmark>, _: Boolean) -> bookmarkBatches.add(bookmarks) }

        val alarmRepository = mock<AlarmRepository>()
        val alarmScheduler = mock<AlarmScheduler>()
        val upsertTaskUseCase = UpsertTaskUseCase(
            tasksRepository = taskRepository,
            upsertAlarm = UpsertAlarmUseCase(alarmRepository, alarmScheduler),
            deleteAlarmUseCase = DeleteAlarmUseCase(alarmRepository, alarmScheduler),
            widgetUpdater = mock<WidgetUpdater>()
        )
        useCase = ImportJsonDataUseCaseImpl(
            storageManager = storage,
            transactionProvider = transactionProvider,
            noteRepository = noteRepository,
            upsertTaskUseCase = upsertTaskUseCase,
            diaryRepository = diaryRepository,
            bookmarkRepository = bookmarkRepository,
            ioDispatcher = Dispatchers.Unconfined
        )
    }

    @Test
    fun `import inserts large arrays in bounded batches`() = runTest {
        val notes = (0..100).map { BackupNote(id = "note-$it", title = "Note $it") }
        val diary = (0..100).map { BackupDiaryEntry(id = "diary-$it", title = "Entry $it") }
        val bookmarks = (0..100).map {
            BackupBookmark(id = "bookmark-$it", title = "Bookmark $it", url = "https://example.com/$it")
        }
        storage.content = backupJson.encodeToString(
            JsonBackupData(
                notes = notes,
                noteFolders = listOf(BackupNoteFolder(id = "folder-1", name = "Folder")),
                tasks = listOf(
                    BackupTask(id = "task-1", title = "First"),
                    BackupTask(id = "task-2", title = "Second")
                ),
                diary = diary,
                bookmarks = bookmarks
            )
        )

        useCase("backup", encrypted = false, password = null)

        assertEquals(listOf(1), noteFolderBatches.map(List<*>::size))
        assertEquals(listOf(100, 1), noteBatches.map(List<*>::size))
        assertEquals(listOf(100, 1), diaryBatches.map(List<*>::size))
        assertEquals(listOf(100, 1), bookmarkBatches.map(List<*>::size))
        assertEquals(listOf("task-1", "task-2"), tasks.map(Task::id))
        assertEquals(notes.map(BackupNote::id), noteBatches.flatten().map(Note::id))
        assertEquals(
            listOf(
                setOf("noteFolders"),
                setOf("notes", "tasks", "diary", "bookmarks")
            ),
            storage.requestedArrays
        )
        assertEquals(1, transactionProvider.transactionCount)
        assertFalse(transactionProvider.rolledBack)
    }

    @Test
    fun `legacy numeric ids preserve note folder relationships`() = runTest {
        storage.content = """
            {
              "notes": [{"title":"Legacy note","folderId":7,"id":10,"unknown":"value"}],
              "noteFolders": [{"name":"Legacy folder","id":7}],
              "unknownTopLevel": {"value":true}
            }
        """.trimIndent()

        useCase("backup", encrypted = false, password = null)

        val folder = noteFolderBatches.single().single()
        val note = noteBatches.single().single()
        assertEquals(folder.id, note.folderId)
        assertEquals("Legacy folder", folder.name)
        assertEquals("Legacy note", note.title)
        assertFalse(folder.id.all(Char::isDigit))
        assertFalse(note.id.all(Char::isDigit))
    }

    @Test
    fun `missing arrays import as empty data`() = runTest {
        storage.content = """{"schemaVersion":1,"unknown":true}"""

        useCase("backup", encrypted = false, password = null)

        assertTrue(noteFolderBatches.isEmpty())
        assertTrue(noteBatches.isEmpty())
        assertTrue(tasks.isEmpty())
        assertTrue(diaryBatches.isEmpty())
        assertTrue(bookmarkBatches.isEmpty())
        assertFalse(transactionProvider.rolledBack)
    }

    @Test
    fun `invalid json reports read failure and rolls back transaction`() = runTest {
        storage.content = """{"notes":["""

        assertFailsWith<BackupDataException.CouldNotReadFile> {
            useCase("backup", encrypted = false, password = null)
        }

        assertTrue(transactionProvider.rolledBack)
    }

    private companion object {
        val backupJson = Json {
            encodeDefaults = true
            explicitNulls = false
        }
    }
}

private class RecordingTransactionProvider : DatabaseTransactionProvider {
    var transactionCount = 0
    var rolledBack = false

    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        transactionCount++
        return try {
            block()
        } catch (error: Throwable) {
            rolledBack = true
            throw error
        }
    }
}

private class StreamingTestStorageManager : StorageManager {
    var content = ""
    val requestedArrays = mutableListOf<Set<String>>()

    override suspend fun readJsonArraysFromFile(
        fileUri: String,
        arrayNames: Set<String>,
        onItem: suspend (arrayName: String, item: JsonElement) -> Unit
    ): ReadJsonFileResult {
        requestedArrays.add(arrayNames)
        val root = runCatching { Json.parseToJsonElement(content).jsonObject }
            .getOrElse { return ReadJsonFileResult.CouldNotReadFile }
        root.forEach { (name, value) ->
            if (name in arrayNames) {
                value.jsonArray.forEach { onItem(name, it) }
            }
        }
        return ReadJsonFileResult.Success
    }

    override suspend fun writeBufferedFile(
        directoryUri: String,
        fileName: String,
        mimeType: String,
        block: suspend BufferedFileWriter.() -> Unit
    ): Unit = unused()

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

    private fun <T> unused(): T = error("Unused in test")
}
