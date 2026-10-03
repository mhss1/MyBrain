package com.mhss.app.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Task(
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.LOW,
    val createdDate: Long = 0L,
    val updatedDate: Long = 0L,
    val subTasks: List<SubTask> = emptyList(),
    @Serializable(with = TaskDueDateSerializer::class)
    val dueDate: Long? = null,
    val recurring: Boolean = false,
    val frequency: TaskFrequency = TaskFrequency.DAILY,
    val frequencyAmount: Int = 1,
    val alarmId: Int? = null,
    val id: String
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

object TaskDueDateSerializer : KSerializer<Long?> {
    private val serializer = Long.serializer().nullable
    override val descriptor = serializer.descriptor

    override fun deserialize(decoder: Decoder): Long? =
        serializer.deserialize(decoder)?.takeUnless { it == 0L }

    override fun serialize(encoder: Encoder, value: Long?) {
        serializer.serialize(encoder, value)
    }
}
