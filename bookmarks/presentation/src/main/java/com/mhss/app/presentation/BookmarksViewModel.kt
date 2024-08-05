package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.ui.R
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.use_case.*
import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.preferences.domain.model.OrderType
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.toInt
import com.mhss.app.preferences.domain.model.toOrder
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import com.mhss.app.ui.ItemView
import com.mhss.app.ui.toNotesView
import com.mhss.app.util.date.now
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class BookmarksViewModel(
    private val addBookmark: AddBookmarkUseCase,
    private val updateBookmark: UpdateBookmarkUseCase,
    private val deleteBookmark: DeleteBookmarkUseCase,
    private val getAlBookmarks: GetAllBookmarksUseCase,
    private val searchBookmarks: SearchBookmarksUseCase,
    private val getBookmark: GetBookmarkUseCase,
    getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase
) : ViewModel() {


    var uiState by mutableStateOf(UiState())
        private set

    private var getBookmarksJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                getPreference(
                    intPreferencesKey(PrefsConstants.BOOKMARK_ORDER_KEY),
                    Order.DateModified(OrderType.ASC).toInt()
                ),
                getPreference(
                    intPreferencesKey(PrefsConstants.BOOKMARK_VIEW_KEY),
                    ItemView.LIST.value
                )
            ) { order, view ->
                uiState = uiState.copy(bookmarksOrder = order.toOrder())
                getBookmarks(order.toOrder())
                if (uiState.bookmarksView.value != view) {
                    uiState = uiState.copy(bookmarksView = view.toNotesView())
                }
            }.collect()
        }
    }

    fun onEvent(event: BookmarkEvent) {
        when (event) {
            is BookmarkEvent.AddBookmark -> viewModelScope.launch {
                uiState = if (
                    event.bookmark.url.isBlank()
                    && event.bookmark.title.isBlank()
                    && event.bookmark.description.isBlank()
                )
                    uiState.copy(navigateUp = true)
                else {
                    if (event.bookmark.url.isValidUrl()) {
                        addBookmark(event.bookmark)
                        uiState.copy(navigateUp = true)
                    } else
                        uiState.copy(error = R.string.invalid_url)
                }
            }
            is BookmarkEvent.DeleteBookmark -> viewModelScope.launch {
                deleteBookmark(event.bookmark)
                uiState = uiState.copy(navigateUp = true)
            }
            is BookmarkEvent.GetBookmark -> viewModelScope.launch {
                val bookmark = getBookmark(event.bookmarkId)
                uiState = uiState.copy(bookmark = bookmark)
            }
            is BookmarkEvent.SearchBookmarks -> viewModelScope.launch {
                val bookmarks = searchBookmarks(event.query)
                uiState = uiState.copy(searchBookmarks = bookmarks)
            }
            is BookmarkEvent.UpdateBookmark -> viewModelScope.launch {
                uiState = if (!event.bookmark.url.isValidUrl()) {
                    uiState.copy(error = R.string.invalid_url)
                } else {
                    updateBookmark(event.bookmark.copy(updatedDate = now()))
                    uiState.copy(navigateUp = true)
                }
            }
            is BookmarkEvent.UpdateOrder -> viewModelScope.launch {
                savePreference(
                    intPreferencesKey(PrefsConstants.BOOKMARK_ORDER_KEY),
                    event.order.toInt()
                )
            }
            is BookmarkEvent.UpdateView -> viewModelScope.launch {
                savePreference(
                    intPreferencesKey(PrefsConstants.BOOKMARK_VIEW_KEY),
                    event.view.value
                )
            }
            BookmarkEvent.ErrorDisplayed -> uiState = uiState.copy(error = null)
        }
    }

    data class UiState(
        val bookmarks: List<Bookmark> = emptyList(),
        val bookmarksOrder: Order = Order.DateModified(OrderType.ASC),
        val bookmarksView: ItemView = ItemView.LIST,
        val bookmark: Bookmark? = null,
        val error: Int? = null,
        val searchBookmarks: List<Bookmark> = emptyList(),
        val navigateUp: Boolean = false
    )

    private fun getBookmarks(order: Order) {
        getBookmarksJob?.cancel()
        getBookmarksJob = getAlBookmarks(order)
            .onEach { bookmarks ->
                uiState = uiState.copy(
                    bookmarks = bookmarks,
                    bookmarksOrder = order
                )
            }.launchIn(viewModelScope)
    }
}