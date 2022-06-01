package com.mhss.app.mybrain.domain.use_case.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
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
    folder: String,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState = viewModel.notesUiState
    LaunchedEffect(true) {viewModel.onEvent(NoteEvent.GetFolderNotes(folder)) }
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
                                "{${Constants.FOLDER_NAME}}",
                                ""
                            )
                        )
                    }
                )
            }
        }
    } else {
        LazyVerticalGrid(
            cells = GridCells.Adaptive(150.dp),
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
                                    "{${Constants.FOLDER_NAME}}",
                                    ""
                                )
                            )
                        },
                        modifier = Modifier.height(220.dp)
                    )
                }
            }
        }
    }
}