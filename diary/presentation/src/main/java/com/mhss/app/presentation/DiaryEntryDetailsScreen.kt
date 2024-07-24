package com.mhss.app.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import androidx.navigation.NavHostController
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.Mood
import com.mhss.app.app.R
import com.mhss.app.ui.DateTimeDialog
import com.mhss.app.util.date.fullDate
import com.mhss.app.util.date.now
import dev.jeziellago.compose.markdowntext.MarkdownText
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryDetailsScreen(
    navController: NavHostController,
    entryId: Int,
    viewModel: DiaryViewModel = koinViewModel()
) {
    LaunchedEffect(true) {
        if (entryId != -1) {
            viewModel.onEvent(DiaryEvent.GetEntry(entryId))
        }
    }
    val state = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    var openDialog by rememberSaveable { mutableStateOf(false) }

    var title by rememberSaveable { mutableStateOf(state.entry?.title ?: "") }
    var content by rememberSaveable { mutableStateOf(state.entry?.content ?: "") }
    var mood by rememberSaveable { mutableStateOf(state.entry?.mood ?: Mood.OKAY) }
    var date by rememberSaveable {
        mutableLongStateOf(
            state.entry?.createdDate ?: now()
        )
    }
    val readingMode = state.readingMode
    var showDateDialog by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current

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
            navController.navigateUp()
        }
        if (state.error != null) {
            snackbarHostState.showSnackbar(
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
                updatedDate = now()
            )
            if (entryChanged(
                    state.entry,
                    entry
                )
            ) viewModel.onEvent(DiaryEvent.UpdateEntry(entry))
            else navController.navigateUp()
        } else
            navController.navigateUp()
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        showDateDialog = true
                    }) {
                        Text(
                            text = date.fullDate(context),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
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
                            updatedDate = now()
                        )
                        viewModel.onEvent(DiaryEvent.AddEntry(entry))

                    },
                    containerColor = MaterialTheme.colorScheme.primary,
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.mood),
                style = MaterialTheme.typography.bodyLarge,
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
                    markdown = content,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .padding(10.dp),
                    linkColor = Color.Blue,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            } else {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(text = stringResource(R.string.content)) },
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp)
                )
            }
        }
        if (showDateDialog) DateTimeDialog(
            onDismissRequest = { showDateDialog = false },
            initialDate = date
        ) {
            date = it
            showDateDialog = false
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
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
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
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
                painter = painterResource(id = mood.iconRes),
                contentDescription = stringResource(mood.titleRes),
                tint = if (chosen) mood.color else Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(mood.titleRes),
                color = if (chosen) mood.color else Color.Gray,
                style = MaterialTheme.typography.bodyMedium
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