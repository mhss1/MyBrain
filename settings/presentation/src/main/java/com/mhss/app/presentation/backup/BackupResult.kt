package com.mhss.app.presentation.backup

import com.mhss.app.domain.exception.BackupDataException

sealed class BackupResult {
    data object ExportSuccess : BackupResult()
    data object ImportSuccess : BackupResult()
    data class Error(val error: BackupDataException) : BackupResult()
    data object Loading : BackupResult()
    data object Idle : BackupResult()
}
