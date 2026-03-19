package com.mhss.app.presentation.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.exception.BackupDataException
import com.mhss.app.domain.model.BackupFormat
import com.mhss.app.domain.model.BackupFrequency
import com.mhss.app.domain.repository.BackupScheduler
import com.mhss.app.domain.repository.FileUtilsRepository
import com.mhss.app.domain.use_case.ExportDataUseCase
import com.mhss.app.domain.use_case.ImportDataUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.PrefsKey
import com.mhss.app.preferences.domain.model.PrefsKey.BooleanKey
import com.mhss.app.preferences.domain.model.PrefsKey.IntKey
import com.mhss.app.preferences.domain.model.PrefsKey.StringKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class BackupViewModel(
    private val exportData: ExportDataUseCase,
    private val importData: ImportDataUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val backupScheduler: BackupScheduler,
    private val fileUtilsRepository: FileUtilsRepository,
) : ViewModel() {

    private val _backupResult = MutableStateFlow<BackupResult>(BackupResult.Idle)
    val backupResult: StateFlow<BackupResult> = _backupResult

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        val backupError = throwable as? BackupDataException ?: BackupDataException.GenericError()
        _backupResult.update { BackupResult.Error(backupError) }
    }

    fun <T> getSettings(key: PrefsKey<T>, defaultValue: T): Flow<T> {
        return getPreference(key, defaultValue)
    }

    fun getAutoBackupFolderPath(): Flow<String?> {
        return getPreference(
            stringPreferencesKey(PrefsConstants.AUTO_BACKUP_FOLDER_URI),
            ""
        ).map { uri ->
            if (uri.isBlank()) null
            else fileUtilsRepository.getPathFromUri(uri)
        }
    }

    fun onEvent(event: BackupEvent) {
        when (event) {
            is BackupEvent.ImportData -> importDatabase(
                event.fileUri,
                event.format,
                event.encrypted,
                event.password
            )

            is BackupEvent.ExportData -> exportDatabase(
                uri = event.directoryUri,
                exportNotes = event.exportNotes,
                exportTasks = event.exportTasks,
                exportDiary = event.exportDiary,
                exportBookmarks = event.exportBookmarks,
                format = event.format,
                encrypted = event.encrypted,
                password = event.password
            )

            is BackupEvent.SetAutoBackupEnabled -> {
                viewModelScope.launch {
                    saveSettings(BooleanKey(PrefsConstants.AUTO_BACKUP_ENABLED), event.enabled)
                    if (!event.enabled) {
                        backupScheduler.cancelBackup()
                    } else {
                        // If enabling, reschedule if folder is already set
                        val folderUri = getPreference(
                            stringPreferencesKey(PrefsConstants.AUTO_BACKUP_FOLDER_URI),
                            ""
                        ).firstOrNull()
                        if (!folderUri.isNullOrBlank()) rescheduleBackup()
                    }
                }
            }

            is BackupEvent.SelectAutoBackupFolder -> {
                viewModelScope.launch {
                    saveSettings(StringKey(PrefsConstants.AUTO_BACKUP_FOLDER_URI), event.folderUri)
                    fileUtilsRepository.takePersistablePermission(event.folderUri)
                }
            }

            is BackupEvent.SaveFrequenciesAndReschedule -> {
                viewModelScope.launch {
                    saveSettings(IntKey(PrefsConstants.AUTO_BACKUP_FREQUENCY), event.frequency.value)
                    saveSettings(IntKey(PrefsConstants.AUTO_BACKUP_FREQUENCY_AMOUNT), event.amount)
                    rescheduleBackup()
                }
            }
        }
    }

    private suspend fun <T> saveSettings(key: PrefsKey<T>, value: T) {
        savePreference(key, value)
    }

    private suspend fun rescheduleBackup() {
        val isEnabled = getPreference(
            BooleanKey(PrefsConstants.AUTO_BACKUP_ENABLED),
            false
        ).first()
        val folderUri = getPreference(
            stringPreferencesKey(PrefsConstants.AUTO_BACKUP_FOLDER_URI),
            ""
        ).first()

        if (isEnabled && folderUri.isNotBlank()) {
            val frequencyValue = getPreference(
                IntKey(PrefsConstants.AUTO_BACKUP_FREQUENCY),
                BackupFrequency.DAILY.value
            ).first()
            val frequencyAmount = getPreference(
                IntKey(PrefsConstants.AUTO_BACKUP_FREQUENCY_AMOUNT),
                1
            ).first()

            val frequency = BackupFrequency.entries.firstOrNull { it.value == frequencyValue }
                ?: BackupFrequency.DAILY

            backupScheduler.scheduleBackup(
                folderUri = folderUri,
                frequency = frequency,
                frequencyAmount = frequencyAmount
            )
        } else {
            backupScheduler.cancelBackup()
        }
    }

    private fun importDatabase(
        uri: String,
        format: BackupFormat,
        encrypted: Boolean,
        password: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            _backupResult.update { BackupResult.Loading }
            importData(
                fileUri = uri,
                format = format,
                encrypted = encrypted,
                password = password
            )
            _backupResult.update { BackupResult.ImportSuccess }
        }
    }

    private fun exportDatabase(
        uri: String,
        exportNotes: Boolean,
        exportTasks: Boolean,
        exportDiary: Boolean,
        exportBookmarks: Boolean,
        format: BackupFormat,
        encrypted: Boolean,
        password: String
    ) {
        viewModelScope.launch(exceptionHandler) {
            _backupResult.update { BackupResult.Loading }
            fileUtilsRepository.takePersistablePermission(uri)
            exportData(
                directoryUri = uri,
                exportNotes = exportNotes,
                exportTasks = exportTasks,
                exportDiary = exportDiary,
                exportBookmarks = exportBookmarks,
                format = format,
                encrypted = encrypted,
                password = password
            )
            _backupResult.update { BackupResult.ExportSuccess }
        }
    }

}
