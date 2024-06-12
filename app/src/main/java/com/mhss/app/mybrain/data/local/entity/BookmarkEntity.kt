package com.mhss.app.mybrain.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mhss.app.mybrain.domain.model.bookmarks.Bookmark
import kotlinx.serialization.Serializable

@Entity(tableName = "bookmarks")
@Serializable
data class BookmarkEntity(
    val url: String,
    val title: String = "",
    val description: String = "",
    @ColumnInfo(name = "created_date")
    val createdDate: Long = 0L,
    @ColumnInfo(name = "updated_date")
    val updatedDate: Long = 0L,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)

fun BookmarkEntity.toBookmark() = Bookmark(
    url = url,
    title = title,
    description = description,
    createdDate = createdDate,
    updatedDate = updatedDate,
    id = id
)

fun Bookmark.toBookmarkEntity() = BookmarkEntity(
    url = url,
    title = title,
    description = description,
    createdDate = createdDate,
    updatedDate = updatedDate,
    id = id
)
