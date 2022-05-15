package com.mhss.app.mybrain.presentation.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.SubTask
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.util.date.toFullDate
import com.mhss.app.mybrain.util.settings.Priority
import com.mhss.app.mybrain.util.settings.toInt
import com.mhss.app.mybrain.util.settings.toPriority
import java.util.*

@Composable
fun AddTaskBottomSheetContent(
    onAddTask: (Task) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf(Priority.LOW) }
    var dueDate by rememberSaveable { mutableStateOf(Calendar.getInstance()) }
    var dueDateExists by rememberSaveable { mutableStateOf(false) }
    val subTasks = remember { mutableStateListOf<SubTask>() }
    val priorities = listOf(Priority.LOW, Priority.MEDIUM, Priority.HIGH)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .defaultMinSize(minHeight = 1.dp)
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
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
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Column {
            subTasks.forEachIndexed { index, item ->
                SubTaskItem(
                    subTask = item,
                    onCheckedChange = { subTasks[index] = subTasks[index].copy(isCompleted = it) },
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
        val indicator = @Composable { tabPositions: List<TabPosition> ->
            AnimatedTabIndicator(Modifier.tabIndicatorOffset(tabPositions[priority.ordinal]))
        }
        TabRow(
            selectedTabIndex = priority.toInt(),
            indicator = indicator,
            modifier = Modifier.clip(RoundedCornerShape(14.dp))
        ) {
            priorities.forEachIndexed { index, it ->
                Tab(
                    text = { Text(stringResource(it.title)) },
                    selected = priority.toInt() == index,
                    onClick = { priority = index.toPriority() },
                    modifier = Modifier.background(it.color)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = dueDateExists, onCheckedChange = { dueDateExists = it })
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.due_date),
                style = MaterialTheme.typography.body2
            )
        }
        if (dueDateExists)
        Row(
            Modifier
                .fillMaxWidth()
                .clickable {
                    val date = if (dueDate.timeInMillis == 0L) Calendar.getInstance() else dueDate
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
                        }, date[Calendar.YEAR], date[Calendar.MONTH], date[Calendar.DAY_OF_MONTH]
                    )
                    datePicker.show()
                }
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(R.drawable.ic_alarm), stringResource(R.string.due_date))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.due_date),
                    style = MaterialTheme.typography.body1
                )
            }
            Text(
                text = dueDate.timeInMillis.toFullDate(),
                style = MaterialTheme.typography.body2
            )
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
                        createdDate = System.currentTimeMillis(),
                        updatedDate = System.currentTimeMillis(),
                        subTasks = subTasks
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
            shape = RoundedCornerShape(15.dp)
        ) {
            Text(
                text = stringResource(R.string.add_task),
                style = MaterialTheme.typography.h6
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