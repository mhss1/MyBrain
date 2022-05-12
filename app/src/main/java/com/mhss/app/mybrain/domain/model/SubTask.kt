package com.mhss.app.mybrain.domain.model

import java.util.*

data class SubTask(
    val title: String,
    val isCompleted: Boolean,
    val id: UUID = UUID.randomUUID()
)
