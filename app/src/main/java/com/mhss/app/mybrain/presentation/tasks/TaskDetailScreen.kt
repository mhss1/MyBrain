package com.mhss.app.mybrain.presentation.tasks

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.navigation.NavHostController
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.SubTask
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.presentation.util.Screen
import com.mhss.app.mybrain.util.date.formatDateDependingOnDay
import com.mhss.app.mybrain.util.settings.TaskFrequency
import com.mhss.app.mybrain.util.settings.Priority
import com.mhss.app.mybrain.util.settings.toTaskFrequency
import com.mhss.app.mybrain.util.settings.toInt
import com.mhss.app.mybrain.util.settings.toPriority
import org.koin.androidx.compose.koinViewModel
import java.util.*

@SuppressLint("InlinedApi")
@Composable
fun TaskDetailScreen(
    navController: NavHostController,
    taskId: Int,
    viewModel: TasksViewModel = koinViewModel()
) {
    LaunchedEffect(true) {
        viewModel.onEvent(TaskEvent.GetTask(taskId))
    }
    val uiState = viewModel.taskDetailsUiState
    val scaffoldState = rememberScaffoldState()
    var openDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf(Priority.LOW) }
    var dueDate by rememberSaveable { mutableLongStateOf(0L) }
    var recurring by rememberSaveable { mutableStateOf(false) }
    var frequency by rememberSaveable { mutableStateOf(TaskFrequency.DAILY) }
    var frequencyAmount by rememberSaveable { mutableIntStateOf(1) }
    var dueDateExists by rememberSaveable { mutableStateOf(false) }
    var completed by rememberSaveable { mutableStateOf(false) }
    val subTasks = remember { mutableStateListOf<SubTask>() }
    val priorities = listOf(Priority.LOW, Priority.MEDIUM, Priority.HIGH)
    val formattedDate by remember {
        derivedStateOf {
            dueDate.formatDateDependingOnDay()
        }
    }

    LaunchedEffect(uiState.task) {
        title = uiState.task.title
        description = uiState.task.description
        priority = uiState.task.priority.toPriority()
        dueDate = uiState.task.dueDate
        dueDateExists = uiState.task.dueDate != 0L
        completed = uiState.task.isCompleted
        recurring = uiState.task.recurring
        frequency = uiState.task.frequency.toTaskFrequency()
        frequencyAmount = uiState.task.frequencyAmount
        subTasks.addAll(uiState.task.subTasks)
    }
    LaunchedEffect(uiState) {
        if (uiState.navigateUp) {
            openDialog = false
            navController.popBackStack<Screen.TaskSearchScreen>( false)
            navController.navigateUp()
        }
        if (uiState.error != null) {
            val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                uiState.error,
                if (uiState.errorAlarm) context.getString(R.string.grant_permission) else null
            )
            if (snackbarResult == SnackbarResult.ActionPerformed) {
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    intent.data = Uri.parse("package:" + context.applicationContext.packageName)
                    context.startActivity(intent)
                }
            }
            viewModel.onEvent(TaskEvent.ErrorDisplayed)
        }
    }
    BackHandler {
        updateTaskIfChanged(
            uiState.task,
            uiState.task.copy(
                title = title,
                description = description,
                dueDate = if (dueDateExists) dueDate else 0L,
                priority = priority.toInt(),
                subTasks = subTasks,
                recurring = recurring,
                frequency = frequency.value,
                frequencyAmount = frequencyAmount
            ),
            {
                navController.popBackStack(Screen.TaskSearchScreen, false)
                navController.navigateUp()
            }
        ) {
            viewModel.onEvent(TaskEvent.UpdateTask(it, dueDate != uiState.task.dueDate))
        }
    }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = { openDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.delete_task)
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp,
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
            onTitleChange = { title = it },
            onDescriptionChange = { description = it },
            onPriorityChange = { priority = it },
            onDueDateExist = {
                dueDateExists = it
                if (it) dueDate = Calendar.getInstance().timeInMillis
            },
            onDueDateChange = { dueDate = it },
            onRecurringChange = { recurring = it },
            onFrequencyChange = { frequency = it },
            onFrequencyAmountChange = { frequencyAmount = it },
            onComplete = {
                completed = it
                viewModel.onEvent(
                    TaskEvent.CompleteTask(
                        uiState.task,
                        it
                    )
                )
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
                        uiState.task.title
                    )
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                    shape = RoundedCornerShape(25.dp),
                    onClick = {
                        viewModel.onEvent(TaskEvent.DeleteTask(uiState.task))
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
) {
    val context = LocalContext.current
    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
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
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(12.dp))
        PriorityTabRow(
            priorities = priorities,
            priority,
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
                style = MaterialTheme.typography.body2
            )
        }
        AnimatedVisibility(dueDateExists) {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            val date =
                                if (dueDate == 0L) Calendar.getInstance() else Calendar
                                    .getInstance()
                                    .apply { timeInMillis = dueDate }
                            val tempDate = Calendar.getInstance()
                            val timePicker = TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    tempDate[Calendar.HOUR_OF_DAY] = hour
                                    tempDate[Calendar.MINUTE] = minute
                                    onDueDateChange(tempDate.timeInMillis)
                                }, date[Calendar.HOUR_OF_DAY], date[Calendar.MINUTE], false
                            )
                            val datePicker = DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    tempDate[Calendar.YEAR] = year
                                    tempDate[Calendar.MONTH] = month
                                    tempDate[Calendar.DAY_OF_MONTH] = day
                                    timePicker.show()
                                },
                                date[Calendar.YEAR],
                                date[Calendar.MONTH],
                                date[Calendar.DAY_OF_MONTH]
                            )
                            datePicker.show()
                        }
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_alarm),
                            stringResource(R.string.due_date),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.due_date),
                            style = MaterialTheme.typography.body1
                        )
                    }
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.body2
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
                        style = MaterialTheme.typography.body2
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
                                stringResource(it.title)
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
    }
}

@Composable
fun PriorityTabRow(
    priorities: List<Priority>,
    selectedPriority: Priority,
    onChange: (Priority) -> Unit
) {
    val indicator = @Composable { tabPositions: List<TabPosition> ->
        AnimatedTabIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedPriority.toInt()]))
    }
    TabRow(
        selectedTabIndex = selectedPriority.toInt(),
        indicator = indicator,
        modifier = Modifier.clip(RoundedCornerShape(14.dp))
    ) {
        priorities.forEachIndexed { index, it ->
            Tab(
                text = { Text(stringResource(it.title)) },
                selected = selectedPriority.toInt() == index,
                onClick = {
                    onChange(index.toPriority())
                },
                modifier = Modifier.background(it.color)
            )
        }
    }
}

@Composable
fun AnimatedTabIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(5.dp)
            .fillMaxSize()
            .border(BorderStroke(2.dp, Color.White), RoundedCornerShape(8.dp))
    )
}

private fun updateTaskIfChanged(
    task: Task,
    newTask: Task,
    onNotChanged: () -> Unit = {},
    onUpdate: (Task) -> Unit,
) {
    if (taskChanged(task, newTask)) onUpdate(newTask) else onNotChanged()
}

private fun taskChanged(
    task: Task,
    newTask: Task
): Boolean {
    return task.title != newTask.title ||
            task.description != newTask.description ||
            task.dueDate != newTask.dueDate ||
            task.priority != newTask.priority ||
            task.subTasks != newTask.subTasks ||
            task.recurring != newTask.recurring ||
            task.frequency != newTask.frequency ||
            task.frequencyAmount != newTask.frequencyAmount
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
                    }
                ) {
                    Text(text = getText(item))
                }
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