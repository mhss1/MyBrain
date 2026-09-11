package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.repository.BookmarkRepository
import com.mhss.app.preferences.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetAllBookmarksUseCase(
    private val bookmarksRepository: BookmarkRepository
) {
    operator fun invoke(sortOrder: SortOrder) : Flow<List<Bookmark>>{
        return bookmarksRepository.getAllBookmarks(sortOrder)
    }
}
