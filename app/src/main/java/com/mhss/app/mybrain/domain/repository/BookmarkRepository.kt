package com.mhss.app.mybrain.domain.repository

import com.mhss.app.mybrain.data.local.dao.BookmarkDao
import com.mhss.app.mybrain.domain.model.Bookmark
import javax.inject.Inject

interface BookmarkRepository {

    suspend fun getAllBookmarks(): List<Bookmark>

    suspend fun getBookmark(id: Int): Bookmark

    suspend fun addBookmark(bookmark: Bookmark)

    suspend fun deleteBookmark(bookmark: Bookmark)

    suspend fun updateBookmark(bookmark: Bookmark)
}