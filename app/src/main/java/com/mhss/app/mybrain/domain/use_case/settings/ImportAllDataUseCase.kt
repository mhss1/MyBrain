package com.mhss.app.mybrain.domain.use_case.settings

import android.net.Uri
import com.mhss.app.mybrain.domain.repository.BackupRepository
import javax.inject.Inject

class ImportAllDataUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(fileUri: Uri, encrypted: Boolean, password: String) = repository.importDatabase(
        fileUri,
        encrypted,
        password
    )
}
