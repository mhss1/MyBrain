package com.mhss.app.mybrain.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "bookmarks")
@Serializable
data class Bookmark(
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
