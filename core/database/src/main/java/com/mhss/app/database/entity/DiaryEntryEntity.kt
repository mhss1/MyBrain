package com.mhss.app.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mhss.app.domain.model.Mood
import com.mhss.app.mybrain.domain.model.diary.DiaryEntry
import kotlinx.serialization.Serializable

@Entity(tableName = "diary")
@Serializable
data class DiaryEntryEntity(
    val title: String = "",
    val content: String = "",
    @ColumnInfo(name = "created_date")
    val createdDate: Long = 0L,
    @ColumnInfo(name = "updated_date")
    val updatedDate: Long = 0L,
    val mood: Mood,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)

fun DiaryEntryEntity.toDiaryEntry() = DiaryEntry(
    title = title,
    content = content,
    createdDate = createdDate,
    updatedDate = updatedDate,
    mood = mood,
    id = id
)

fun DiaryEntry.toDiaryEntryEntity() = DiaryEntryEntity(
    title = title,
    content = content,
    createdDate = createdDate,
    updatedDate = updatedDate,
    mood = mood,
    id = id
)

fun List<DiaryEntryEntity>.withoutIds() = map { it.copy(id = 0) }