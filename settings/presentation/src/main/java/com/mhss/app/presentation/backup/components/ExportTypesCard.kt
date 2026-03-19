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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.R
import com.mhss.app.ui.theme.MyBrainTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportTypesCard(
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
