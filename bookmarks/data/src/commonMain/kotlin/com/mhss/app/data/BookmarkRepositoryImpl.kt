@file:OptIn(ExperimentalUuidApi::class)

package com.mhss.app.data

import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.map
import com.mhss.app.database.DefaultPagingConfig
import com.mhss.app.database.dao.BookmarkDao
import com.mhss.app.database.dao.BookmarkOrder
import com.mhss.app.database.dao.QueryOrder
import com.mhss.app.database.dao.SyncDao
import com.mhss.app.database.dao.incrementAndGet
import com.mhss.app.database.entity.DeletedEntityEntity
import com.mhss.app.database.entity.DeletedEntityType
import com.mhss.app.database.entity.toBookmark
import com.mhss.app.database.entity.toBookmarkEntity
import com.mhss.app.database.helpers.DatabaseTransactionProvider
import com.mhss.app.database.sync.LocalChangeObserver
import com.mhss.app.datetime.now
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.domain.repository.BookmarkRepository
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
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
    private val syncDao: SyncDao,
    private val changeObserver: LocalChangeObserver,
    private val transactionProvider: DatabaseTransactionProvider,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : BookmarkRepository {

    override fun getPagedBookmarks(sortOrder: SortOrder): Flow<PagingData<Bookmark>> {
        val sortOrderBy = when (sortOrder) {
            is SortOrder.Alphabetical -> BookmarkOrder.TITLE
            is SortOrder.DateCreated -> BookmarkOrder.CREATED_DATE
            else -> BookmarkOrder.UPDATED_DATE
        }
        return Pager(config = DefaultPagingConfig) {
            bookmarkDao.getPagedBookmarks(sortOrderBy, sortOrder.sortType.toQueryOrder())
        }.flow.map { page -> page.map { it.toBookmark() } }
    }

    override fun searchPagedBookmarks(query: String): Flow<PagingData<Bookmark>> =
        Pager(config = DefaultPagingConfig) {
            bookmarkDao.searchPagedBookmarks(query)
        }.flow.map { page -> page.map { it.toBookmark() } }

    override fun getAllBookmarks(sortOrder: SortOrder): Flow<List<Bookmark>> {
        val sortOrderBy = when (sortOrder) {
            is SortOrder.Alphabetical -> BookmarkOrder.TITLE
            is SortOrder.DateCreated -> BookmarkOrder.CREATED_DATE
            else -> BookmarkOrder.UPDATED_DATE
        }
        return bookmarkDao.getAllBookmarks(sortOrderBy, sortOrder.sortType.toQueryOrder())
            .map { bookmarks -> bookmarks.map { it.toBookmark() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun getBookmark(id: String): Bookmark {
        return withContext(ioDispatcher) {
            bookmarkDao.getBookmark(id)?.toBookmark() ?: throw IllegalArgumentException("Bookmark with id $id not found")
        }
    }

    override suspend fun searchBookmarks(query: String): List<Bookmark> {
        return withContext(ioDispatcher) {
            bookmarkDao.searchBookmarks(query).map { it.toBookmark() }
        }
    }

    override suspend fun upsertBookmarks(bookmarks: List<Bookmark>, notifyChange: Boolean) {
        withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                val stamped = bookmarks.map { it.toBookmarkEntity(syncSeq = syncDao.incrementAndGet()) }
                bookmarkDao.upsertBookmarks(stamped)
            }
            if (notifyChange) changeObserver.notifyChange()
        }
    }

    override suspend fun addBookmark(bookmark: Bookmark): Long {
        return withContext(ioDispatcher) {
            val result = transactionProvider.runInTransaction {
                bookmarkDao.insertBookmark(bookmark.toBookmarkEntity(syncSeq = syncDao.incrementAndGet()))
            }
            changeObserver.notifyChange()
            result
        }
    }

    override suspend fun deleteBookmark(bookmark: Bookmark) {
        withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                bookmarkDao.deleteBookmark(bookmark.toBookmarkEntity())
                syncDao.insertDeletedEntity(
                    DeletedEntityEntity(
                        id = Uuid.generateV7().toString(),
                        entityId = bookmark.id,
                        entityType = DeletedEntityType.BOOKMARK.key,
                        deletedAt = now(),
                        syncSeq = syncDao.incrementAndGet()
                    )
                )
            }
            changeObserver.notifyChange()
        }
    }

    override suspend fun updateBookmark(bookmark: Bookmark) {
        withContext(ioDispatcher) {
            transactionProvider.runInTransaction {
                bookmarkDao.updateBookmark(
                    bookmark.toBookmarkEntity(syncSeq = syncDao.incrementAndGet())
                )
            }
            changeObserver.notifyChange()
        }
    }
}

private fun SortType.toQueryOrder() = when (this) {
    SortType.ASC -> QueryOrder.ASC
    SortType.DESC -> QueryOrder.DESC
}
