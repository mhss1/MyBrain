package com.mhss.app.domain.use_case

import com.mhss.app.alarm.use_case.DeleteAlarmUseCase
import com.mhss.app.alarm.use_case.UpsertAlarmUseCase
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.model.TaskFrequency
import com.mhss.app.domain.repository.TaskRepository
import com.mhss.app.widget.WidgetUpdater
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import org.koin.core.annotation.Single
import kotlin.time.Clock.System.now
import kotlin.time.Instant

@Single
class UpsertTaskUseCase(
    private val tasksRepository: TaskRepository,
    private val upsertAlarm: UpsertAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val widgetUpdater: WidgetUpdater
) {
    suspend operator fun invoke(
        task: Task,
        previousTask: Task? = null,
        updateWidget: Boolean = true
    ): Boolean {
        val nowMillis = now().toEpochMilliseconds()

        val taskWithResolvedRecurrence = task.rollRecurringDueDateIfNeeded(
            previousTask = previousTask,
            nowMillis = nowMillis
        )

        val updatedTask = when {
            shouldDeleteAlarm(taskWithResolvedRecurrence, previousTask, nowMillis) -> {
                deleteAlarmUseCase(previousTask!!.alarmId!!)
                taskWithResolvedRecurrence.copy(alarmId = null)
            }

            shouldScheduleAlarm(taskWithResolvedRecurrence, nowMillis) -> {
                val alarmId = upsertAlarm(taskWithResolvedRecurrence.alarmId ?: 0, taskWithResolvedRecurrence.dueDate)
                taskWithResolvedRecurrence.copy(alarmId = alarmId)
            }

            else -> taskWithResolvedRecurrence
        }

        tasksRepository.upsertTask(updatedTask)
        if (updateWidget) widgetUpdater.updateAll(WidgetUpdater.WidgetType.Tasks)

        return isAlarmSchedulingValid(updatedTask)
    }

    private fun Task.rollRecurringDueDateIfNeeded(previousTask: Task?, nowMillis: Long): Task {
        return if (
            isCompleted &&
            previousTask?.isCompleted == false &&
            recurring &&
            dueDate != 0L
        ) {
            val timeZone = TimeZone.currentSystemDefault()
            var nextDueInstant = Instant.fromEpochMilliseconds(dueDate)
            val frequencyAmount = frequencyAmount.coerceAtLeast(1)
            do {
                nextDueInstant = nextDueInstant.advance(frequency, frequencyAmount, timeZone)
            } while (nextDueInstant.toEpochMilliseconds() <= nowMillis)
            copy(
                dueDate = nextDueInstant.toEpochMilliseconds(),
                isCompleted = false
            )
        } else {
            this
        }
    }

    private fun Instant.advance(
        frequency: TaskFrequency,
        frequencyAmount: Int,
        timeZone: TimeZone
    ): Instant = when (frequency) {
        TaskFrequency.EVERY_MINUTES -> this.plus(frequencyAmount, DateTimeUnit.MINUTE, timeZone)
        TaskFrequency.HOURLY -> this.plus(frequencyAmount, DateTimeUnit.HOUR, timeZone)
        TaskFrequency.DAILY -> this.plus(frequencyAmount, DateTimeUnit.DAY, timeZone)
        TaskFrequency.WEEKLY -> this.plus(frequencyAmount, DateTimeUnit.WEEK, timeZone)
        TaskFrequency.MONTHLY -> this.plus(DateTimePeriod(months = frequencyAmount), timeZone)
        TaskFrequency.ANNUAL -> this.plus(DateTimePeriod(years = frequencyAmount), timeZone)
    }

    private fun shouldScheduleAlarm(task: Task, nowMillis: Long): Boolean {
        return !task.isCompleted &&
                task.dueDate != 0L &&
                task.dueDate > nowMillis
    }

    private fun shouldDeleteAlarm(
        task: Task,
        previousTask: Task?,
        nowMillis: Long
    ): Boolean {
        if (previousTask?.alarmId == null) return false
        return task.dueDate <= nowMillis || // due date is now in the past, so keeping an alarm no longer makes sense.
                (task.isCompleted && !previousTask.isCompleted) // task has just been marked completed, so its alarm must be cleared.
    }
    
    private fun isAlarmSchedulingValid(task: Task): Boolean {
        return task.isCompleted || task.alarmId != null || task.dueDate == 0L
    }
}
