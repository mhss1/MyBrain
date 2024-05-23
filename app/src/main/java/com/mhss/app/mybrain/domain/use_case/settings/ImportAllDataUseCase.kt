package com.mhss.app.mybrain.domain.use_case.settings

import android.net.Uri
import com.mhss.app.mybrain.domain.repository.backup.BackupRepository
import org.koin.core.annotation.Single

@Single
class ImportAllDataUseCase(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(fileUri: Uri, encrypted: Boolean, password: String) = repository.importDatabase(
        fileUri,
        encrypted,
        password
    )
}
