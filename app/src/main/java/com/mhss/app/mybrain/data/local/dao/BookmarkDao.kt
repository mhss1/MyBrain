package com.mhss.app.mybrain.data.local.dao

import androidx.room.*
import com.mhss.app.mybrain.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getBookmark(id: Int): Bookmark

    @Query("SELECT * FROM bookmarks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%'")
    suspend fun getBookmark(query: String): List<Bookmark>

    @Insert
    suspend fun insertBookmark(bookmark: Bookmark)

    @Update
    suspend fun updateBookmark(bookmark: Bookmark)

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)

    @Insert
    suspend fun insertBookmarks(bookmarks: List<Bookmark>)

}