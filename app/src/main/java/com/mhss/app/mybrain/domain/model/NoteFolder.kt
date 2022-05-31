package com.mhss.app.mybrain.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_folders")
data class NoteFolder(
    @PrimaryKey(autoGenerate = false)
    val name: String
)
