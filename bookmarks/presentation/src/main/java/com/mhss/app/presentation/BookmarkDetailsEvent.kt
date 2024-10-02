package com.mhss.app.presentation

import com.mhss.app.domain.model.Bookmark

sealed class BookmarkDetailsEvent {
    data class ScreenOnStop(val bookmark: Bookmark): BookmarkDetailsEvent()
    data class DeleteBookmark(val bookmark: Bookmark) : BookmarkDetailsEvent()
    data object ErrorDisplayed : BookmarkDetailsEvent()
}