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
import com.mhss.app.domain.repository.BackupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
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
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
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

                val notes = database.noteDao().getAllNotes()
                val noteFolders = database.noteDao().getAllNoteFolders().first()
                val tasks = database.taskDao().getAllTasks().first()
                val diary = database.diaryDao().getAllEntries().first()
                val bookmarks = database.bookmarkDao().getAllBookmarks().first()

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

                val oldNoteFolderIdsMap = HashMap<Int, Int>()
                for ((i, folder) in backupData.noteFolders.withIndex()) {
                    oldNoteFolderIdsMap[folder.id] = i
                }

                database.withTransaction {
                    val newNoteFolderIds = database.noteDao().insertNoteFolders(backupData.noteFolders.withoutIds())
                    val notes = if (newNoteFolderIds.size != oldNoteFolderIdsMap.keys.size) {
                        println("BackupRepositoryImpl.importDatabase: New folder count (${newNoteFolderIds.size}) does not match old folder count. {${oldNoteFolderIdsMap.keys.size})")
                        backupData.notes.withoutIds()
                    } else backupData.notes.map { note ->
                        note.copy(
                            folderId = note.folderId?.let {
                                newNoteFolderIds[oldNoteFolderIdsMap[it]!!].toInt()
                            },
                            id = 0
                        )
                    }
                    database.noteDao().insertNotes(notes)
                    database.taskDao().insertTasks(backupData.tasks.withoutIds())
                    database.diaryDao().insertEntries(backupData.diary.withoutIds())
                    database.bookmarkDao().insertBookmarks(backupData.bookmarks.withoutIds())
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
        @SerialName("notes") val notes: List<NoteEntity>,
        @SerialName("noteFolders") val noteFolders: List<NoteFolderEntity>,
        @SerialName("tasks") val tasks: List<TaskEntity>,
        @SerialName("diary") val diary: List<DiaryEntryEntity>,
        @SerialName("bookmarks") val bookmarks: List<BookmarkEntity>
    )
}