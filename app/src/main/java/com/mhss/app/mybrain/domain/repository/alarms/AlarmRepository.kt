package com.mhss.app.mybrain.domain.repository.alarms

import com.mhss.app.mybrain.domain.model.alarm.Alarm

interface AlarmRepository {

    suspend fun getAlarms(): List<Alarm>

    suspend fun insertAlarm(alarm: Alarm)

    suspend fun deleteAlarm(alarm: Alarm)

    suspend fun deleteAlarm(id: Int)
}