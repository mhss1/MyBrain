package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.BackupRepository
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