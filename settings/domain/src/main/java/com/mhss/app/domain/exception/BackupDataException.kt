package com.mhss.app.domain.exception

sealed class BackupDataException : Exception() {

    data class InvalidBackupLocation(
        val uri: String,
    ) : BackupDataException()

    data class CouldNotCreateDirectory(
        val directoryName: String,
        val parent: String,
    ) : BackupDataException()

    data class CouldNotCreateFile(
        val fileName: String,
        val parent: String,
    ) : BackupDataException()

    data object CouldNotReadFile : BackupDataException()

    data class CouldNotWriteFile(
        val fileName: String,
        val parent: String,
    ) : BackupDataException()

    data class GenericError(
        val details: String? = null,
    ) : BackupDataException()
}
