package com.mhss.app.mybrain.domain.use_case.alarm

import com.mhss.app.mybrain.domain.model.Alarm
import com.mhss.app.mybrain.domain.repository.AlarmRepository
import com.mhss.app.mybrain.domain.repository.AlarmScheduler
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