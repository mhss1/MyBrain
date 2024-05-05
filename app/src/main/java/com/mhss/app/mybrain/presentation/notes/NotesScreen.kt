package com.mhss.app.mybrain.presentation.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowRow
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.NoteFolder
import com.mhss.app.mybrain.presentation.util.Screen
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    navController: NavHostController,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState = viewModel.notesUiState
    var orderSettingsVisible by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var openCreateFolderDialog by remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scaffoldState.snackbarHostState.showSnackbar(
                it
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
                        text = if (selectedTab == 0) stringResource(R.string.notes) else stringResource(
                            R.string.folders
                        ),
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold)
                    )
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        navController.navigate(
                            Screen.NoteDetailsScreen.route.replace(
                                "{${Constants.NOTE_ID_ARG}}",
                                "-1"
                            ).replace(
                                "{${Constants.FOLDER_ID}}",
                                "-1"
                            )
                        )
                    } else {
                        openCreateFolderDialog = true
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
            ) {
                Icon(
                    modifier = Modifier.size(25.dp),
                    painter = if (selectedTab == 0) painterResource(R.drawable.ic_add) else painterResource(
                        R.drawable.ic_create_folder
                    ),
                    contentDescription = stringResource(R.string.add_note),
                    tint = Color.White
                )
            }
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTab,
                backgroundColor = MaterialTheme.colors.background
            ) {
                Tab(
                    text = { Text(stringResource(R.string.notes)) },
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                    }
                )
                Tab(
                    text = { Text(stringResource(R.string.folders)) },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                    }
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
                            painter = painterResource(R.drawable.ic_settings_sliders),
                            contentDescription = stringResource(R.string.order_by)
                        )
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.NoteSearchScreen.route)
                    }) {
                        Icon(
                            modifier = Modifier.size(25.dp),
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = stringResource(R.string.search)
                        )
                    }
                }
                AnimatedVisibility(visible = orderSettingsVisible) {
                    NotesSettingsSection(
                        uiState.notesOrder,
                        uiState.noteView,
                        onOrderChange = {
                            viewModel.onEvent(NoteEvent.UpdateOrder(it))
                        },
                        onViewChange = {
                            viewModel.onEvent(NoteEvent.UpdateView(it))
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
                        )
                    ) {
                        items(uiState.notes, key = { it.id }) { note ->
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
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        items(uiState.notes) { note ->
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
                                                ""
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
                        Screen.NoteFolderDetailsScreen.route.replace(
                            "{${Constants.FOLDER_ID}}",
                            "${it.id}"
                        )
                    )
                }
                if (openCreateFolderDialog)
                    CreateFolderDialog(
                        onCreate = {
                            viewModel.onEvent(NoteEvent.CreateFolder(NoteFolder(it.trim())))
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
                elevation = 8.dp
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .clickable { onItemClick(folder) },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_folder),
                        contentDescription = folder.name,
                        modifier = Modifier.size(100.dp)
                    )
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
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

@Composable
fun NotesSettingsSection(
    order: Order,
    view: ItemView,
    onOrderChange: (Order) -> Unit,
    onViewChange: (ItemView) -> Unit
) {
    val orders = listOf(
        Order.DateModified(),
        Order.DateCreated(),
        Order.Alphabetical()
    )
    val orderTypes = listOf(
        OrderType.ASC(),
        OrderType.DESC()
    )
    val noteViews = listOf(
        ItemView.LIST,
        ItemView.GRID
    )
    Column(
        Modifier.background(color = MaterialTheme.colors.background)
    ) {
        Text(
            text = stringResource(R.string.order_by),
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(start = 8.dp)
        )
        FlowRow(
            modifier = Modifier.padding(end = 8.dp)
        ) {
            orders.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = order.orderTitle == it.orderTitle,
                        onClick = {
                            if (order.orderTitle != it.orderTitle)
                                onOrderChange(
                                    it.copy(orderType = order.orderType)
                                )
                        }
                    )
                    Text(text = it.orderTitle, style = MaterialTheme.typography.body1)
                }
            }
        }
        Divider()
        FlowRow {
            orderTypes.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = order.orderType.orderTitle == it.orderTitle,
                        onClick = {
                            if (order.orderTitle != it.orderTitle)
                                onOrderChange(
                                    order.copy(it)
                                )
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = it.orderTitle, style = MaterialTheme.typography.body1)
                }
            }
        }
        Divider()
        Text(
            text = stringResource(R.string.view_as),
            style = MaterialTheme.typography.body1,
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
                    Text(text = stringResource(it.title), style = MaterialTheme.typography.body1)
                }
            }
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
            text = stringResource(R.string.no_notes_message),
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            modifier = Modifier.size(125.dp),
            painter = painterResource(id = R.drawable.notes_img),
            contentDescription = stringResource(R.string.no_notes_message),
            alpha = 0.7f
        )
    }
}

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
                text = stringResource(id = R.string.create_folder),
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
                    onCreate(name)
                },
            ) {
                Text(stringResource(R.string.create_folder), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                shape = RoundedCornerShape(25.dp),
                onClick = { onDismiss() },
            ) {
                Text(stringResource(R.string.cancel), color = Color.White)
            }
        }
    )
}
