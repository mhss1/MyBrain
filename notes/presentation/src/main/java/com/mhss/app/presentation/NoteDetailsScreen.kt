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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mhss.app.presentation.components.AiResultSheet
import com.mhss.app.presentation.components.GradientIconButton
import com.mhss.app.presentation.components.ShareNoteAsPlainTextOption
import com.mhss.app.ui.R
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.components.common.defaultMarkdownTypography
import com.mhss.app.ui.snackbar.LocalisedSnackbarHost
import com.mhss.app.ui.theme.Orange
import com.mhss.app.util.date.formatDateDependingOnDay
import com.mikepenz.markdown.coil2.Coil2ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NoteDetailsScreen(
    navController: NavHostController,
    noteId: String?,
    folderId: String?,
    viewModel: NoteDetailsViewModel = koinViewModel(
        parameters = { parametersOf(noteId.orEmpty(), folderId.orEmpty()) }
    ),
) {
    val state by viewModel.noteUiState.collectAsStateWithLifecycle()
    var openDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var openFolderDialog by rememberSaveable { mutableStateOf(false) }
    var showShareMenu by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val title = viewModel.title
    val content = viewModel.content
    val pinned = state.pinned
    val readingMode = state.readingMode
    val folder = state.folder
    val lastModified by remember(state.note?.updatedDate) {
        derivedStateOf {
            state.note?.updatedDate?.formatDateDependingOnDay(context) ?: ""
        }
    }
    var wordCountString by remember { mutableStateOf("") }
    val aiEnabled by viewModel.aiEnabled.collectAsStateWithLifecycle()
    val aiState = viewModel.aiState
    val showAiSheet = aiState.showAiSheet

    val liquidState = rememberLiquidState()
    LaunchedEffect(content) {
        delay(500)
        wordCountString = content.countWords().toString()
    }
    LaunchedEffect(state.navigateUp) {
        if (state.navigateUp) {
            openDeleteDialog = false
            navController.navigateUp()
        }
    }
    LifecycleStartEffect(Unit) {
        onStopOrDispose {
            viewModel.onEvent(NoteDetailsEvent.ScreenOnStop)
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
                                .clip(CircleShape)
                                .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                .clickable { openFolderDialog = true }
                                .weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_folder),
                                stringResource(R.string.folders),
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                                    .size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = folder.name,
                                modifier = Modifier.padding(end = 8.dp, top = 8.dp, bottom = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        IconButton(onClick = { openFolderDialog = true }) {
                            Icon(
                                painterResource(R.drawable.ic_create_folder),
                                stringResource(R.string.folders),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(onClick = { showShareMenu = true }) {
                        Icon(
                            painterResource(R.drawable.ic_share),
                            stringResource(R.string.share_note),
                            modifier = Modifier.size(18.dp),
                        )
                        DropdownMenu(
                            expanded = showShareMenu,
                            onDismissRequest = { showShareMenu = false }
                        ) {
                            ShareNoteAsPlainTextOption(
                                title = title,
                                content = content,
                                onOptionSelected = { showShareMenu = false }
                            )
                        }
                    }
                    if (state.note != null) IconButton(onClick = { openDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = stringResource(R.string.delete_task),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = {
                        viewModel.onEvent(NoteDetailsEvent.UpdatePinned(!pinned))
                    }) {
                        Icon(
                            painter = if (pinned) painterResource(id = R.drawable.ic_pin_filled)
                            else painterResource(id = R.drawable.ic_pin),
                            contentDescription = stringResource(R.string.pin_note),
                            modifier = Modifier.size(18.dp),
                            tint = Orange
                        )
                    }
                    IconButton(onClick = {
                        viewModel.onEvent(NoteDetailsEvent.ToggleReadingMode)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_read_mode),
                            contentDescription = stringResource(R.string.reading_mode),
                            modifier = Modifier.size(18.dp),
                            tint = if (readingMode) Color.Green else Color.Gray
                        )
                    }
                }
            )
        },
        snackbarHost = {
            LocalisedSnackbarHost(state.snackbarHostState)
        }
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
                        ) {
                            viewModel.onEvent(NoteDetailsEvent.Summarize(content))
                            keyboardController?.hide()
                        }
                    }
                    item {
                        GradientIconButton(
                            text = stringResource(id = R.string.auto_format),
                            iconPainter = painterResource(id = R.drawable.ic_auto_format),
                        ) {
                            viewModel.onEvent(NoteDetailsEvent.AutoFormat(content))
                            keyboardController?.hide()
                        }
                    }
                    item {
                        GradientIconButton(
                            text = stringResource(id = R.string.correct_spelling),
                            iconPainter = painterResource(id = R.drawable.ic_spelling),
                        ) {
                            viewModel.onEvent(NoteDetailsEvent.CorrectSpelling(content))
                            keyboardController?.hide()
                        }
                    }
                }
            }
            if (readingMode)
                Markdown(
                    content = content.preserveLineBreaks(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .padding(8.dp)
                        .liquefiable(liquidState),
                    imageTransformer = Coil2ImageTransformerImpl,
                    typography = defaultMarkdownTypography()
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
                    liquidState = liquidState,
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

private fun String.countWords(): Int {
    var count = 0
    var inWord = false

    forEach { char ->
        if (char == ' ' || char == '\n') {
            inWord = false
        } else if (!inWord) {
            count++
            inWord = true
        }
    }

    return count
}

private fun String.preserveLineBreaks(): String {
    // Convert single newlines to markdown line breaks (two spaces + newline)
    // but preserve existing double newlines as paragraph breaks
    return this
        .replace("\r\n", "\n") // Normalize line endings
        .replace("\r", "\n") // Handle old Mac line endings
        .replace(Regex("\n(?!\n)"), "  \n") // Convert single newlines to markdown line breaks
}