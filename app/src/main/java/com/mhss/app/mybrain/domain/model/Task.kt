package com.mhss.app.mybrain.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    val title: String,
    val description: String = "",
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    val priority: Int = 0,
    @ColumnInfo(name = "created_date")
    val createdDate: Long = 0L,
    @ColumnInfo(name = "updated_date")
    val updatedDate: Long = 0L,
    @ColumnInfo(name = "sub_tasks")
    val subTasks: List<SubTask> = emptyList(),
    val dueDate: Long = 0L,
    val recurring: Boolean = false,
    val frequency: Int = 0,
    @ColumnInfo(name = "frequency_amount")
    val frequencyAmount: Int = 1,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
