package com.mhss.app.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation.NavHostController
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.TaskFrequency
import com.mhss.app.ui.R
import com.mhss.app.ui.color
import com.mhss.app.ui.components.common.AnimatedTabIndicator
import com.mhss.app.ui.components.common.DateDialog
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.components.common.NumberPicker
import com.mhss.app.ui.components.common.TimeDialog
import com.mhss.app.ui.components.tasks.TaskCheckBox
import com.mhss.app.ui.snackbar.LocalisedSnackbarHost
import com.mhss.app.ui.snackbar.showSnackbar
import com.mhss.app.ui.titleRes
import com.mhss.app.util.date.formatDate
import com.mhss.app.util.date.formatTime
import com.mhss.app.util.date.now
import com.mhss.app.util.permissions.Permission
import com.mhss.app.util.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Suppress("AssignedValueIsNeverRead")
@Composable
fun TaskDetailScreen(
    navController: NavHostController,
    taskId: String,
    viewModel: TaskDetailsViewModel = koinViewModel(parameters = { parametersOf(taskId) }),
) {
    val alarmPermissionState = rememberPermissionState(Permission.SCHEDULE_ALARMS)
    val uiState by viewModel.taskDetailsUiState.collectAsState()
    val snackbarHostState = uiState.snackbarHostState
    var openDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.LOW) }
    var dueDate by remember { mutableLongStateOf(0L) }
    var recurring by remember { mutableStateOf(false) }
    var frequency by remember { mutableStateOf(TaskFrequency.DAILY) }
    var frequencyAmount by remember { mutableIntStateOf(1) }
    var dueDateExists by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(false) }
    val subTasks = remember { mutableStateListOf<SubTask>() }
    val priorities = listOf(Priority.LOW, Priority.MEDIUM, Priority.HIGH)
    val formattedDate by remember {
        derivedStateOf { dueDate.formatDate() }
    }
    val formattedTime by remember {
        derivedStateOf { dueDate.formatTime(context) }
    }

    LaunchedEffect(uiState.task) {
        val task = uiState.task
        if (task != null) {
            title = task.title
            description = task.description
            priority = task.priority
            dueDate = task.dueDate
            dueDateExists = task.dueDate != 0L
            completed = task.isCompleted
            recurring = task.recurring
            frequency = task.frequency
            frequencyAmount = task.frequencyAmount
            subTasks.clear()
            subTasks.addAll(task.subTasks)
        }
    }
    LaunchedEffect(uiState.navigateUp, uiState.alarmError) {
        if (uiState.navigateUp) {
            openDialog = false
            navController.navigateUp()
        }
        if (uiState.alarmError) {
            dueDateExists = false
            val snackbarResult = snackbarHostState.showSnackbar(R.string.no_alarm_permission, R.string.grant_permission)
            if (snackbarResult == SnackbarResult.ActionPerformed) {
                alarmPermissionState.launchRequest()
            }
            viewModel.onEvent(TaskDetailsEvent.ErrorDisplayed)
        }
    }
    LifecycleStartEffect(Unit) {
        onStopOrDispose {
            if (!viewModel.taskDetailsUiState.value.navigateUp) {
                viewModel.taskDetailsUiState.value.task?.let { task ->
                    viewModel.onEvent(
                        TaskDetailsEvent.ScreenOnStop(
                            task.copy(
                                title = title,
                                description = description,
                                isCompleted = completed,
                                dueDate = if (dueDateExists) dueDate else 0L,
                                priority = priority,
                                subTasks = subTasks,
                                recurring = recurring,
                                frequency = frequency,
                                frequencyAmount = frequencyAmount
                            )
                        )
                    )
                }
            }
        }
    }
    Scaffold(
        snackbarHost = { LocalisedSnackbarHost(snackbarHostState) },
        topBar = {
            MyBrainAppBar(
                title = "",
                actions = {
                    IconButton(onClick = { openDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.delete_task)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        TaskDetailsContent(
            modifier = Modifier.padding(paddingValues),
            completed = completed,
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate,
            dueDateExists = dueDateExists,
            recurring = recurring,
            frequency = frequency,
            frequencyAmount = frequencyAmount,
            subTasks = subTasks,
            priorities = priorities,
            formattedDate = formattedDate,
            formattedTime = formattedTime,
            onTitleChange = { title = it },
            onDescriptionChange = { description = it },
            onPriorityChange = { priority = it },
            onDueDateExist = { checked ->
                dueDateExists = checked
                if (checked) {
                    dueDate = now()
                    viewModel.onEvent(TaskDetailsEvent.DueDateEnabled)
                }
            },
            onDueDateChange = { dueDate = it },
            onRecurringChange = { recurring = it },
            onFrequencyChange = { frequency = it },
            onFrequencyAmountChange = { frequencyAmount = it },
            onComplete = {
                completed = it
            }
        )
    }
    if (openDialog)
        AlertDialog(
            shape = RoundedCornerShape(25.dp),
            onDismissRequest = { openDialog = false },
            title = { Text(stringResource(R.string.delete_task_confirmation_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.delete_task_confirmation_message,
                        uiState.task?.title ?: "Untitled"
                    )
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(25.dp),
                    onClick = {
                        viewModel.onEvent(TaskDetailsEvent.DeleteTask)
                    },
                ) {
                    Text(stringResource(R.string.delete_task), color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    shape = RoundedCornerShape(25.dp),
                    onClick = {
                        openDialog = false
                    }) {
                    Text(stringResource(R.string.cancel), color = Color.White)
                }
            }
        )
}

@Composable
fun TaskDetailsContent(
    modifier: Modifier = Modifier,
    completed: Boolean,
    title: String,
    description: String,
    priority: Priority,
    dueDate: Long,
    dueDateExists: Boolean,
    recurring: Boolean,
    frequency: TaskFrequency,
    frequencyAmount: Int,
    subTasks: MutableList<SubTask>,
    priorities: List<Priority>,
    formattedDate: String,
    formattedTime: String,
    focusRequester: FocusRequester? = null,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onDueDateExist: (Boolean) -> Unit,
    onDueDateChange: (Long) -> Unit,
    onRecurringChange: (Boolean) -> Unit,
    onFrequencyChange: (TaskFrequency) -> Unit,
    onFrequencyAmountChange: (Int) -> Unit,
    onComplete: (Boolean) -> Unit,
    optionalContent: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskCheckBox(
                isComplete = completed,
                borderColor = priority.color
            ) {
                onComplete(!completed)
            }
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text(text = stringResource(R.string.title)) },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (focusRequester != null) Modifier.focusRequester(focusRequester)
                        else Modifier
                    )
            )
            LaunchedEffect(focusRequester) {
                focusRequester?.requestFocus()
            }
        }
        Spacer(Modifier.height(12.dp))
        Column {
            subTasks.forEachIndexed { index, item ->
                SubTaskItem(
                    subTask = item,
                    onChange = { subTasks[index] = it },
                    onDelete = { subTasks.removeAt(index) }
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clickable {
                    subTasks.add(
                        SubTask(
                            title = "",
                            isCompleted = false,
                        )
                    )
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.add_sub_task),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Icon(
                modifier = Modifier.size(10.dp),
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = stringResource(
                    id = R.string.add_sub_task
                )
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.priority),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(12.dp))
        PriorityTabRow(
            priorities = priorities,
            selectedPriority = priority,
            onChange = onPriorityChange
        )
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = dueDateExists,
                onCheckedChange = onDueDateExist
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.due_date),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        var showDateDialog by remember {
            mutableStateOf(false)
        }
        var showTimeDialog by remember {
            mutableStateOf(false)
        }
        if (showDateDialog) DateDialog(
            onDismissRequest = { showDateDialog = false },
            initialDate = dueDate
        ) {
            onDueDateChange(it)
            showDateDialog = false
        }
        if (showTimeDialog) TimeDialog(
            onDismissRequest = { showTimeDialog = false },
            initialDate = dueDate
        ) {
            onDueDateChange(it)
            showTimeDialog = false
        }
        AnimatedVisibility(dueDateExists) {
            Column {
                Row(
                    Modifier
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_alarm),
                        stringResource(R.string.due_date),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.due_date),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .clickable { showDateDialog = true }
                            .padding(horizontal = 28.dp, vertical = 16.dp)
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .clickable { showTimeDialog = true }
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = recurring,
                        onCheckedChange = onRecurringChange
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.recurring),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                AnimatedVisibility(recurring) {
                    var frequencyMenuVisible by remember { mutableStateOf(false) }
                    Column {
                        DropDownItem(
                            title = stringResource(R.string.recurring),
                            expanded = frequencyMenuVisible,
                            items = TaskFrequency.entries,
                            selectedItem = frequency,
                            getText = {
                                stringResource(it.titleRes)
                            },
                            onItemSelected = {
                                frequencyMenuVisible = false
                                onFrequencyChange(it)
                            },
                            onDismissRequest = {
                                frequencyMenuVisible = false
                            },
                            onClick = {
                                frequencyMenuVisible = true
                            })
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NumberPicker(
                                stringResource(R.string.repeats_every),
                                frequencyAmount
                            ) {
                                if (it > 0) onFrequencyAmountChange(it)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(text = stringResource(R.string.description)) },
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth()
        )
        optionalContent()
    }
}

@Composable
fun PriorityTabRow(
    priorities: List<Priority>,
    selectedPriority: Priority,
    onChange: (Priority) -> Unit
) {
    SecondaryTabRow(
        selectedTabIndex = selectedPriority.value,
        indicator = {
            AnimatedTabIndicator(Modifier.tabIndicatorOffset(selectedPriority.value))
        },
        divider = {},
        modifier = Modifier.clip(RoundedCornerShape(14.dp))
    ) {
        priorities.forEach {
            Tab(
                text = { Text(stringResource(it.titleRes)) },
                selected = selectedPriority == it,
                onClick = {
                    onChange(it)
                },
                modifier = Modifier.background(it.color),
                unselectedContentColor = Color.White.copy(alpha = 0.7f),
                selectedContentColor = Color.White
            )
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