package com.mhss.app.mybrain.domain.repository

import com.mhss.app.mybrain.domain.model.Alarm

interface AlarmScheduler {

    fun scheduleAlarm(alarm: Alarm)

    fun cancelAlarm(alarmId: Int)

    fun canScheduleExactAlarms(): Boolean
}