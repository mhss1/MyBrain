package com.mhss.app.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mhss.app.alarm.model.Alarm

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val time: Long,
)

fun AlarmEntity.toAlarm() = Alarm(
    id = id,
    time = time,
)

fun Alarm.toAlarmEntity() = AlarmEntity(
    id = id,
    time = time,
)
