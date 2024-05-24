package com.mhss.app.mybrain.domain.use_case.settings

import com.mhss.app.mybrain.domain.repository.backup.BackupRepository
import org.koin.core.annotation.Single

@Single
class ExportAllDataUseCase(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(directoryUri: String, encrypted: Boolean, password: String) = repository.exportDatabase(
        directoryUri,
        encrypted,
        password
    )

}