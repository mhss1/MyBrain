package com.mhss.app.mybrain.domain.repository.alarms

import com.mhss.app.mybrain.domain.model.alarm.Alarm

interface AlarmScheduler {

    fun scheduleAlarm(alarm: Alarm)

    fun cancelAlarm(alarmId: Int)

    fun canScheduleExactAlarms(): Boolean
}