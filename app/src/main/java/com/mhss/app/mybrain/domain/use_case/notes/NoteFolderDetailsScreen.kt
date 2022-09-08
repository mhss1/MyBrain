package com.mhss.app.mybrain.domain.use_case.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.presentation.notes.NoteEvent
import com.mhss.app.mybrain.presentation.notes.NoteItem
import com.mhss.app.mybrain.presentation.notes.NotesViewModel
import com.mhss.app.mybrain.presentation.util.Screen
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.ItemView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteFolderDetailsScreen(
    navController: NavHostController,
    id: Int,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState = viewModel.notesUiState
    val folder = uiState.folder

    var openDeleteDialog by remember { mutableStateOf(false) }
    var openEditDialog by remember { mutableStateOf(false) }

    val scaffoldState = rememberScaffoldState()
    LaunchedEffect(true) {viewModel.onEvent(NoteEvent.GetFolderNotes(id)) }
    LaunchedEffect(uiState) {
        if (viewModel.notesUiState.navigateUp) {
            navController.popBackStack(route = Screen.NotesScreen.route, inclusive = false)
        }
        if (uiState.error != null) {
            scaffoldState.snackbarHostState.showSnackbar(
                uiState.error
            )
            viewModel.onEvent(NoteEvent.ErrorDisplayed)
        }
    }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = folder?.name ?: "",
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold)
                    )
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp,
                actions = {
                    IconButton(onClick = { openDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, stringResource(R.string.delete_folder))
                    }
                    IconButton(onClick = { openEditDialog = true }) {
                        Icon(Icons.Default.Edit, stringResource(R.string.delete_folder))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                        navController.navigate(
                            Screen.NoteDetailsScreen.route.replace(
                                "{${Constants.NOTE_ID_ARG}}",
                                "${-1}"
                            ).replace(
                                "{${Constants.FOLDER_ID}}",
                                "$id"
                            )
                        )
                },
                backgroundColor = MaterialTheme.colors.primary,
            ) {
                Icon(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_note),
                    tint = Color.White
                )
            }
        }
    ) { _ ->
        if (uiState.noteView == ItemView.LIST) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(
                    top = 12.dp,
                    bottom = 24.dp,
                    start = 12.dp,
                    end = 12.dp
                )
            ) {
                items(uiState.folderNotes, key = { it.id }) { note ->
                    NoteItem(
                        note = note,
                        onClick = {
                            navController.navigate(
                                Screen.NoteDetailsScreen.route.replace(
                                    "{${Constants.NOTE_ID_ARG}}",
                                    "${note.id}"
                                ).replace(
                                    "{${Constants.FOLDER_ID}}",
                                    "-1"
                                )
                            )
                        }
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    top = 12.dp,
                    bottom = 24.dp,
                    start = 12.dp,
                    end = 12.dp
                )
            ) {
                items(uiState.folderNotes) { note ->
                    key(note.id) {
                        NoteItem(
                            note = note,
                            onClick = {
                                navController.navigate(
                                    Screen.NoteDetailsScreen.route.replace(
                                        "{${Constants.NOTE_ID_ARG}}",
                                        "${note.id}"
                                    ).replace(
                                        "{${Constants.FOLDER_ID}}",
                                        "-1"
                                    )
                                )
                            },
                            modifier = Modifier.animateItemPlacement().height(220.dp)
                        )
                    }
                }
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
                            R.string.delete_folder_confirmation_message,
                        )
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            viewModel.onEvent(NoteEvent.DeleteFolder(folder!!))
                            openDeleteDialog = false
                        },
                    ) {
                        Text(stringResource(R.string.delete_folder), color = Color.White)
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
        if (openEditDialog){
            var name by remember { mutableStateOf(folder?.name ?: "") }
            AlertDialog(
                onDismissRequest = { openEditDialog = false },
                title = {
                    Text(
                        text = stringResource(id = R.string.edit_folder),
                        style = MaterialTheme.typography.h6
                    )
                },
                text = {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = {
                            Text(
                                text = stringResource(id = R.string.name),
                                style = MaterialTheme.typography.body1
                            )
                        },
                    )
                },
                confirmButton = {
                    Button(
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            viewModel.onEvent(NoteEvent.UpdateFolder(folder?.copy(name = name)!!))
                            openEditDialog = false
                        },
                    ) {
                        Text(stringResource(R.string.save), color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        shape = RoundedCornerShape(25.dp),
                        onClick = { openEditDialog = false },
                    ) {
                        Text(stringResource(R.string.cancel), color = Color.White)
                    }
                }
            )
        }
    }
}