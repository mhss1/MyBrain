@file:OptIn(ExperimentalLayoutApi::class)

package com.mhss.app.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mhss.app.app.R
import com.mhss.app.domain.model.*
import com.mhss.app.presentation.components.AiResultSheet
import com.mhss.app.presentation.components.GradientIconButton
import com.mhss.app.ui.theme.Orange
import com.mhss.app.ui.toUserMessage
import com.mhss.app.util.date.formatDateDependingOnDay
import dev.jeziellago.compose.markdowntext.MarkdownText
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailsScreen(
    navController: NavHostController,
    noteId: Int,
    folderId: Int,
    viewModel: NotesViewModel = koinViewModel()
) {
    LaunchedEffect(true) {
        if (noteId != -1) viewModel.onEvent(NoteEvent.GetNote(noteId))
        if (folderId != -1) viewModel.onEvent(NoteEvent.GetFolder(folderId))
    }
    val state = viewModel.notesUiState
    val snackbarHostState = remember { SnackbarHostState() }
    var openDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var openFolderDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    var title by rememberSaveable { mutableStateOf(state.note?.title ?: "") }
    var content by rememberSaveable { mutableStateOf(state.note?.content ?: "") }
    var pinned by rememberSaveable { mutableStateOf(state.note?.pinned ?: false) }
    val readingMode = state.readingMode
    var folder: NoteFolder? by remember { mutableStateOf(state.folder) }
    val lastModified by remember(state.note) {
        derivedStateOf { state.note?.updatedDate?.formatDateDependingOnDay(context) }
    }
    val wordCountString by remember {
        derivedStateOf { content.split(" ").size.toString() }
    }
    val aiEnabled by viewModel.aiEnabled.collectAsStateWithLifecycle()
    val aiState = viewModel.aiState
    val showAiSheet = aiState.showAiSheet

    LaunchedEffect(state.note) {
        if (state.note != null) {
            title = state.note.title
            content = state.note.content
            pinned = state.note.pinned
            folder = state.folder
        }
    }
    LaunchedEffect(state) {
        if (state.navigateUp) {
            openDeleteDialog = false
            navController.navigateUp()
        }
        if (state.error != null) {
            snackbarHostState.showSnackbar(
                context.getString(state.error)
            )
            viewModel.onEvent(NoteEvent.ErrorDisplayed)
        }
        if (state.folder != folder) folder = state.folder
    }
    BackHandler {
        addOrUpdateNote(
            Note(
                title = title,
                content = content,
                pinned = pinned,
                folderId = folder?.id
            ),
            state.note,
            onNotChanged = {
                navController.navigateUp()
            },
            onUpdate = {
                if (state.note != null) {
                    viewModel.onEvent(
                        NoteEvent.UpdateNote(
                            state.note.copy(
                                title = title,
                                content = content,
                                folderId = folder?.id
                            )
                        )
                    )
                } else {
                    viewModel.onEvent(
                        NoteEvent.AddNote(
                            Note(
                                title = title,
                                content = content,
                                pinned = pinned,
                                folderId = folder?.id
                            )
                        )
                    )
                }

            }
        )
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (folder != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(25.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                .clickable { openFolderDialog = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_folder),
                                stringResource(R.string.folders),
                                modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = folder?.name!!,
                                modifier = Modifier.padding(end = 8.dp, top = 8.dp, bottom = 8.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        IconButton(onClick = { openFolderDialog = true }) {
                            Icon(
                                painterResource(R.drawable.ic_create_folder),
                                stringResource(R.string.folders),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                },
                actions = {
                    if (state.note != null) IconButton(onClick = { openDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.delete_task)
                        )
                    }
                    IconButton(onClick = {
                        pinned = !pinned
                        if (state.note != null) {
                            viewModel.onEvent(NoteEvent.PinNote)
                        }
                    }) {
                        Icon(
                            painter = if (pinned) painterResource(id = R.drawable.ic_pin_filled)
                            else painterResource(id = R.drawable.ic_pin),
                            contentDescription = stringResource(R.string.pin_note),
                            modifier = Modifier.size(24.dp),
                            tint = Orange
                        )
                    }
                    IconButton(onClick = {
                        viewModel.onEvent(NoteEvent.ToggleReadingMode)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_read_mode),
                            contentDescription = stringResource(R.string.reading_mode),
                            modifier = Modifier.size(24.dp),
                            tint = if (readingMode) Color.Green else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(text = stringResource(R.string.title)) },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            AnimatedVisibility(aiEnabled) {
                LazyRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        GradientIconButton(
                            text = stringResource(id = R.string.summarize),
                            iconPainter = painterResource(id = R.drawable.ic_summarize),
                        ) { viewModel.onEvent(NoteEvent.Summarize(content)) }
                    }
                    item {
                        GradientIconButton(
                            text = stringResource(id = R.string.auto_format),
                            iconPainter = painterResource(id = R.drawable.ic_auto_format),
                        ) { viewModel.onEvent(NoteEvent.AutoFormat(content)) }
                    }
                    item {
                        GradientIconButton(
                            text = stringResource(id = R.string.correct_spelling),
                            iconPainter = painterResource(id = R.drawable.ic_spelling),
                        ) { viewModel.onEvent(NoteEvent.CorrectSpelling(content)) }
                    }
                }
            }
            if (readingMode)
                MarkdownText(
                    markdown = content,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .padding(8.dp),
                    linkColor = Color.Blue,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            else
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = {
                        Text(text = stringResource(R.string.note_content))
                    },
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp)
                        .imePadding()
                )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = lastModified ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
                Text(
                    text = wordCountString,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
            }
        }
        AnimatedVisibility(
            visible = showAiSheet,
            enter = slideInVertically(
                initialOffsetY = { it }, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessVeryLow
                )
            ),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(700))
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        viewModel.onEvent(NoteEvent.AiResultHandled)
                    }, contentAlignment = Alignment.BottomCenter
            ) {
                AiResultSheet(
                    loading = aiState.loading,
                    result = aiState.result,
                    error = aiState.error?.toUserMessage(),
                    onCopyClick = {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("ai result", aiState.result.toString())
                        clipboard.setPrimaryClip(clip)
                        viewModel.onEvent(NoteEvent.AiResultHandled)
                    },
                    onReplaceClick = {
                        content = aiState.result.toString()
                        viewModel.onEvent(NoteEvent.AiResultHandled)
                    },
                    onAddToNoteClick = {
                        content = aiState.result + "\n" + content
                        viewModel.onEvent(NoteEvent.AiResultHandled)
                    }
                )
            }
        }
        if (openDeleteDialog)
            AlertDialog(
                shape = RoundedCornerShape(25.dp),
                onDismissRequest = { openDeleteDialog = false },
                title = { Text(stringResource(R.string.delete_note_confirmation_title)) },
                text = {
                    Text(
                        stringResource(
                            R.string.delete_note_confirmation_message,
                            state.note?.title!!
                        )
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            viewModel.onEvent(NoteEvent.DeleteNote(state.note!!))
                        },
                    ) {
                        Text(stringResource(R.string.delete_note), color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            openDeleteDialog = false
                        }) {
                        Text(stringResource(R.string.cancel), color = Color.White)
                    }
                }
            )
        if (openFolderDialog)
            BasicAlertDialog(
                onDismissRequest = { openFolderDialog = false },
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(stringResource(R.string.change_folder))
                    FlowRow {
                        Row(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                .clickable {
                                    folder = null
                                    openFolderDialog = false
                                }
                                .background(if (folder == null) MaterialTheme.colorScheme.onBackground else Color.Transparent),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.none),
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (folder == null) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                            )
                        }
                        state.folders.forEach {
                            Row(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(25.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                    .clickable {
                                        folder = it
                                        openFolderDialog = false
                                    }
                                    .background(if (folder?.id == it.id) MaterialTheme.colorScheme.onBackground else Color.Transparent),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_folder),
                                    stringResource(R.string.folders),
                                    modifier = Modifier.padding(
                                        start = 8.dp,
                                        top = 8.dp,
                                        bottom = 8.dp
                                    ),
                                    tint = if (folder?.id == it.id) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = it.name,
                                    modifier = Modifier.padding(
                                        end = 8.dp,
                                        top = 8.dp,
                                        bottom = 8.dp
                                    ),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (folder?.id == it.id) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
    }
}

private fun addOrUpdateNote(
    newNote: Note,
    note: Note? = null,
    onNotChanged: () -> Unit = {},
    onUpdate: (Note) -> Unit,
) {
    if (note != null) {
        if (noteChanged(newNote, note))
            onUpdate(note)
        else
            onNotChanged()
    } else {
        onUpdate(newNote)
    }
}

private fun noteChanged(
    note: Note,
    newNote: Note
): Boolean {
    return note.title != newNote.title ||
            note.content != newNote.content ||
            note.folderId != newNote.folderId
}