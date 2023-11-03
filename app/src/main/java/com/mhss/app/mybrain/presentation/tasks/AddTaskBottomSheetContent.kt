package com.mhss.app.mybrain.presentation.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.SubTask
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.util.date.formatDateDependingOnDay
import com.mhss.app.mybrain.util.settings.TaskFrequency
import com.mhss.app.mybrain.util.settings.Priority
import com.mhss.app.mybrain.util.settings.toInt
import com.mhss.app.mybrain.util.settings.toPriority
import com.mhss.app.mybrain.util.settings.toTaskFrequency
import java.util.*

@Composable
fun AddTaskBottomSheetContent(
    onAddTask: (Task) -> Unit,
    focusRequester: FocusRequester
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf(Priority.LOW) }
    var dueDate by rememberSaveable { mutableStateOf(Calendar.getInstance()) }
    var dueDateExists by rememberSaveable { mutableStateOf(false) }
    var recurring by rememberSaveable { mutableStateOf(false) }
    var frequency by rememberSaveable { mutableIntStateOf(0) }
    var frequencyAmount by rememberSaveable { mutableIntStateOf(1) }
    val subTasks = remember { mutableStateListOf<SubTask>() }
    val priorities = listOf(Priority.LOW, Priority.MEDIUM, Priority.HIGH)
    val context = LocalContext.current
    val formattedDate by remember {
        derivedStateOf {
            dueDate.timeInMillis.formatDateDependingOnDay()
        }
    }

    Column(
        modifier = Modifier
            .defaultMinSize(minHeight = 1.dp)
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SheetHandle(Modifier.align(Alignment.CenterHorizontally))
        Text(
            text = stringResource(R.string.add_task),
            style = MaterialTheme.typography.h5
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(text = stringResource(R.string.title)) },
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )
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
            onChange = { priority = it }
        )
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = dueDateExists, onCheckedChange = { dueDateExists = it })
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
                                if (dueDate.timeInMillis == 0L) Calendar.getInstance() else dueDate
                            val tempDate = Calendar.getInstance()
                            val timePicker = TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    tempDate[Calendar.HOUR_OF_DAY] = hour
                                    tempDate[Calendar.MINUTE] = minute
                                    dueDate = tempDate
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
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_alarm),
                            stringResource(R.string.due_date)
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
                    Checkbox(checked = recurring, onCheckedChange = {
                        recurring = it
                        if (!it) frequency = 0
                    })
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.recurring),
                        style = MaterialTheme.typography.body2
                    )
                }
                AnimatedVisibility(recurring) {
                    var expanded by remember { mutableStateOf(false) }
                    Column {
                        Box {
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                TaskFrequency.values().forEach { f ->
                                    DropdownMenuItem(
                                        onClick = {
                                            expanded = false
                                            frequency = f.ordinal
                                        }
                                    ) {
                                        Text(text = stringResource(f.title))
                                    }
                                }
                            }
                            Row(
                                Modifier
                                    .clickable { expanded = true }
                                    .padding(8.dp)
                                ,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(
                                        frequency.toTaskFrequency().title
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = stringResource(R.string.recurring),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        NumberPicker(
                            stringResource(R.string.repeats_every),
                            frequencyAmount
                        ) {
                            if (it > 0) frequencyAmount = it
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(text = stringResource(R.string.description)) },
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                onAddTask(
                    Task(
                        title = title,
                        description = description,
                        priority = priority.toInt(),
                        dueDate = if (dueDateExists) dueDate.timeInMillis else 0L,
                        recurring = recurring,
                        frequency = frequency,
                        frequencyAmount = frequencyAmount,
                        createdDate = System.currentTimeMillis(),
                        updatedDate = System.currentTimeMillis(),
                        subTasks = subTasks.toList()
                    )
                )
                title = ""
                description = ""
                priority = Priority.LOW
                dueDate = Calendar.getInstance()
                dueDateExists = false
                subTasks.clear()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = stringResource(R.string.add_task),
                style = MaterialTheme.typography.h6.copy(Color.White)
            )
        }
        Spacer(modifier = Modifier.height(54.dp))
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

@Composable
fun SheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .size(width = 60.dp, height = 4.dp)
            .background(Color.Gray)
            .padding(5.dp)
    )
}

@Preview
@Composable
fun AddTaskSheetPreview() {
    AddTaskBottomSheetContent(onAddTask = {}, FocusRequester())
}