package com.mhss.app.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.use_case.AddBookmarkUseCase
import com.mhss.app.domain.use_case.DeleteBookmarkUseCase
import com.mhss.app.domain.use_case.GetBookmarkUseCase
import com.mhss.app.domain.use_case.UpdateBookmarkUseCase
import com.mhss.app.util.date.now
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class BookmarkDetailsViewModel(
    private val getBookmark: GetBookmarkUseCase,
    private val updateBookmark: UpdateBookmarkUseCase,
    private val addBookmark: AddBookmarkUseCase,
    private val deleteBookmark: DeleteBookmarkUseCase,
    bookmarkId: Int,
) : ViewModel() {

    var bookmarkDetailsUiState by mutableStateOf(BookmarkDetailsUiState())
        private set

    init {
        viewModelScope.launch {
            val bookmark = if (bookmarkId != -1) getBookmark(bookmarkId) else null
            bookmarkDetailsUiState = bookmarkDetailsUiState.copy(
                bookmark = bookmark
            )
        }
    }

    fun onEvent(event: BookmarkDetailsEvent) {
        when (event) {
            BookmarkDetailsEvent.ErrorDisplayed -> {
                bookmarkDetailsUiState = bookmarkDetailsUiState.copy(error = null)
            }

            is BookmarkDetailsEvent.ScreenOnStop -> viewModelScope.launch {
                if (bookmarkDetailsUiState.bookmark == null) {
                    if (event.bookmark.url.isNotBlank()
                        || event.bookmark.title.isNotBlank()
                        || event.bookmark.description.isNotBlank()
                    ) {
                        val bookmark = event.bookmark.copy(
                            createdDate = now(),
                            updatedDate = now()
                        )
                        val id = addBookmark(bookmark)
                        bookmarkDetailsUiState = bookmarkDetailsUiState.copy(bookmark = bookmark.copy(id = id.toInt()))
                    }
                } else if (bookmarkChanged(bookmarkDetailsUiState.bookmark!!, event.bookmark)) {
                    val newBookmark = bookmarkDetailsUiState.bookmark!!.copy(
                        title = event.bookmark.title,
                        description = event.bookmark.description,
                        url = event.bookmark.url,
                        updatedDate = now()
                    )
                    updateBookmark(newBookmark)
                    bookmarkDetailsUiState = bookmarkDetailsUiState.copy(bookmark = newBookmark)
                }
            }

            is BookmarkDetailsEvent.DeleteBookmark -> viewModelScope.launch {
                deleteBookmark(bookmarkDetailsUiState.bookmark!!)
                bookmarkDetailsUiState = bookmarkDetailsUiState.copy(navigateUp = true)
            }
        }
    }

    data class BookmarkDetailsUiState(
        val bookmark: Bookmark? = null,
        val navigateUp: Boolean = false,
        val error: Int? = null,
    )

    private fun bookmarkChanged(
        bookmark: Bookmark,
        newBookmark: Bookmark,
    ): Boolean {
        return bookmark.title != newBookmark.title ||
                bookmark.description != newBookmark.description ||
                bookmark.url != newBookmark.url
    }
}