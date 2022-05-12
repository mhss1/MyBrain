package com.mhss.app.mybrain.domain.use_case.bookmarks

import com.mhss.app.mybrain.domain.repository.BookmarkRepository
import javax.inject.Inject

class GetAllBookmarksUseCase @Inject constructor(
    private val bookmarksRepository: BookmarkRepository
) {
    suspend operator fun invoke() = bookmarksRepository.getAllBookmarks()
}