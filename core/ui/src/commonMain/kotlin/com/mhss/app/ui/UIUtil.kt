package com.mhss.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mhss.app.domain.model.BackupFrequency
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.TaskFrequency
import com.mhss.app.preferences.domain.model.SortOrder
import com.mhss.app.preferences.domain.model.SortType
import com.mhss.app.ui.navigation.Screen
import com.mhss.app.ui.theme.Green
import com.mhss.app.ui.theme.Orange
import com.mhss.app.ui.theme.Red
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource


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

enum class FontSizeSettings(val title: StringResource, val value: Int, val scale: Float) {
    SMALL(Res.string.font_size_small, 0, 0.8f),
    NORMAL(Res.string.font_size_normal, 1, 1.0f),
    LARGE(Res.string.font_size_large, 2, 1.2f),
    EXTRA_LARGE(Res.string.font_size_extra_large, 3, 1.5f)
}

enum class ItemView(val title: StringResource, val value: Int) {
    LIST(Res.string.list, 0),
    GRID(Res.string.grid, 1)
}

fun Int.toNotesView(): ItemView {
    return ItemView.entries.first { it.value == this }
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
        FontSizeSettings.SMALL.value -> stringResource(FontSizeSettings.SMALL.title)
        FontSizeSettings.NORMAL.value -> stringResource(FontSizeSettings.NORMAL.title)
        FontSizeSettings.LARGE.value -> stringResource(FontSizeSettings.LARGE.title)
        FontSizeSettings.EXTRA_LARGE.value -> stringResource(FontSizeSettings.EXTRA_LARGE.title)
        else -> stringResource(FontSizeSettings.NORMAL.title)
    }
}

val SortOrder.titleRes: StringResource
    get() = when (this) {
        is SortOrder.Alphabetical -> Res.string.alphabetical
        is SortOrder.DateCreated -> Res.string.date_created
        is SortOrder.DateModified -> Res.string.date_modified
        is SortOrder.Priority -> Res.string.priority
        is SortOrder.DueDate -> Res.string.due_date
        is SortOrder.Done -> Res.string.done
    }

val SortType.titleRes: StringResource
    get() = when (this) {
        is SortType.ASC -> Res.string.ascending
        is SortType.DESC -> Res.string.descending
    }

val TaskFrequency.titleRes: StringResource
    get() = when (this) {
        TaskFrequency.EVERY_MINUTES -> Res.string.every_minute
        TaskFrequency.HOURLY -> Res.string.every_hour
        TaskFrequency.DAILY -> Res.string.every_day
        TaskFrequency.WEEKLY -> Res.string.every_week
        TaskFrequency.MONTHLY -> Res.string.every_month
        TaskFrequency.ANNUAL -> Res.string.every_year
    }

val BackupFrequency.titleRes: StringResource
    get() = when (this) {
        BackupFrequency.HOURLY -> Res.string.every_hour
        BackupFrequency.DAILY -> Res.string.every_day
        BackupFrequency.WEEKLY -> Res.string.every_week
        BackupFrequency.MONTHLY -> Res.string.every_month
    }

val Priority.titleRes: StringResource
    get() = when (this) {
        Priority.LOW -> Res.string.low
        Priority.MEDIUM -> Res.string.medium
        Priority.HIGH -> Res.string.high
    }

val Priority.color: Color
    get() = when (this) {
        Priority.LOW -> Green
        Priority.MEDIUM -> Orange
        Priority.HIGH -> Red
    }

fun Set<String>.toIntList() = this.toList().map { it.toInt() }

enum class FirstDayOfWeekSettings(val title: StringResource, val value: Int) {
    SATURDAY(Res.string.saturday, 0),
    SUNDAY(Res.string.sunday, 1),
    MONDAY(Res.string.monday, 2);

    companion object {
        fun fromValue(value: Int): FirstDayOfWeekSettings =
            entries.firstOrNull { it.value == value } ?: SUNDAY
    }
}

