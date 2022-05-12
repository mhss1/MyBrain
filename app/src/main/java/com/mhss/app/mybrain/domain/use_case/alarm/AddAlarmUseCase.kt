package com.mhss.app.mybrain.domain.use_case.alarm

import com.mhss.app.mybrain.domain.model.Alarm
import com.mhss.app.mybrain.domain.repository.AlarmRepository
import javax.inject.Inject

class AddAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm) = alarmRepository.insertAlarm(alarm)
}