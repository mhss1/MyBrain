package com.mhss.app.ui.components.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.mhss.app.domain.model.Note
import com.mhss.app.ui.ItemView
import com.mhss.app.ui.Res
import com.mhss.app.ui.components.PagingLoadState
import com.mhss.app.ui.components.PagingPlaceholder
import com.mhss.app.ui.search_notes
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteSearchContent(
    notes: LazyPagingItems<Note>,
    modifier: Modifier = Modifier,
    onQueryChange: (String) -> Unit,
    onNoteClick: (Note) -> Unit,
    view: ItemView,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        var query by rememberSaveable {
            mutableStateOf("")
        }
        LaunchedEffect(query) { onQueryChange(query) }
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(true) { focusRequester.requestFocus() }
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
            },
            label = { Text(stringResource(Res.string.search_notes)) },
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .focusRequester(focusRequester)
        )
        PagingLoadState(notes)
        if (view == ItemView.LIST) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(12.dp)
            ) {
                items(notes.itemCount, key = notes.itemKey { it.id }) { index ->
                    notes[index]?.let { note ->
                        NoteCard(
                            note = note,
                            onClick = {
                                onNoteClick(note)
                            }
                        )
                    } ?: PagingPlaceholder()
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(150.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(12.dp)
            ) {
                items(notes.itemCount, key = notes.itemKey { it.id }) { index ->
                    notes[index]?.let { note ->
                        key(note.id) {
                            NoteCard(
                                note = note,
                                onClick = {
                                    onNoteClick(note)
                                },
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    } ?: PagingPlaceholder(Modifier.padding(bottom = 12.dp))
                }
            }
        }
    }
}