package com.mhss.app.mybrain.domain.model.tasks

import com.mhss.app.mybrain.util.settings.Priority
import com.mhss.app.mybrain.util.settings.TaskFrequency
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.LOW,
    val createdDate: Long = 0L,
    val updatedDate: Long = 0L,
    val subTasks: List<SubTask> = emptyList(),
    val dueDate: Long = 0L,
    val recurring: Boolean = false,
    val frequency: TaskFrequency = TaskFrequency.DAILY,
    val frequencyAmount: Int = 1,
    val id: Int = 0
)
