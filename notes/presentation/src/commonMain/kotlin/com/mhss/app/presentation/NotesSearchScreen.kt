package com.mhss.app.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.mhss.app.ui.components.notes.NoteSearchContent
import com.mhss.app.ui.navigation.Screen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotesSearchScreen(
    navController: NavHostController,
    viewModel: NotesViewModel = koinViewModel()
) {
    val state by viewModel.notesUiState.collectAsStateWithLifecycle()
    NoteSearchContent(
        modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues()),
        notes = viewModel.searchResults.collectAsLazyPagingItems(),
        onQueryChange = { viewModel.onEvent(NoteEvent.SearchNotes(it)) },
        onNoteClick = {
            navController.navigate(
                Screen.NoteDetailsScreen(
                    noteId = it.id,
                    folderId = it.folderId
                )
            )
        },
        view = state.noteView
    )
}
