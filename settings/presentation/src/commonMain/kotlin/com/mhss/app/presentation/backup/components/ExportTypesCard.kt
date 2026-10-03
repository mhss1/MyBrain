package com.mhss.app.presentation.backup.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.Res
import com.mhss.app.ui.bookmarks
import com.mhss.app.ui.diary
import com.mhss.app.ui.export_types
import com.mhss.app.ui.markdown_notes_export_not_supported
import com.mhss.app.ui.notes
import com.mhss.app.ui.tasks
import com.mhss.app.ui.theme.MyBrainTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportTypesCard(
    exportNotes: Boolean,
    notesExportEnabled: Boolean,
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
                text = stringResource(Res.string.export_types),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ExportTypeChip(
                    text = stringResource(Res.string.notes),
                    selected = exportNotes,
                    enabled = notesExportEnabled,
                    onClick = { onExportNotesChanged(!exportNotes) }
                )
                ExportTypeChip(
                    text = stringResource(Res.string.tasks),
                    selected = exportTasks,
                    onClick = { onExportTasksChanged(!exportTasks) }
                )
                ExportTypeChip(
                    text = stringResource(Res.string.diary),
                    selected = exportDiary,
                    onClick = { onExportDiaryChanged(!exportDiary) }
                )
                ExportTypeChip(
                    text = stringResource(Res.string.bookmarks),
                    selected = exportBookmarks,
                    onClick = { onExportBookmarksChanged(!exportBookmarks) }
                )
            }
            if (!notesExportEnabled) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(Res.string.markdown_notes_export_not_supported),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExportTypeChip(
    text: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier,
        shape = CircleShape
    )
}

@Preview(showBackground = true)
@Composable
private fun ExportTypesCardPreview() {
    MyBrainTheme {
        ExportTypesCard(
            exportNotes = false,
            notesExportEnabled = false,
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
