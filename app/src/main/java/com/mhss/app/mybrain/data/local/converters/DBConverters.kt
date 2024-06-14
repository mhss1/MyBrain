package com.mhss.app.mybrain.data.local.converters

import androidx.room.TypeConverter
import com.mhss.app.mybrain.domain.model.diary.Mood
import com.mhss.app.mybrain.domain.model.tasks.Priority
import com.mhss.app.mybrain.domain.model.tasks.SubTask
import com.mhss.app.mybrain.domain.model.tasks.TaskFrequency
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DBConverters {

    @TypeConverter
    fun fromSubTasksList(value: List<SubTask>): String {
        return Json.encodeToString(value)
    }
    @TypeConverter
    fun toSubTasksList(value: String): List<SubTask> {
        return Json.decodeFromString<List<SubTask>>(value)
    }

    @TypeConverter
    fun toMood(value: Int) = enumValues<Mood>()[value]
    @TypeConverter
    fun fromMood(value: Mood) = value.ordinal


    @TypeConverter
    fun toTaskFrequency(value: Int) = TaskFrequency.entries.firstOrNull { it.value == value } ?: TaskFrequency.DAILY
    @TypeConverter
    fun fromTaskFrequency(frequency: TaskFrequency) = frequency.value

    @TypeConverter
    fun toPriority(value: Int) = Priority.entries.firstOrNull { it.value == value } ?: Priority.LOW
    @TypeConverter
    fun fromPriority(priority: Priority) = priority.value
}