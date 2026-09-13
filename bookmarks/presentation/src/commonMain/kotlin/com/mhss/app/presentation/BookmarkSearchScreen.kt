package com.mhss.app.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.mhss.app.ui.ItemView
import com.mhss.app.ui.Res
import com.mhss.app.ui.components.PagingLoadState
import com.mhss.app.ui.components.PagingPlaceholder
import com.mhss.app.ui.invalid_url
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.search_bookmarks
import com.mhss.app.ui.snackbar.LocalisedSnackbarHost
import com.mhss.app.ui.snackbar.showSnackbar
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BookmarkSearchScreen(
    navController: NavHostController,
    viewModel: BookmarksViewModel = koinViewModel()
) {
    val bookmarks = viewModel.searchResults.collectAsLazyPagingItems()
    val state = viewModel.uiState
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { LocalisedSnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            var query by rememberSaveable {
                mutableStateOf("")
            }
            LaunchedEffect(query) { viewModel.onEvent(BookmarkEvent.SearchBookmarks(query)) }
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(true) { focusRequester.requestFocus() }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(stringResource(Res.string.search_bookmarks)) },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .focusRequester(focusRequester)
            )
            PagingLoadState(bookmarks)
            if (state.bookmarksView == ItemView.LIST) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(bookmarks.itemCount, key = bookmarks.itemKey { it.id }) { index ->
                        bookmarks[index]?.let { bookmark ->
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
                                }
                            )
                        } ?: PagingPlaceholder()
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(bookmarks.itemCount, key = bookmarks.itemKey { it.id }) { index ->
                        bookmarks[index]?.let { bookmark ->
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
                                    modifier = Modifier.animateItem()
                                )
                            }
                        } ?: PagingPlaceholder()
                    }
                }
            }
        }
    }
}
