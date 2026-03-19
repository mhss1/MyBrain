package com.mhss.app.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhss.app.alarm.use_case.DeleteAlarmUseCase
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.use_case.GetTaskByAlarmUseCase
import com.mhss.app.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val deleteAlarmUseCase: DeleteAlarmUseCase by inject()
    private val getTaskByAlarm: GetTaskByAlarmUseCase by inject()

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult = goAsync()

        scope.launch {
            val task = intent?.getTaskBackwardsCompat() ?: run {
                pendingResult.finish()
                return@launch
            }

            val manager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.sendNotification(task, context, task.alarmId ?: return@launch)
            deleteAlarmUseCase(task.alarmId ?: return@launch)

            pendingResult.finish()
        }
    }


    // Newly used name is alarm id but previous versions use task id name
    private suspend fun Intent.getTaskBackwardsCompat(): Task? {
        val alarmId =
            getIntExtra(Constants.ALARM_ID_EXTRA, -1).takeIf { it != -1 }
                ?: getIntExtra(Constants.TASK_ID_EXTRA, -1).takeIf { it != -1 }
        return alarmId?.let { getTaskByAlarm(it) }
    }

}
