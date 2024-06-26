package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.BackupRepository
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
