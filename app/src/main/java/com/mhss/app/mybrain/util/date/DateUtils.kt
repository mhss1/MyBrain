package com.mhss.app.mybrain.util.date

import java.text.SimpleDateFormat
import java.util.*

fun Long.toFullDate(): String {
    val sdf = SimpleDateFormat("MMM dd,yyyy h:mm a", Locale.getDefault())
    return sdf.format(this)
}