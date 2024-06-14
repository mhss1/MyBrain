@file:OptIn(ExperimentalLayoutApi::class)

package com.mhss.app.mybrain.presentation.bookmarks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.presentation.common.navigation.Screen
import com.mhss.app.mybrain.presentation.common.ItemView
import com.mhss.app.mybrain.domain.model.preferences.Order
import com.mhss.app.mybrain.domain.model.preferences.OrderType
import kotlinx.coroutines.launch
import com.mhss.app.mybrain.app.getString
import com.mhss.app.mybrain.presentation.common.titleRes
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    navController: NavHostController,
    viewModel: BookmarksViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var orderSettingsVisible by remember { mutableStateOf(false) }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState)},
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.bookmarks),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(
                        Screen.BookmarkDetailScreen()
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    modifier = Modifier.size(25.dp),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.add_bookmark),
                    tint = Color.White
                )
            }
        },
    ) { paddingValues ->
        if (uiState.bookmarks.isEmpty())
            NoBookmarksMessage()
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(paddingValues),
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
                    navController.navigate(Screen.BookmarkSearchScreen)
                }) {
                    Icon(
                        modifier = Modifier.size(25.dp),
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = stringResource(R.string.search)
                    )
                }
            }
            AnimatedVisibility(visible = orderSettingsVisible) {
                BookmarksSettingsSection(
                    uiState.bookmarksOrder,
                    uiState.bookmarksView,
                    onOrderChange = {
                        viewModel.onEvent(BookmarkEvent.UpdateOrder(it))
                    },
                    onViewChange = {
                        viewModel.onEvent(BookmarkEvent.UpdateView(it))
                    }
                )
            }
            if (uiState.bookmarksView == ItemView.LIST) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(uiState.bookmarks, key = { it.id }) { bookmark ->
                        BookmarkItem(
                            bookmark = bookmark,
                            onClick = {
                                navController.navigate(
                                    Screen.BookmarkDetailScreen(
                                        bookmarkId = bookmark.id
                                    )
                                )
                            },
                            onInvalidUrl = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        getString(R.string.invalid_url)
                                    )
                                }
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(uiState.bookmarks) { bookmark ->
                        key(bookmark.id) {
                            BookmarkItem(
                                bookmark = bookmark,
                                onClick = {
                                    navController.navigate(
                                        Screen.BookmarkDetailScreen(
                                            bookmarkId = bookmark.id
                                        )
                                    )
                                },
                                onInvalidUrl = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            getString(R.string.invalid_url)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .animateItem()
                                    .height(220.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarksSettingsSection(
    order: Order,
    view: ItemView,
    onOrderChange: (Order) -> Unit,
    onViewChange: (ItemView) -> Unit
) {
    val orders = remember {
        listOf(
            Order.DateModified(),
            Order.DateCreated(),
            Order.Alphabetical()
        )
    }
    val orderTypes = remember {
        listOf(
            OrderType.ASC,
            OrderType.DESC
        )
    }
    val views = remember {
        listOf(
            ItemView.LIST,
            ItemView.GRID
        )
    }
    Column(
        Modifier.background(color = MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = stringResource(R.string.order_by),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
        FlowRow(
            modifier = Modifier.padding(end = 8.dp)
        ) {
            orders.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = order == it,
                        onClick = {
                            if (order != it)
                                onOrderChange(
                                    it.copyOrder(orderType = order.orderType)
                                )
                        }
                    )
                    Text(text = stringResource(it.titleRes), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        HorizontalDivider()
        FlowRow {
            orderTypes.forEach {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = order.orderType == it,
                        onClick = {
                            if (order != it)
                                onOrderChange(
                                    order.copyOrder(it)
                                )
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(it.titleRes), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        HorizontalDivider()
        Text(
            text = stringResource(R.string.view_as),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        )
        FlowRow {
            views.forEach {
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
                    Text(text = stringResource(it.title), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun NoBookmarksMessage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_bookmarks_message),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            modifier = Modifier.size(125.dp),
            painter = painterResource(id = R.drawable.bookmarks_img),
            contentDescription = stringResource(R.string.no_bookmarks_message),
            alpha = 0.7f
        )
    }
}