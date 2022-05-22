package com.mhss.app.mybrain.util.alarms

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhss.app.mybrain.domain.use_case.alarm.DeleteAlarmUseCase
import com.mhss.app.mybrain.domain.use_case.tasks.GetTaskByIdUseCase
import com.mhss.app.mybrain.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var deleteAlarmUseCase: DeleteAlarmUseCase
    @Inject
    lateinit var getTaskByIdUseCase: GetTaskByIdUseCase

    override fun onReceive(context: Context?, intent: Intent?) {
        runBlocking {
            val task = intent?.getIntExtra(Constants.TASK_ID_EXTRA, 0)?.let { getTaskByIdUseCase(it) }
            task?.let {
                val manager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.sendNotification(task, context, task.id)
                deleteAlarmUseCase(task.id)
            }
        }
    }
}