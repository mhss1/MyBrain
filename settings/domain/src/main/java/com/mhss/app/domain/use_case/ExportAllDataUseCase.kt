package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.BackupRepository
import org.koin.core.annotation.Single

@Single
class ExportAllDataUseCase(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(
        directoryUri: String,
        exportNotes: Boolean,
        exportTasks: Boolean ,
        exportDiary: Boolean,
        exportBookmarks: Boolean,
        encrypted: Boolean,
        password: String,
    ) = repository.exportDatabase(
        directoryUri = directoryUri,
        exportNotes = exportNotes,
        exportTasks = exportTasks,
        exportDiary = exportDiary,
        exportBookmarks = exportBookmarks,
        encrypted = encrypted,
        password = password
    )

}