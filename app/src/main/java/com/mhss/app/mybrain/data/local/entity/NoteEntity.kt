package com.mhss.app.mybrain.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.mhss.app.mybrain.domain.model.notes.Note
import kotlinx.serialization.Serializable

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = NoteFolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ]
)
@Serializable
data class NoteEntity(
    val title: String = "",
    val content: String = "",
    @ColumnInfo(name = "created_date")
    val createdDate: Long = 0L,
    @ColumnInfo(name = "updated_date")
    val updatedDate: Long = 0L,
    val pinned: Boolean = false,
    @ColumnInfo(name = "folder_id")
    val folderId: Int? = null,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
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

fun Note.toNoteEntity(): NoteEntity {
    return NoteEntity(
        title = title,
        content = content,
        createdDate = createdDate,
        updatedDate = updatedDate,
        pinned = pinned,
        folderId = folderId,
        id = id
    )
}