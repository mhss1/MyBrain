package com.mhss.app.mybrain.domain.use_case.alarm

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import com.mhss.app.mybrain.domain.model.Alarm
import com.mhss.app.mybrain.domain.repository.AlarmRepository
import com.mhss.app.mybrain.util.alarms.scheduleAlarm
import org.koin.core.annotation.Single

@Single
class AddAlarmUseCase(
    private val alarmRepository: AlarmRepository,
    private val context: Context
) {
    suspend operator fun invoke(alarm: Alarm): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return false
            }
        }
        alarmManager.scheduleAlarm(alarm, context)
        alarmRepository.insertAlarm(alarm)
        return true
    }


}