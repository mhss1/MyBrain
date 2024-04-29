package com.mhss.app.mybrain.data.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
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

                val gson = Gson()

                val notes = notesDao.getAllNotes().first().map {
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

                val backupJson = gson.toJson(backupData)

                val outputStream =
                    destination?.let { context.contentResolver.openOutputStream(it.uri) }
                        ?: return@withContext false

                outputStream.write(backupJson.toByteArray())

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
                val gson = Gson()
                val inputStream = context.contentResolver.openInputStream(fileUri)
                val backupJson = inputStream?.bufferedReader().use { it?.readText() }
                    ?: return@withContext false
                val backupData = gson.fromJson(backupJson, BackupData::class.java)
                notesDao.insertNoteFolders(backupData.noteFolders)
                notesDao.insertNotes(backupData.notes)
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

    private data class BackupData(
        val notes: List<Note>,
        val noteFolders: List<NoteFolder>,
        val tasks: List<Task>,
        val diary: List<DiaryEntry>,
        val bookmarks: List<Bookmark>
    )
}