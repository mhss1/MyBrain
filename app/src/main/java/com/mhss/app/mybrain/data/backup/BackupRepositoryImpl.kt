package com.mhss.app.mybrain.data.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.room.withTransaction
import com.mhss.app.mybrain.data.local.MyBrainDatabase
import com.mhss.app.mybrain.domain.model.Bookmark
import com.mhss.app.mybrain.domain.model.DiaryEntry
import com.mhss.app.mybrain.domain.model.Note
import com.mhss.app.mybrain.domain.model.NoteFolder
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.domain.repository.BackupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

class BackupRepositoryImpl(
    private val context: Context,
    private val database: MyBrainDatabase,
    private val ioDispatcher: CoroutineDispatcher
) : BackupRepository {

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun exportDatabase(
        directoryUri: Uri,
        encrypted: Boolean, // To be added in a future version
        password: String // To be added in a future version
    ): Boolean {
        return withContext(ioDispatcher) {
            try {
                val fileName = "MyBrain_Backup_${System.currentTimeMillis()}.json"
                val pickedDir = DocumentFile.fromTreeUri(context, directoryUri)
                val destination = pickedDir!!.createFile("application/json", fileName)

                val notes = database.noteDao().getAllNotes().map {
                    it.copy(id = 0)
                }
                val noteFolders = database.noteDao().getAllNoteFolders().first()
                val tasks = database.taskDao().getAllTasks().first().map {
                    it.copy(id = 0)
                }
                val diary = database.diaryDao().getAllEntries().first().map {
                    it.copy(id = 0)
                }
                val bookmarks = database.bookmarkDao().getAllBookmarks().first().map {
                    it.copy(id = 0)
                }

                val backupData = BackupData(notes, noteFolders, tasks, diary, bookmarks)

                val outputStream =
                    destination?.let { context.contentResolver.openOutputStream(it.uri) }
                        ?: return@withContext false

                outputStream.use {
                    Json.encodeToStream(backupData, outputStream)
                }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun importDatabase(
        fileUri: Uri,
        encrypted: Boolean, // To be added in a future version
        password: String // To be added in a future version
    ): Boolean {
        return withContext(ioDispatcher) {
            try {
                val json = Json {
                    ignoreUnknownKeys = true
                }
                val backupData = context.contentResolver.openInputStream(fileUri)?.use {
                    json.decodeFromStream<BackupData>(it)
                } ?: return@withContext false
                val oldNoteFolderIds = backupData.noteFolders.map { it.id }
                database.withTransaction {
                    val newNoteFolderIds = database.noteDao().insertNoteFolders(backupData.noteFolders.map { it.copy(id = 0) })
                    if (newNoteFolderIds.size != oldNoteFolderIds.size) throw Exception("New folder count does not match old folder count.")
                    val notes = backupData.notes.map { note ->
                        if (note.folderId != null) {
                            note.copy(
                                folderId = newNoteFolderIds[oldNoteFolderIds.indexOf(note.folderId)].toInt()
                            )
                        } else note
                    }
                    database.noteDao().insertNotes(notes)
                    database.taskDao().insertTasks(backupData.tasks)
                    database.diaryDao().insertEntries(backupData.diary)
                    database.bookmarkDao().insertBookmarks(backupData.bookmarks)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    @Serializable
    private data class BackupData(
        val notes: List<Note>,
        val noteFolders: List<NoteFolder>,
        val tasks: List<Task>,
        val diary: List<DiaryEntry>,
        val bookmarks: List<Bookmark>
    )
}