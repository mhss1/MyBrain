package com.mhss.app.mybrain.presentation.settings

import android.net.Uri
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.mybrain.domain.repository.RoomBackupRepository
import com.mhss.app.mybrain.domain.use_case.settings.GetSettingsUseCase
import com.mhss.app.mybrain.domain.use_case.settings.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val backupRepository: RoomBackupRepository
) : ViewModel() {

    private val _backupResult = MutableStateFlow<BackupResult>(BackupResult.None)
    val backupResult: StateFlow<BackupResult> = _backupResult

    fun <T> getSettings(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return getSettingsUseCase(key, defaultValue)
    }

    fun <T> saveSettings(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            saveSettingsUseCase(key, value)
        }
    }

    fun importDatabase(
        uri: Uri,
        encrypted: Boolean,
        password: String
    ) {
        viewModelScope.launch {
            _backupResult.update { BackupResult.Loading }
            val result = backupRepository.importDatabase(uri, encrypted, password)
            if (result) {
                _backupResult.update { BackupResult.ImportSuccess }
            } else {
                _backupResult.update { BackupResult.ImportFailed }
            }
        }
    }

    fun exportDatabase(
        uri: Uri,
        encrypted: Boolean,
        password: String
    ) {
        viewModelScope.launch {
            _backupResult.update { BackupResult.Loading }
            val result = backupRepository.exportDatabase(uri, encrypted, password)
            if (result) {
                _backupResult.update { BackupResult.ExportSuccess }
            } else {
                _backupResult.update { BackupResult.ExportFailed }
            }
        }
    }

    sealed class BackupResult {
        data object ExportSuccess : BackupResult()
        data object ExportFailed : BackupResult()
        data object ImportSuccess : BackupResult()
        data object ImportFailed : BackupResult()
        data object Loading : BackupResult()
        data object None : BackupResult()
    }

}