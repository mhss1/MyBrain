package com.mhss.app.mybrain.domain.repository

import com.mhss.app.mybrain.domain.model.DiaryEntry

interface DiaryRepository {

    suspend fun getAllEntries(): List<DiaryEntry>

    suspend fun getEntry(id: Int): DiaryEntry

    suspend fun searchEntries(title: String): List<DiaryEntry>

    suspend fun addEntry(diary: DiaryEntry)

    suspend fun updateEntry(diary: DiaryEntry)

    suspend fun deleteEntry(diary: DiaryEntry)

}