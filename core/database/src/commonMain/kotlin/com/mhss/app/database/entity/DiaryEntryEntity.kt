package com.mhss.app.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.model.Mood

@Entity(
    tableName = "diary",
    indices = [
        Index(value = ["sync_seq"]),
        Index(value = ["updated_date"]),
        Index(value = ["created_date"])
    ]
)
data class DiaryEntryEntity(
    val title: String = "",
    val content: String = "",
    @ColumnInfo(name = "created_date")
    val createdDate: Long = 0L,
    @ColumnInfo(name = "updated_date")
    val updatedDate: Long = 0L,
    val mood: Mood,
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "sync_seq", defaultValue = "1")
    val syncSeq: Long = 1L,
)

fun DiaryEntryEntity.toDiaryEntry() = DiaryEntry(
    title = title,
    content = content,
    createdDate = createdDate,
    updatedDate = updatedDate,
    mood = mood,
    id = id
)

fun DiaryEntry.toDiaryEntryEntity(id: String = this.id, syncSeq: Long = 0L) = DiaryEntryEntity(
    title = title,
    content = content,
    createdDate = createdDate,
    updatedDate = updatedDate,
    mood = mood,
    id = id,
    syncSeq = syncSeq
)
