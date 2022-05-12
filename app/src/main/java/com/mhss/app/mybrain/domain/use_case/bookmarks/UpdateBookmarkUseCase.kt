package com.mhss.app.mybrain.domain.use_case.bookmarks

import com.mhss.app.mybrain.domain.model.Bookmark
import com.mhss.app.mybrain.domain.repository.BookmarkRepository
import javax.inject.Inject

class UpdateBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(bookmark: Bookmark) = bookmarkRepository.updateBookmark(bookmark)
}