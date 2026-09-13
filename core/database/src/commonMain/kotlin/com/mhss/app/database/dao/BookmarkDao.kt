package com.mhss.app.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.RawQuery
import androidx.room3.RoomRawQuery
import androidx.room3.Update
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.mhss.app.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface BookmarkDao {

    @RawQuery(observedEntities = [BookmarkEntity::class])
    fun pageBookmarks(query: RoomRawQuery): PagingSource<Int, BookmarkEntity>

    fun getPagedBookmarks(orderBy: BookmarkOrder, order: QueryOrder): PagingSource<Int, BookmarkEntity> =
        pageBookmarks(bookmarkQuery(orderBy, order))

    @Query("SELECT * FROM bookmarks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' ORDER BY updated_date DESC")
    fun searchPagedBookmarks(query: String): PagingSource<Int, BookmarkEntity>

    @RawQuery(observedEntities = [BookmarkEntity::class])
    fun observeBookmarks(query: RoomRawQuery): Flow<List<BookmarkEntity>>

    fun getAllBookmarks(orderBy: BookmarkOrder, order: QueryOrder): Flow<List<BookmarkEntity>> {
        return observeBookmarks(bookmarkQuery(orderBy, order))
    }

    @Query("SELECT * FROM bookmarks")
    suspend fun getAllFullBookmarks(): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getBookmark(id: String): BookmarkEntity?

    @Query("SELECT * FROM bookmarks WHERE id IN (:ids)")
    suspend fun getBookmarksByIds(ids: List<String>): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE updated_date > :timestamp")
    suspend fun getBookmarksUpdatedAfter(timestamp: Long): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE sync_seq > :seq AND sync_seq <= :maxSeq")
    suspend fun getBookmarksAfterSeq(seq: Long, maxSeq: Long): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%'")
    suspend fun searchBookmarks(query: String): List<BookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)

    @Upsert
    suspend fun upsertBookmarks(bookmarks: List<BookmarkEntity>)

}

enum class BookmarkOrder(val column: String) {
    TITLE("title"),
    CREATED_DATE("created_date"),
    UPDATED_DATE("updated_date")
}

private fun bookmarkQuery(orderBy: BookmarkOrder, order: QueryOrder): RoomRawQuery {
    val orderBySql = when (orderBy) {
    BookmarkOrder.TITLE -> "title COLLATE NOCASE ${order.sql}"
    else -> "${orderBy.column} ${order.sql}"
    }
    return RoomRawQuery("SELECT * FROM bookmarks ORDER BY $orderBySql")
}
