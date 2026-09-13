package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.mhss.app.domain.use_case.AddBookmarkUseCase
import com.mhss.app.domain.use_case.GetAllBookmarksUseCase
import com.mhss.app.domain.use_case.SearchBookmarksUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.toInt
import com.mhss.app.preferences.domain.model.toSortOrder
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import com.mhss.app.ui.ItemView
import com.mhss.app.ui.toNotesView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class BookmarksViewModel(
    private val addBookmark: AddBookmarkUseCase,
    private val getAlBookmarks: GetAllBookmarksUseCase,
    private val searchBookmarks: SearchBookmarksUseCase,
    getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    private val pagingRequest = MutableStateFlow<SortOrder>(SortOrder.DateModified(SortType.DESC))
    val bookmarks = pagingRequest.flatMapLatest { getAlBookmarks.paged(it) }.cachedIn(viewModelScope)

    private val searchQuery = MutableStateFlow("")
    val searchResults = searchQuery.flatMapLatest {
        delay(250)
        searchBookmarks.paged(it)
    }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            combine(
                getPreference(
                    intPreferencesKey(PrefsConstants.BOOKMARK_ORDER_KEY),
                    SortOrder.DateModified(SortType.DESC).toInt()
                ),
                getPreference(
                    intPreferencesKey(PrefsConstants.BOOKMARK_VIEW_KEY),
                    ItemView.LIST.value
                )
            ) { order, view ->
                uiState = uiState.copy(bookmarksSortOrder = order.toSortOrder())
                getBookmarks(order.toSortOrder())
                if (uiState.bookmarksView.value != view) {
                    uiState = uiState.copy(bookmarksView = view.toNotesView())
                }
            }.collect()
        }
    }

    fun onEvent(event: BookmarkEvent) {
        when (event) {
            is BookmarkEvent.AddBookmark -> viewModelScope.launch {
                addBookmark(event.bookmark)
            }

            is BookmarkEvent.SearchBookmarks -> searchQuery.value = event.query

            is BookmarkEvent.UpdateOrder -> viewModelScope.launch {
                savePreference(
                    intPreferencesKey(PrefsConstants.BOOKMARK_ORDER_KEY),
                    event.sortOrder.toInt()
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
        val bookmarksSortOrder: SortOrder = SortOrder.DateModified(SortType.DESC),
        val bookmarksView: ItemView = ItemView.LIST,
        val error: Int? = null,
    )

    private fun getBookmarks(sortOrder: SortOrder) {
        pagingRequest.value = sortOrder
        uiState = uiState.copy(bookmarksSortOrder = sortOrder)
    }
}
