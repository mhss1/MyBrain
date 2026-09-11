package com.mhss.app.presentation

import com.mhss.app.domain.model.Bookmark
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.ui.ItemView

sealed class BookmarkEvent {
    data class AddBookmark(val bookmark: Bookmark) : BookmarkEvent()
    data class SearchBookmarks(val query: String) : BookmarkEvent()
    data class UpdateOrder(val sortOrder: SortOrder) : BookmarkEvent()
    data class UpdateView(val view: ItemView) : BookmarkEvent()
    data object ErrorDisplayed: BookmarkEvent()
}
