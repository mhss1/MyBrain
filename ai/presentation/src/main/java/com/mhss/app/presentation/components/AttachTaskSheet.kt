package com.mhss.app.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.compositeOver
import com.mhss.app.domain.model.Task
import com.mhss.app.ui.components.tasks.TaskSearchContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachTaskSheet(
    state: SheetState,
    onDismissRequest: () -> Unit,
    tasks: List<Task>,
    onQueryChange: (String) -> Unit,
    onTaskClick: (Task) -> Unit
) {
    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f).compositeOver(
            MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        TaskSearchContent(
            tasks = tasks,
            onQueryChange = onQueryChange,
            onTaskClick = onTaskClick,
            onCompleteTask = {_ ->}
        )
    }
}