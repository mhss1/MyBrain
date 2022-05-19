package com.mhss.app.mybrain.util.settings

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.app.getString
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
sealed class OrderType(val orderTitle: String) {
    class ASC(val title: String = getString(R.string.ascending)) : OrderType(title)
    class DESC(val title: String = getString(R.string.descending)) : OrderType(title)
}
sealed class Order(val orderType: OrderType, val orderTitle: String){
    abstract fun copy(orderType: OrderType): Order
    data class Alphabetical(val type: OrderType = OrderType.ASC(), val title: String = getString(R.string.alphabetical)) : Order(type, title) {
        override fun copy(orderType: OrderType): Order {
            return this.copy(type = orderType)
        }
    }

    data class DateCreated(val type: OrderType = OrderType.ASC(), val title: String = getString(R.string.date_created)) : Order(type, title) {
        override fun copy(orderType: OrderType): Order {
            return this.copy(type = orderType)
        }
    }

    data class DateModified(val type: OrderType = OrderType.ASC(), val title: String = getString(R.string.date_modified)) : Order(type, title) {
        override fun copy(orderType: OrderType): Order {
            return this.copy(type = orderType)
        }
    }

    data class Priority(val type: OrderType = OrderType.ASC(), val title: String = getString(R.string.priority)) : Order(type, title) {
        override fun copy(orderType: OrderType): Order {
            return this.copy(type = orderType)
        }
    }
}

enum class Priority( @StringRes val title: Int, val color: Color) {
    LOW(R.string.low, Green),
    MEDIUM(R.string.medium, Orange),
    HIGH(R.string.high, Red)
}

enum class ItemView(@StringRes val title: Int, val value: Int) {
    LIST(R.string.list, 0),
    GRID(R.string.grid, 1)
}

fun Int.toNotesView(): ItemView {
    return ItemView.values().first { it.value == this }
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

fun Int.toOrder(): Order {
    return when(this){
        0 -> Order.Alphabetical(OrderType.ASC())
        1 -> Order.DateCreated(OrderType.ASC())
        2 -> Order.DateModified(OrderType.ASC())
        3 -> Order.Priority(OrderType.ASC())
        4 -> Order.Alphabetical(OrderType.DESC())
        5 -> Order.DateCreated(OrderType.DESC())
        6 -> Order.DateModified(OrderType.DESC())
        7 -> Order.Priority(OrderType.DESC())
        else -> Order.Alphabetical(OrderType.ASC())
    }
}
fun Order.toInt(): Int {
    return when (this.orderType) {
        is OrderType.ASC -> {
            when (this) {
                is Order.Alphabetical -> 0
                is Order.DateCreated -> 1
                is Order.DateModified -> 2
                is Order.Priority -> 3
            }
        }
        is OrderType.DESC -> {
            when (this) {
                is Order.Alphabetical -> 4
                is Order.DateCreated -> 5
                is Order.DateModified -> 6
                is Order.Priority -> 7
            }
        }
    }
}