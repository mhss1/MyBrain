package com.mhss.app.mybrain.util

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

object BackupUtil {

    suspend inline fun <reified T> List<T>.toJson(): String{
        val moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<List<T>> = moshi.adapter(Types.newParameterizedType(List::class.java, T::class.java))
        return adapter.toJson(this)
    }

    suspend inline fun <reified T> String.fromJson(): List<T>?{
        val moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<List<T>> = moshi.adapter(Types.newParameterizedType(List::class.java, T::class.java))
        return adapter.fromJson(this)
    }

}