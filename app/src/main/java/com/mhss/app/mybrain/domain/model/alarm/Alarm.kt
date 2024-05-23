package com.mhss.app.mybrain.domain.model.alarm

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val time: Long,
)
