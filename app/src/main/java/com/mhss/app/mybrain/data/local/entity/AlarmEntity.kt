package com.mhss.app.mybrain.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mhss.app.mybrain.domain.model.alarm.Alarm

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
