package com.mhss.app.alarm.use_case

import com.mhss.app.alarm.model.Alarm
import com.mhss.app.alarm.repository.AlarmRepository
import com.mhss.app.alarm.repository.AlarmScheduler
import org.koin.core.annotation.Single

@Single
class AddAlarmUseCase(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(alarm: Alarm): Boolean {
        if (!alarmScheduler.canScheduleExactAlarms()) return false
        alarmScheduler.scheduleAlarm(alarm)
        alarmRepository.insertAlarm(alarm)
        return true
    }


}