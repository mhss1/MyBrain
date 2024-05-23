package com.mhss.app.mybrain.domain.use_case.alarm

import com.mhss.app.mybrain.domain.model.alarm.Alarm
import com.mhss.app.mybrain.domain.repository.alarms.AlarmRepository
import com.mhss.app.mybrain.domain.repository.alarms.AlarmScheduler
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