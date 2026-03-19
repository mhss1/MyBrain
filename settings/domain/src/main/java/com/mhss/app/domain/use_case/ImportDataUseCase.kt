package com.mhss.app.domain.use_case

import com.mhss.app.domain.exception.BackupDataException
import com.mhss.app.domain.model.BackupFormat
import com.mhss.app.domain.use_case.`interface`.ImportJsonDataUseCase
import org.koin.core.annotation.Factory

@Factory
class ImportDataUseCase(
    private val importJsonData: ImportJsonDataUseCase
) {
    suspend operator fun invoke(
        fileUri: String,
        format: BackupFormat,
        encrypted: Boolean,
        password: String
    ) = when (format) {
        BackupFormat.JSON -> importJsonData(fileUri, encrypted, password)
        BackupFormat.MARKDOWN -> throw BackupDataException.GenericError(
            details = "Markdown import is not supported",
        )
    }
}
