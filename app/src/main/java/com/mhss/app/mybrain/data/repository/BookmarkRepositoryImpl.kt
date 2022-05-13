package com.mhss.app.mybrain.data.repository

import com.mhss.app.mybrain.data.local.dao.BookmarkDao
import com.mhss.app.mybrain.domain.model.Bookmark
import com.mhss.app.mybrain.domain.repository.BookmarkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookmarkRepositoryImpl (
    private val bookmarkDao: BookmarkDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BookmarkRepository {
    override suspend fun getAllBookmarks(): List<Bookmark> {
        return withContext(ioDispatcher) {
            bookmarkDao.getAll()
        }
    }

    override suspend fun getBookmark(id: Int): Bookmark {
        return withContext(ioDispatcher) {
            bookmarkDao.getBookmark(id)
        }
    }

    override suspend fun searchBookmarks(query: String): List<Bookmark> {
        return withContext(ioDispatcher) {
            bookmarkDao.getBookmarksByTitle(query)
        }
    }

    override suspend fun addBookmark(bookmark: Bookmark) {
        withContext(ioDispatcher) {
            bookmarkDao.insertBookmark(bookmark)
        }
    }

    override suspend fun deleteBookmark(bookmark: Bookmark) {
        withContext(ioDispatcher) {
            bookmarkDao.deleteBookmark(bookmark)
        }
    }

    override suspend fun updateBookmark(bookmark: Bookmark) {
        withContext(ioDispatcher) {
            bookmarkDao.updateBookmark(bookmark)
        }
    }
}