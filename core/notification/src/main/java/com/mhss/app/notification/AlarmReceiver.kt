package com.mhss.app.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhss.app.alarm.model.Alarm
import com.mhss.app.alarm.use_case.AddAlarmUseCase
import com.mhss.app.alarm.use_case.DeleteAlarmUseCase
import com.mhss.app.util.Constants
import com.mhss.app.domain.model.TaskFrequency
import com.mhss.app.domain.use_case.UpdateTaskUseCase
import com.mhss.app.domain.use_case.GetTaskByIdUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val deleteAlarmUseCase: DeleteAlarmUseCase by inject()
    private val addAlarmUseCase: AddAlarmUseCase by inject()
    private val getTaskByIdUseCase: GetTaskByIdUseCase by inject()
    private val updateTaskUseCase: UpdateTaskUseCase by inject()

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
                        TaskFrequency.EVERY_MINUTES -> calendar.add(Calendar.MINUTE, task.frequencyAmount)
                        TaskFrequency.HOURLY -> calendar.add(Calendar.HOUR, task.frequencyAmount)
                        TaskFrequency.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, task.frequencyAmount)
                        TaskFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, task.frequencyAmount)
                        TaskFrequency.MONTHLY -> calendar.add(Calendar.MONTH, task.frequencyAmount)
                        TaskFrequency.ANNUAL -> calendar.add(Calendar.YEAR, task.frequencyAmount)
                    }
                    val newTask = task.copy(
                        dueDate = calendar.timeInMillis,
                    )
                    updateTaskUseCase(newTask, true)
                    addAlarmUseCase(Alarm(newTask.id, newTask.dueDate))
                }
            }

            notificationJob.join()
            recurrenceJob.join()
            pendingResult.finish()
        }
    }
}