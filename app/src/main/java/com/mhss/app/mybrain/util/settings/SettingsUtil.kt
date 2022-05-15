package com.mhss.app.mybrain.util.settings

import androidx.annotation.StringRes
import androidx.compose.material.ContentAlpha.high
import androidx.compose.ui.graphics.Color
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.ui.theme.Green
import com.mhss.app.mybrain.ui.theme.Orange
import com.mhss.app.mybrain.ui.theme.Red


enum class ThemeSettings(val value: Int) {
    LIGHT(0),
    DARK(1),
    AUTO(2)
}

enum class StartUpScreenSettings(val value: Int) {
    DASHBOARD(0),
    SPACES(1)
}
sealed class OrderType {
    object ASC : OrderType()
    object DESC : OrderType()
}
sealed class Order(val type: OrderType){
    class Alphabetical(type: OrderType) : Order(type)
    class DateCreated(type: OrderType) : Order(type)
    class DateModified(type: OrderType) : Order(type)
    class Custom<Class, VarType : Comparable<VarType>> constructor(val selector: (Class) -> VarType?, type: OrderType) : Order(type)
}

enum class Priority( @StringRes val title: Int, val color: Color) {
    LOW(R.string.low, Green),
    MEDIUM(R.string.medium, Orange),
    HIGH(R.string.high, Red)
}


fun Int.toPriority(): Priority {
    return when (this) {
        0 -> Priority.LOW
        1 -> Priority.MEDIUM
        2 -> Priority.HIGH
        else -> Priority.LOW
    }
}

fun Priority.toInt(): Int {
    return when (this) {
        Priority.LOW -> 0
        Priority.MEDIUM -> 1
        Priority.HIGH -> 2
    }
}