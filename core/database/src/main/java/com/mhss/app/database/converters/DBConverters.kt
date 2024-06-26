package com.mhss.app.database.converters

import androidx.room.TypeConverter
import com.mhss.app.domain.model.Mood
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.TaskFrequency
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