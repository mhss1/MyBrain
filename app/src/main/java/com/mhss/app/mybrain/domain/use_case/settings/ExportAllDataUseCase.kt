package com.mhss.app.mybrain.domain.use_case.settings

import android.net.Uri
import com.mhss.app.mybrain.domain.repository.BackupRepository
import org.koin.core.annotation.Single

@Single
class ExportAllDataUseCase(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(directoryUri: Uri, encrypted: Boolean, password: String) = repository.exportDatabase(
        directoryUri,
        encrypted,
        password
    )

}