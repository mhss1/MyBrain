@file:Suppress("AssignedValueIsNeverRead")

package com.mhss.app.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.mhss.app.ui.ItemView
import com.mhss.app.ui.Res
import com.mhss.app.ui.add_note
import com.mhss.app.ui.cancel
import com.mhss.app.ui.components.PagingLoadState
import com.mhss.app.ui.components.PagingPlaceholder
import com.mhss.app.ui.components.common.LiquidFloatingActionButton
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.components.notes.NoteCard
import com.mhss.app.ui.delete_folder
import com.mhss.app.ui.delete_folder_confirmation_message
import com.mhss.app.ui.delete_note_confirmation_title
import com.mhss.app.ui.edit_folder
import com.mhss.app.ui.ic_add
import com.mhss.app.ui.ic_delete
import com.mhss.app.ui.ic_edit
import com.mhss.app.ui.name
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.save
import com.mhss.app.ui.snackbar.LocalisedSnackbarHost
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NoteFolderDetailsScreen(
    navController: NavHostController,
    id: String,
    viewModel: NoteFolderDetailsViewModel = koinViewModel(parameters = { parametersOf(id) })
) {
    val notes = viewModel.folderNotes.collectAsLazyPagingItems()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val folder = uiState.folder
    val snackbarHostState = uiState.snackbarHostState

    var openDeleteDialog by remember { mutableStateOf(false) }
    var openEditDialog by remember { mutableStateOf(false) }

    val liquidState = rememberLiquidState()
    LaunchedEffect(uiState.navigateUp) {
        if (uiState.navigateUp) {
            navController.navigateUp()
        }
    }
    Scaffold(
        snackbarHost = { LocalisedSnackbarHost(snackbarHostState) },
        topBar = {
            MyBrainAppBar(
                title = folder?.name ?: "",
                actions = {
                    IconButton(onClick = { openDeleteDialog = true }) {
                        Icon(painterResource(Res.drawable.ic_delete), stringResource(Res.string.delete_folder))
                    }
                    IconButton(onClick = { openEditDialog = true }) {
                        Icon(painterResource(Res.drawable.ic_edit), stringResource(Res.string.edit_folder))
                    }
                }
            )
        },
        floatingActionButton = {
            LiquidFloatingActionButton(
                onClick = {
                    navController.navigate(
                        Screen.NoteDetailsScreen(folderId = id)
                    )
                },
                iconPainter = painterResource(Res.drawable.ic_add),
                contentDescription = stringResource(Res.string.add_note),
                liquidState = liquidState
            )
        }
    ) { contentPadding ->
        Column(
            Modifier.fillMaxSize().padding(contentPadding).liquefiable(liquidState)
        ) {
            PagingLoadState(notes)
            if (uiState.noteView == ItemView.LIST) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(
                        top = 12.dp,
                        bottom = 24.dp,
                        start = 12.dp,
                        end = 12.dp
                    ),
                ) {
                    items(notes.itemCount, key = notes.itemKey { it.id }) { index ->
                        notes[index]?.let { note ->
                            NoteCard(
                                note = note,
                                onClick = {
                                    navController.navigate(
                                        Screen.NoteDetailsScreen(
                                            noteId = note.id,
                                            folderId = id
                                        )
                                    )
                                }
                            )
                        } ?: PagingPlaceholder()
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
                    ),
                    modifier = Modifier.liquefiable(liquidState)
                ) {
                    items(notes.itemCount, key = notes.itemKey { it.id }) { index ->
                        notes[index]?.let { note ->
                            key(note.id) {
                                NoteCard(
                                    note = note,
                                    onClick = {
                                        navController.navigate(
                                            Screen.NoteDetailsScreen(
                                                noteId = note.id,
                                                folderId = id
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .animateItem()
                                        .height(220.dp)
                                )
                            }
                        } ?: PagingPlaceholder()
                    }
                }
            }
        }
        if (openDeleteDialog)
            AlertDialog(
                shape = RoundedCornerShape(25.dp),
                onDismissRequest = { openDeleteDialog = false },
                title = { Text(stringResource(Res.string.delete_note_confirmation_title)) },
                text = {
                    Text(
                        stringResource(
                            Res.string.delete_folder_confirmation_message,
                        )
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            viewModel.deleteCurrentFolder()
                            openDeleteDialog = false
                        },
                    ) {
                        Text(stringResource(Res.string.delete_folder), color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            openDeleteDialog = false
                        }) {
                        Text(stringResource(Res.string.cancel), color = Color.White)
                    }
                }
            )
        if (openEditDialog) {
            var folderName by remember { mutableStateOf(folder?.name ?: "") }
            AlertDialog(
                onDismissRequest = { openEditDialog = false },
                title = {
                    Text(
                        text = stringResource(Res.string.edit_folder),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    TextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        label = {
                            Text(
                                text = stringResource(Res.string.name),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                    )
                },
                confirmButton = {
                    Button(
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            viewModel.updateCurrentFolderName(folderName)
                            openEditDialog = false
                        },
                    ) {
                        Text(stringResource(Res.string.save), color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        shape = RoundedCornerShape(25.dp),
                        onClick = { openEditDialog = false },
                    ) {
                        Text(stringResource(Res.string.cancel), color = Color.White)
                    }
                }
            )
        }
    }
}
