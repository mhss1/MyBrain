package com.mhss.app.mybrain.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diaries")
data class Diary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
