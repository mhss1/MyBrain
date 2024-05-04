package com.mhss.app.mybrain.presentation.diary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.mhss.app.mybrain.util.settings.Order
import com.mhss.app.mybrain.util.settings.OrderType

@Composable
fun DiaryScreen(
    navController: NavHostController,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    var orderSettingsVisible by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.diary),
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold)
                    )
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp,
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.DiaryChartScreen.route)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chart),
                            contentDescription = stringResource(R.string.diary_chart),
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(
                        Screen.DiaryDetailScreen.route.replace(
                            "{${Constants.DIARY_ID_ARG}}",
                            "${-1}"
                        )
                    )
                },
                backgroundColor = MaterialTheme.colors.primary,
            ) {
                Icon(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_entry),
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        if (uiState.entries.isEmpty()) { NoEntriesMessage() }
        Column(Modifier.padding(paddingValues)) {
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
                    navController.navigate(Screen.DiarySearchScreen.route)
                }) {
                    Icon(
                        modifier = Modifier.size(25.dp),
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = stringResource(R.string.search)
                    )
                }
            }
            AnimatedVisibility(visible = orderSettingsVisible) {
                DiarySettingsSection(
                    uiState.entriesOrder,
                    onOrderChange = {
                        viewModel.onEvent(DiaryEvent.UpdateOrder(it))
                    },
                )
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(12.dp)
            ) {
                items(uiState.entries, key = { it.id }) { entry ->
                    DiaryEntryItem(
                        entry = entry,
                        onClick = {
                            navController.navigate(
                                Screen.DiaryDetailScreen.route.replace(
                                    "{${Constants.DIARY_ID_ARG}}",
                                    "${entry.id}"
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DiarySettingsSection(order: Order, onOrderChange: (Order) -> Unit) {
    val orders = listOf(
        Order.DateModified(),
        Order.DateCreated(),
        Order.Alphabetical()
    )
    val orderTypes = listOf(
        OrderType.ASC(),
        OrderType.DESC()
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
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun NoEntriesMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_entries_message),
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            modifier = Modifier.size(125.dp),
            painter = painterResource(id = R.drawable.diary_img),
            contentDescription = stringResource(R.string.no_entries_message),
            alpha = 0.7f
        )
    }
}
