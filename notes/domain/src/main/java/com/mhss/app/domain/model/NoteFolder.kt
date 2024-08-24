package com.mhss.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NoteFolder(
    val name: String,
    val id: Int = 0
)
