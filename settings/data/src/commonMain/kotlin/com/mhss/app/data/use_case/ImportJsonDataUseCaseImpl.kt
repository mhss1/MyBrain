package com.mhss.app.data.use_case

import com.mhss.app.database.helpers.DatabaseTransactionProvider
import com.mhss.app.domain.exception.BackupDataException
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.domain.model.backup.BackupBookmark
import com.mhss.app.domain.model.backup.BackupDiaryEntry
import com.mhss.app.domain.model.backup.BackupNote
import com.mhss.app.domain.model.backup.BackupNoteFolder
import com.mhss.app.domain.model.backup.BackupTask
import com.mhss.app.domain.model.backup.toBookmark
import com.mhss.app.domain.model.backup.toDiaryEntry
import com.mhss.app.domain.model.backup.toNote
import com.mhss.app.domain.model.backup.toNoteFolder
import com.mhss.app.domain.model.backup.toTask
import com.mhss.app.domain.repository.BookmarkRepository
import com.mhss.app.domain.repository.DiaryRepository
import com.mhss.app.domain.repository.NoteRepository
import com.mhss.app.domain.use_case.UpsertTaskUseCase
import com.mhss.app.domain.use_case.`interface`.ImportJsonDataUseCase
import com.mhss.app.storage.ReadJsonFileResult
import com.mhss.app.storage.StorageManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Factory
class ImportJsonDataUseCaseImpl(
    private val storageManager: StorageManager,
    private val transactionProvider: DatabaseTransactionProvider,
    private val noteRepository: NoteRepository,
    private val upsertTaskUseCase: UpsertTaskUseCase,
    private val diaryRepository: DiaryRepository,
    private val bookmarkRepository: BookmarkRepository,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
): ImportJsonDataUseCase {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    override suspend fun invoke(
        fileUri: String,
        encrypted: Boolean,
        password: String?
    ) {
        withContext(ioDispatcher) {
            try {
                transactionProvider.runInTransaction {
                    val noteFolderIdMap = HashMap<String, String>()
                    importNoteFolders(fileUri, noteFolderIdMap)
                    importRemainingData(fileUri, noteFolderIdMap)
                }
            } catch (e: BackupDataException) {
                throw e
            } catch (_: SerializationException) {
                throw BackupDataException.CouldNotReadFile
            } catch (_: Exception) {
                throw BackupDataException.GenericError()
            }
        }
    }

    private suspend fun importNoteFolders(
        fileUri: String,
        noteFolderIdMap: MutableMap<String, String>
    ) {
        val folders = ArrayList<NoteFolder>(IMPORT_BATCH_SIZE)
        storageManager.readJsonArraysFromFile(
            fileUri = fileUri,
            arrayNames = setOf(NOTE_FOLDERS)
        ) { _, item ->
            val folder = json.decodeFromJsonElement<BackupNoteFolder>(item)

            val id = folder.id.toSafeBackupId()
            if (folder.id.all(Char::isDigit)) {
                noteFolderIdMap[folder.id] = id
            }

            folders.add(folder.copy(id = id).toNoteFolder())
            if (folders.size == IMPORT_BATCH_SIZE) {
                folders.flushWith(noteRepository::upsertNoteFolders)
            }
        }.requireSuccess()
        folders.flushWith(noteRepository::upsertNoteFolders)
    }

    private suspend fun importRemainingData(
        fileUri: String,
        noteFolderIdMap: Map<String, String>
    ) {
        val notes = ArrayList<Note>(IMPORT_BATCH_SIZE)
        val diaryEntries = ArrayList<DiaryEntry>(IMPORT_BATCH_SIZE)
        val bookmarks = ArrayList<Bookmark>(IMPORT_BATCH_SIZE)
        storageManager.readJsonArraysFromFile(
            fileUri = fileUri,
            arrayNames = setOf(NOTES, TASKS, DIARY, BOOKMARKS)
        ) { arrayName, item ->
            when (arrayName) {
                NOTES -> {
                    val note = json.decodeFromJsonElement<BackupNote>(item)
                    val folderId = note.folderId
                    val newFolderId = when {
                        folderId == null -> null
                        folderId.isBlank() -> null
                        folderId.all(Char::isDigit) -> noteFolderIdMap[folderId]
                        else -> folderId
                    }
                    notes.add(note.copy(
                        folderId = newFolderId,
                        id = note.id.toSafeBackupId()
                    ).toNote())
                    if (notes.size == IMPORT_BATCH_SIZE) {
                        notes.flushWith(noteRepository::upsertNotes)
                    }
                }

                TASKS -> {
                    val task = json.decodeFromJsonElement<BackupTask>(item)
                    upsertTaskUseCase(
                        task = task.copy(id = task.id.toSafeBackupId()).toTask(),
                        updateWidget = false
                    )
                }

                DIARY -> {
                    val entry = json.decodeFromJsonElement<BackupDiaryEntry>(item)
                    diaryEntries.add(entry.copy(id = entry.id.toSafeBackupId()).toDiaryEntry())
                    if (diaryEntries.size == IMPORT_BATCH_SIZE) {
                        diaryEntries.flushWith(diaryRepository::upsertEntries)
                    }
                }

                BOOKMARKS -> {
                    val bookmark = json.decodeFromJsonElement<BackupBookmark>(item)
                    bookmarks.add(bookmark.copy(id = bookmark.id.toSafeBackupId()).toBookmark())
                    if (bookmarks.size == IMPORT_BATCH_SIZE) {
                        bookmarks.flushWith(bookmarkRepository::upsertBookmarks)
                    }
                }
            }
        }.requireSuccess()

        notes.flushWith(noteRepository::upsertNotes)
        diaryEntries.flushWith(diaryRepository::upsertEntries)
        bookmarks.flushWith(bookmarkRepository::upsertBookmarks)
    }

    private suspend inline fun <T> MutableList<T>.flushWith(
        upsert: suspend (List<T>, Boolean) -> Unit
    ) {
        if (isEmpty()) return
        upsert(toList(), true)
        clear()
    }

    private fun ReadJsonFileResult.requireSuccess() {
        if (this == ReadJsonFileResult.CouldNotReadFile) {
            throw BackupDataException.CouldNotReadFile
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun String.toSafeBackupId(): String {
        return if (this.isBlank() || this == "null" || this.all(Char::isDigit)) {
            Uuid.generateV7().toString()
        } else {
            this
        }
    }

    private companion object {
        const val IMPORT_BATCH_SIZE = 100
        const val NOTES = "notes"
        const val NOTE_FOLDERS = "noteFolders"
        const val TASKS = "tasks"
        const val DIARY = "diary"
        const val BOOKMARKS = "bookmarks"
    }

}
