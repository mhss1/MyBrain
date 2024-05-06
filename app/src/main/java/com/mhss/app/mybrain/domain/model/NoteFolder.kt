package com.mhss.app.mybrain.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "note_folders",
)
@Serializable
data class NoteFolder(
    val name: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
