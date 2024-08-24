package com.mhss.app.domain.use_case

import com.mhss.app.domain.repository.BookmarkRepository
import org.koin.core.annotation.Single

@Single
class GetBookmarkUseCase(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(id: Int) = bookmarkRepository.getBookmark(id)
}