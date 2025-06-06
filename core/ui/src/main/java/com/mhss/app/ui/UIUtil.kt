package com.mhss.app.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.TaskFrequency
import com.mhss.app.network.NetworkResult
import com.mhss.app.preferences.domain.model.Order
import com.mhss.app.preferences.domain.model.OrderType
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.theme.Green
import com.mhss.app.ui.theme.Orange
import com.mhss.app.ui.theme.Red
import com.mhss.app.ui.theme.Rubik


enum class ThemeSettings(val value: Int) {
    LIGHT(0),
    DARK(1),
    AUTO(2)
}

enum class StartUpScreenSettings(val value: Int, val screen: Screen) {
    DASHBOARD(0, Screen.DashboardScreen),
    SPACES(1, Screen.SpacesScreen),
    NOTES(2, Screen.NotesScreen),
    TASKS(3, Screen.TasksScreen()),
    DIARY(4, Screen.DiaryScreen),
    BOOKMARKS(5, Screen.BookmarksScreen),
    CALENDAR(6, Screen.CalendarScreen),
    ASSISTANT(7, Screen.AssistantScreen)
}

fun Int.toStartUpScreen(): StartUpScreenSettings {
    return StartUpScreenSettings.entries.first { it.value == this }
}

enum class FontSizeSettings(@StringRes val title: Int, val value: Int, val scale: Float) {
    SMALL(R.string.font_size_small, 0, 0.8f),
    NORMAL(R.string.font_size_normal, 1, 1.0f),
    LARGE(R.string.font_size_large, 2, 1.2f),
    EXTRA_LARGE(R.string.font_size_extra_large, 3, 1.5f)
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

fun Int.toFontSizeScale(): Float {
    return when (this) {
        FontSizeSettings.SMALL.value -> FontSizeSettings.SMALL.scale
        FontSizeSettings.NORMAL.value -> FontSizeSettings.NORMAL.scale
        FontSizeSettings.LARGE.value -> FontSizeSettings.LARGE.scale
        FontSizeSettings.EXTRA_LARGE.value -> FontSizeSettings.EXTRA_LARGE.scale
        else -> FontSizeSettings.NORMAL.scale
    }
}

@Composable
fun Int.getFontSizeName(): String {
    return when (this) {
        FontSizeSettings.SMALL.value -> stringResource(R.string.font_size_small)
        FontSizeSettings.NORMAL.value -> stringResource(R.string.font_size_normal)
        FontSizeSettings.LARGE.value -> stringResource(R.string.font_size_large)
        FontSizeSettings.EXTRA_LARGE.value -> stringResource(R.string.font_size_extra_large)
        else -> stringResource(R.string.font_size_normal)
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

@Composable
fun NetworkResult.Failure.toUserMessage(): String {
    return when (this) {
        NetworkResult.InvalidKey -> stringResource(R.string.invalid_api_key)
        NetworkResult.InternetError -> stringResource(R.string.no_internet_connection)
        is NetworkResult.OtherError -> if (message != null) message.toString() else stringResource(R.string.unexpected_error)
    }
}

