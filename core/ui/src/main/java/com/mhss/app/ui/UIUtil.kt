package com.mhss.app.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import com.mhss.app.app.R
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.TaskFrequency
import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.preferences.domain.model.OrderType
import com.mhss.app.ui.theme.Green
import com.mhss.app.ui.theme.Orange
import com.mhss.app.ui.theme.Red
import com.mhss.app.ui.theme.Rubik


enum class ThemeSettings(val value: Int) {
    LIGHT(0),
    DARK(1),
    AUTO(2)
}

enum class StartUpScreenSettings(val value: Int) {
    DASHBOARD(0),
    SPACES(1)
}

enum class ItemView(@StringRes val title: Int, val value: Int) {
    LIST(R.string.list, 0),
    GRID(R.string.grid, 1)
}

fun Int.toNotesView(): ItemView {
    return ItemView.entries.first { it.value == this }
}

fun Int.toFontFamily(): FontFamily {
    return when (this) {
        0 -> FontFamily.Default
        1 -> Rubik
        2 -> FontFamily.Monospace
        3 -> FontFamily.SansSerif
        else -> FontFamily.Default
    }
}

fun FontFamily.toInt(): Int {
    return when (this) {
        FontFamily.Default -> 0
        Rubik -> 1
        FontFamily.Monospace -> 2
        FontFamily.SansSerif -> 3
        else -> 0
    }
}

@Composable
fun FontFamily.getName(): String {
    return when (this) {
        FontFamily.Default -> stringResource(R.string.font_system_default)
        Rubik -> "Rubik"
        FontFamily.Monospace -> "Monospace"
        FontFamily.SansSerif -> "Sans Serif"
        else -> stringResource(R.string.font_system_default)
    }
}

val Order.titleRes: Int
    get() = when (this) {
        is Order.Alphabetical -> R.string.alphabetical
        is Order.DateCreated -> R.string.date_created
        is Order.DateModified -> R.string.date_modified
        is Order.Priority -> R.string.priority
        is Order.DueDate -> R.string.due_date
    }

val OrderType.titleRes: Int
    get() = when (this) {
        is OrderType.ASC -> R.string.ascending
        is OrderType.DESC -> R.string.descending
    }

val TaskFrequency.titleRes: Int
    get() = when (this) {
        TaskFrequency.EVERY_MINUTES -> R.string.every_minute
        TaskFrequency.HOURLY -> R.string.every_hour
        TaskFrequency.DAILY -> R.string.every_day
        TaskFrequency.WEEKLY -> R.string.every_week
        TaskFrequency.MONTHLY -> R.string.every_month
        TaskFrequency.ANNUAL -> R.string.every_year
    }

val Priority.titleRes: Int
    get() = when (this) {
        Priority.LOW -> R.string.low
        Priority.MEDIUM -> R.string.medium
        Priority.HIGH -> R.string.high
    }

val Priority.color: Color
    get() = when (this) {
        Priority.LOW -> Green
        Priority.MEDIUM -> Orange
        Priority.HIGH -> Red
    }

fun Set<String>.toIntList() = this.toList().map { it.toInt() }

