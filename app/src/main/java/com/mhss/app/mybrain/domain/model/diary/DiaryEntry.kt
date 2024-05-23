package com.mhss.app.mybrain.domain.model.diary

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "diary")
@Serializable
data class DiaryEntry(
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
