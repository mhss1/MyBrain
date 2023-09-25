package com.mhss.app.mybrain.util.alarms

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhss.app.mybrain.domain.use_case.tasks.UpdateTaskCompletedUseCase
import com.mhss.app.mybrain.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class TaskActionButtonBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var updateTaskCompleted: UpdateTaskCompletedUseCase

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Constants.ACTION_COMPLETE) {
            runBlocking {
                val taskId = intent.getIntExtra(Constants.TASK_ID_EXTRA, 0)
                updateTaskCompleted(taskId, true)
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(taskId)
            }
        }
    }
}