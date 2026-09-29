package com.mhss.app.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.model.SubTask
import com.mhss.app.ui.Res
import com.mhss.app.ui.delete_sub_task
import com.mhss.app.ui.ic_check
import com.mhss.app.ui.ic_delete
import com.mhss.app.ui.ic_drag_handle
import com.mhss.app.ui.preview.BasePreview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SubTaskItem(
    subTask: SubTask,
    onChange: (SubTask) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
    isDragging: Boolean = false
) {
    val dismissState = rememberSwipeToDismissBoxState(positionalThreshold = { it * 0.4f })
    val deleteLabel = stringResource(Res.string.delete_sub_task)
    var isFocused by remember { mutableStateOf(false) }
    val checkboxColor = if (subTask.isCompleted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.fillMaxWidth().semantics {
            customActions = listOf(
                CustomAccessibilityAction(deleteLabel) {
                    onDelete()
                    true
                }
            )
        },
        enableDismissFromStartToEnd = false,
        gesturesEnabled = !isDragging,
        onDismiss = { onDelete() },
        backgroundContent = {
            if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_delete),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .toggleable(
                        value = subTask.isCompleted,
                        role = Role.Checkbox,
                        onValueChange = { onChange(subTask.copy(isCompleted = it)) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(20.dp).border(2.dp, checkboxColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (subTask.isCompleted) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_check),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = checkboxColor
                        )
                    }
                }
            }
            BasicTextField(
                value = subTask.title,
                onValueChange = { onChange(subTask.copy(title = it)) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else null,
                    color = if (subTask.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { isFocused = it.isFocused }
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isFocused) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                        else Color.Transparent
                    )
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier.width(40.dp).height(48.dp).then(dragHandleModifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_drag_handle),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubTaskItemPreview() {
    BasePreview {
        Column {
            SubTaskItem(subTask = SubTask("Plan the weekend"), {}, {})
            SubTaskItem(subTask = SubTask("Pick up groceries and prepare meals for the week"), {}, {})
            SubTaskItem(subTask = SubTask("Book the tickets", true), {}, {})
        }
    }
}
