package com.mhss.app.mybrain.data.local.converters

import androidx.room.TypeConverter
import com.mhss.app.mybrain.domain.model.diary.Mood
import com.mhss.app.mybrain.domain.model.tasks.SubTask
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
}