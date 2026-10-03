package com.mhss.app.presentation.backup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhss.app.domain.model.BackupFormat
import com.mhss.app.domain.model.BackupFrequency
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.PrefsKey.BooleanKey
import com.mhss.app.preferences.domain.model.PrefsKey.IntKey
import com.mhss.app.presentation.backup.components.AutoBackupCard
import com.mhss.app.presentation.backup.components.BackupFormatCard
import com.mhss.app.presentation.backup.components.BackupStatusMessage
import com.mhss.app.presentation.backup.components.ExportTypesCard
import com.mhss.app.presentation.backup.components.SectionHeader
import com.mhss.app.ui.Res
import com.mhss.app.ui.cancel
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.export
import com.mhss.app.ui.export_import
import com.mhss.app.ui.ic_export
import com.mhss.app.ui.ic_import
import com.mhss.app.ui.import_confirmation_message
import com.mhss.app.ui.import_confirmation_title
import com.mhss.app.ui.import_data
import com.mhss.app.ui.import_format
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ImportExportScreen(
    viewModel: BackupViewModel = koinViewModel(),
) {
    val backupResult by viewModel.backupResult.collectAsStateWithLifecycle()

    val encrypted by remember { mutableStateOf(false) }
    val password by remember { mutableStateOf("") }
    var exportNotes by remember { mutableStateOf(true) }
    var exportTasks by remember { mutableStateOf(true) }
    var exportDiary by remember { mutableStateOf(true) }
    var exportBookmarks by remember { mutableStateOf(true) }
    var exportFormat by remember { mutableStateOf(BackupFormat.JSON) }
    var importFormat by remember { mutableStateOf(BackupFormat.JSON) }
    var openImportDialog by rememberSaveable { mutableStateOf(false) }
    var pendingImportPath by remember { mutableStateOf<String?>(null) }

    val isAutoBackupEnabled by viewModel.getSettings(
        BooleanKey(PrefsConstants.AUTO_BACKUP_ENABLED),
        false
    ).collectAsStateWithLifecycle(false)

    val externalNotesEnabled by viewModel.getSettings(
        BooleanKey(PrefsConstants.EXTERNAL_NOTES_ENABLED),
        false
    ).collectAsStateWithLifecycle(false)

    val autoBackupFrequencyValue by viewModel.getSettings(
        IntKey(PrefsConstants.AUTO_BACKUP_FREQUENCY),
        BackupFrequency.DAILY.value
    ).collectAsStateWithLifecycle(BackupFrequency.DAILY.value)
    val autoBackupFrequencyAmount by viewModel.getSettings(
        IntKey(PrefsConstants.AUTO_BACKUP_FREQUENCY_AMOUNT),
        1
    ).collectAsStateWithLifecycle(1)

    val autoBackupFrequency =
        BackupFrequency.entries.firstOrNull { it.value == autoBackupFrequencyValue }
            ?: BackupFrequency.DAILY

    val kmpContext = LocalPlatformContext.current
    val pickFileLauncher = rememberFilePickerLauncher(
        FilePickerFileType.Document,
        selectionMode = FilePickerSelectionMode.Single
    ) { files ->
        files.firstOrNull()?.getPath(kmpContext)?.let {
            pendingImportPath = it
            openImportDialog = true
        }
    }
    val chooseDirectoryLauncher = rememberFilePickerLauncher(
        FilePickerFileType.Folder,
        selectionMode = FilePickerSelectionMode.Single
    ) { files ->
        files.firstOrNull()?.getPath(kmpContext)?.let {
            viewModel.onEvent(
                BackupEvent.ExportData(
                    directoryUri = it,
                    exportNotes = exportNotes && !externalNotesEnabled,
                    exportTasks = exportTasks,
                    exportDiary = exportDiary,
                    exportBookmarks = exportBookmarks,
                    format = exportFormat,
                    encrypted = encrypted,
                    password = password
                )
            )
        }
    }

    val chooseAutoBackupDirectoryLauncher = rememberFilePickerLauncher(
        FilePickerFileType.Folder,
        selectionMode = FilePickerSelectionMode.Single
    ) { files ->
        files.firstOrNull()?.let { file ->
            viewModel.onEvent(BackupEvent.SelectAutoBackupFolder(folderUri = file.uri.toString()))
        }
    }

    Scaffold(
        topBar = { MyBrainAppBar(stringResource(Res.string.export_import)) }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            BackupStatusMessage(backupResult = backupResult)

            SectionHeader(
                title = stringResource(Res.string.export),
                iconRes = Res.drawable.ic_export
            )
            Spacer(Modifier.height(8.dp))
            BackupFormatCard(
                selectedFormat = exportFormat,
                onFormatSelected = { exportFormat = it }
            )
            Spacer(Modifier.height(10.dp))

            ExportTypesCard(
                exportNotes = exportNotes && !externalNotesEnabled,
                notesExportEnabled = !externalNotesEnabled,
                exportTasks = exportTasks,
                exportDiary = exportDiary,
                exportBookmarks = exportBookmarks,
                onExportNotesChanged = { exportNotes = it },
                onExportTasksChanged = { exportTasks = it },
                onExportDiaryChanged = { exportDiary = it },
                onExportBookmarksChanged = { exportBookmarks = it }
            )
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { chooseDirectoryLauncher.launch() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Icon(
                    painterResource(Res.drawable.ic_export),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.export),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(20.dp))

            SectionHeader(
                title = stringResource(Res.string.import_data),
                iconRes = Res.drawable.ic_import
            )
            Spacer(Modifier.height(6.dp))
            BackupFormatCard(
                selectedFormat = importFormat,
                onFormatSelected = { importFormat = it },
                options = listOf(UiExportFormat.JSON),
                titleRes = Res.string.import_format
            )
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { pickFileLauncher.launch() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painterResource(Res.drawable.ic_import),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.import_data),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
            if (backupResult == BackupResult.Loading) {
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(32.dp),
                    strokeWidth = 3.dp
                )
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(20.dp))

            val selectedBackupFolder by viewModel.getAutoBackupFolderPath()
                .collectAsStateWithLifecycle(null)
            AutoBackupCard(
                isEnabled = isAutoBackupEnabled,
                selectedFolder = selectedBackupFolder,
                frequency = autoBackupFrequency,
                frequencyAmount = autoBackupFrequencyAmount,
                onSwitchToggled = {
                    viewModel.onEvent(BackupEvent.SetAutoBackupEnabled(it))
                },
                onSaveFrequencies = { frequency, amount ->
                    viewModel.onEvent(BackupEvent.SaveFrequenciesAndReschedule(frequency, amount))
                },
                chooseDirectoryLauncher = chooseAutoBackupDirectoryLauncher
            )
            Spacer(Modifier.height(24.dp))
        }
        if (openImportDialog)
            AlertDialog(
                shape = RoundedCornerShape(25.dp),
                onDismissRequest = {
                    openImportDialog = false
                    pendingImportPath = null
                },
                title = { Text(stringResource(Res.string.import_confirmation_title)) },
                text = {
                    Text(stringResource(Res.string.import_confirmation_message))
                },
                confirmButton = {
                    Button(
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            pendingImportPath?.let { path ->
                                viewModel.onEvent(
                                    BackupEvent.ImportData(
                                        path,
                                        importFormat,
                                        encrypted,
                                        password
                                    )
                                )
                            }
                            openImportDialog = false
                            pendingImportPath = null
                        },
                    ) {
                        Text(stringResource(Res.string.import_data))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openImportDialog = false
                            pendingImportPath = null
                        }) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            )
    }
}
