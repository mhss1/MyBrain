package com.mhss.app.domain.model.backup

import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.model.TaskFrequency
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackupTask(
    @SerialName("title")
    val title: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("isCompleted")
    val isCompleted: Boolean = false,
    @SerialName("priority")
    val priority: Int = 0,
    @SerialName("createdDate")
    val createdDate: Long = 0L,
    @SerialName("updatedDate")
    val updatedDate: Long = 0L,
    @SerialName("subTasks")
    val subTasks: List<BackupSubTask> = emptyList(),
    @SerialName("dueDate")
    val dueDate: Long? = null,
    @SerialName("recurring")
    val recurring: Boolean = false,
    @SerialName("frequency")
    val frequency: Int = 2,
    @SerialName("frequencyAmount")
    val frequencyAmount: Int = 1,
    @SerialName("alarmId")
    val alarmId: Int? = null,
    @SerialName("id")
    @Serializable(BackupStringIdSerializer::class)
    val id: String = ""
)

fun Task.toBackupTask() = BackupTask(
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = priority.value,
    createdDate = createdDate,
    updatedDate = updatedDate,
    subTasks = subTasks.map(SubTask::toBackupSubTask),
    dueDate = dueDate,
    recurring = recurring,
    frequency = frequency.value,
    frequencyAmount = frequencyAmount,
    alarmId = alarmId,
    id = id
)

fun BackupTask.toTask() = Task(
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = Priority.entries.firstOrNull { it.value == priority } ?: Priority.LOW,
    createdDate = createdDate,
    updatedDate = updatedDate,
    subTasks = subTasks.map(BackupSubTask::toSubTask),
    dueDate = dueDate?.takeUnless { it == 0L },
    recurring = recurring,
    frequency = TaskFrequency.entries.firstOrNull { it.value == frequency } ?: TaskFrequency.DAILY,
    frequencyAmount = frequencyAmount,
    alarmId = alarmId,
    id = id
)
