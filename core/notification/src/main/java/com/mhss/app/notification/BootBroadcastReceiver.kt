package com.mhss.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhss.app.alarm.repository.AlarmScheduler
import com.mhss.app.alarm.use_case.GetAllAlarmsUseCase
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val getAllAlarms: GetAllAlarmsUseCase by inject()
    private val alarmScheduler: AlarmScheduler by inject()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            runBlocking {
                val alarms = getAllAlarms()
                alarms.forEach {
                    alarmScheduler.scheduleAlarm(it)
                }
            }
        }

    }

}