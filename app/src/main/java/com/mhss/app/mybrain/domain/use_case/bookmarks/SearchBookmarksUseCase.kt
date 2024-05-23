package com.mhss.app.mybrain.domain.use_case.bookmarks

import com.mhss.app.mybrain.domain.repository.bookmarks.BookmarkRepository
import org.koin.core.annotation.Single

@Single
class SearchBookmarksUseCase(
    private val bookmarksRepository: BookmarkRepository
) {
    suspend operator fun invoke(query: String) = bookmarksRepository.searchBookmarks(query)
}