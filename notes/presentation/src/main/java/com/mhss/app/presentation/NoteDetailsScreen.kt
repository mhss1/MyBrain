@file:OptIn(ExperimentalLayoutApi::class)

package com.mhss.app.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mhss.app.ui.R
import com.mhss.app.domain.model.*
import com.mhss.app.presentation.components.AiResultSheet
import com.mhss.app.presentation.components.GradientIconButton
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.theme.Orange
import com.mhss.app.ui.toUserMessage
import com.mhss.app.util.date.formatDateDependingOnDay
import com.mikepenz.markdown.coil2.Coil2ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NoteDetailsScreen(
    navController: NavHostController,
    noteId: Int,
    folderId: Int,
    viewModel: NoteDetailsViewModel = koinViewModel(
        parameters = { parametersOf(noteId, folderId) }
    ),
) {
    val state = viewModel.noteUiState
    var openDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var openFolderDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val title = viewModel.title
    val content = viewModel.content
    val pinned = state.pinned
    val readingMode = state.readingMode
    val folder = state.folder
    var lastModified by remember { mutableStateOf("") }
    var wordCountString by remember { mutableStateOf("") }
    val aiEnabled by viewModel.aiEnabled.collectAsStateWithLifecycle()
    val aiState = viewModel.aiState
    val showAiSheet = aiState.showAiSheet

    LaunchedEffect(content) {
        delay(500)
        wordCountString = content.words().toString()
    }
    LaunchedEffect(state.note) {
        if (state.note != null) {
            lastModified = state.note.updatedDate.formatDateDependingOnDay(context)
        }
    }
    LaunchedEffect(state.navigateUp) {
        if (state.navigateUp) {
            openDeleteDialog = false
            navController.navigateUp()
        }
    }
    LifecycleStartEffect(Unit) {
        onStopOrDispose {
            viewModel.onEvent(
                NoteDetailsEvent.ScreenOnStop
            )
        }
    }
    Scaffold(
        topBar = {
            MyBrainAppBar(
                title = "",
                actions = {
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
                                text = folder.name,
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
                    if (state.note != null) IconButton(onClick = { openDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.delete_task)
                        )
                    }
                    IconButton(onClick = {
                        viewModel.onEvent(NoteDetailsEvent.UpdatePinned(!pinned))
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
                        viewModel.onEvent(NoteDetailsEvent.ToggleReadingMode)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_read_mode),
                            contentDescription = stringResource(R.string.reading_mode),
                            modifier = Modifier.size(24.dp),
                            tint = if (readingMode) Color.Green else Color.Gray
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.onEvent(NoteDetailsEvent.UpdateTitle(it)) },
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
                        ) { viewModel.onEvent(NoteDetailsEvent.Summarize(content)) }
                    }
                    item {
                        GradientIconButton(
                            text = stringResource(id = R.string.auto_format),
                            iconPainter = painterResource(id = R.drawable.ic_auto_format),
                        ) { viewModel.onEvent(NoteDetailsEvent.AutoFormat(content)) }
                    }
                    item {
                        GradientIconButton(
                            text = stringResource(id = R.string.correct_spelling),
                            iconPainter = painterResource(id = R.drawable.ic_spelling),
                        ) { viewModel.onEvent(NoteDetailsEvent.CorrectSpelling(content)) }
                    }
                }
            }
            if (readingMode)
                Markdown(
                    content = content,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .padding(8.dp),
                    imageTransformer = Coil2ImageTransformerImpl,
                    colors = markdownColor(
                        linkText = Color.Blue
                    ),
                    typography = markdownTypography(
                        text = MaterialTheme.typography.bodyMedium,
                        h1 = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        h2 = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        h3 = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        h4 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        h5 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        h6 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                )
            else
                OutlinedTextField(
                    value = content,
                    onValueChange = { viewModel.onEvent(NoteDetailsEvent.UpdateContent(it)) },
                    label = {
                        Text(text = stringResource(R.string.note_content))
                    },
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp)
                )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = lastModified,
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
                        viewModel.onEvent(NoteDetailsEvent.AiResultHandled)
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
                        viewModel.onEvent(NoteDetailsEvent.AiResultHandled)
                    },
                    onReplaceClick = {
                        viewModel.onEvent(NoteDetailsEvent.UpdateContent(aiState.result.toString()))
                        viewModel.onEvent(NoteDetailsEvent.AiResultHandled)
                    },
                    onAddToNoteClick = {
                        viewModel.onEvent(NoteDetailsEvent.UpdateContent(aiState.result + "\n" + content))
                        viewModel.onEvent(NoteDetailsEvent.AiResultHandled)
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
                            viewModel.onEvent(NoteDetailsEvent.DeleteNote(state.note!!))
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
        if (openFolderDialog) AlertDialog(
            onDismissRequest = { openFolderDialog = false },
            confirmButton = {},
            text = {
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
                                    viewModel.onEvent(NoteDetailsEvent.UpdateFolder(null))
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
                                        viewModel.onEvent(NoteDetailsEvent.UpdateFolder(it))
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
            })
    }
}

private fun String.words(): Int {
    var count = 0
    var inWord = false

    forEach { char ->
        if (char == ' ') {
            inWord = false
        } else if (!inWord) {
            count++
            inWord = true
        }
    }

    return count
}