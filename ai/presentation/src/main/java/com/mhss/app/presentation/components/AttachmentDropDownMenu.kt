package com.mhss.app.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.R

@Composable
fun AttachmentDropDownMenu(
    expanded: Boolean,
    excludedItems: List<AttachmentMenuItem>,
    onDismiss: () -> Unit,
    onItemClick: (AttachmentMenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = remember(excludedItems) {
        AttachmentMenuItem.entries.filterNot { it in excludedItems }
    }
    DropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        items.forEach {
            DropdownMenuItem(
                text = { Text(stringResource(it.titleRes)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(it.iconRes),
                        contentDescription = stringResource(it.titleRes),
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = { onItemClick(it) }
            )
        }
    }
}

enum class AttachmentMenuItem(val titleRes: Int, val iconRes: Int) {
    Note(R.string.add_note, R.drawable.ic_add_note),
    Task(R.string.add_task, R.drawable.ic_check),
    CalendarEvents(R.string.calendar_events_next_7_days, R.drawable.ic_calendar)
}