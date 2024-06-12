package com.mhss.app.mybrain.domain.model.notes

import kotlinx.serialization.Serializable

@Serializable
data class NoteFolder(
    val name: String,
    val id: Int = 0
)
