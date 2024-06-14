package com.mhss.app.mybrain.domain.model.tasks

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

enum class TaskFrequency(val value: Int) {
    EVERY_MINUTES(0),
    HOURLY(1),
    DAILY(2),
    WEEKLY(3),
    MONTHLY(4),
    ANNUAL(5)
}

enum class Priority(val value: Int) {
    LOW( 0),
    MEDIUM(1),
    HIGH(2)
}
