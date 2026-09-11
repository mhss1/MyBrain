package com.mhss.app.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.mhss.app.domain.model.Note

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["sync_seq"]),
        Index(value = ["pinned", "updated_date"]),
        Index(value = ["folder_id", "pinned", "updated_date"])
    ]
)
data class NoteEntity(
    val title: String = "",
    val content: String = "",
    @ColumnInfo(name = "created_date")
    val createdDate: Long = 0L,
    @ColumnInfo(name = "updated_date")
    val updatedDate: Long = 0L,
    val pinned: Boolean = false,
    @ColumnInfo(name = "folder_id")
    val folderId: String? = null,
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "sync_seq", defaultValue = "1")
    val syncSeq: Long = 1L,
)

fun NoteEntity.toNote(): Note {
    return Note(
        title = title,
        content = content,
        createdDate = createdDate,
        updatedDate = updatedDate,
        pinned = pinned,
        folderId = folderId,
        id = id,
    )
}

fun Note.toNoteEntity(id: String = this.id, syncSeq: Long = 0L): NoteEntity {
    return NoteEntity(
        title = title,
        content = content,
        createdDate = createdDate,
        updatedDate = updatedDate,
        pinned = pinned,
        folderId = folderId,
        id = id,
        syncSeq = syncSeq
    )
}
