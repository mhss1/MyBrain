package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.BackupFormat
import com.mhss.app.domain.use_case.`interface`.ExportJsonDataUseCase
import com.mhss.app.domain.use_case.`interface`.ExportMarkdownDataUseCase
import org.koin.core.annotation.Factory

@Factory
class ExportDataUseCase(
    private val exportJsonData: ExportJsonDataUseCase,
    private val exportMarkdownData: ExportMarkdownDataUseCase,
) {
    suspend operator fun invoke(
        directoryUri: String,
        exportNotes: Boolean,
        exportTasks: Boolean ,
        exportDiary: Boolean,
        exportBookmarks: Boolean,
        format: BackupFormat,
        encrypted: Boolean,
        password: String?,
    ) = when(format) {
        BackupFormat.JSON -> exportJsonData(
            directoryUri,
            exportNotes,
            exportTasks,
            exportDiary,
            exportBookmarks,
            encrypted,
            password
        )
        BackupFormat.MARKDOWN -> exportMarkdownData(
            directoryUri,
            exportNotes,
            exportTasks,
            exportDiary,
            exportBookmarks,
            encrypted,
            password
        )
    }
}