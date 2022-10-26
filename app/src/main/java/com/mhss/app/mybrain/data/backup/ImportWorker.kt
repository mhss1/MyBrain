package com.mhss.app.mybrain.data.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.app.getString
import com.mhss.app.mybrain.data.local.dao.BookmarkDao
import com.mhss.app.mybrain.data.local.dao.DiaryDao
import com.mhss.app.mybrain.data.local.dao.NoteDao
import com.mhss.app.mybrain.data.local.dao.TaskDao
import com.mhss.app.mybrain.domain.model.Bookmark
import com.mhss.app.mybrain.domain.model.DiaryEntry
import com.mhss.app.mybrain.domain.model.NotesBackUp
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.util.BackupUtil.listFromJson
import com.mhss.app.mybrain.util.BackupUtil.objectFromJson
import com.mhss.app.mybrain.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream

@HiltWorker
class ImportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notesDao: NoteDao,
    private val tasksDao: TaskDao,
    private val diaryDao: DiaryDao,
    private val bookmarksDao: BookmarkDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork() = withContext(Dispatchers.IO) {

        val backupDir = inputData.getString("uri") ?: return@withContext Result.failure()

        import(backupDir)

    }

    private suspend fun import(uriString: String?): Result {
        val contentResolver = applicationContext.contentResolver

        val uri = Uri.parse(uriString)
        val fileName = uri.fileName()

        uri?.let {
            val json = contentResolver.readTextFromFile(uri)
            json?.let {
                try {
                    return when(fileName){
                        Constants.BACKUP_NOTES_FILE_NAME -> {
                            val notesObject = json.objectFromJson<NotesBackUp>() ?: return Result.failure()
                            val folders = notesObject.folders.map{it.copy(id = 0)}
                            val notes = notesObject.notes.map{it.copy(id = 0, folderId = null)}
                            notesDao.insertNoteFolders(folders)
                            notesDao.insertNotes(notes)
                            Result.success(workDataOf("success" to getString(R.string.notes)))
                        }
                        Constants.BACKUP_TASKS_FILE_NAME -> {
                            val tasks = json.listFromJson<Task>()?.map{it.copy(id = 0)} ?: return Result.failure()
                            tasksDao.insertTasks(tasks)
                            Result.success(workDataOf("success" to getString(R.string.tasks)))
                        }
                        Constants.BACKUP_DIARY_FILE_NAME -> {
                            val diaryEntries = json.listFromJson<DiaryEntry>()?.map{it.copy(id = 0)} ?: return Result.failure()
                            diaryDao.insertEntries(diaryEntries)
                            Result.success(workDataOf("success" to getString(R.string.diary)))
                        }
                        Constants.BACKUP_BOOKMARKS_FILE_NAME -> {
                            val bookmarks = json.listFromJson<Bookmark>()?.map{it.copy(id = 0)} ?: return Result.failure()
                            bookmarksDao.insertBookmarks(bookmarks)
                            Result.success(workDataOf("success" to getString(R.string.bookmarks)))
                        }
                        else -> Result.failure()
                    }
                }catch (e: Exception){
                    return Result.failure()
                }
            }
        } ?: return Result.failure()
    }

    private fun ContentResolver.readTextFromFile(uri: Uri?): String? {
        return uri?.let {
            openFileDescriptor(uri, "r")?.use { pfd ->
                FileInputStream(pfd.fileDescriptor).use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                }
            }
        }
    }

    private fun Uri.fileName(): String {
        val resolver = applicationContext.contentResolver
        val query = resolver.query(
            this,
            null,
            null,
            null,
            null
        )
        query?.use { cursor ->
            val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                return cursor.getString(id)
            }
        }
        return ""
    }

}