package com.mhss.app.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mhss.app.domain.model.NoteFolder
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(
    tableName = "note_folders",
)
@Serializable
data class NoteFolderEntity(
    @SerialName("name")
    val name: String = "",
    @SerialName("id")
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)

fun NoteFolderEntity.toNoteFolder(): NoteFolder {
    return NoteFolder(
        name = name,
        id = id,
    )
}

fun NoteFolder.toNoteFolderEntity(): NoteFolderEntity {
    return NoteFolderEntity(
        name = name,
        id = id,
    )
}

fun List<NoteFolderEntity>.withoutIds() = map { it.copy(id = 0) }