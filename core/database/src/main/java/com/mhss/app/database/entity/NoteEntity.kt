package com.mhss.app.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.mhss.app.domain.model.Note
import kotlinx.serialization.SerialName
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
    @SerialName("title")
    val title: String = "",
    @SerialName("content")
    val content: String = "",
    @ColumnInfo(name = "created_date")
    @SerialName("createdDate")
    val createdDate: Long = 0L,
    @ColumnInfo(name = "updated_date")
    @SerialName("updatedDate")
    val updatedDate: Long = 0L,
    @SerialName("pinned")
    val pinned: Boolean = false,
    @ColumnInfo(name = "folder_id")
    @SerialName("folderId")
    val folderId: Int? = null,
    @PrimaryKey(autoGenerate = true)
    @SerialName("id")
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

fun List<NoteEntity>.withoutIds() = map { it.copy(id = 0) }