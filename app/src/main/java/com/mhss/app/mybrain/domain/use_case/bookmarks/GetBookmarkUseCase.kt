package com.mhss.app.mybrain.domain.use_case.bookmarks

import com.mhss.app.mybrain.domain.repository.bookmarks.BookmarkRepository
import org.koin.core.annotation.Single

@Single
class GetBookmarkUseCase(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(id: Int) = bookmarkRepository.getBookmark(id)
}