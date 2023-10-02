package com.mhss.app.mybrain.util.alarms

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhss.app.mybrain.domain.model.Alarm
import com.mhss.app.mybrain.domain.use_case.alarm.AddAlarmUseCase
import com.mhss.app.mybrain.domain.use_case.alarm.DeleteAlarmUseCase
import com.mhss.app.mybrain.domain.use_case.tasks.GetTaskByIdUseCase
import com.mhss.app.mybrain.domain.use_case.tasks.UpdateTaskUseCase
import com.mhss.app.mybrain.util.Constants
import com.mhss.app.mybrain.util.settings.TaskFrequency
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var deleteAlarmUseCase: DeleteAlarmUseCase
    @Inject
    lateinit var addAlarmUseCase: AddAlarmUseCase

    @Inject
    lateinit var getTaskByIdUseCase: GetTaskByIdUseCase
    @Inject
    lateinit var updateTaskUseCase: UpdateTaskUseCase

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult = goAsync()
        scope.launch {
            val task =
                intent?.getIntExtra(Constants.TASK_ID_EXTRA, 0)?.let { getTaskByIdUseCase(it) }
                    ?: kotlin.run {
                        pendingResult.finish()
                        return@launch
                    }
            val notificationJob = launch {
                val manager =
                    context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.sendNotification(task, context, task.id)
                if (!task.recurring) deleteAlarmUseCase(task.id)
            }
            val recurrenceJob = launch {
                if (task.recurring) {
                    val newTask = task.copy(
                        dueDate = task.dueDate + when (task.frequency) {
                            TaskFrequency.DAILY.value -> 24L * 60 * 60 * 1000
                            TaskFrequency.WEEKLY.value -> 7L * 24 * 60 * 60 * 1000
                            TaskFrequency.MONTHLY.value -> 30L * 24 * 60 * 60 * 1000
                            else -> 24L * 60 * 60 * 1000
                        } * task.frequencyAmount
                    )
                    updateTaskUseCase(newTask)
                    addAlarmUseCase(Alarm(newTask.id, newTask.dueDate))
                }
            }

            notificationJob.join()
            recurrenceJob.join()
            pendingResult.finish()
        }
    }
}