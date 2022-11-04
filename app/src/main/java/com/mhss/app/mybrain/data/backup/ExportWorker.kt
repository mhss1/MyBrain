package com.mhss.app.mybrain.data.backup

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mhss.app.mybrain.data.local.dao.BookmarkDao
import com.mhss.app.mybrain.data.local.dao.DiaryDao
import com.mhss.app.mybrain.data.local.dao.NoteDao
import com.mhss.app.mybrain.data.local.dao.TaskDao
import com.mhss.app.mybrain.domain.model.NotesBackUp
import com.mhss.app.mybrain.util.BackupUtil.toJson
import com.mhss.app.mybrain.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


@HiltWorker
class ExportWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notesDao: NoteDao,
    private val tasksDao: TaskDao,
    private val diaryDao: DiaryDao,
    private val bookmarksDao: BookmarkDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork() = withContext(Dispatchers.IO) {
        export()
    }

    private suspend fun export(): Result {
        return try {
            val notes = notesDao.getAllNotes().first()
            val folders = notesDao.getAllNoteFolders().first()
            val notesBackup = NotesBackUp(notes, folders).toJson()
            saveFile(Constants.BACKUP_NOTES_FILE_NAME, notesBackup)
            setProgress(workDataOf("progress" to 25))

            val tasks = tasksDao.getAllTasks().first().toJson()
            saveFile(Constants.BACKUP_TASKS_FILE_NAME, tasks)
            setProgress(workDataOf("progress" to 50))

            val diary = diaryDao.getAllEntries().first().toJson()
            saveFile(Constants.BACKUP_DIARY_FILE_NAME, diary)
            setProgress(workDataOf("progress" to 75))

            val bookmarks = bookmarksDao.getAllBookmarks().first().toJson()
            saveFile(Constants.BACKUP_BOOKMARKS_FILE_NAME, bookmarks)
            setProgress(workDataOf("progress" to 100))

            Result.success(workDataOf("success" to true))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }


    private fun saveFile(fileName: String, content: String) {
        val contentResolver = applicationContext.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE,"text/plain")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + File.separator + "${Constants.EXPORT_DIR}/")
        }
        val fileUri = getFileUri(fileName) ?: contentResolver.insert(MediaStore.Files.getContentUri("external"), values)

        fileUri?.let {
            contentResolver.openFileDescriptor(it, "w")?.use { pfd ->
                FileOutputStream(pfd.fileDescriptor).use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
            }
        }
    }

    private fun getFileUri(name: String): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME
        )
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} = ? AND ${MediaStore.Files.FileColumns.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(
            Environment.DIRECTORY_DOCUMENTS + File.separator + "${Constants.EXPORT_DIR}/",
            name
        )
        val resolver = applicationContext.contentResolver
        val query = resolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            null
        )
        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(idColumn)
                return ContentUris.withAppendedId(
                    MediaStore.Files.getContentUri("external"),
                    id
                )
            }
        }
        return null
    } else {
            val docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val file = File(docs.path + File.separator + "${Constants.EXPORT_DIR}/", name)
            if (!file.exists()) {
                file.parentFile?.mkdirs()
                file.createNewFile()
            }
            return Uri.fromFile(file)
        }
    }

}