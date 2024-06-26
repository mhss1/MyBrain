package com.mhss.app.data

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.room.withTransaction
import com.mhss.app.database.MyBrainDatabase
import com.mhss.app.database.entity.BookmarkEntity
import com.mhss.app.database.entity.DiaryEntryEntity
import com.mhss.app.database.entity.NoteEntity
import com.mhss.app.database.entity.NoteFolderEntity
import com.mhss.app.database.entity.TaskEntity
import com.mhss.app.database.entity.withoutIds
import com.mhss.app.di.namedIoDispatcher
import com.mhss.app.domain.repository.BackupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class BackupRepositoryImpl(
    private val context: Context,
    private val database: MyBrainDatabase,
    @Named(namedIoDispatcher) private val ioDispatcher: CoroutineDispatcher
) : BackupRepository {

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun exportDatabase(
        directoryUri: String,
        encrypted: Boolean, // To be added in a future version
        password: String // To be added in a future version
    ): Boolean {
        return withContext(ioDispatcher) {
            try {
                val fileName = "MyBrain_Backup_${System.currentTimeMillis()}.json"
                val pickedDir = DocumentFile.fromTreeUri(context, directoryUri.toUri())
                val destination = pickedDir!!.createFile("application/json", fileName)

                val notes = database.noteDao().getAllNotes().withoutIds()
                val noteFolders = database.noteDao().getAllNoteFolders().first().withoutIds()
                val tasks = database.taskDao().getAllTasks().first().withoutIds()
                val diary = database.diaryDao().getAllEntries().first().withoutIds()
                val bookmarks = database.bookmarkDao().getAllBookmarks().first().withoutIds()

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
        fileUri: String,
        encrypted: Boolean, // To be added in a future version
        password: String // To be added in a future version
    ): Boolean {
        return withContext(ioDispatcher) {
            try {
                val json = Json {
                    ignoreUnknownKeys = true
                }
                val backupData = context.contentResolver.openInputStream(fileUri.toUri())?.use {
                    json.decodeFromStream<BackupData>(it)
                } ?: return@withContext false
                val oldNoteFolderIds = backupData.noteFolders.map { it.id }
                database.withTransaction {
                    val newNoteFolderIds = database.noteDao().insertNoteFolders(backupData.noteFolders)
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
        val notes: List<NoteEntity>,
        val noteFolders: List<NoteFolderEntity>,
        val tasks: List<TaskEntity>,
        val diary: List<DiaryEntryEntity>,
        val bookmarks: List<BookmarkEntity>
    )
}