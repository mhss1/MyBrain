package com.mhss.app.mybrain.presentation.tasks

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.SubTask
import com.mhss.app.mybrain.domain.model.Task
import com.mhss.app.mybrain.util.date.formatDateDependingOnDay
import com.mhss.app.mybrain.util.settings.TaskFrequency
import com.mhss.app.mybrain.util.settings.Priority
import com.mhss.app.mybrain.util.settings.toInt
import java.util.*

@Composable
fun AddTaskBottomSheetContent(
    onAddTask: (Task) -> Unit,
    focusRequester: FocusRequester
) {
    var completed by rememberSaveable { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf(Priority.LOW) }
    var dueDate by rememberSaveable { mutableStateOf(Calendar.getInstance()) }
    var dueDateExists by rememberSaveable { mutableStateOf(false) }
    var recurring by rememberSaveable { mutableStateOf(false) }
    var frequency by rememberSaveable { mutableStateOf(TaskFrequency.DAILY) }
    var frequencyAmount by rememberSaveable { mutableIntStateOf(1) }
    val subTasks = remember { mutableStateListOf<SubTask>() }
    val priorities = listOf(Priority.LOW, Priority.MEDIUM, Priority.HIGH)
    val formattedDate by remember {
        derivedStateOf {
            dueDate.timeInMillis.formatDateDependingOnDay()
        }
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .defaultMinSize(minHeight = 1.dp)
            .padding(horizontal = 16.dp)
    ) {
        SheetHandle(Modifier.align(Alignment.CenterHorizontally))
        Text(
            text = stringResource(R.string.add_task),
            style = MaterialTheme.typography.h5
        )
        Spacer(Modifier.height(16.dp))
        TaskDetailsContent(
            modifier = Modifier.weight(1f),
            completed = completed,
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate.timeInMillis,
            dueDateExists = dueDateExists,
            recurring = recurring,
            frequency = frequency,
            frequencyAmount = frequencyAmount,
            subTasks = subTasks,
            priorities = priorities,
            formattedDate = formattedDate,
            focusRequester = focusRequester,
            onTitleChange = { title = it },
            onDescriptionChange = { description = it },
            onPriorityChange = { priority = it },
            onDueDateExist = { dueDateExists = it },
            onDueDateChange = { dueDate.timeInMillis = it },
            onRecurringChange = { recurring = it },
            onFrequencyChange = { frequency = it },
            onFrequencyAmountChange = { frequencyAmount = it },
            onComplete = { completed = it }
        )
        Button(
            onClick = {
                onAddTask(
                    Task(
                        title = title,
                        description = description,
                        isCompleted = completed,
                        priority = priority.toInt(),
                        dueDate = if (dueDateExists) dueDate.timeInMillis else 0L,
                        recurring = recurring,
                        frequency = frequency.value,
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
                keyboardController?.hide()
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
    }
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

@Preview(showBackground = true)
@Composable
fun AddTaskSheetPreview() {
    AddTaskBottomSheetContent(onAddTask = {}, FocusRequester())
}