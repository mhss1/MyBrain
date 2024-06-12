package com.mhss.app.mybrain.domain.model.bookmarks

import kotlinx.serialization.Serializable

@Serializable
data class Bookmark(
    val url: String,
    val title: String = "",
    val description: String = "",
    val createdDate: Long = 0L,
    val updatedDate: Long = 0L,
    val id: Int = 0
)
