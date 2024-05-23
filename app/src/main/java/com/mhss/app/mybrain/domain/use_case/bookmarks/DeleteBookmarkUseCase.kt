package com.mhss.app.mybrain.domain.use_case.bookmarks

import com.mhss.app.mybrain.domain.model.bookmarks.Bookmark
import com.mhss.app.mybrain.domain.repository.bookmarks.BookmarkRepository
import org.koin.core.annotation.Single

@Single
class DeleteBookmarkUseCase(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(bookmark: Bookmark) = bookmarkRepository.deleteBookmark(bookmark)
}