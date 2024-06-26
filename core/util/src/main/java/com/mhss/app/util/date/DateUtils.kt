package com.mhss.app.util.date

import android.content.Context
import android.text.format.DateFormat
import com.mhss.app.app.R
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearsUntil
import java.time.format.DateTimeFormatter
import java.util.Locale

val Long.localDateTime
    get() = Instant.fromEpochMilliseconds(this).toLocalDateTime(
        TimeZone.currentSystemDefault()
    )

fun Long.formatDateDependingOnDay(context: Context): String {
    val localDT = localDateTime
    val hourPatternString = if (is24HourFormat(context)) "H:mm" else "h:mm a"
    val datePattern = if (localDT.isToday()) {
        hourPatternString
    } else "MMM dd,yyyy $hourPatternString"

    return DateTimeFormatter
        .ofPattern(datePattern, Locale.getDefault())
        .format(localDT.toJavaLocalDateTime())
}

fun Long.fullDate(context: Context): String {
    val localDT = localDateTime
    val hourPattern = if (is24HourFormat(context)) "H:mm" else "h:mm a"
    return DateTimeFormatter
        .ofPattern("MMM dd,yyyy $hourPattern", Locale.getDefault())
        .format(localDT.toJavaLocalDateTime())
}

fun Long.formatDateForMapping(): String {
    return DateTimeFormatter
        .ofPattern("EEEE d, MMM yyy", Locale.getDefault())
        .format(localDateTime.toJavaLocalDateTime())
}

fun Long.formatTime(context: Context): String {
    val minutes = this % HOUR_MILLIS
    val pattern = if (is24HourFormat(context)) {
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

fun Context.formatEventStartEnd(start: Long, end: Long, location: String?, allDay: Boolean): String {
    return if (allDay)
        getString(R.string.all_day)
    else
        getString(
            if (!location.isNullOrBlank())
                R.string.event_time_at
            else R.string.event_time,
            start.formatTime(this),
            end.formatTime(this),
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

val Long.hour: Int
    get() = localDateTime.hour

val Long.minute: Int
    get() = localDateTime.minute

fun Long.at(hours: Int, minutes: Int): Long {
    val date = localDateTime
    return LocalDateTime(
        year = date.year,
        month = date.month,
        dayOfMonth = date.dayOfMonth,
        hour = hours,
        minute = minutes,
        second = 0,
    ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

private var is24Hour: Boolean? = null

fun is24HourFormat(context: Context): Boolean {
    if (is24Hour == null) {
        is24Hour = DateFormat.is24HourFormat(context)
    }
    return is24Hour!!
}

const val HOUR_MILLIS = 60 * 60 * 1000L