package com.mhss.app.mybrain.util

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.*

object BackupUtil {

    inline fun <reified T> List<T>.toJson(): String{
        val moshi = Moshi.Builder().add(UUIDAdapter()).addLast(KotlinJsonAdapterFactory()).build()
        val adapter: JsonAdapter<List<T>> = moshi.adapter(Types.newParameterizedType(List::class.java, T::class.java))
        return adapter.toJson(this)
    }

    inline fun <reified T> T.toJson(): String{
        val moshi = Moshi.Builder().add(UUIDAdapter()).addLast(KotlinJsonAdapterFactory()).build()
        val adapter: JsonAdapter<T> = moshi.adapter(T::class.java)
        return adapter.toJson(this)
    }

    inline fun <reified T> String.objectFromJson(): T?{
        val moshi = Moshi.Builder().add(UUIDAdapter()).addLast(KotlinJsonAdapterFactory()).build()
        val adapter: JsonAdapter<T> = moshi.adapter(T::class.java)
        return adapter.fromJson(this)
    }

    inline fun <reified T> String.listFromJson(): List<T>?{
        val moshi = Moshi.Builder().add(UUIDAdapter()).addLast(KotlinJsonAdapterFactory()).build()
        val adapter: JsonAdapter<List<T>> = moshi.adapter(Types.newParameterizedType(List::class.java, T::class.java))
        return adapter.fromJson(this)
    }


}

class UUIDAdapter {
    @FromJson
    fun fromJson(json: String): UUID {
        return UUID.fromString(json)
    }

    @ToJson
    fun toJson(uuid: UUID): String {
        return uuid.toString()
    }
}