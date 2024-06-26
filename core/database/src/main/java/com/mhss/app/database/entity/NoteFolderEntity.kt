package com.mhss.app.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mhss.app.domain.model.NoteFolder
import kotlinx.serialization.Serializable

@Entity(
    tableName = "note_folders",
)
@Serializable
data class NoteFolderEntity(
    val name: String,
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