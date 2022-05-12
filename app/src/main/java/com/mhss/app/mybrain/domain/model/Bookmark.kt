package com.mhss.app.mybrain.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    val url: String,
    val title: String,
    val description: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
