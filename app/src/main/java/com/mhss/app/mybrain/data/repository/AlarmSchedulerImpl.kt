package com.mhss.app.mybrain.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import com.mhss.app.mybrain.domain.model.alarm.Alarm
import com.mhss.app.mybrain.domain.repository.alarms.AlarmScheduler
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.alarms.AlarmReceiver
import org.koin.core.annotation.Single
@Single
class AlarmSchedulerImpl(
    private val context: Context
): AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    override fun scheduleAlarm(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(Constants.TASK_ID_EXTRA, alarm.id)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, alarm.time, pendingIntent)

    }

    override fun cancelAlarm(alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    override fun canScheduleExactAlarms(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    }
}

