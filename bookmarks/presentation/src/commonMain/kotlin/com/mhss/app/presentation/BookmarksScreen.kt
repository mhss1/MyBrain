@file:OptIn(ExperimentalLayoutApi::class)

package com.mhss.app.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import com.mhss.app.ui.ItemView
import com.mhss.app.ui.Res
import com.mhss.app.ui.add_bookmark
import com.mhss.app.ui.bookmarks
import com.mhss.app.ui.bookmarks_img
import com.mhss.app.ui.components.common.LiquidFloatingActionButton
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.ic_add
import com.mhss.app.ui.ic_search
import com.mhss.app.ui.ic_settings_sliders
import com.mhss.app.ui.invalid_url
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.no_bookmarks_message
import com.mhss.app.ui.order_by
import com.mhss.app.ui.search
import com.mhss.app.ui.snackbar.showSnackbar
import com.mhss.app.ui.titleRes
import com.mhss.app.ui.view_as
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource as cmpStringResource

@Composable
fun BookmarksScreen(
    navController: NavHostController,
    viewModel: BookmarksViewModel = koinViewModel()
) {
    val uiState = viewModel.uiState
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var orderSettingsVisible by remember { mutableStateOf(false) }
    val liquidState = rememberLiquidState()
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState)},
        topBar = {
            MyBrainAppBar(stringResource(Res.string.bookmarks))
        },
        floatingActionButton = {
            LiquidFloatingActionButton(
                onClick = {
                    navController.navigate(
                        Screen.BookmarkDetailScreen()
                    )
                },
                iconPainter = painterResource(Res.drawable.ic_add),
                contentDescription = stringResource(Res.string.add_bookmark),
                liquidState = liquidState
            )
        },
    ) { paddingValues ->
        if (uiState.bookmarks.isEmpty())
            NoBookmarksMessage()
        Column(Modifier.fillMaxSize().liquefiable(liquidState)) {
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
                        painter = painterResource(Res.drawable.ic_settings_sliders),
                        contentDescription = stringResource(Res.string.order_by)
                    )
                }
                IconButton(onClick = {
                    navController.navigate(Screen.BookmarkSearchScreen)
                }) {
                    Icon(
                        modifier = Modifier.size(25.dp),
                        painter = painterResource(Res.drawable.ic_search),
                        contentDescription = stringResource(Res.string.search)
                    )
                }
            }
            AnimatedVisibility(visible = orderSettingsVisible) {
                BookmarksSettingsSection(
                    uiState.bookmarksSortOrder,
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
                                    snackbarHostState.showSnackbar(Res.string.invalid_url)
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
                                        snackbarHostState.showSnackbar(Res.string.invalid_url)
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
    sortOrder: SortOrder,
    view: ItemView,
    onOrderChange: (SortOrder) -> Unit,
    onViewChange: (ItemView) -> Unit
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
                    Text(text = cmpStringResource(it.titleRes), style = MaterialTheme.typography.bodyLarge)
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
                    Text(text = cmpStringResource(it.titleRes), style = MaterialTheme.typography.bodyLarge)
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
                    Text(text = cmpStringResource(it.title), style = MaterialTheme.typography.bodyLarge)
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
            text = stringResource(Res.string.no_bookmarks_message),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            modifier = Modifier.size(125.dp),
            painter = painterResource(Res.drawable.bookmarks_img),
            contentDescription = stringResource(Res.string.no_bookmarks_message),
            alpha = 0.7f
        )
    }
}