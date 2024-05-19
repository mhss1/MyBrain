package com.mhss.app.mybrain.domain.use_case.bookmarks

import com.mhss.app.mybrain.domain.model.Bookmark
import com.mhss.app.mybrain.domain.repository.BookmarkRepository
import org.koin.core.annotation.Single

@Single
class AddBookmarkUseCase(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(bookmark: Bookmark) = bookmarkRepository.addBookmark(bookmark)
}