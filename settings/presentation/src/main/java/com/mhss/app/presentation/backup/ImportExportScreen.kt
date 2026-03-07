package com.mhss.app.presentation.backup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhss.app.domain.model.BackupFormat
import com.mhss.app.domain.model.BackupFrequency
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.PrefsKey.BooleanKey
import com.mhss.app.preferences.domain.model.PrefsKey.IntKey
import com.mhss.app.ui.R
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.components.common.NumberPicker
import com.mhss.app.ui.theme.MyBrainTheme
import com.mhss.app.ui.theme.SuccessColor
import com.mhss.app.ui.titleRes
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerLauncher
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import org.koin.androidx.compose.koinViewModel

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
                    exportNotes = exportNotes,
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
        topBar = { MyBrainAppBar(stringResource(R.string.export_import)) }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            SectionHeader(
                title = stringResource(R.string.export),
                iconRes = R.drawable.ic_export
            )
            Spacer(Modifier.height(8.dp))
            BackupFormatCard(
                selectedFormat = exportFormat,
                onFormatSelected = { exportFormat = it }
            )
            Spacer(Modifier.height(10.dp))

            ExportTypesCard(
                exportNotes = exportNotes,
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
                    painterResource(R.drawable.ic_export),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.export),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
            BackupStatusMessage(
                backupResult = backupResult,
                successResult = BackupResult.ExportSuccess,
                failureResult = BackupResult.ExportFailed,
                successText = stringResource(R.string.export_success),
                failureText = stringResource(R.string.export_failed)
            )

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(20.dp))

            SectionHeader(
                title = stringResource(R.string.import_data),
                iconRes = R.drawable.ic_import
            )
            Spacer(Modifier.height(6.dp))
            BackupFormatCard(
                selectedFormat = importFormat,
                onFormatSelected = { importFormat = it },
                options = listOf(UiExportFormat.JSON),
                titleRes = R.string.import_format
            )
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { pickFileLauncher.launch() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_import),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.import_data),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
            BackupStatusMessage(
                backupResult = backupResult,
                successResult = BackupResult.ImportSuccess,
                failureResult = BackupResult.ImportFailed,
                successText = stringResource(R.string.import_success),
                failureText = stringResource(R.string.import_failed)
            )

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
                title = { Text(stringResource(R.string.import_confirmation_title)) },
                text = {
                    Text(stringResource(R.string.import_confirmation_message))
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
                        Text(stringResource(R.string.import_data))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openImportDialog = false
                            pendingImportPath = null
                        }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BackupStatusMessage(
    backupResult: BackupResult?,
    successResult: BackupResult,
    failureResult: BackupResult,
    successText: String,
    failureText: String,
    modifier: Modifier = Modifier
) {
    when (backupResult) {
        failureResult -> {
            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = failureText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        successResult -> {
            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SuccessColor
            ) {
                Text(
                    text = successText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }

        else -> {}
    }
}

@Composable
private fun BackupFormatCard(
    selectedFormat: BackupFormat,
    onFormatSelected: (BackupFormat) -> Unit,
    modifier: Modifier = Modifier,
    options: List<UiExportFormat> = UiExportFormat.entries,
    titleRes: Int = R.string.export_format,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = options.firstOrNull { it.format == selectedFormat } ?: options.first()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Box {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 1.dp,
                    onClick = { expanded = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(selectedOption.iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(selectedOption.labelRes),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(titleRes),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(option.iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(text = stringResource(option.labelRes))
                                }
                            },
                            onClick = {
                                expanded = false
                                onFormatSelected(option.format)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExportTypesCard(
    exportNotes: Boolean,
    exportTasks: Boolean,
    exportDiary: Boolean,
    exportBookmarks: Boolean,
    onExportNotesChanged: (Boolean) -> Unit,
    onExportTasksChanged: (Boolean) -> Unit,
    onExportDiaryChanged: (Boolean) -> Unit,
    onExportBookmarksChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.export_types),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ExportTypeChip(
                    text = stringResource(R.string.notes),
                    selected = exportNotes,
                    onClick = { onExportNotesChanged(!exportNotes) }
                )
                ExportTypeChip(
                    text = stringResource(R.string.tasks),
                    selected = exportTasks,
                    onClick = { onExportTasksChanged(!exportTasks) }
                )
                ExportTypeChip(
                    text = stringResource(R.string.diary),
                    selected = exportDiary,
                    onClick = { onExportDiaryChanged(!exportDiary) }
                )
                ExportTypeChip(
                    text = stringResource(R.string.bookmarks),
                    selected = exportBookmarks,
                    onClick = { onExportBookmarksChanged(!exportBookmarks) }
                )
            }
        }
    }
}

@Composable
private fun ExportTypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier,
        shape = CircleShape
    )
}

@Composable
fun AutoBackupCard(
    isEnabled: Boolean,
    selectedFolder: String?,
    frequency: BackupFrequency,
    frequencyAmount: Int,
    onSwitchToggled: (Boolean) -> Unit,
    onSaveFrequencies: (BackupFrequency, Int) -> Unit,
    chooseDirectoryLauncher: FilePickerLauncher,
    modifier: Modifier = Modifier
) {
    var localFrequency by remember(frequency) { mutableStateOf(frequency) }
    var localFrequencyAmount by remember(frequencyAmount) { mutableIntStateOf(frequencyAmount) }

    val hasUnsavedChanges = localFrequency != frequency || localFrequencyAmount != frequencyAmount

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_refresh),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.auto_backup),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = stringResource(R.string.auto_backup_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onSwitchToggled(it) }
                )
            }
            AnimatedVisibility(isEnabled) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    AutoBackupFolderCard(
                        selectedFolder = selectedFolder,
                        chooseDirectoryLauncher = chooseDirectoryLauncher
                    )
                    Spacer(Modifier.height(12.dp))
                    var frequencyMenuVisible by remember { mutableStateOf(false) }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DropDownItem(
                            title = stringResource(R.string.backup_frequency),
                            expanded = frequencyMenuVisible,
                            items = BackupFrequency.entries,
                            selectedItem = localFrequency,
                            getText = {
                                stringResource(it.titleRes)
                            },
                            onItemSelected = {
                                frequencyMenuVisible = false
                                localFrequency = it
                            },
                            onDismissRequest = {
                                frequencyMenuVisible = false
                            },
                            onClick = {
                                frequencyMenuVisible = true
                            }
                        )
                        NumberPicker(
                            stringResource(R.string.repeats_every),
                            localFrequencyAmount
                        ) {
                            if (it > 0) localFrequencyAmount = it
                        }
                    }
                    AnimatedVisibility(hasUnsavedChanges) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    onSaveFrequencies(localFrequency, localFrequencyAmount)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.save),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoBackupFolderCard(
    selectedFolder: String?,
    chooseDirectoryLauncher: FilePickerLauncher,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.backup_folder),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { chooseDirectoryLauncher.launch() },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
                .copy(0.07f)
                .compositeOver(MaterialTheme.colorScheme.surface),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_folder),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = selectedFolder ?: stringResource(R.string.select_backup_folder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedFolder != null) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun <T> DropDownItem(
    modifier: Modifier = Modifier,
    title: String,
    expanded: Boolean,
    items: Iterable<T>,
    selectedItem: T,
    getText: @Composable (T) -> String,
    onItemSelected: (T) -> Unit,
    onDismissRequest: () -> Unit,
    onClick: () -> Unit,
) {
    Box(modifier) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        onDismissRequest()
                        onItemSelected(item)
                    },
                    text = {
                        Text(text = getText(item))
                    }
                )
            }
        }
        Row(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getText(selectedItem)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = title,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExportTypesCardPreview() {
    MyBrainTheme {
        ExportTypesCard(
            exportNotes = true,
            exportTasks = true,
            exportDiary = false,
            exportBookmarks = true,
            onExportNotesChanged = {},
            onExportTasksChanged = {},
            onExportDiaryChanged = {},
            onExportBookmarksChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BackupFormatCardPreview() {
    MyBrainTheme {
        BackupFormatCard(
            selectedFormat = BackupFormat.JSON,
            onFormatSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AutoBackupCardPreview() {
    MyBrainTheme {
        AutoBackupCard(
            isEnabled = true,
            selectedFolder = "/storage/emulated/0/Documents/MyBrain",
            frequency = BackupFrequency.DAILY,
            frequencyAmount = 1,
            onSwitchToggled = {},
            onSaveFrequencies = { _, _ -> },
            chooseDirectoryLauncher = rememberFilePickerLauncher(
                type = FilePickerFileType.Folder,
                selectionMode = FilePickerSelectionMode.Single
            ) {}
        )
    }
}
