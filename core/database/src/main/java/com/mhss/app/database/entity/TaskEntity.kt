package com.mhss.app.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.model.TaskFrequency
import kotlinx.serialization.Serializable

@Entity(tableName = "tasks")
@Serializable
data class TaskEntity(
    val title: String,
    val description: String = "",
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.LOW,
    @ColumnInfo(name = "created_date")
    val createdDate: Long = 0L,
    @ColumnInfo(name = "updated_date")
    val updatedDate: Long = 0L,
    @ColumnInfo(name = "sub_tasks")
    val subTasks: List<SubTask> = emptyList(),
    val dueDate: Long = 0L,
    val recurring: Boolean = false,
    val frequency: TaskFrequency = TaskFrequency.DAILY,
    @ColumnInfo(name = "frequency_amount")
    val frequencyAmount: Int = 1,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)

fun TaskEntity.toTask() = Task(
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = priority,
    createdDate = createdDate,
    updatedDate = updatedDate,
    subTasks = subTasks,
    dueDate = dueDate,
    recurring = recurring,
    frequency = frequency,
    frequencyAmount = frequencyAmount,
    id = id
)

fun Task.toTaskEntity() = TaskEntity(
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = priority,
    createdDate = createdDate,
    updatedDate = updatedDate,
    subTasks = subTasks,
    dueDate = dueDate,
    recurring = recurring,
    frequency = frequency,
    frequencyAmount = frequencyAmount,
    id = id
)

fun List<TaskEntity>.withoutIds() = map { it.copy(id = 0) }
