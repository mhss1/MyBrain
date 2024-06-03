package com.mhss.app.mybrain.util.date

import android.text.format.DateFormat
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.app.MyBrainApplication.Companion.appContext
import com.mhss.app.mybrain.app.getString
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearsUntil
import java.time.format.DateTimeFormatter
import java.util.Locale

val Long.localDateTime
    get() = Instant.fromEpochMilliseconds(this).toLocalDateTime(
        TimeZone.currentSystemDefault()
    )

fun Long.formatDateDependingOnDay(): String {
    val localDT = localDateTime
    val hourPatternString = if (is24Hour) "H:mm" else "h:mm a"
    val datePattern = if (localDT.isToday()) {
        hourPatternString
    } else "MMM dd,yyyy $hourPatternString"

    return DateTimeFormatter
        .ofPattern(datePattern, Locale.getDefault())
        .format(localDT.toJavaLocalDateTime())
}

fun Long.fullDate(): String {
    val localDT = localDateTime
    val hourPattern = if (is24Hour) "H:mm" else "h:mm a"
    return DateTimeFormatter
        .ofPattern("MMM dd,yyyy $hourPattern", Locale.getDefault())
        .format(localDT.toJavaLocalDateTime())
}

fun Long.formatDateForMapping(): String {
    return DateTimeFormatter
        .ofPattern("EEEE d, MMM yyy", Locale.getDefault())
        .format(localDateTime.toJavaLocalDateTime())
}

fun Long.formatTime(): String {
    val minutes = this % HOUR_MILLIS
    val pattern = if (is24Hour) {
        "H:mm"
    } else {
        if (minutes == 0L) "h a" else "h:mm a"
    }
    return DateTimeFormatter
        .ofPattern(pattern, Locale.getDefault())
        .format(localDateTime.toJavaLocalDateTime())
}

fun Long.formatDate(): String {
    return DateTimeFormatter
        .ofPattern("EEE, MMM dd, yyyy", Locale.getDefault())
        .format(localDateTime.toJavaLocalDateTime())
}

fun Long.monthName(): String {
    val localDT = localDateTime
    val formatter = if (localDT.isCurrentYear()) {
        DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())
    } else {
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    }
    return formatter.format(localDT.toJavaLocalDateTime())
}

fun Long.inTheLast30Days(): Boolean {
    return Instant.fromEpochMilliseconds(this).daysUntil(
        Clock.System.now(),
        TimeZone.currentSystemDefault()
    ) <= 30
}

fun Long.inTheLastYear(): Boolean {
    return Instant.fromEpochMilliseconds(this).yearsUntil(
        Clock.System.now(),
        TimeZone.currentSystemDefault()
    ) == 0
}

fun Long.inTheLastWeek(): Boolean {
    return Instant.fromEpochMilliseconds(this).daysUntil(
        Clock.System.now(),
        TimeZone.currentSystemDefault()
    ) <= 7
}

fun LocalDateTime.isCurrentYear(): Boolean {
    return year == now().localDateTime.year
}

fun Long.isDueDateOverdue(): Boolean {
    return this < now()
}

fun formatEventStartEnd(start: Long, end: Long, location: String?, allDay: Boolean): String {
    return if (allDay)
        getString(R.string.all_day)
    else
        getString(
            if (!location.isNullOrBlank())
                R.string.event_time_at
            else R.string.event_time,
            start.formatTime(),
            end.formatTime(),
            location ?: ""
        )
}

fun now() = Clock.System.now().toEpochMilliseconds()

fun LocalDateTime.isToday(): Boolean {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return today.year == year
            && today.month == month
            && today.dayOfMonth == dayOfMonth
}

private val is24Hour by lazy {
    DateFormat.is24HourFormat(appContext)
}

const val HOUR_MILLIS = 60 * 60 * 1000L