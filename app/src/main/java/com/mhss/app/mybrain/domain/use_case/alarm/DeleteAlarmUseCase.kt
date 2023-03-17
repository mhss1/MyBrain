package com.mhss.app.mybrain.domain.use_case.alarm

import android.app.AlarmManager
import android.content.Context
import com.mhss.app.mybrain.domain.repository.AlarmRepository
import com.mhss.app.mybrain.util.alarms.cancelAlarm
import javax.inject.Inject

class DeleteAlarmUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val context: Context
) {
    suspend operator fun invoke(alarmId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancelAlarm(alarmId, context)
        alarmRepository.deleteAlarm(alarmId)
    }
}