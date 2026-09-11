package com.mhss.app.domain.repository

import com.mhss.app.domain.model.Bookmark
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {

    fun getAllBookmarks(
        sortOrder: SortOrder = SortOrder.DateModified(SortType.DESC)
    ): Flow<List<Bookmark>>

    suspend fun getBookmark(id: String): Bookmark

    suspend fun searchBookmarks(query: String): List<Bookmark>

    suspend fun addBookmark(bookmark: Bookmark): Long

    suspend fun upsertBookmarks(bookmarks: List<Bookmark>, notifyChange: Boolean = true)

    suspend fun deleteBookmark(bookmark: Bookmark)

    suspend fun updateBookmark(bookmark: Bookmark)
}
