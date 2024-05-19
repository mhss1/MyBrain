package com.mhss.app.mybrain.domain.use_case.alarm

import com.mhss.app.mybrain.domain.repository.AlarmRepository
import org.koin.core.annotation.Single

@Single
class GetAllAlarmsUseCase(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke() = alarmRepository.getAlarms()
}