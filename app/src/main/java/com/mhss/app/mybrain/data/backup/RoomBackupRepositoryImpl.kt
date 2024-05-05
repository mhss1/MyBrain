package com.mhss.app.mybrain.data.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.mhss.app.mybrain.data.local.dao.BookmarkDao
import com.mhss.app.mybrain.data.local.dao.DiaryDao
import com.mhss.app.mybrain.data.local.dao.NoteDao
import com.mhss.app.mybrain.data.local.dao.TaskDao
import com.mhss.app.mybrain.domain.model.Bookmark
import com.mhss.app.mybrain.domain.model.DiaryEntry
import com.mhss.app.mybrain.domain.model.Note
import com.mhss.app.mybrain.domain.model.NoteFolder
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.domain.repository.RoomBackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RoomBackupRepositoryImpl @Inject constructor(
    private val context: Context,
    private val notesDao: NoteDao,
    private val tasksDao: TaskDao,
    private val diaryDao: DiaryDao,
    private val bookmarksDao: BookmarkDao,
) : RoomBackupRepository {

    override suspend fun exportDatabase(
        directoryUri: Uri,
        encrypted: Boolean, // To be added in a future version
        password: String // To be added in a future version
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "MyBrain_Backup_${System.currentTimeMillis()}.json"
                val pickedDir = DocumentFile.fromTreeUri(context, directoryUri)
                val destination = pickedDir!!.createFile("application/json", fileName)

                val notes = notesDao.getAllNotes().map {
                    it.copy(id = 0)
                }
                val noteFolders = notesDao.getAllNoteFolders().first()
                val tasks = tasksDao.getAllTasks().first().map {
                    it.copy(id = 0)
                }
                val diary = diaryDao.getAllEntries().first().map {
                    it.copy(id = 0)
                }
                val bookmarks = bookmarksDao.getAllBookmarks().first().map {
                    it.copy(id = 0)
                }

                val backupData = BackupData(notes, noteFolders, tasks, diary, bookmarks)

                val backupJson = Json.encodeToString(backupData)

                val outputStream =
                    destination?.let { context.contentResolver.openOutputStream(it.uri) }
                        ?: return@withContext false

                outputStream.use {
                    it.write(backupJson.toByteArray())
                }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    override suspend fun importDatabase(
        fileUri: Uri,
        encrypted: Boolean, // To be added in a future version
        password: String // To be added in a future version
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(fileUri)
                val backupJson = inputStream?.bufferedReader().use { it?.readText() }
                    ?: return@withContext false
                val backupData = Json.decodeFromString<BackupData>(backupJson)
                val noteFolders = backupData.noteFolders
                val oldNoteFolderIds = noteFolders.map { it.id }
                val newNoteFolderIds = notesDao.insertNoteFolders(noteFolders.map { it.copy(id = 0) })
                if (newNoteFolderIds.size != oldNoteFolderIds.size) return@withContext false
                val notes = backupData.notes.map { note ->
                    if (note.folderId != null) {
                        note.copy(
                            folderId = newNoteFolderIds[oldNoteFolderIds.indexOf(note.folderId)].toInt()
                        )
                    } else note
                }
                notesDao.insertNotes(notes)
                tasksDao.insertTasks(backupData.tasks)
                diaryDao.insertEntries(backupData.diary)
                bookmarksDao.insertBookmarks(backupData.bookmarks)
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