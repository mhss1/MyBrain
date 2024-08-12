package com.mhss.app.presentation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.mhss.app.ui.components.notes.NoteSearchContent
import com.mhss.app.ui.navigation.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotesSearchScreen(
    navController: NavHostController,
    viewModel: NotesViewModel = koinViewModel()
) {
    val state = viewModel.notesUiState
    NoteSearchContent(
        notes = state.searchNotes,
        onQueryChange = { viewModel.onEvent(NoteEvent.SearchNotes(it)) },
        onNoteClick = {
            navController.navigate(
                Screen.NoteDetailsScreen(
                    noteId = it.id
                )
            )
        },
        view = state.noteView
    )
}