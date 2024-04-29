package com.mhss.app.mybrain.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = NoteFolder::class,
            parentColumns = ["name"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ]
)
data class Note(
    val title: String = "",
    val content: String = "",
    @ColumnInfo(name = "created_date")
    val createdDate: Long = 0L,
    @ColumnInfo(name = "updated_date")
    val updatedDate: Long = 0L,
    val pinned: Boolean = false,
    @ColumnInfo(name = "folder_id")
    val folderId: String? = null,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)
