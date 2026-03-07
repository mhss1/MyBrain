package com.mhss.app.domain.use_case.`interface`
 
interface ExportMarkdownDataUseCase {
    suspend operator fun invoke(
        directoryUri: String,
        exportNotes: Boolean,
        exportTasks: Boolean,
        exportDiary: Boolean,
        exportBookmarks: Boolean,
        encrypted: Boolean,
        password: String?,
    ): Boolean
}