package com.mhss.app.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.compositeOver
import com.mhss.app.domain.model.Note
import com.mhss.app.ui.ItemView
import com.mhss.app.ui.components.notes.NoteSearchContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachNoteSheet(
    state: SheetState,
    onDismissRequest: () -> Unit,
    notes: List<Note>,
    view: ItemView,
    onQueryChange: (String) -> Unit,
    onNoteClick: (Note) -> Unit
) {
    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f).compositeOver(
            MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        NoteSearchContent(
            notes = notes,
            onQueryChange = onQueryChange,
            onNoteClick = onNoteClick,
            view = view
        )
    }
}