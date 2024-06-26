package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.BookmarkRepository
import org.koin.core.annotation.Single

@Single
class SearchBookmarksUseCase(
    private val bookmarksRepository: BookmarkRepository
) {
    suspend operator fun invoke(query: String) = bookmarksRepository.searchBookmarks(query)
}