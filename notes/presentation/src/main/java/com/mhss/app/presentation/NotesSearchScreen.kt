package com.mhss.app.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
        modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues()),
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