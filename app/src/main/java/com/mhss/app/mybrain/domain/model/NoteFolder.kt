package com.mhss.app.mybrain.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_folders")
data class NoteFolder(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "folder_name")
    val name: String
)
