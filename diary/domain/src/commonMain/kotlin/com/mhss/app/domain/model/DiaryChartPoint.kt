package com.mhss.app.domain.model

data class DiaryChartPoint(
    val createdDate: Long = 0L,
    val mood: Mood,
)
