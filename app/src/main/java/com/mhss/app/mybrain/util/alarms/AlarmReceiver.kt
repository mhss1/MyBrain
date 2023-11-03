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
import java.util.Calendar
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
                    val calendar = Calendar.getInstance().apply { timeInMillis = task.dueDate }
                    when (task.frequency) {
                        TaskFrequency.EVERY_MINUTES.value -> calendar.add(Calendar.MINUTE, task.frequencyAmount)
                        TaskFrequency.HOURLY.value -> calendar.add(Calendar.HOUR, task.frequencyAmount)
                        TaskFrequency.DAILY.value -> calendar.add(Calendar.DAY_OF_YEAR, task.frequencyAmount)
                        TaskFrequency.WEEKLY.value -> calendar.add(Calendar.WEEK_OF_YEAR, task.frequencyAmount)
                        TaskFrequency.MONTHLY.value -> calendar.add(Calendar.MONTH, task.frequencyAmount)
                        TaskFrequency.ANNUAL.value -> calendar.add(Calendar.YEAR, task.frequencyAmount)
                        else -> calendar.add(Calendar.DAY_OF_YEAR, task.frequencyAmount)
                    }
                    val newTask = task.copy(
                        dueDate = calendar.timeInMillis,
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