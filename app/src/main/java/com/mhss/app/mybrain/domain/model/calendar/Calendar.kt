package com.mhss.app.mybrain.domain.model.calendar

data class Calendar(
    val id: Long,
    val name: String,
    val account: String,
    val color: Int,
    val included: Boolean = true
)
