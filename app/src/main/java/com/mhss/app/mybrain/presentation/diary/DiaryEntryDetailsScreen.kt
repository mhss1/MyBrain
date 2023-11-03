package com.mhss.app.mybrain.presentation.diary

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mhss.app.mybrain.domain.model.DiaryEntry
import com.mhss.app.mybrain.presentation.util.Screen
import com.mhss.app.mybrain.util.diary.Mood
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.util.date.fullDate
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.util.*

@Composable
fun DiaryEntryDetailsScreen(
    navController: NavHostController,
    entryId: Int,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    LaunchedEffect(true) {
        if (entryId != -1) {
            viewModel.onEvent(DiaryEvent.GetEntry(entryId))
        }
    }
    val state = viewModel.uiState
    val scaffoldState = rememberScaffoldState()
    var openDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    var title by rememberSaveable { mutableStateOf(state.entry?.title ?: "") }
    var content by rememberSaveable { mutableStateOf(state.entry?.content ?: "") }
    var mood by rememberSaveable { mutableStateOf(state.entry?.mood ?: Mood.OKAY) }
    var date by rememberSaveable {
        mutableLongStateOf(
            state.entry?.createdDate ?: System.currentTimeMillis()
        )
    }
    val readingMode = state.readingMode

    LaunchedEffect(state.entry) {
        if (state.entry != null && title.isBlank() && content.isBlank()) {
            title = state.entry.title
            content = state.entry.content
            date = state.entry.createdDate
            mood = state.entry.mood
        }
    }
    LaunchedEffect(state) {
        if (state.navigateUp) {
            openDialog = false
            navController.popBackStack(route = Screen.DiaryScreen.route, inclusive = false)
        }
        if (state.error != null) {
            scaffoldState.snackbarHostState.showSnackbar(
                state.error
            )
            viewModel.onEvent(DiaryEvent.ErrorDisplayed)
        }
    }
    BackHandler {
        if (state.entry != null) {
            val entry = state.entry.copy(
                title = title,
                content = content,
                mood = mood,
                createdDate = date,
                updatedDate = System.currentTimeMillis()
            )
            if (entryChanged(
                    state.entry,
                    entry
                )
            ) viewModel.onEvent(DiaryEvent.UpdateEntry(entry))
            else navController.popBackStack(route = Screen.DiaryScreen.route, inclusive = false)
        } else
            navController.popBackStack(route = Screen.DiaryScreen.route, inclusive = false)
    }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = {
                        viewModel.onEvent(DiaryEvent.ToggleReadingMode)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_read_mode),
                            contentDescription = stringResource(R.string.reading_mode),
                            modifier = Modifier.size(24.dp),
                            tint = if (readingMode) Color.Green else Color.Gray
                        )
                    }
                    if (state.entry != null) IconButton(onClick = { openDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.delete_entry)
                        )
                    }
                    TextButton(onClick = {
                        showDatePicker(
                            Calendar.getInstance().apply { timeInMillis = date },
                            context,
                            onDateSelected = {
                                date = it
                            }
                        )
                    }) {
                        Text(
                            text = date.fullDate(),
                            color = MaterialTheme.colors.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp,
            )
        },
        floatingActionButton = {
            if (state.entry == null)
                FloatingActionButton(
                    onClick = {
                        val entry = DiaryEntry(
                            title = title,
                            content = content,
                            mood = mood,
                            createdDate = date,
                            updatedDate = System.currentTimeMillis()
                        )
                        viewModel.onEvent(DiaryEvent.AddEntry(entry))

                    },
                    backgroundColor = MaterialTheme.colors.primary,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_save),
                        contentDescription = stringResource(R.string.save_entry),
                        modifier = Modifier.size(25.dp),
                        tint = Color.White
                    )
                }
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp)
                .padding(paddingValues)
        ) {
            Text(
                text = stringResource(R.string.mood),
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(start = 10.dp)
            )
            Spacer(Modifier.height(8.dp))
            EntryMoodSection(
                currentMood = mood,
            ) { mood = it }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(text = stringResource(R.string.title)) },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            if (readingMode) {
                MarkdownText(
                    markdown = content.ifBlank { stringResource(R.string.content) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(vertical = 6.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(20.dp))
                        .padding(10.dp)
                )
            } else {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(text = stringResource(R.string.content)) },
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (openDialog)
            AlertDialog(
                shape = RoundedCornerShape(25.dp),
                onDismissRequest = { openDialog = false },
                title = { Text(stringResource(R.string.delete_diary_entry_confirmation_title)) },
                text = {
                    Text(
                        stringResource(
                            R.string.delete_diary_entry_confirmation_message
                        )
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            viewModel.onEvent(DiaryEvent.DeleteEntry(state.entry!!))
                        },
                    ) {
                        Text(
                            stringResource(R.string.delete_entry),
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    Button(
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            openDialog = false
                        }) {
                        Text(
                            stringResource(R.string.cancel),
                            color = Color.White
                        )
                    }
                }
            )
    }
}

@Composable
fun EntryMoodSection(
    currentMood: Mood,
    onMoodChange: (Mood) -> Unit
) {
    val moods = listOf(Mood.AWESOME, Mood.GOOD, Mood.OKAY, Mood.BAD, Mood.TERRIBLE)
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        moods.forEach { mood ->
            MoodItem(
                mood = mood,
                chosen = mood == currentMood,
                onMoodChange = { onMoodChange(mood) }
            )
        }
    }
}

@Composable
private fun MoodItem(mood: Mood, chosen: Boolean, onMoodChange: () -> Unit) {
    Box(Modifier.clip(RoundedCornerShape(8.dp))) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable { onMoodChange() }
                .padding(6.dp)
        ) {
            Icon(
                painter = painterResource(id = mood.icon),
                contentDescription = stringResource(mood.title),
                tint = if (chosen) mood.color else Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(mood.title),
                color = if (chosen) mood.color else Color.Gray,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

private fun entryChanged(
    entry: DiaryEntry?,
    newEntry: DiaryEntry
): Boolean {
    return entry?.title != newEntry.title ||
            entry.content != newEntry.content ||
            entry.mood != newEntry.mood ||
            entry.createdDate != newEntry.createdDate
}

private fun showDatePicker(
    date: Calendar,
    context: Context,
    onDateSelected: (Long) -> Unit
) {

    val tempDate = Calendar.getInstance()
    val timePicker = TimePickerDialog(
        context,
        { _, hour, minute ->
            tempDate[Calendar.HOUR_OF_DAY] = hour
            tempDate[Calendar.MINUTE] = minute
            onDateSelected(tempDate.timeInMillis)
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