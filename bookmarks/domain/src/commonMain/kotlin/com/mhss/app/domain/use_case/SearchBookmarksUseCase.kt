package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.BookmarkRepository
import org.koin.core.annotation.Single

@Single
class SearchBookmarksUseCase(
    private val bookmarksRepository: BookmarkRepository
) {
    fun paged(query: String) = bookmarksRepository.searchPagedBookmarks(query)

    suspend operator fun invoke(query: String) = bookmarksRepository.searchBookmarks(query)
}
