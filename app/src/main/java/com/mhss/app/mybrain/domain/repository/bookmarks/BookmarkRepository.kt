package com.mhss.app.mybrain.domain.repository.bookmarks

import com.mhss.app.mybrain.domain.model.bookmarks.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {

    fun getAllBookmarks(): Flow<List<Bookmark>>

    suspend fun getBookmark(id: Int): Bookmark

    suspend fun searchBookmarks(query: String): List<Bookmark>

    suspend fun addBookmark(bookmark: Bookmark)

    suspend fun deleteBookmark(bookmark: Bookmark)

    suspend fun updateBookmark(bookmark: Bookmark)
}