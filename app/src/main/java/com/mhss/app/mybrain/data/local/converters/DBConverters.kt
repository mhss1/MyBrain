package com.mhss.app.mybrain.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mhss.app.mybrain.domain.model.SubTask
import com.mhss.app.mybrain.util.diary.Mood

class DBConverters {

    @TypeConverter
    fun fromSubTasksList(value: List<SubTask>): String {
        val gson = Gson()
        val type = TypeToken.getParameterized(List::class.java, SubTask::class.java).type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toSubTasksList(value: String): List<SubTask> {
        val gson = Gson()
        val type = TypeToken.getParameterized(List::class.java, SubTask::class.java).type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun toMood(value: Int) = enumValues<Mood>()[value]

    @TypeConverter
    fun fromMood(value: Mood) = value.ordinal
}