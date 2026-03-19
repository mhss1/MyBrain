package com.mhss.app.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mhss.app.domain.exception.BackupDataException
import com.mhss.app.domain.model.BackupFormat
import com.mhss.app.domain.use_case.ExportDataUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import kotlinx.coroutines.flow.firstOrNull
import org.koin.android.annotation.KoinWorker

@KoinWorker
class BackupWorker(
    private val exportData: ExportDataUseCase,
    private val getPreference: GetPreferenceUseCase,
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {


    override suspend fun doWork(): Result {
        val folderUri = getPreference(
            stringPreferencesKey(PrefsConstants.AUTO_BACKUP_FOLDER_URI),
            ""
        ).firstOrNull()

        if (folderUri.isNullOrBlank()) return Result.failure()

        return try {
            exportData(
                directoryUri = folderUri,
                exportNotes = true,
                exportTasks = true,
                exportDiary = true,
                exportBookmarks = true,
                format = BackupFormat.JSON,
                encrypted = false,
                password = null
            )
            Result.success()
        } catch (_: BackupDataException) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "auto_backup_work"
    }
}
