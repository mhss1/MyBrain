package com.mhss.app.mybrain.domain.repository.backup


interface BackupRepository {

    suspend fun exportDatabase(directoryUri: String, encrypted: Boolean, password: String): Boolean

    suspend fun importDatabase(fileUri: String, encrypted: Boolean, password: String): Boolean
}