package com.mhss.app.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.datetime.LocalDateTimeFormatter
import com.mhss.app.datetime.now
import com.mhss.app.domain.model.Priority
import com.mhss.app.ui.Res
import com.mhss.app.ui.add_sub_task
import com.mhss.app.ui.add_task
import com.mhss.app.ui.cancel
import com.mhss.app.ui.color
import com.mhss.app.ui.components.common.DateDialog
import com.mhss.app.ui.components.common.TimeDialog
import com.mhss.app.ui.components.common.frostedGlass
import com.mhss.app.ui.due_date
import com.mhss.app.ui.ic_add
import com.mhss.app.ui.ic_alarm
import com.mhss.app.ui.ic_bullet_list
import com.mhss.app.ui.ic_remove
import com.mhss.app.ui.ic_send_message
import com.mhss.app.ui.preview.BasePreview
import com.mhss.app.ui.titleRes
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.rememberLiquidState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.stringResource as cmpStringResource

@Composable
fun AddTaskFloatingCard(
    onAddTask: (AddTaskInput) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    liquidState: LiquidState? = null,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf(Priority.LOW) }
    var dueDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingDueDate by remember { mutableLongStateOf(now()) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var focusNewSubTask by remember { mutableStateOf(false) }
    var focusTitleRequested by remember { mutableStateOf(false) }
    val subTasks = remember { mutableStateListOf<String>() }
    val titleFocusRequester = remember { FocusRequester() }
    val subTaskFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val formatter = LocalDateTimeFormatter.current

    fun submit() {
        if (title.isBlank()) return
        onAddTask(
            AddTaskInput(
                title = title,
                priority = priority,
                dueDate = dueDate,
                subTasks = subTasks.toList(),
            )
        )
        keyboardController?.hide()
        onDismiss()
    }

    fun addSubTask() {
        if (subTasks.lastOrNull()?.isBlank() == true) {
            subTaskFocusRequester.requestFocus()
            keyboardController?.show()
        } else {
            focusNewSubTask = true
            subTasks.add("")
        }
    }

    LaunchedEffect(Unit) {
        titleFocusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(subTasks.size, focusTitleRequested) {
        if (focusTitleRequested) {
            titleFocusRequester.requestFocus()
            keyboardController?.show()
            focusTitleRequested = false
        } else if (focusNewSubTask && subTasks.isNotEmpty()) {
            subTaskFocusRequester.requestFocus()
            keyboardController?.show()
            focusNewSubTask = false
        }
    }

    if (showDateDialog) {
        DateDialog(
            initialDate = dueDate ?: now(),
            onDismissRequest = { showDateDialog = false },
            onDatePicked = {
                pendingDueDate = it
                showDateDialog = false
                showTimeDialog = true
            }
        )
    }

    if (showTimeDialog) {
        TimeDialog(
            initialDate = pendingDueDate,
            onDismissRequest = { showTimeDialog = false },
            onTimePicked = {
                dueDate = it
                showTimeDialog = false
                focusTitleRequested = true
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .dropShadow(RoundedCornerShape(24.dp)) {
                alpha = 0.05f
                spread = 7f
                radius = 36f
            }
            .then(
                if (liquidState != null) {
                    Modifier.frostedGlass(
                        liquidState = liquidState,
                        shape = RoundedCornerShape(24.dp),
                        refraction = 0.40f,
                        frost = 8.dp,
                        curve = 0.05f,
                    )
                } else {
                    Modifier.background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(verticalAlignment = Alignment.Top) {
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(titleFocusRequester)
                        .padding(vertical = 10.dp),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    maxLines = 3,
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.add_task),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                            )
                        }
                        innerTextField()
                    }
                )
                TextButton(
                    onClick = {
                        keyboardController?.hide()
                        onDismiss()
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
            if (subTasks.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 132.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subTasks.forEachIndexed { index, subTask ->
                        SubTaskInput(
                            value = subTask,
                            onValueChange = { subTasks[index] = it },
                            onRemove = {
                                focusNewSubTask = false
                                focusTitleRequested = true
                                subTasks.removeAt(index)
                            },
                            focusRequester = if (index == subTasks.lastIndex) {
                                subTaskFocusRequester
                            } else {
                                null
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AddSubTaskButton(onClick = ::addSubTask)
                    DueDateButton(
                        dueDate = dueDate,
                        formattedDate = dueDate?.let(formatter::formatDateDependingOnDay),
                        onClick = {
                            keyboardController?.hide()
                            showDateDialog = true
                        },
                        onClear = { dueDate = null }
                    )
                    PriorityButton(
                        priority = priority,
                        onClick = {
                            priority = Priority.entries[
                                (priority.ordinal + 1) % Priority.entries.size
                            ]
                        }
                    )
                }
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { submit() },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_send_message),
                        contentDescription = stringResource(Res.string.add_task),
                        tint = if (title.isNotBlank()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                        },
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SubTaskInput(
    value: String,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
    focusRequester: FocusRequester?,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_bullet_list),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .then(
                    focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier
                )
                .padding(vertical = 8.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.add_sub_task),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                innerTextField()
            }
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_remove),
                contentDescription = stringResource(Res.string.cancel),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun DueDateButton(
    dueDate: Long?,
    formattedDate: String?,
    onClick: () -> Unit,
    onClear: () -> Unit,
) {
    if (dueDate == null) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(34.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_alarm),
                    contentDescription = stringResource(Res.string.due_date),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        return
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .height(34.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_alarm),
                contentDescription = stringResource(Res.string.due_date),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = formattedDate.orEmpty(),
                style = MaterialTheme.typography.labelMedium
            )
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_remove),
                    contentDescription = stringResource(Res.string.due_date),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun AddSubTaskButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(34.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_add),
            contentDescription = stringResource(Res.string.add_sub_task),
            modifier = Modifier.padding(10.dp)
        )
    }
}

@Composable
private fun PriorityButton(
    priority: Priority,
    onClick: () -> Unit,
) {
    val priorityColor by animateColorAsState(priority.color, label = "priorityColor")
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = priorityColor.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, priorityColor)
    ) {
        AnimatedContent(
            targetState = priority,
            label = "priority"
        ) { item ->
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = CircleShape,
                    color = item.color
                ) {}
                Spacer(Modifier.width(6.dp))
                Text(
                    text = cmpStringResource(item.titleRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = item.color
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTaskFloatingCardPreview() {
    BasePreview {
        AddTaskFloatingCard(
            onAddTask = {},
            onDismiss = {},
            liquidState = rememberLiquidState()
        )
    }
}
