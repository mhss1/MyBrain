package com.mhss.app.mybrain.data.repository

import com.mhss.app.mybrain.data.local.dao.BookmarkDao
import com.mhss.app.mybrain.data.local.entity.toBookmark
import com.mhss.app.mybrain.data.local.entity.toBookmarkEntity
import com.mhss.app.mybrain.di.namedIoDispatcher
import com.mhss.app.mybrain.domain.model.bookmarks.Bookmark
import com.mhss.app.mybrain.domain.repository.bookmarks.BookmarkRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class BookmarkRepositoryImpl(
    private val bookmarkDao: BookmarkDao,
    @Named(namedIoDispatcher) private val ioDispatcher: CoroutineDispatcher
) : BookmarkRepository {

    override fun getAllBookmarks(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarks()
            .flowOn(ioDispatcher)
            .map { bookmarks ->
                bookmarks.map {
                    it.toBookmark()
                }
            }
    }

    override suspend fun getBookmark(id: Int): Bookmark {
        return withContext(ioDispatcher) {
            bookmarkDao.getBookmark(id).toBookmark()
        }
    }

    override suspend fun searchBookmarks(query: String): List<Bookmark> {
        return withContext(ioDispatcher) {
            bookmarkDao.getBookmark(query).map { it.toBookmark() }
        }
    }

    override suspend fun addBookmark(bookmark: Bookmark) {
        withContext(ioDispatcher) {
            bookmarkDao.insertBookmark(bookmark.toBookmarkEntity())
        }
    }

    override suspend fun deleteBookmark(bookmark: Bookmark) {
        withContext(ioDispatcher) {
            bookmarkDao.deleteBookmark(bookmark.toBookmarkEntity())
        }
    }

    override suspend fun updateBookmark(bookmark: Bookmark) {
        withContext(ioDispatcher) {
            bookmarkDao.updateBookmark(bookmark.toBookmarkEntity())
        }
    }
}