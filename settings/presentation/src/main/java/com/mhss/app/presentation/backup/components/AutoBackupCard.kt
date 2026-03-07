package com.mhss.app.presentation.backup.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.model.BackupFrequency
import com.mhss.app.ui.R
import com.mhss.app.ui.components.common.NumberPicker
import com.mhss.app.ui.theme.MyBrainTheme
import com.mhss.app.ui.titleRes
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerLauncher
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher

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
    var frequencyMenuVisible by remember { mutableStateOf(false) }

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
                    onCheckedChange = onSwitchToggled
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
                            onItemSelected = { localFrequency = it },
                            onDismissRequest = { frequencyMenuVisible = false },
                            onClick = { frequencyMenuVisible = true }
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
private fun <T> DropDownItem(
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
            Text(text = getText(selectedItem))
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
