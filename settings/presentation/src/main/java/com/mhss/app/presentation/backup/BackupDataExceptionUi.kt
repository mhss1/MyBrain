package com.mhss.app.presentation.backup

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mhss.app.domain.exception.BackupDataException
import com.mhss.app.ui.R

@Composable
fun BackupDataException.toUiMessage(): String = when (this) {
    is BackupDataException.InvalidBackupLocation -> stringResource(
        R.string.backup_error_invalid_location,
        uri
    )
    is BackupDataException.CouldNotCreateDirectory -> stringResource(
        R.string.backup_error_create_directory,
        path(parent, directoryName)
    )
    is BackupDataException.CouldNotCreateFile -> stringResource(
        R.string.backup_error_create_file,
        path(parent, fileName)
    )
    BackupDataException.CouldNotReadFile -> stringResource(R.string.backup_error_read_file)
    is BackupDataException.CouldNotWriteFile -> stringResource(
        R.string.backup_error_write_file,
        path(parent, fileName)
    )
    is BackupDataException.GenericError -> stringResource(R.string.backup_error_generic)
}

private fun path(parent: String, child: String): String {
    val normalizedParent = parent.trim().trimEnd('/')
    val normalizedChild = child.trim().trimStart('/')

    return when {
        normalizedParent.isBlank() -> normalizedChild
        normalizedChild.isBlank() -> normalizedParent
        else -> "$normalizedParent/$normalizedChild"
    }
}
