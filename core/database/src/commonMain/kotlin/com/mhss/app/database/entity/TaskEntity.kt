package com.mhss.app.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.model.TaskFrequency

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["sync_seq"]),
        Index(value = ["updated_date"]),
        Index(value = ["is_completed", "updated_date"])
    ]
)
data class TaskEntity(
    val title: String,
    val description: String = "",
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    val priority: Int = Priority.LOW.value,
    @ColumnInfo(name = "created_date")
    val createdDate: Long = 0L,
    @ColumnInfo(name = "updated_date")
    val updatedDate: Long = 0L,
    @ColumnInfo(name = "sub_tasks")
    val subTasks: List<SubTask> = emptyList(),
    val dueDate: Long = 0L,
    val recurring: Boolean = false,
    val frequency: Int = TaskFrequency.DAILY.value,
    @ColumnInfo(name = "frequency_amount")
    val frequencyAmount: Int = 1,
    val alarmId: Int? = null,
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "sync_seq", defaultValue = "1")
    val syncSeq: Long = 1L,
)

fun TaskEntity.toTask() = Task(
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = Priority.entries.firstOrNull { it.value == priority } ?: Priority.LOW,
    createdDate = createdDate,
    updatedDate = updatedDate,
    subTasks = subTasks,
    dueDate = dueDate,
    recurring = recurring,
    frequency = TaskFrequency.entries.firstOrNull { it.value == frequency } ?: TaskFrequency.DAILY,
    frequencyAmount = frequencyAmount,
    alarmId = alarmId,
    id = id
)

fun Task.toTaskEntity(id: String = this.id, syncSeq: Long = 0L) = TaskEntity(
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = priority.value,
    createdDate = createdDate,
    updatedDate = updatedDate,
    subTasks = subTasks,
    dueDate = dueDate,
    recurring = recurring,
    frequency = frequency.value,
    frequencyAmount = frequencyAmount,
    alarmId = alarmId,
    id = id,
    syncSeq = syncSeq
)
