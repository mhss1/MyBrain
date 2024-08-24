package com.mhss.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.use_case.ExportAllDataUseCase
import com.mhss.app.domain.use_case.ImportAllDataUseCase
import com.mhss.app.preferences.domain.model.PrefsKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SettingsViewModel(
    private val savePreference: SavePreferenceUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val exportData: ExportAllDataUseCase,
    private val importData: ImportAllDataUseCase
) : ViewModel() {

    private val _backupResult = MutableStateFlow<BackupResult>(BackupResult.None)
    val backupResult: StateFlow<BackupResult> = _backupResult

    fun <T> getSettings(key: PrefsKey<T>, defaultValue: T): Flow<T> {
        return getPreference(key, defaultValue)
    }

    fun <T> saveSettings(key: PrefsKey<T>, value: T) {
        viewModelScope.launch {
            savePreference(key, value)
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ImportData -> importDatabase(
                event.fileUri,
                event.encrypted,
                event.password
            )

            is SettingsEvent.ExportData -> exportDatabase(
                event.directoryUri,
                event.encrypted,
                event.password
            )
        }
    }

    private fun importDatabase(
        uri: String,
        encrypted: Boolean,
        password: String
    ) {
        viewModelScope.launch {
            _backupResult.update { BackupResult.Loading }
            val result = importData(uri, encrypted, password)
            if (result) {
                _backupResult.update { BackupResult.ImportSuccess }
            } else {
                _backupResult.update { BackupResult.ImportFailed }
            }
        }
    }

    private fun exportDatabase(
        uri: String,
        encrypted: Boolean,
        password: String
    ) {
        viewModelScope.launch {
            _backupResult.update { BackupResult.Loading }
            val result = exportData(uri, encrypted, password)
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