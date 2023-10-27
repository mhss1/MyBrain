package com.mhss.app.mybrain.data.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.mhss.app.mybrain.data.local.MyBrainDatabase
import com.mhss.app.mybrain.domain.repository.RoomBackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class RoomBackupRepositoryImpl @Inject constructor(
    private val database: MyBrainDatabase,
    private val context: Context,
) : RoomBackupRepository {

    private val dbPath = File(context.getDatabasePath(database.openHelper.databaseName).toURI())

    override suspend fun exportDatabase(
        directoryUri: Uri,
        encrypted: Boolean, // To be added in a future version
        password: String // To be added in a future version
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "MyBrain_Backup_${System.currentTimeMillis()}.sqlite3"
                val pickedDir = DocumentFile.fromTreeUri(context, directoryUri)
                val destination = pickedDir!!.createFile("application/sqlite3", fileName)


                val outputStream =
                    destination?.let { context.contentResolver.openOutputStream(it.uri) }
                        ?: return@withContext false

                dbPath.inputStream().copyTo(outputStream)

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
                database.close()
                context.contentResolver.openInputStream(fileUri)?.use {
                    it.copyTo(dbPath.outputStream())
                } ?: return@withContext false
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}