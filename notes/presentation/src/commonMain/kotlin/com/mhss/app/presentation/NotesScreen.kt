@file:OptIn(ExperimentalLayoutApi::class)

package com.mhss.app.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mhss.app.domain.model.NoteFolder
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import com.mhss.app.ui.ItemView
import com.mhss.app.ui.Res
import com.mhss.app.ui.add_note
import com.mhss.app.ui.cancel
import com.mhss.app.ui.components.common.LiquidFloatingActionButton
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.components.notes.NoteCard
import com.mhss.app.ui.create_folder
import com.mhss.app.ui.folders
import com.mhss.app.ui.ic_add
import com.mhss.app.ui.ic_create_folder
import com.mhss.app.ui.ic_folder
import com.mhss.app.ui.ic_search
import com.mhss.app.ui.ic_settings_sliders
import com.mhss.app.ui.name
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.no_notes_message
import com.mhss.app.ui.notes
import com.mhss.app.ui.notes_img
import com.mhss.app.ui.order_by
import com.mhss.app.ui.search
import com.mhss.app.ui.show_all_notes
import com.mhss.app.ui.snackbar.LocalisedSnackbarHost
import com.mhss.app.ui.titleRes
import com.mhss.app.ui.view_as
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource as cmpStringResource

@Suppress("AssignedValueIsNeverRead")
@Composable
fun NotesScreen(
    navController: NavHostController,
    viewModel: NotesViewModel = koinViewModel()
) {
    val uiState by viewModel.notesUiState.collectAsStateWithLifecycle()
    var orderSettingsVisible by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var openCreateFolderDialog by remember { mutableStateOf(false) }
    val liquidState = rememberLiquidState()
    Scaffold(
        snackbarHost = {
            LocalisedSnackbarHost(uiState.snackbarHostState)
        },
        topBar = {
            MyBrainAppBar(
                if (selectedTab == 0) stringResource(Res.string.notes) else stringResource(
                    Res.string.folders
                )
            )
        },
        floatingActionButton = {
            LiquidFloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        navController.navigate(Screen.NoteDetailsScreen())
                    } else {
                        openCreateFolderDialog = true
                    }
                },
                iconPainter = if (selectedTab == 0) painterResource(Res.drawable.ic_add) else painterResource(
                    Res.drawable.ic_create_folder
                ),
                contentDescription = stringResource(Res.string.add_note),
                liquidState = liquidState
            )

        },
    ) { paddingValues ->
        Column(modifier = Modifier.liquefiable(liquidState).padding(paddingValues).fillMaxSize()) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                Tab(
                    text = {
                        Text(
                            stringResource(Res.string.notes),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                    },
                    unselectedContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Tab(
                    text = {
                        Text(
                            stringResource(Res.string.folders),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                    },
                    unselectedContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
            if (selectedTab == 0) {
                if (uiState.notes.isEmpty())
                    NoNotesMessage()
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { orderSettingsVisible = !orderSettingsVisible }) {
                        Icon(
                            modifier = Modifier.size(25.dp),
                            painter = painterResource(Res.drawable.ic_settings_sliders),
                            contentDescription = stringResource(Res.string.order_by)
                        )
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.NoteSearchScreen)
                    }) {
                        Icon(
                            modifier = Modifier.size(25.dp),
                            painter = painterResource(Res.drawable.ic_search),
                            contentDescription = stringResource(Res.string.search)
                        )
                    }
                }
                AnimatedVisibility(visible = orderSettingsVisible) {
                    NotesSettingsSection(
                        uiState.notesSortOrder,
                        uiState.noteView,
                        uiState.showAllNotes,
                        onOrderChange = {
                            viewModel.onEvent(NoteEvent.UpdateOrder(it))
                        },
                        onViewChange = {
                            viewModel.onEvent(NoteEvent.UpdateView(it))
                        },
                        onShowAllNotesChange = {
                            viewModel.onEvent(NoteEvent.ShowAllNotes(it))
                        }
                    )
                }
                if (uiState.noteView == ItemView.LIST) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            top = 12.dp,
                            bottom = 24.dp,
                            start = 12.dp,
                            end = 12.dp
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = {
                                    navController.navigate(
                                        Screen.NoteDetailsScreen(
                                            noteId = note.id,
                                            folderId = note.folderId
                                        )
                                    )
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.notes) { note ->
                            key(note.id) {
                                NoteCard(
                                    note = note,
                                    onClick = {
                                        navController.navigate(
                                            Screen.NoteDetailsScreen(
                                                noteId = note.id,
                                                folderId = note.folderId
                                            )
                                        )
                                    },
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                FoldersTab(uiState.folders) {
                    navController.navigate(
                        Screen.NoteFolderDetailsScreen(
                            folderId = it.id
                        )
                    )
                }
                if (openCreateFolderDialog)
                    CreateFolderDialog(
                        onCreate = {
                            viewModel.onEvent(
                                NoteEvent.CreateFolder(name = it.trim())
                            )
                            openCreateFolderDialog = false
                        },
                        onDismiss = {
                            openCreateFolderDialog = false
                        }
                    )
            }
        }
    }
}

@Composable
fun FoldersTab(
    folders: List<NoteFolder>,
    onItemClick: (NoteFolder) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            top = 12.dp,
            bottom = 24.dp,
            start = 12.dp,
            end = 12.dp
        )
    ) {
        items(folders) { folder ->
            Card(
                modifier = Modifier.height(180.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.elevatedCardElevation(
                    8.dp
                )
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .clickable { onItemClick(folder) },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_folder),
                        contentDescription = folder.name,
                        modifier = Modifier.size(100.dp)
                    )
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotesSettingsSection(
    sortOrder: SortOrder,
    view: ItemView,
    showAllNotes: Boolean,
    onOrderChange: (SortOrder) -> Unit,
    onViewChange: (ItemView) -> Unit,
    onShowAllNotesChange: (Boolean) -> Unit
) {
    val sortOrders = remember {
        listOf(
            SortOrder.DateModified(),
            SortOrder.DateCreated(),
            SortOrder.Alphabetical()
        )
    }
    val sortTypes = remember {
        listOf(
            SortType.ASC,
            SortType.DESC
        )
    }
    val noteViews = remember {
        listOf(
            ItemView.LIST,
            ItemView.GRID
        )
    }
    Column(
        Modifier.background(color = MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = stringResource(Res.string.order_by),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
        FlowRow(
            modifier = Modifier.padding(end = 8.dp)
        ) {
            sortOrders.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = sortOrder::class == it::class,
                        onClick = {
                            if (sortOrder != it)
                                onOrderChange(
                                    it.copyOrder(sortType = sortOrder.sortType)
                                )
                        }
                    )
                    Text(
                        text = cmpStringResource(it.titleRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        HorizontalDivider()
        FlowRow {
            sortTypes.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = sortOrder.sortType == it,
                        onClick = {
                            if (sortOrder != it)
                                onOrderChange(
                                    sortOrder.copyOrder(it)
                                )
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = cmpStringResource(it.titleRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        HorizontalDivider()
        Text(
            text = stringResource(Res.string.view_as),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        )
        FlowRow {
            noteViews.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = view.title == it.title,
                        onClick = {
                            if (view.title != it.title)
                                onViewChange(
                                    it
                                )
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = cmpStringResource(it.title),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = showAllNotes, onCheckedChange = { onShowAllNotesChange(it) })
            Text(
                text = stringResource(Res.string.show_all_notes),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun NoNotesMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.no_notes_message),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            modifier = Modifier.size(125.dp),
            painter = painterResource(Res.drawable.notes_img),
            contentDescription = stringResource(Res.string.no_notes_message),
            alpha = 0.7f
        )
    }
}

@Suppress("AssignedValueIsNeverRead")
@Composable
fun CreateFolderDialog(
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(Res.string.create_folder),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
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
                    onCreate(name)
                },
            ) {
                Text(stringResource(Res.string.create_folder), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                shape = RoundedCornerShape(25.dp),
                onClick = { onDismiss() },
            ) {
                Text(stringResource(Res.string.cancel), color = Color.White)
            }
        }
    )
}
