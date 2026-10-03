package com.mhss.app.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.mhss.app.domain.model.Bookmark

@Entity(
    tableName = "bookmarks",
    indices = [
        Index(value = ["sync_seq"]),
        Index(value = ["updated_date"])
    ]
)
data class BookmarkEntity(
    val url: String,
    val title: String = "",
    val description: String = "",
    @ColumnInfo(name = "created_date")
    val createdDate: Long = 0L,
    @ColumnInfo(name = "updated_date")
    val updatedDate: Long = 0L,
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "sync_seq", defaultValue = "1")
    val syncSeq: Long = 1L,
)

fun BookmarkEntity.toBookmark() = Bookmark(
    url = url,
    title = title,
    description = description,
    createdDate = createdDate,
    updatedDate = updatedDate,
    id = id
)

fun Bookmark.toBookmarkEntity(id: String = this.id, syncSeq: Long = 0L) = BookmarkEntity(
    url = url,
    title = title,
    description = description,
    createdDate = createdDate,
    updatedDate = updatedDate,
    id = id,
    syncSeq = syncSeq
)
