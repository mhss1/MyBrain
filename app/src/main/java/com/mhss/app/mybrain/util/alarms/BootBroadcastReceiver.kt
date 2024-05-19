package com.mhss.app.mybrain.util.alarms

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhss.app.mybrain.domain.use_case.alarm.GetAllAlarmsUseCase
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val getAllAlarms: GetAllAlarmsUseCase by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            runBlocking {
                val alarms = getAllAlarms()
                alarms.forEach {
                    alarmManager.scheduleAlarm(it, context)
                }
            }
        }

    }

}