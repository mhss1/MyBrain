package com.mhss.app.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.model.TaskFrequency
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "tasks")
@Serializable
data class TaskEntity(
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String = "",
    @SerialName("isCompleted")
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    @SerialName("priority")
    val priority: Int = Priority.LOW.value,
    @SerialName("createdDate")
    @ColumnInfo(name = "created_date")
    val createdDate: Long = 0L,
    @SerialName("updatedDate")
    @ColumnInfo(name = "updated_date")
    val updatedDate: Long = 0L,
    @SerialName("subTasks")
    @ColumnInfo(name = "sub_tasks")
    val subTasks: List<SubTask> = emptyList(),
    @SerialName("dueDate")
    val dueDate: Long = 0L,
    @SerialName("recurring")
    val recurring: Boolean = false,
    @SerialName("frequency")
    val frequency: Int = TaskFrequency.DAILY.value,
    @SerialName("frequencyAmount")
    @ColumnInfo(name = "frequency_amount")
    val frequencyAmount: Int = 1,
    @SerialName("id")
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
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
    id = id
)

fun Task.toTaskEntity() = TaskEntity(
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
    id = id
)

fun List<TaskEntity>.withoutIds() = map { it.copy(id = 0) }
