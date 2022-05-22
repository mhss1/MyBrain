package com.mhss.app.mybrain.presentation.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowRow
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.presentation.util.Screen
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.ItemView
import com.mhss.app.mybrain.util.settings.Order
import com.mhss.app.mybrain.util.settings.OrderType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    navController: NavHostController,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val uiState = viewModel.notesUiState
    var orderSettingsVisible by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.notes),
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
                    navController.navigate(
                        Screen.NoteDetailsScreen.route.replace(
                            "{${Constants.NOTE_ID_ARG}}",
                            "${-1}"
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
        },
    ) {
        if (uiState.notes.isEmpty())
            NoNotesMessage()
        Column {
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
            if (uiState.noteView == ItemView.LIST){
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp, start = 12.dp, end = 12.dp)
                ) {
                    items(uiState.notes, key = {it.id}) { note ->
                        NoteItem(
                            note = note,
                            onClick = {
                                navController.navigate(
                                    Screen.NoteDetailsScreen.route.replace(
                                        "{${Constants.NOTE_ID_ARG}}",
                                        "${note.id}"
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
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp, start = 12.dp, end = 12.dp)
                ){
                    items(uiState.notes){ note ->
                        key(note.id) {
                            NoteItem(
                                note = note,
                                onClick = {
                                    navController.navigate(
                                        Screen.NoteDetailsScreen.route.replace(
                                            "{${Constants.NOTE_ID_ARG}}",
                                            "${note.id}"
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
    }
}

@Composable
fun NotesSettingsSection(order: Order, view: ItemView, onOrderChange: (Order) -> Unit, onViewChange: (ItemView) -> Unit) {
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
            text = stringResource(R.string.no_notes_yet),
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            modifier = Modifier.size(125.dp),
            painter = painterResource(id = R.drawable.notes_img),
            contentDescription = stringResource(R.string.no_notes_yet),
            alpha = 0.7f
        )
    }
}
