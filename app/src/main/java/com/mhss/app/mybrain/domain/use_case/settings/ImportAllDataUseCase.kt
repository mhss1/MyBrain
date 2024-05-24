package com.mhss.app.mybrain.domain.use_case.settings

import com.mhss.app.mybrain.domain.repository.backup.BackupRepository
import org.koin.core.annotation.Single

@Single
class ImportAllDataUseCase(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(fileUri: String, encrypted: Boolean, password: String) = repository.importDatabase(
        fileUri,
        encrypted,
        password
    )
}
