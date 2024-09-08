package com.mhss.app.domain.repository


interface BackupRepository {

    suspend fun exportDatabase(
        directoryUri: String,
        exportNotes: Boolean,
        exportTasks: Boolean ,
        exportDiary: Boolean,
        exportBookmarks: Boolean,
        encrypted: Boolean,
        password: String,
    ): Boolean

    suspend fun importDatabase(fileUri: String, encrypted: Boolean, password: String): Boolean
}