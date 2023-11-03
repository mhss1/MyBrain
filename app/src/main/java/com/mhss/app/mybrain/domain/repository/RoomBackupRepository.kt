package com.mhss.app.mybrain.domain.repository

import android.net.Uri

interface RoomBackupRepository {

    suspend fun exportDatabase(directoryUri: Uri, encrypted: Boolean, password: String): Boolean

    suspend fun importDatabase(fileUri: Uri, encrypted: Boolean, password: String): Boolean
}